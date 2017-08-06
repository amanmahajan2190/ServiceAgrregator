package demo;

import client.ClientFactory;
import client.ClientType;
import request.RootTask;
import request.Task;
import response.dto.UserDto;
import response.transformer.TransformerFactory;
import response.transformer.TransformerType;
import response.viewmodels.UserViewModel;

import java.time.Duration;
import java.util.ArrayList;

public class Demo {

    public static RootTask getRequestTree(ClientFactory clientFactory, TransformerFactory transformerFactory) {
        Task task = new Task(clientFactory,
                ClientType.HTTP,
                transformerFactory,
                TransformerType.JACKSON,
                UserDto.class);
        task.setName("0");
        task.setTimeOut(Duration.ofMillis(5000));
        task.setRequestUrl("https://jsonplaceholder.typicode.com/users/1");

        Task currentNode = task;
        for (int i = 0; i < 4; i++) {
            currentNode.setChildren(new ArrayList<>());
            for (int j = 0; j < 4; j++) {
                Task newRequest = new Task(clientFactory,
                        ClientType.HTTP,
                        transformerFactory,
                        TransformerType.JACKSON,
                        UserDto.class);
                newRequest.setParents(new ArrayList<>());
                newRequest.getParents().add(currentNode);
                newRequest.setName(currentNode.getName() + " -> " + j);
                newRequest.setRequestUrl("https://jsonplaceholder.typicode.com/users/1");
                currentNode.getChildren().add(newRequest);
            }

            currentNode = currentNode.getChildren().get(0);
        }

        return new RootTask(TransformerType.USER,
                transformerFactory,
                task,
                UserViewModel.class);
    }
}
