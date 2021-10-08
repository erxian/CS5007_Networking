package echo;

import java.net.Socket;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.util.Arrays;

public class Handler implements Runnable {
    private Socket clientSocket;

    public Handler(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    // this is a helper function, print byte array
    //public static String print(byte[] bytes) {
    //  StringBuilder sb = new StringBuilder();
    //  sb.append("[ ");
    //  for (byte b : bytes) {
    //      sb.append(String.format("0x%02X ", b));
    //  }
    //  sb.append("]"); return sb.toString();
    //}

    // convert the partial of an byte array to an int
    public static int byteToint(byte[] bytes, int start, int end) {
    	return ((bytes[start] & 0xFF) << 8) |
    		((bytes[end] & 0xFF) << 0);
    }

    // covert an int to byte array, using two bytes to store the value
    public static byte[] intToByteArray(int value) {
    		return new byte[] {
        		(byte)(value >> 8),
        		(byte)value };
    }

    // encode an int number to bytes with big endian and associated 
    // with the length of number
    // e.g.
    // parameter: int result = 15;
    // returns 0x00 0x02 0x31 0x35
    public static byte[] encodeAnswer(int result) throws IOException {
    	// the length of the result
    	int numDigits = String.valueOf(result).length();
    	byte[] answerLen = intToByteArray(numDigits);
  
    	// convert result to String, and getBytes
    	String answer = String.valueOf(result);
    	byte[] answerBytes = answer.getBytes();
  
    	// appending answer length and anwer itself
    	ByteArrayOutputStream appendAnswer = new ByteArrayOutputStream();
    	appendAnswer.write(answerLen);
    	appendAnswer.write(answerBytes);
    	return appendAnswer.toByteArray();
    }

    // evaluate the arithmetic expression, such as "1+5-3"
    // after get the int result 3, encode the result to bytes array
    public static byte[] evalExpression(String expression) throws IOException {
    	// evaluate the expression
    	String[] nums = expression.split("\\+|(?=-)");
    	int result = Arrays.stream(nums).mapToInt(Integer::parseInt).sum();
  
    	return encodeAnswer(result);
    }
    
    // this function will process input stream from client-socket
    // and evaluate the received arithmetic expressions, ecode the
    // answers to bytes array and send back to client-socket
    public static void processInputStream(InputStream in, OutputStream out)
            throws IOException {
        int len = -1; 
        // using two bytes to store int number in the protocol design
        int reservedByte = 2;
        int MAX_BUFFER_SIZE = 16;
        byte[] buffer = new byte[reservedByte]; 

        // use while loop to read the first two bytes 
        while ((len = in.read(buffer, 0, reservedByte)) != -1) {
            // use response to store the output stream
            ByteArrayOutputStream response = new ByteArrayOutputStream();
            response.write(buffer, 0, reservedByte);
            int expressionNum = byteToint(buffer, 0, 1);
            byte[] lenBuffer = new byte[reservedByte];

            for (int i=0; i<expressionNum; i++) {
                // read the subsequent two bytes where store the expression length
                in.read(lenBuffer, 0, reservedByte);
                int expressionLen = byteToint(lenBuffer, 0, 1);
                byte[] expBuffer = new byte[1024]; // store the expression content
                // buffer reading at most 16 bytes each time
                if (expressionLen > MAX_BUFFER_SIZE) {
                    int count = 0;
                    int tempLen = expressionLen;
                    // read untill expressionLen is smaller than MAX_BUFFER_SIZE
                    while (tempLen/MAX_BUFFER_SIZE > 0) {
                         in.read(expBuffer, count*MAX_BUFFER_SIZE, MAX_BUFFER_SIZE);
                         tempLen -= MAX_BUFFER_SIZE;
                         count += 1;
                     }
                     // read the remaining part of the expression
                     in.read(expBuffer, count*MAX_BUFFER_SIZE, tempLen);
                } else {
                    in.read(expBuffer, 0, expressionLen);
                }
    		    String expression = new String(Arrays.copyOfRange(expBuffer, 0, expressionLen));
                System.out.println(i+1 + " expression: " + expression + " = ");

    		    byte[] encodedanswer = evalExpression(expression);
                response.write(encodedanswer);
          }
          // sending answers to client
          out.write(response.toByteArray());
      }
  }

   @Override
   public void run() {
      String client = String.format("[%s:%d]", clientSocket.getInetAddress(), clientSocket.getPort());
      System.out.println(String.format("Handle client %s", client));
      try {
         InputStream in = clientSocket.getInputStream();
         OutputStream out = clientSocket.getOutputStream();

         // process input stream according to the protocol
         processInputStream(in, out);

         System.out.println(String.format("Bye bye %s", client));
         clientSocket.close();
      } catch (IOException e) {
         e.printStackTrace();
      }
   }
}
