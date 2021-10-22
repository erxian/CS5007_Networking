package hw2;

import java.net.Socket;
import java.net.ServerSocket;
import java.io.IOException;

public class ThreadedHttpServer {
  public static final int SERVER_PORT = 8181;
  
  public static void main(String... args) throws IOException {
    System.out.println("Threaded HTTP Server");
    ServerSocket serverSocket = new ServerSocket(SERVER_PORT);
    RequestRecords rd = new RequestRecords();
    try {
      System.out.println("Start to accept incoming connections");
      while (true) {
        Socket clientSocket = serverSocket.accept();
        new Thread(new RequestHandler(clientSocket, rd)).start();
      }
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      serverSocket.close();
    }
  }
}
