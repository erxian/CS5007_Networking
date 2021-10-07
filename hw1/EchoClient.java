package echo;

import java.net.InetAddress;
import java.net.Socket;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.PrintWriter;
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
      //PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true);
      OutputStream out = clientSocket.getOutputStream();
      //BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
      InputStream in = clientSocket.getInputStream();
      System.out.println("Connected to server " + clientSocket.getInetAddress()
                         + " port " + clientSocket.getPort());
      Scanner scanner = new Scanner(System.in);
      while (true) {
        System.out.print("Enter text: ");
        String line = scanner.nextLine();
        if ("quit".equalsIgnoreCase(line)) {
          break;
        }
        //writer.println(line);
        //String response = reader.readLine();
        //System.out.println("Server response: " + response);
      
      	byte[] message = new byte[] {0x00, 0x06,
					 0x00, 0x03, 0x31, 0x2D, 0x32, 0x00, 0x0A,
				         0x33, 0x2B, 0x34, 0x2D, 0x35, 0x2D, 0x31, 0x2B, 0x31, 0x30,
					 0x00, 0x03, 0x31, 0x2D, 0x32, 0x00, 0x0A,
					 0x33, 0x2B, 0x34, 0x2D, 0x35, 0x2D, 0x31, 0x2B, 0x31, 0x30,
					 0x00, 0x03, 0x31, 0x2D, 0x32, 0x00, 0x0A,
					 0x33, 0x2B, 0x34, 0x2D, 0x35, 0x2D, 0x31, 0x2B, 0x31, 0x30
				    };
      	out.write(message); 
	
	// receive from server
	ByteArrayOutputStream response = new ByteArrayOutputStream();
	byte[] buffer = new byte[1024];
	//int len;
	//while ((len = in.read(buffer)) != -1) {
	//	response.write(buffer, 0, len);
	//}
	response.write(buffer, 0, in.read(buffer));
	System.out.println("Server response is:" + print(response.toByteArray()));
      }
      clientSocket.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
