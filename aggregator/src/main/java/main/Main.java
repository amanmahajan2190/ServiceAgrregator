package main;

import client.ClientFactory;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import request.RequestFactory;
import request.RequestType;
import request.RootTask;
import response.transformer.TransformerFactory;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;

public class Main {
    public static AsyncHttpClient asyncHttpClient = new DefaultAsyncHttpClient();
    public static boolean end = false;
    public static ScheduledThreadPoolExecutor delayer = (ScheduledThreadPoolExecutor) Executors.newScheduledThreadPool(5);
    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);


    public static void main(String[] args) throws InterruptedException, IOException {
        LOGGER.info("MAIN START");
        ClientFactory clientFactory = new ClientFactory();
        TransformerFactory transformerFactory = new TransformerFactory();
        RequestFactory requestFactory = new RequestFactory(transformerFactory, clientFactory);
        RootTask t = requestFactory.getRequest(RequestType.DEMO);
        t.execute((response, status) -> {
            LOGGER.info("GOD CALLBACK.");
            LOGGER.info(response.getClass().getName());
        });
        while (!end) {
            LOGGER.info("Waiting for request tree to end.");
            Thread.sleep(5000);

            if (end) {
                break;
            }
        }

        asyncHttpClient.close();
        LOGGER.info("MAIN END");
    }
}
