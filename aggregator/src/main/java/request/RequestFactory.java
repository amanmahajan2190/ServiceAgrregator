package request;

import client.ClientFactory;
import client.ClientType;
import demo.Demo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import response.dto.DtoBase;
import response.transformer.TransformerFactory;
import response.transformer.TransformerType;
import response.viewmodels.ViewModel;

public class RequestFactory {
    private TransformerFactory transformerFactory;
    private ClientFactory clientFactory;
    private static final Logger LOGGER = LoggerFactory.getLogger(RequestFactory.class);

    public RequestFactory(TransformerFactory transformerFactory, ClientFactory clientFactory) {
        this.transformerFactory = transformerFactory;
        this.clientFactory = clientFactory;
    }

    public Task createTask(ClientType clientType,
                           TransformerType transformerType,
                           Class<? extends DtoBase> valueType){
        return new Task(this.clientFactory,
                clientType,
                this.transformerFactory,
                transformerType,
                valueType);
    }

    public RootTask createRootTask(TransformerType transformerType,
                                   Task task,
                                   Class<? extends ViewModel> valueType){
        return new RootTask(transformerType,
                this.transformerFactory,
                task,
                valueType);
    }

    public RootTask getRequest(RequestType requestType) {
        RootTask rootTask = null;
        switch (requestType) {
            case DEMO:
                rootTask = Demo.getRequestTree(clientFactory, transformerFactory);
                break;
            default:
                LOGGER.error("No instance found for request type: {}", requestType);

        }

        if (rootTask != null) {
            rootTask.preProcess();
        }

        return rootTask;
    }
}
