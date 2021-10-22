package hw2;

import java.util.*;

public class HttpRequest {
    private HttpMethod method;
    private String path;
    private String version;
    private HashMap<String, String> header;
    private String body;

    public HttpRequest() {
    }
    
    public HttpMethod getMethod() {
        return this.method;
    } 

    public String getPath() {
        return this.path;
    } 

    public String getVersion() {
        return this.version;
    } 

    public HashMap<String, String> getHeader() {
        return this.header;
    }

    public String getBody() {
        return this.body;
    }

    void setMethod(String methodName) throws HttpParseException {
        for (HttpMethod method: HttpMethod.values()) {
            if (methodName.equals(method.name())) {
                this.method = method;
                return;
            }
        }
        throw new HttpParseException("Unsupported Request Method");
    }

    void setPath(String path) throws HttpParseException {
        if (path == null || path.length()==0) {
            throw new HttpParseException("Missing URLs in Request Line");
        }
        this.path = path; 
    }

    void setVersion(String version) throws HttpParseException {
        List<String> HTTP_VERSION = Arrays.asList("HTTP/1.1", "HTTP/1.0");
        if (!HTTP_VERSION.contains(version)) {
           throw new HttpParseException("Unsupported HTTP Version");
        }
        this.version = version; 
    }

    void setHeader(HashMap<String, String> header) {
        this.header = header;
    }

    void setBody(String body) {
        this.body = body;
    }

    @Override
    public String toString() {
        return "HttpReques{" +
                "method: " + method +
                ", path: " + path +
                ", version: " + version +
                ", header: " + header.toString() +
                ", body: " + body +
                '}';
    }
}
