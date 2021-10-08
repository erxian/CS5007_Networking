package echo;

import java.net.InetAddress;
import java.net.Socket;
import java.io.IOException;
import java.util.Scanner;
import java.io.OutputStream;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;

public class EchoClient {
  public static final int SERVER_PORT = 8080;
  public static String print(byte[] bytes) {
      StringBuilder sb = new StringBuilder();
      sb.append("[ ");
      for (byte b : bytes) {
          sb.append(String.format("0x%02X ", b));
      }
      sb.append("]");
      return sb.toString();
  }
  
  public static void main(String... args) throws IOException{
    System.out.println("Echo client");
    try {
      InetAddress local = InetAddress.getLocalHost();  // Can use InetAddress.getByName().
      System.out.println("LocalHost: " + local);
      Socket clientSocket = new Socket(local, SERVER_PORT);
      OutputStream out = clientSocket.getOutputStream();
      InputStream in = clientSocket.getInputStream();
      System.out.println("Connected to server " + clientSocket.getInetAddress()
                         + " port " + clientSocket.getPort());
      Scanner scanner = new Scanner(System.in);
      while (true) {
        System.out.print("Enter whatever you want: ");
        String line = scanner.nextLine();
        if ("quit".equalsIgnoreCase(line)) {
          break;
        }

        // sample expressions encoded according to the protocol 
      	byte[] message = new byte[] {0x00, 0x06,
            0x00, 0x03, 0x31, 0x2D, 0x32,
            0x00, 0x0A, 0x33, 0x2B, 0x34, 0x2D, 0x35, 0x2D, 0x31, 0x2B, 0x31, 0x30,
            0x00, 0x03, 0x31, 0x2D, 0x32,
            0x00, 0x06, 0x37, 0x2B, 0x31, 0x32, 0x2D, 0x33,
            0x00, 0x12, 0x31, 0x2B, 0x32, 0x2D, 0x33, 0x2B, 0x34, 0x2B, 0x35, 0x2D, 0x39, 0x2B, 0x36, 0x2B, 0x37, 0x2D, 0x31, 0x33,
            0x00, 0x03, 0x30, 0x2D, 0x32};
        // send message to sever
      	out.write(message); 

    	// receive from server
        byte[] buffer = new byte[128];
        //response.write(buffer, 0, in.read(buffer));
        in.read(buffer, 0, 2);
        int answerNum = ((buffer[0] & 0XFF) << 8) | ((buffer[1] & 0XFF) << 0);
        System.out.println("there are: " + answerNum + " answers"); 
        byte[] lenBuffer = new byte[2];
        for (int i=0; i<answerNum; i++) {
            in.read(lenBuffer, 0, 2);
            int answerLen = ((lenBuffer[0] & 0XFF) << 8) | ((lenBuffer[1] & 0XFF) << 0);
            byte[] ansBuffer = new byte[10];
            in.read(ansBuffer, 0, answerLen);
            String answer = new String(Arrays.copyOfRange(ansBuffer, 0, answerLen));
            System.out.println(i+1 + " answer is: " + answer);
        }
    }
    clientSocket.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
