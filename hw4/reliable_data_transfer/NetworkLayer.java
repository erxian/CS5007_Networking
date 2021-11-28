package hw4;

import static hw4.Config.BIT_ERROR_PROB;
import static hw4.Config.MAX_SEGMENT_SIZE;
import static hw4.Config.MSG_LOST_PROB;
import static hw4.Util.log;
import static hw4.Util.randomBitError;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.io.Closeable;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

public class NetworkLayer implements Closeable {
  private int localPort;
  private int remotePort;
  private InetAddress addr;
  private DatagramSocket clientSocket;
  private DatagramSocket serverSocket;

  public NetworkLayer(int localPort, int remotePort) throws Exception {
    addr = InetAddress.getByName("localhost");
    this.localPort = localPort;
    this.remotePort = remotePort;
    clientSocket = new DatagramSocket();
    // serverSocket listens localPort for incoming packets.
    serverSocket = new DatagramSocket(localPort);
    log("NetworkLayer initialized");
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

  public void send(byte[] data) throws IOException {
    if (ThreadLocalRandom.current().nextDouble() < MSG_LOST_PROB) {
      return;
    }
    if (ThreadLocalRandom.current().nextDouble() < BIT_ERROR_PROB) {
      //System.out.println("before biterror: " + print(data));
      randomBitError(data);
      //System.out.println("after biterror: " + print(data));
    }
    //System.out.println("data: " + print(data));
    DatagramPacket pkt = new DatagramPacket(data, data.length, addr, remotePort);
    clientSocket.send(pkt);
  }

  public byte[] recv() throws IOException {
    byte[] buf = new byte[MAX_SEGMENT_SIZE];
    DatagramPacket pkt = new DatagramPacket(buf, MAX_SEGMENT_SIZE);
    serverSocket.receive(pkt);
    return Arrays.copyOf(pkt.getData(), pkt.getLength());
  }

  @Override
  public void close() throws IOException {
    log("Shutdown NetworkLayer");
    clientSocket.close();
    serverSocket.close();
  }
}
