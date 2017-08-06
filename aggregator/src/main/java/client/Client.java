package client;


import main.AsyncHandler;

public interface Client {
    public void sendRequest();

    public void sendRequestAsync(String requestUrl, AsyncHandler handler);

    public void cancel();
}
