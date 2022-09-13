package ApiCalls;

import com.mashape.unirest.http.exceptions.UnirestException;

import java.io.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Main {

    static File resultFile;
    public static int checkServiceStatus(String appName) throws IOException, InterruptedException {
        List<String> result = new ArrayList<>();
        ProcessBuilder builder = new ProcessBuilder();
        builder.command("docker","--context", "new-context","inspect","-f","'{{.State.Status}}'",appName);
        Process process = builder.start();
        BufferedReader inputReader = new BufferedReader(new InputStreamReader(process.getInputStream()));

        int exitCode = process.waitFor();
        if(exitCode == 0){
            System.out.println("----------Result--------");
            while(inputReader.ready())
            {
                result.add(inputReader.readLine());
            }
            System.out.println(result.get(0));

        }else{
            System.out.println("Execution failed!");
            return -1;
        }
        if(result.get(0).contains("running"))
            return 1;
        else
            return 0;
    }
    public static void sendScheduledApiRequests(int number,long timeDelay){

        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(10);
        for (int i = 1;i<number;i++) {
            scheduler.schedule(new Request(i), i*timeDelay, TimeUnit.MILLISECONDS);
        }
        scheduler.shutdown();
    }

    public static void calculateDownTime(String appName,String resultFileName,int deployNumber) throws IOException, InterruptedException {
        int counter = 0;
        long startTime = 0L,elapsedTime = 0L;
        Request request = new Request(1);
        createResultFile(resultFileName);
        BufferedWriter writer = new BufferedWriter(new FileWriter(resultFile));
        if(checkServiceStatus(appName) == -1)
            request.run();
        stopContainer(appName);
        Thread.sleep(1000);
        while(true){
            if(checkServiceStatus(appName) == 0){
                startTime = System.nanoTime();
                startContainer(appName,request);
                elapsedTime = System.nanoTime() - startTime;
                BigDecimal responseTime = new BigDecimal(elapsedTime).divide(new BigDecimal(1000000000L));
                writer.write((counter+1)+"-"+responseTime.doubleValue()+"\n");
                counter++;
                Thread.sleep(1000L);
                stopContainer(appName);
            }
            if(counter == deployNumber){
                startContainer(appName,request);
                writer.close();
                break;
            }
        }

    }
    public static void startContainer(String appName,Request request) throws IOException, InterruptedException {
        while (checkServiceStatus(appName) == 0)
            request.run();
    }
    public static void stopContainer(String appName) throws IOException, InterruptedException {
        ProcessBuilder builder = new ProcessBuilder();
        builder.command("docker","--context", "new-context","stop",appName);
        Process process = builder.start();
        int exitCode = process.waitFor();
        if(exitCode == 0){
            System.out.println("Stopped container : "+appName);
        }else
            System.out.println("Could not stop the container : "+appName);

    }
    public static void createResultFile(String name){
        try {
            deleteIfExist(name);
            resultFile = new File(System.getProperty("user.dir")+"/"+name+".txt");
            if (resultFile.createNewFile()) {
                System.out.println("File created: " + resultFile.getName());
            } else {
                System.out.println("File already exists.");
            }
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }
    public static void deleteIfExist(String name){
        try
        {
            File f= new File(System.getProperty("user.dir")+"/"+name+".txt");           //file to be delete
            if(f.delete())
            {
                System.out.println("Previous "+f.getName() + " file deleted");   //getting and printing the file name
            }

        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }
    public static void main(String[] args) throws IOException, InterruptedException {
        calculateDownTime("helloworld","result",1);
    }
}