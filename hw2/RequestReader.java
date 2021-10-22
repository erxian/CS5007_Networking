package hw2;

import java.io.IOException;
import java.io.InputStream;
import java.lang.StringBuilder;

public class RequestReader {
    private static final int BUFSIZE = 16;

    private InputStream in;
    private byte[] buf;
    
    public RequestReader(InputStream in) {
        this.in = in;
        this.buf = new byte[BUFSIZE];
    }
   
    public String read() throws IOException {
        StringBuilder sb = new StringBuilder();
        int len = -1;
        while ((len = in.read(buf, 0, BUFSIZE)) != -1) {
            sb.append(new String(buf, 0, len));
            if (len < BUFSIZE) {
               break;
            } 
        }
        return sb.toString();
    }
}
