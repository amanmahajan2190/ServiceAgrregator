package main;

public interface AsyncHandler<T> {
    /**
     * @return
     * @throws Exception
     */
    public void onCompleted(T response, Status status);
}
