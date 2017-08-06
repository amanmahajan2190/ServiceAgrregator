import client.Client;
import client.ClientFactory;
import demo.SampleTasks;
import main.AsyncHandler;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import request.AsyncRequestHandler;
import request.Task;
import response.transformer.TransformerFactory;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class TestTask {

    @Mock
    private TransformerFactory transformerFactory;

    @Mock
    private ClientFactory clientFactory;

    @Mock
    private AsyncHandler asyncHandler;

    @Mock
    private AsyncRequestHandler asyncRequestHandler;

    private SampleTasks sampleTasks;

    @Before
    public void setUp() {
        Client client = mock(Client.class);
        sampleTasks = new SampleTasks(this.clientFactory, this.transformerFactory, this.asyncRequestHandler);
    }

    @Test
    public void testSingleTask() {
        Task task = sampleTasks.getSingleTask();

        verify(this.asyncRequestHandler, times(1)).onCompleted();
    }

    public void tearDown() {

    }
}
