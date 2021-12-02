package hw4;

import static hw4.Config.TIMEOUT_MSEC;
import static hw4.Config.MSG_TYPE_DATA;
import static hw4.Config.MSG_TYPE_ACK;
import static hw4.Util.log;

import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.io.ByteArrayOutputStream;
import java.io.IOException;


// DemoTransportLayer is an example of transport layer that deals with
// data loss but not bit error. The goal is the show synchronizations
// needed to complete SW and GBN.
public class DemoTransportLayer extends TransportLayer {
  private Semaphore sem;
  private ScheduledFuture<?> timer;
  private ScheduledExecutorService scheduler;
  private DemoTimer demoTimer;
  private int sequence_num_send = 0;
  private int sequence_num_wait = 0;
  private byte[] prev_ack;
  private int count = 0;

  public DemoTransportLayer(NetworkLayer networkLayer) throws IOException {
    super(networkLayer);
    sem = new Semaphore(1);  // Guard to send 1 pkt at a time.
    scheduler = Executors.newScheduledThreadPool(1);
    prev_ack = initAck(); // init Ack with sequence num -1.
    demoTimer = new DemoTimer();
  }

  // this is a helper function, print byte array
  private String print(byte[] bytes) {
    StringBuilder sb = new StringBuilder();
    sb.append("[ ");
    for (byte b : bytes) {
      sb.append(String.format("0x%02X ", b));
    }
    sb.append("]"); return sb.toString();
  }

  // covert an int to byte array, using two bytes to store the value
  private static byte[] intToByteArray(int value) {
      return new byte[] {
          (byte)value };
  }

  // covert an int to byte array, using two bytes to store the value
  private static byte[] intToByteArrays(int value) {
      return new byte[] {
          (byte)(value >> 8),
          (byte)value };
  }

  private byte[] generatePkt(byte[] data) throws IOException {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
    byte[] type = intToByteArrays(MSG_TYPE_DATA);
    byte[] seq = intToByteArrays(this.sequence_num_send);
    byte[] checksum = intToByteArrays(calChecksum(MSG_TYPE_DATA,
                          this.sequence_num_send, data));
    //System.out.println("send checksum:" + byteArrayToInt(checksum));
    outputStream.write(type);
    outputStream.write(seq);
    outputStream.write(checksum);
    outputStream.write(data);

    return outputStream.toByteArray();
  }

  private int invertBits(int num) {
      // calculating number of
      // bits in the number
      int x = (int)(Math.log(num) /
                    Math.log(2)) + 1;
      // Inverting the
      // bits one by one
      for (int i = 0; i < x; i++) {
        num = (num ^ (1 << i));
      }
      return num;      
  }

  private byte[] initAck() throws IOException {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
    byte[] type = intToByteArrays(MSG_TYPE_ACK);
    byte[] seq = intToByteArrays(-1);
    byte[] checksum = intToByteArrays(calAckChecksum(MSG_TYPE_ACK,
                          -1));
    outputStream.write(type);
    outputStream.write(seq);
    outputStream.write(checksum);
    return outputStream.toByteArray();
  }

  private int calChecksum(int type, int seq, byte[] data) {
    int checksum = type + seq;
    int MAX_16_BITS = 65535;
    for(int b:data){
        checksum += (b & 0xff);
    }
    //System.out.println("true: " +String.valueOf(checksum));
    if (checksum > MAX_16_BITS) {
      checksum++;
      return invertBits(checksum);
    }
    return checksum;
  }

  @Override
  public void send(byte[] data) throws IOException {
    try {
      sem.acquire();
      // message format
      log(new String(data));
      //log("No." + this.count + ", MSG of length " + data.length);
      byte[] pkt = generatePkt(data);
      byte[] copy = Arrays.copyOf(pkt, pkt.length);
      networkLayer.send(pkt);
      demoTimer.start(copy);
      /**timer = scheduler.scheduleAtFixedRate(new RetransmissionTask(copy),
                                            TIMEOUT_MSEC,
                                            TIMEOUT_MSEC,
                                            TimeUnit.MILLISECONDS);*/
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    // Start thread to read ACK.
    new Thread(() -> {
        try {
          // receive ACK;
          while(true) {
            byte[] ack_data = networkLayer.recv(); //block
            int type = byteArrayToInt(Arrays.copyOfRange(ack_data, 0, 2));
            int ack = byteArrayToInt(Arrays.copyOfRange(ack_data, 2, 4));
            int checksum = byteArrayToInt(Arrays.copyOfRange(ack_data, 4, 6));
            int re_checksum = calAckChecksum(type, ack);
            //System.out.println(String.format("checksum: %d, re_checksum: %d", checksum,
            //  re_checksum));
            if (checksum == re_checksum && ack == this.sequence_num_send) {
              //checksum == re_checksum &&
              this.sequence_num_send = (ack == 0) ? 1 : 0;
              //System.out.println("send close");
              //System.out.println(String.format("next send_seq: %d", this.sequence_num_send));
              demoTimer.stop();
              //timer.cancel(true);
              sem.release();
              break;
            } else {
              System.out.println("need retransmited");
            }
        }
          // do nothing, wait timeout and resend
        } catch (Exception e) {
          e.printStackTrace();
        }
    }).start();
    this.count++;
  }

  private static int byteArrayToInt(byte[] bytes) {
      return ((bytes[0] & 0xFF) << 8) |
              ((bytes[1] & 0xFF) << 0);
  }

  private boolean isValidSeq(int seq) {
    return this.sequence_num_wait == seq;
  }

  private int calAckChecksum(int type, int seq) {
    return type + seq;
  }

  private byte[] generateAck() throws IOException {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
    byte[] type = intToByteArrays(MSG_TYPE_ACK);
    byte[] seq = intToByteArrays(this.sequence_num_wait);
    byte[] checksum = intToByteArrays(calAckChecksum(MSG_TYPE_ACK,
                          this.sequence_num_wait));
    outputStream.write(type);
    outputStream.write(seq);
    outputStream.write(checksum);
    return outputStream.toByteArray();
  }

  @Override
  public byte[] recv() throws IOException {
    byte[] msg = null;
    while(true) {
      byte[] data = networkLayer.recv();
      int type = byteArrayToInt(Arrays.copyOfRange(data, 0, 2));
      int seq = byteArrayToInt(Arrays.copyOfRange(data, 2, 4));
      // if sequence number is not correct, send previous ack_pkt
      // and do nothing
      String s = "invalid seq";
      if(!isValidSeq(seq)) {
        //System.out.println(s);
        byte[] copy = Arrays.copyOf(this.prev_ack, this.prev_ack.length);
        networkLayer.send(copy);
        continue;
      }

      int checksum = byteArrayToInt(Arrays.copyOfRange(data, 4, 6));
      //System.out.println(String.format("recv: %d %d %d", type, seq, checksum));
      byte[] message = Arrays.copyOfRange(data, 6, data.length);
      //System.out.println("recv:" + print(message));
      log(new String(message));
      //log("No." + this.count + ", MSG of length " + message.length);
      int re_checksum = calChecksum(type, seq, message);
      if (checksum == re_checksum) {
        // generate new ack
        this.prev_ack = generateAck();
        // update next wait seq num 0->1 or 1->0
        this.sequence_num_wait = (seq == 0) ? 1 : 0;
        //System.out.println(String.format("next wait_seq: %d", this.sequence_num_wait));
        msg = message;
        byte[] copy = Arrays.copyOf(this.prev_ack, this.prev_ack.length);
        networkLayer.send(copy);
        break;
      } else {
        System.out.println("data corrupt" + ", checksum: " + String.valueOf(checksum) +
          ", re_checksum: " + String.valueOf(re_checksum));
      }
      byte[] copy = Arrays.copyOf(this.prev_ack, this.prev_ack.length);
      networkLayer.send(copy);
    }
    this.count++;
    return msg;
  }

  @Override
  public void close() throws IOException {
    try {
      sem.acquire();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    super.close();
  }

  private class DemoTimer {
    private ScheduledFuture<?> timer;
    private ScheduledExecutorService scheduler;

    public DemoTimer() {
      scheduler = Executors.newScheduledThreadPool(1);
    } // Constructor

    // Starts the timer
    public void start(byte[] data) {
      timer = scheduler.scheduleAtFixedRate(new RetransmissionTask(data),
                                                TIMEOUT_MSEC,
                                                TIMEOUT_MSEC,
                                                TimeUnit.MILLISECONDS);
    } // start

    // Stops the timer
    public void stop() {
      timer.cancel(true);
    } // stop

    // Restarts the timer
    public void restart(byte[] data) {
      stop();
      start(data);
    } // restart
  }

  private class RetransmissionTask implements Runnable {
    private byte[] data;

    public RetransmissionTask(byte[] data) {
      this.data = data;
    }

    @Override
    public void run() {
      try {
        byte[] copy = Arrays.copyOf(data, data.length);
        //System.out.println("retransmited data: " + print(copy));
        networkLayer.send(copy);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }
}
