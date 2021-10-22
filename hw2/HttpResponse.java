package hw2;

import java.util.HashMap;

public class HttpResponse {
    final static String CRLF = "\r\n";
    final static String CONTENT_LENGTH = "Content-Length";
    final static String CONTENT_TYPE = "Content-Type";
    private String version;
    private HttpStatusCode httpStatusCode;
    private HashMap<String, String> header = new HashMap<String, String>();
    private String text = "";

    public HttpResponse() {
    } 

    public HttpStatusCode getHttpStatusCode() {
        return httpStatusCode;
    }

    void setVersion(String version) {
        this.version = version;
    }

    void setHttpStatusCode(HttpStatusCode httpStatusCode) {
        this.httpStatusCode = httpStatusCode;
    }

    void setHeader(String name, String value) {
  
        this.header.put(name, value);
    }

    void setText(String text) {
        this.text = text;
    }

    public String formatResponse() {
        return version + " " +
            String.valueOf(httpStatusCode.STATUS_CODE) + " " +
            httpStatusCode.MESSAGE + CRLF +
            "Content-Type: " + header.get("Content-Type") + CRLF +
            "Content-Length: " + header.get("Content-Length") + CRLF +
            CRLF +
            text;  
    }
}
