package hw2;

import java.util.ArrayList;
import java.sql.Timestamp;
import java.util.concurrent.TimeUnit;


public class RequestRecords {
    ArrayList<Timestamp> getRecords = new ArrayList<>();
    ArrayList<Timestamp> postRecords = new ArrayList<>();
    ArrayList<String> expressions =  new ArrayList<>();
    final static int MAX_EXPREESION_NUM = 10;

    private int last_min_get;
    private int last_hour_get;
    private int last_24_hour_get;
    private int lifetime_get = 0;
 
    private int last_min_post;
    private int last_hour_post;
    private int last_24_hour_post;
    private int lifetime_post = 0;

    public RequestRecords() {
    }

    public void addGetRecords() {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());    
        getRecords.add(timestamp);
    }

    public void addPostRecords(String expression) {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());    
        postRecords.add(timestamp);
        if(expressions.size() == MAX_EXPREESION_NUM) {
            expressions.remove(0);
        }
        expressions.add(expression);
    }

    private void retriveGetRecords(Timestamp minute,
            Timestamp hour, Timestamp day) {
        last_min_get = 0;
        last_hour_get = 0;
        last_24_hour_get = 0;
        lifetime_get = getRecords.size();

        for(int i=getRecords.size() - 1; i>=0 ; i--) {
            if(getRecords.get(i).after(minute)) {
                last_min_get += 1;
            }
            if(getRecords.get(i).after(hour)) {
                last_hour_get += 1;
            }
            if(getRecords.get(i).after(day)) {
                last_24_hour_get += 1;
            }
        }
    }

    private void retrivePostRecords(Timestamp minute,
            Timestamp hour, Timestamp day) {
        last_min_post = 0;
        last_hour_post = 0;
        last_24_hour_post = 0;
        lifetime_post = postRecords.size();

        for(int i=postRecords.size() - 1; i>=0 ; i--) {
            if(postRecords.get(i).after(minute)) {
                last_min_post += 1;
            }
            if(postRecords.get(i).after(hour)) {
                last_hour_post += 1;
            }
            if(postRecords.get(i).after(day)) {
                last_24_hour_post += 1;
            }
        }
    }

    private void retriveRecords() {
        Timestamp oneMinuteBefore = new Timestamp(System.currentTimeMillis() -
                TimeUnit.MINUTES.toMillis(1));
        Timestamp oneHourBefore = new Timestamp(System.currentTimeMillis() -
                TimeUnit.MINUTES.toMillis(60));
        Timestamp oneDayBefore = new Timestamp(System.currentTimeMillis() -
                TimeUnit.MINUTES.toMillis(60*24));

        retriveGetRecords(oneMinuteBefore, oneHourBefore, oneDayBefore);
        retrivePostRecords(oneMinuteBefore, oneHourBefore, oneDayBefore);
    }

    public String buildRecords() {
        retriveRecords();
        FileHandler file = new FileHandler();
        String text = file.readFile();
        text = text.replace("$last_min_post", String.valueOf(last_min_post))
                .replace("$last_hour_post", String.valueOf(last_hour_post))
                .replace("$last_24_hour_post", String.valueOf(last_24_hour_post))
                .replace("$lifetime_post", String.valueOf(lifetime_post))
                .replace("$last_min_get", String.valueOf(last_min_get))
                .replace("$last_hour_get", String.valueOf(last_hour_get))
                .replace("$last_24_hour_get", String.valueOf(last_24_hour_get))
                .replace("$lifetime_get", String.valueOf(lifetime_get));

        String expressionsToHtml = "";
        for(int i=expressions.size() - 1; i>= 0; i--) {
            String start = "<li>";
            String end = "</li>";
            String CRLF = "\r\n"; 
            expressionsToHtml += start+expressions.get(i)+end+CRLF;
        } 
        text = text.replace("$last_10_expressions", expressionsToHtml);
        return text;
    }
}
