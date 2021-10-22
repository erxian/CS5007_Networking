package hw2;

import java.util.HashMap;
import java.util.Date;
import java.util.Arrays;
import java.util.ArrayList;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.sql.Timestamp;


public class HttpParser {
    final static String CRLF = "\r\n";
    final static String POST_URLs = "/api/evalexpression";
    final static String GET_URLs = "/api/gettime";
    final static String HTML_URLs = "/status.html";
    final static String CONTENT_LENGTH = "Content-Length";
    final static String CONTENT_TYPE = "Content-Type";
    private HttpRequest httpRequest = new HttpRequest();
    private HttpResponse httpResponse = new HttpResponse();
    private RequestRecords requestRecords;

    public HttpParser(RequestRecords requestRecords) {
        this.requestRecords = requestRecords;
    }

    public HttpRequest getHttpRequest() {
        return httpRequest;
    }

    public void setRequestLine(String[] requestLine) 
            throws HttpParseException {
        httpRequest.setMethod(requestLine[0]);
        httpRequest.setPath(requestLine[1]);
        httpRequest.setVersion(requestLine[2]);
    }

    public int setHeaderLines(String[] requestMessage) {
        // get header lines, header start at line 1 and end before line "\r\n"
        HashMap<String, String> header = new HashMap<String, String>();
        int h = 1;
        while (!requestMessage[h].trim().isEmpty()) {
            String[] headerLine = requestMessage[h].split(("\\s*:\\s*"));
            header.put(headerLine[0], headerLine[1]);
            h += 1;
        }
        httpRequest.setHeader(header);
        return h;
    }

    public void setBodyLines(String[] requestMessage, int h) {
        String body = "";
        for(int i=h; i<requestMessage.length; i++) {
           body += requestMessage[i];
        }
        httpRequest.setBody(body);
    }

    public HttpResponse parseHttpRequest(String request)
            throws HttpParseException {
        String[] requestMessage = request.split(CRLF);
        String[] requestLine = requestMessage[0].split(" ");

        setRequestLine(requestLine); // Get
        if (httpRequest.getMethod().equals(HttpMethod.POST)) { 
            int headerLinesLen = setHeaderLines(requestMessage);
            setBodyLines(requestMessage, headerLinesLen);  
        }
        return executeRequest();
    }

    private void responseBuilder(HttpStatusCode httpStatusCode,
            String content_type, String content_length, String text) {
        httpResponse.setVersion(httpRequest.getVersion());
        httpResponse.setHttpStatusCode(httpStatusCode);
        httpResponse.setHeader(CONTENT_TYPE, content_type);
        httpResponse.setHeader(CONTENT_LENGTH, content_length);
        httpResponse.setText(text);
    }

    private HttpResponse executeRequest() {
        HttpMethod method = httpRequest.getMethod();
        if(!isValidURLs(method)) {
            responseBuilder(HttpStatusCode.ERROR_404_NOT_FOUND,
                    "text/html", String.valueOf(0), "");
            return httpResponse;
        }
        switch(method) {
            case GET:
                if (httpRequest.getPath().equals(HTML_URLs)) {
                    String text = requestRecords.buildRecords();
                    responseBuilder(HttpStatusCode.RESPONSE_200_OK,
                            "text/html",
                            String.valueOf(text.length()),
                            text);
                    break;
                } 
                responseBuilder(HttpStatusCode.RESPONSE_200_OK,
                        "text/html",
                        String.valueOf(getLocalDate().length()),
                        getLocalDate());
                requestRecords.addGetRecords(); 
                break;
            case POST:
                if (evalExpression() == null) {
                    responseBuilder(HttpStatusCode.ERROR_400_BAD_REQUEST,
                        "text/html", String.valueOf(0), "");
                    break;
                }
                responseBuilder(HttpStatusCode.RESPONSE_200_OK,
                        "text/html",
                        String.valueOf(evalExpression().length()),
                        evalExpression());
                requestRecords.addPostRecords(getExpression());
                break;
        }
        return httpResponse;
    }

    private String getLocalDate() {
        DateFormat df = new SimpleDateFormat("EE MMM dd HH:mm:ss yyyy");
        Date date = new Date();
        return df.format(date);
    }

    private String getExpression() {
        int len = Integer.valueOf(
                    httpRequest.getHeader().get(CONTENT_LENGTH)); 
        return httpRequest.getBody().substring(0, len);
    }

    // evaluate the arithmetic expression, such as "1+5-3"
    // after get the int result 3, encode the result to bytes array
    private String evalExpression() {
        String expression = getExpression();
        expression = expression.replace("(", "0+");
        expression = expression.replace(")", "+0");
    	String[] nums = expression.split("\\+|(?=-)");
        if (hasNonNumerical(nums)) {
            return null;
        }
    	int result = Arrays.stream(nums).mapToInt(Integer::parseInt).sum();
    	return String.valueOf(result);
    } 

    private boolean hasNonNumerical(String[] nums) {
        return Arrays.stream(nums)
            .map(num -> isNumeric(num)) 
            .anyMatch(val -> val.equals(false));
    }

    private boolean isNumeric(String str) {
        if (str != null && !"".equals(str.trim())) {
            return str.matches("-?\\d+(\\.\\d+)?");
        }
        return false;
    }

    private boolean isValidURLs(HttpMethod method) {
        String path = httpRequest.getPath();
        if (method.equals(HttpMethod.GET)) {
            return path.equals(GET_URLs) || path.equals(HTML_URLs);
        } else if (method.equals(HttpMethod.POST)) {
            return path.equals(POST_URLs);
        }
        return false;
    }
}
