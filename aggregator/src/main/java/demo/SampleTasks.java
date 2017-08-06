package demo;

import client.ClientFactory;
import client.ClientType;
import request.AsyncRequestHandler;
import request.Task;
import response.dto.UserDto;
import response.transformer.TransformerFactory;
import response.transformer.TransformerType;

public class SampleTasks {

    private ClientFactory clientFactory;
    private TransformerFactory transformerFactory;
    private AsyncRequestHandler asyncRequestHandler;

    public SampleTasks(ClientFactory clientFactory, TransformerFactory transformerFactory, AsyncRequestHandler asyncRequestHandler){
        this.clientFactory = clientFactory;
        this.transformerFactory = transformerFactory;
        this.asyncRequestHandler = asyncRequestHandler;
    }

    public Task getSingleTask(){
        Task task = new Task(
                this.clientFactory,
                ClientType.HTTP,
                this.transformerFactory,
                TransformerType.JACKSON,
                UserDto.class,
                this.asyncRequestHandler);
        task.setName("Simple Task");
        task.setRequestUrl("https://github.com/AsyncHttpClient/async-http-client");
        return task;
    }
}
