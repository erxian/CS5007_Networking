package hw2;

import java.net.InetAddress;
import java.net.Socket;
import java.io.IOException;
import java.util.Scanner;
import java.io.OutputStream;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;

public class HttpClient {
  public static final int SERVER_PORT = 8181;
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
        String message = "POST /api/evalexpression HTTP/1.0\r\nContent-Length: 13\r\n\r\n(7+9)*-11+6+1";
        //String message = "GET /api/gettime HTTP/1.0\r\n\r\n";
        // sample expressions encoded according to the protocol 
      	out.write(message.getBytes()); 

    	// receive from server
        byte[] buffer = new byte[1024];
        in.read(buffer);
        System.out.println(new String(buffer));
        if ("quit".equalsIgnoreCase(line)) {
          break;
        }
      }
    clientSocket.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
