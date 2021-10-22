package hw2;

import java.net.Socket;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.Date;
import java.lang.StringBuilder;
import java.util.*;

public class RequestHandler implements Runnable {
    private Socket clientSocket;
    private RequestRecords requestRecords;

    public RequestHandler(Socket clientSocket,
            RequestRecords requestRecords) {
        this.clientSocket = clientSocket;
        this.requestRecords = requestRecords;
    }

   @Override
   public void run() {
      String client = String.format("[%s:%d]", clientSocket.getInetAddress(), clientSocket.getPort());
      System.out.println(String.format("Handle client %s", client));
      try {
         InputStream in = clientSocket.getInputStream();
         OutputStream out = clientSocket.getOutputStream();

         RequestReader reader = new RequestReader(in);
         String request = reader.read();
         HttpParser httpParser = new HttpParser(requestRecords);
         HttpResponse httpResponse = httpParser.parseHttpRequest(request);

         ResponseWriter responseWriter = new ResponseWriter(out);
         responseWriter.write(httpResponse);
         System.out.println(String.format("Bye bye %s", client));
         clientSocket.close();
      } catch (HttpParseException e) {
          System.out.println("ERROR: " + e.getMessage());
      } catch (IOException e) {
          e.printStackTrace();
      }
   }
}
