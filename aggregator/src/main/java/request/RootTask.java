package request;


import main.AsyncHandler;
import response.transformer.Transformer;
import response.transformer.TransformerFactory;
import response.transformer.TransformerType;
import response.viewmodels.ViewModel;

public class RootTask {
    Task task;
    TransformerType transformerType;
    TransformerFactory transformerFactory;
    Class<? extends ViewModel> viewModelType;

    public RootTask(TransformerType transformerType, TransformerFactory transformerFactory, Task task, Class<? extends ViewModel> resonseType) {
        this.transformerType = transformerType;
        this.transformerFactory = transformerFactory;
        this.task = task;
        this.viewModelType = resonseType;
    }

    public void execute(AsyncHandler<ViewModel> aysncHandler) {
        this.task.execute((obj, status) -> {
            Transformer transformer = RootTask.this.transformerFactory.getInstance(RootTask.this.transformerType);
            ViewModel output = (ViewModel) transformer.Transform(RootTask.this, RootTask.this.viewModelType);
            aysncHandler.onCompleted(output, status);
        });
    }

    public void preProcess(){
        this.task.preProcess(0);
    }

    public <T> T getTaskResponse(String taskName, Class<T> valueType){
        return getTaskResponse(taskName, this.task, valueType);
    }

    private <T> T getTaskResponse(String taskName, Task task, Class<T> valueType){
        if(task.getName().equalsIgnoreCase(taskName)){
            return task.getOutput(valueType);
        }

        if(task.getChildren() != null){
            for(Task subTask : task.getChildren()){
                T retval = getTaskResponse(taskName, subTask, valueType);
                if(retval != null){
                    return retval;
                }
            }
        }

        return null;
    }
}
