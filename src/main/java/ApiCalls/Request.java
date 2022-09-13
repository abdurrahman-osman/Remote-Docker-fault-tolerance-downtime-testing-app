package ApiCalls;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import java.io.File;
import java.math.BigDecimal;

public class Request implements Runnable{
    int num;
    public Request(int num)  {
        this.num = num;
    }
    public void makeRequest() throws UnirestException {
        Unirest.setTimeouts(0, 0);
        long startTime = System.nanoTime();
        HttpResponse<JsonNode> response = Unirest.post("http://10.0.7.132:8083/deploy")
                .field("name", "helloworld")
                .field("version", "0.0.1")
                .field("file", new File(System.getProperty("user.dir")+"/Dockerfile"))
                .field("platform", "edgex")
                .asJson();
        long elapsedTime = System.nanoTime() - startTime;
        BigDecimal responseTime = new BigDecimal(elapsedTime).divide(new BigDecimal(1000000L));
        System.out.println("Request number: "+num+"\nResponse time :"+responseTime.doubleValue()+"\n"+response.getBody().toString());
        System.out.println("Status: "+response.getStatus());

    }

    @Override
    public void run() {
        try {
            makeRequest();
        } catch (UnirestException e) {
            System.out.println(e.getMessage());
            throw new RuntimeException(e);
        }
    }
}