package hw2;


public enum HttpStatusCode {

    RESPONSE_200_OK(200, "OK"),

    /* --- CLIENT ERRORS --- */
    ERROR_404_NOT_FOUND(404, "Not Found"),
    ERROR_400_BAD_REQUEST(400, "Bad Request"),
    ;


    public final int STATUS_CODE;
    public final String MESSAGE;

    HttpStatusCode(int STATUS_CODE, String MESSAGE) {
        this.STATUS_CODE = STATUS_CODE;
        this.MESSAGE = MESSAGE;
    }

}
