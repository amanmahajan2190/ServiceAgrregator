package client.clients;


import client.Client;
import main.Constants;
import main.Main;
import main.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import request.Task;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class HttpClient implements Client {

    private static final Logger LOGGER = LoggerFactory.getLogger(Task.class);

    @Override
    public void sendRequest() {
        throw new NotImplementedException();
    }


    @Override
    public void sendRequestAsync(String requestUrl, main.AsyncHandler asyncHandler){
        LOGGER.info("Sending Request: {}", requestUrl);
        Main.asyncHttpClient
                .prepareGet(requestUrl)
                .execute()
                .toCompletableFuture()
                .thenApply(resp -> {
                    try {
                            asyncHandler.onCompleted(resp.getResponseBody(), Status.SUCESS);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        return resp;
                    }
                )
                .exceptionally((Throwable e) -> {
                    try {
                        asyncHandler.onCompleted(Constants.test, Status.FAILED);
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                    return null;
                });
    }

    @Override
    public void cancel(){
        this.cancel();
    }
}
