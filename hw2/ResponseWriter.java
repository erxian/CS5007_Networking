package hw2;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.StringBuilder;

public class ResponseWriter {
    private static final int MAX_WRITE_SIZE = 16;

    private OutputStream out;
    private byte[] buf;

    public ResponseWriter(OutputStream out) {
        this.out = out;
        this.buf = new byte[MAX_WRITE_SIZE];
    }

    public void write(HttpResponse httpResponse)
            throws IOException {
        byte[] b = httpResponse.formatResponse().getBytes();
        int offset = 0;
        while (offset < b.length) {
            int len = Math.min(b.length - offset, MAX_WRITE_SIZE);
            out.write(b, offset, len);
            offset += len;
        }
    }
}
