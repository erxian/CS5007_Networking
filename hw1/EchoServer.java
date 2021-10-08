package echo;

import java.net.ServerSocket;
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
    
public class EchoServer {
    public static final int SERVER_PORT = 8080;
     
     // this is a helper function, print an byte array 
     public static String print(byte[] bytes) {
       StringBuilder sb = new StringBuilder();
       sb.append("[ ");
       for (byte b : bytes) {
           sb.append(String.format("0x%02X ", b));
       }
       sb.append("]"); return sb.toString(); }

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
	
	// encode an int number to bytes and associated with the length of number 
	// e.g.
	// input: int result = 15;
	// returns 0x00 0x02 0x31 0x35
	public static byte[] encodeAnswer(int result) {
		// the length of the result
		int numDigits = String.valueOf(result).length();
		byte[] answerLen = intToByteArray(numDigits);
		//System.out.println("answer length is: " + print(answerLen));

		// convert result to String, and getBytes
		String answer = String.valueOf(result);
		byte[] answerBytes = answer.getBytes();
		//System.out.println("answer bytes = " + print(answerBytes));
		
		// appending answer length and anwer itself
		byte[] appendAnswer= null;
		try {
			ByteArrayOutputStream output = new ByteArrayOutputStream();
			output.write(answerLen);
			output.write(answerBytes);
			byte[] out = output.toByteArray();
			//System.out.println("an entire answer = " + print(out));
			appendAnswer = out;;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return appendAnswer;
	}

	// evaluate the arithmetic expression, such as "1+5-3"
	// after get the int result 3, encode the result to bytes array 
	public static byte[] evalExpression(String expression) {
		// evaluate the expression
		String[] nums = expression.split("\\+|(?=-)");
		int result = Arrays.stream(nums).mapToInt(Integer::parseInt).sum();
		//System.out.println("expression: " + expression + " = " + result);

		return encodeAnswer(result);
	}

	// this function will finish three things:
	// 1. decode client request according to the protocol
	// 2. evaluate every arithmetic expression
	// 3. encode the result of arithmetric expression
	public static byte[] decodeRequest(byte[] request) throws IOException{
		// firt two bytes store the number of expression
		int expressNum = byteToint(request, 0, 1);

		int pos = 2; // starting from byte[2]
		int interval = 2; // using two bytes to store the length of expression

		// response first two bytes store the number of answers
		ByteArrayOutputStream response = new ByteArrayOutputStream();
		response.write(Arrays.copyOfRange(request, 0, 2));

		// decode each expression's length and its content
		for (int i=0; i<expressNum; i++) {
			int lenBuffer = byteToint(request, pos, pos + interval - 1);
			//System.out.println(lenBuffer);
			String expression = new String(Arrays.copyOfRange(request, pos + interval,
				pos + interval + lenBuffer));

			byte[] val = evalExpression(expression);
			//System.out.println("encoded answer is:" + print(val));
			response.write(val);
			pos = pos + interval + lenBuffer;
		}
		return response.toByteArray();
	}

    public static void processInputStream(InputStream in, OutputStream out)
        throws IOException {

                byte[] buffer = new byte[2]; // store the first two bytes 
                
                int len = -1;
                int byteNum = 2;
                int MAX_BUFFER_SIZE = 8; // if expression is too long, max read bytes is 16
                while ((len = in.read(buffer, 0, byteNum)) != -1) {
                    ByteArrayOutputStream request = new ByteArrayOutputStream();
                    request.write(buffer, 0, byteNum);
                    int expressionNum = byteToint(buffer, 0, 1);
                    //System.out.println("expressionNum is :" + expressionNum);

                    for (int i=0; i<expressionNum; i++) {
                        byte[] lenBuffer = new byte[2]; // store the length of expression 
                        in.read(lenBuffer, 0, 2); // read two bytes
                        request.write(lenBuffer, 0, 2);
                        
                        int expressionLen = byteToint(lenBuffer, 0, 1);
                        System.out.println("lenBuffer is: " + expressionLen);
                      
                        byte[] expBuffer = new byte[MAX_BUFFER_SIZE]; // store the expression
                        if (expressionLen > MAX_BUFFER_SIZE) {
                            // read untill expressionLen is smaller than MAX_BUFFER_SIZE
                            while (expressionLen/MAX_BUFFER_SIZE > 0) { 
                                 in.read(expBuffer, 0, MAX_BUFFER_SIZE); 
                                 request.write(expBuffer, 0, MAX_BUFFER_SIZE);
                                 System.out.println("expression is: " + print(expBuffer));
                                 expressionLen -= MAX_BUFFER_SIZE;
                             }
                             // read the remaining part of the expression 
                             in.read(expBuffer, 0, expressionLen);
                             System.out.println("remaining expression is: " + print(expBuffer));
                             request.write(expBuffer, 0, expressionLen);
                        } else {
                            in.read(expBuffer, 0, expressionLen);
                            request.write(expBuffer, 0, expressionLen);
                            System.out.println("expression is: " + print(expBuffer));
                        }
                        System.out.println("request is: " + print(request.toByteArray()));
                    }

    	            byte[] response = decodeRequest(request.toByteArray());
		            // sending data to client
    	            System.out.println("response is:" + print(response));
		            out.write(response);
                }

    }
 
    public static void main(String... args) throws IOException {
        System.out.println("Echo server");
        ServerSocket serverSocket = new ServerSocket(SERVER_PORT);
        try {
        	System.out.println("Start to accept incoming connections");
        	while (true) {
        		Socket clientSocket = serverSocket.accept();
  			    InputStream in = clientSocket.getInputStream();
	  		    OutputStream out = clientSocket.getOutputStream();
	  		    // process client request	
                processInputStream(in, out);
                 
		        System.out.println("end..."); 
	        	clientSocket.close();
            }
	    } catch (IOException e) {
	    	e.printStackTrace();
	    } finally {
	    	serverSocket.close();
	    }
    }
}

