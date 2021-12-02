package hw4;

import static hw4.Config.TIMEOUT_MSEC;
import static hw4.Config.MSG_TYPE_DATA;
import static hw4.Config.MSG_TYPE_ACK;
import static hw4.Config.WINDOW_SIZE;
import static hw4.Config.BIT_ERROR_PROB;
import static hw4.Config.MSG_LOST_PROB;
import static hw4.Util.log;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Iterator;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

// TODO.
public class GoBackN extends TransportLayer {
  private Semaphore sem;
  private ScheduledFuture<?> timer;
  private ScheduledExecutorService scheduler;
  private int sequence_num_send = 1;
  private int sequence_num_wait = 1;
  private int base = 1;
  private byte[] prev_ack;
  private LinkedList<byte[]> unACKedPacketsSent = new LinkedList<byte[]>();
  private boolean start_daemon_thread = false;

  private ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
  //private Lock readLock = lock.readLock();
  private Lock writeLock = lock.writeLock();

  public GoBackN(NetworkLayer networkLayer) throws IOException {
    super(networkLayer);
    sem = new Semaphore(WINDOW_SIZE);  // Guard to send WINDOW_SIZE pkt at a time.
    scheduler = Executors.newScheduledThreadPool(1);
    prev_ack = initAck(); // init Ack with sequence num 0.
  }

  @Override
  public void send(byte[] data) throws IOException {   
    // Create thread to read ACK.
    Thread daemon_thread = create_deamon_thread();

    try {
      sem.acquire();
      // message format
      byte[] pkt = generatePkt(data);
      byte[] pkt_copy = Arrays.copyOf(pkt, pkt.length);
      networkLayer.send(pkt);
      writeLock.lock();
      try {
        this.unACKedPacketsSent.offer(pkt_copy);
      } finally {
        writeLock.unlock();
      }
      if (this.base == this.sequence_num_send) {
          timer = scheduler.scheduleAtFixedRate(new RetransmissionTask(this.unACKedPacketsSent),
                                          TIMEOUT_MSEC,
                                          TIMEOUT_MSEC,
                                          TimeUnit.MILLISECONDS);
          this.start_daemon_thread = false;
      }
      this.sequence_num_send++;
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    if (!this.start_daemon_thread) {
      daemon_thread.setDaemon(true);
      daemon_thread.start();
      this.start_daemon_thread = true;
    }
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
      if(!isValidSeq(seq)) {
        byte[] copy = Arrays.copyOf(this.prev_ack, this.prev_ack.length);
        networkLayer.send(copy);
        continue;
      }

      int checksum = byteArrayToInt(Arrays.copyOfRange(data, 4, 6));
      byte[] message = Arrays.copyOfRange(data, 6, data.length);
      int re_checksum = calChecksum(type, seq, message);
      if (checksum == re_checksum) {
        // generate new ack
        this.prev_ack = generateAck();
        // update next wait seq_num + 1
        this.sequence_num_wait++;
        msg = message;
        byte[] copy = Arrays.copyOf(this.prev_ack, this.prev_ack.length);
        networkLayer.send(copy);
        break;
      }
      byte[] copy = Arrays.copyOf(this.prev_ack, this.prev_ack.length);
      networkLayer.send(copy);
    }
    return msg;
  }

  private Thread create_deamon_thread() throws IOException {
    return new Thread(() -> {
      try {
        while(true) {
          // receive ACK;
          if(this.unACKedPacketsSent.size() == 0) {
            break;
          }
          byte[] ack_data = networkLayer.recv(); //block
          int type = byteArrayToInt(Arrays.copyOfRange(ack_data, 0, 2));
          int ack = byteArrayToInt(Arrays.copyOfRange(ack_data, 2, 4));
          int checksum = byteArrayToInt(Arrays.copyOfRange(ack_data, 4, 6));
          int re_checksum = calAckChecksum(type, ack);
          if (checksum == re_checksum) { // only care about the ack
            if (ack < this.base) {
              continue; // don't need the ack before base
            }
            int release_key = ack - this.base + 1;
            this.base = ack + 1;
            writeLock.lock();
            try {
              removeACKedPacketsSent(this.unACKedPacketsSent, this.base);
            } finally {
              writeLock.unlock();
            }

            if (this.base == this.sequence_num_send) {
              timer.cancel(true);
            } else {
              timer.cancel(true);
              timer = scheduler.scheduleAtFixedRate(new RetransmissionTask(this.unACKedPacketsSent),
                            TIMEOUT_MSEC,
                            TIMEOUT_MSEC,
                            TimeUnit.MILLISECONDS);
            }
            sem.release(release_key);
          }
        }  
      // do nothing, wait timeout and resend
      } catch (Exception e) {
        e.printStackTrace();
      }
    });
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
    outputStream.write(type);
    outputStream.write(seq);
    outputStream.write(checksum);
    outputStream.write(data);

    return outputStream.toByteArray();
  }

  private int getSeqNum(byte[] data) {
    return byteArrayToInt(Arrays.copyOfRange(data, 2, 4));
  }

  private void removeACKedPacketsSent(LinkedList<byte[]> unACKedPacketsSent,
        int base) {
    synchronized (this.unACKedPacketsSent) {
      Iterator<byte[]> it = this.unACKedPacketsSent.iterator();  
      while (it.hasNext()) {
        byte[] pkt = (byte[]) it.next();
        if (getSeqNum(pkt) < base) {
          it.remove();
        }
      }
    }  
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
    byte[] seq = intToByteArrays(0);
    byte[] checksum = intToByteArrays(calAckChecksum(MSG_TYPE_ACK,
                          0));
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

  private int cal_waiting_time() {
    int MIN_WAIT = 4; // at least wait 4 seconds
    return 1000*(MIN_WAIT + (int) (BIT_ERROR_PROB + MSG_LOST_PROB)*10);
  }

  @Override
  public void close() throws IOException {
    try {
      sem.acquire();
      // in case GBN protocol close socket before ACK
      Thread.sleep(cal_waiting_time());
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    super.close();
  }

  private class RetransmissionTask implements Runnable {
    private LinkedList<byte[]> data;

    public RetransmissionTask(LinkedList<byte[]> data) {
      this.data = data;
    }

    @Override
    public void run() {
      try {
        LinkedList<byte[]> copy = (LinkedList) this.data.clone();
        for (byte[] pkt : copy) {
          networkLayer.send(Arrays.copyOf(pkt, pkt.length));
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }
}
