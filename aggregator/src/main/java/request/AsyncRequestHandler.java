package request;

public abstract class AsyncRequestHandler {
    public void onBeforeSend(){}

    public void onCompleted(){}

    public void onError(){}

    public void onSuccess(){}

    public void onCancel(){}
}
