package request;

import client.Client;
import client.ClientFactory;
import client.ClientType;
import main.AsyncHandler;
import main.CompletableComponents;
import main.Main;
import main.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import response.dto.DtoBase;
import response.transformer.Transformer;
import response.transformer.TransformerFactory;
import response.transformer.TransformerType;
import response.viewmodels.UserViewModel;

import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * This class is inherently an activity that has to completed.
 * The current task will be executed only when all the parents have completed their requests.
 * There are actually two definitions of task completion:
 * 1. Partial Completion: The first one is when all the parents tasks of the current task are complete and current task is also complete but pending completion of children.
 * 2. Final Completion: The second one is when all the parents tasks, current task and all the children are complete.
 * The current task can depend on the parents in two ways.
 * 1. Wait for Partial completion of all the parents. AND OPERATION
 * 2. Wait for Partial completion of one of the parents. OR OPERATION
 */
public class Task {

    private AsyncHandler asyncHandler;
    private AsyncRequestHandler asyncRequestHandler;
    private boolean cancel;
    private boolean finishedExceptionally;
    private boolean isFinalComplete;
    private boolean isPartialComplete;
    private boolean waitForAllParents;
    private Class<? extends DtoBase> valueType;
    private Client client;
    private ClientFactory clientFactory;
    private ClientType clientType;
    private DtoBase dto;
    private Duration timeOut;
    private int pendingChildrenCount = 0;
    private int pendingParentCount = 0;
    private List<Task> children;
    private List<Task> parents;
    private long endTime;
    private long startTime;
    private Status requestStatus;
    private String name;
    private String requestUrl;
    private String response;
    private TransformerFactory transformerFactory;
    private TransformerType transformerType;
    private UUID uuid;

    private static final Logger LOGGER = LoggerFactory.getLogger(Task.class);

    public Task(ClientFactory clientFactory,
                ClientType clientType,
                TransformerFactory transformerFactory,
                TransformerType transformerType,
                Class<? extends DtoBase> responseType,
                AsyncRequestHandler asyncRequestHandler) {
        this.init(clientFactory, clientType, transformerFactory, transformerType, responseType, asyncRequestHandler);
    }

    public Task(ClientFactory clientFactory,
                ClientType clientType,
                TransformerFactory transformerFactory,
                TransformerType transformerType,
                Class<? extends DtoBase> responseType) {
        AsyncRequestHandler aysncRequestHandler = new AsyncRequestHandler() {
        };
        this.init(clientFactory, clientType, transformerFactory, transformerType, responseType, aysncRequestHandler);
    }

    /**
     * @param clientFactory
     * @param transformerFactory
     * @param asyncRequestHandler
     */
    private void init(ClientFactory clientFactory,
                      ClientType clientType,
                      TransformerFactory transformerFactory,
                      TransformerType transformerType,
                      Class<? extends DtoBase> responseType,
                      AsyncRequestHandler asyncRequestHandler) {
        this.uuid = UUID.randomUUID();
        this.cancel = false;
        this.finishedExceptionally = false;
        this.clientFactory = clientFactory;
        this.clientType = clientType;
        this.transformerFactory = transformerFactory;
        this.transformerType = transformerType;
        this.valueType = responseType;
        this.asyncRequestHandler = asyncRequestHandler;
        this.clientType = clientType;
        this.client = this.clientFactory.getInstance(this.clientType);
    }

    /**
     * @param level
     */
    public void preProcess(int level) {
        if (this.parents == null || this.parents.isEmpty()) {
            this.pendingParentCount = 0;
        } else {
            if (this.waitForAllParents) {
                this.pendingParentCount = this.parents.size();
            } else {
                this.pendingParentCount = 1;
            }
        }

        if (this.children == null || this.children.isEmpty()) {
            this.pendingChildrenCount = 0;
        } else {
            this.pendingChildrenCount = this.children.size();
        }

        if (this.children != null) {
            for (Task task : this.children) {
                task.preProcess(level + 1);
            }
        }

        LOGGER.info("Level: {} | Name: {} | Pending children count: {} | Pending parent count: {}",
                level, this.name, this.pendingChildrenCount, this.pendingParentCount);
    }

    public void cancelTree() {
        this.cancel();
        for (Task childTask : this.children) {
            childTask.cancelTree();
        }
    }

    public void cancel() {
        this.cancel = true;
        this.client.cancel();
        this.asyncRequestHandler.onCancel();
    }

    public boolean isRootNode() {
        return this.parents == null || this.parents.isEmpty();
    }

    public <T> T getOutput(Class<T> valueType) {
        if (this.valueType == valueType) {
            return (T) this.dto;
        }

        return null;
    }

    public void execute(AsyncHandler asyncHandler) {
        this.asyncHandler = asyncHandler;
        this.execute();
    }

    /**
     * This method is executed by all the parents of current task.
     */
    private void execute() {
        synchronized (this) {
            // If the task is cancelled we do not need to trigger the children.
            // Just callback to the parent.
            if (this.cancel) {
                this.onPartialCompletion(false);
            }

            // There are no parent nodes present for the root node.
            // Therefore, we should not decrement.
            if (this.isRootNode() == false) {
                this.pendingParentCount--;
            }

            this.startTime = System.nanoTime();
            LOGGER.info("Triggered: {}", this.name);

            // This means all the parents of the current task are complete and we are ready to
            // execute the current task.
            if (this.pendingParentCount == 0) {
                CompletableComponents.within(CompletableFuture.runAsync(() -> {
                    // Trigger sync/async request for the current task.
                    this.client.sendRequestAsync(this.requestUrl, new AsyncHandlerTask());
                }), this.timeOut)
                        .exceptionally((Throwable e) -> {
                            e.printStackTrace();
                            try {
                                Task.this.onPartialCompletion(true);
                            } catch (Exception e1) {
                                e1.printStackTrace();
                            }
                            return null;
                        });
            }
        }
    }

    private void onPartialCompletion(boolean finishedExceptionally) {
        synchronized (this) {
            this.endTime = System.nanoTime();
            if (this.finishedExceptionally) {
                this.asyncRequestHandler.onError();
                if (this.parents != null) {
                    for (Task parentTask : this.parents) {
                        parentTask.onFinalCompletion();
                    }
                }
                return;
            } else {
                this.asyncRequestHandler.onSuccess();
            }

            Transformer transformer = this.transformerFactory.getInstance(this.transformerType);
            this.dto = (DtoBase) transformer.Transform(this.response, this.valueType);
            this.asyncRequestHandler.onCompleted();

            if (this.pendingParentCount == 0) {
                LOGGER.info("Task Partially Finished: {} | Total Time: {} Millis", this.name, (this.endTime - this.startTime) / 1000000);
                LOGGER.info("Time Remaining: {} Millis", this.timeOut.minusNanos(this.endTime - this.startTime).toMillis());
                this.isPartialComplete = true;

                // When the current task has been cancelled it should not execute its children.
                // Just update the parents that it is complete.
                if (this.children != null || (this.children != null && this.cancel == false)) {
                    for (Task child : this.children) {
                        child.setTimeOut(this.timeOut.minusNanos(this.endTime - this.startTime));
                        child.asyncRequestHandler.onBeforeSend();
                        child.execute();
                    }
                } else {
                    this.isFinalComplete = true;
                    if (this.parents != null) {
                        for (Task parentTask : this.parents) {
                            parentTask.onFinalCompletion();
                        }
                    }
                }
            }
        }
    }

    private void onFinalCompletion() {
        synchronized (this) {
            this.pendingChildrenCount--;

            if (this.pendingChildrenCount == 0) {
                LOGGER.info("Task Final Completion: {}", this.name);
                this.isFinalComplete = true;
            }

            if (this.isRootNode() && this.pendingChildrenCount == 0) {
                LOGGER.info("God Complete");
                this.asyncRequestHandler.onCompleted();
                this.asyncHandler.onCompleted(new UserViewModel(), Status.SUCESS);
                LOGGER.info("Total time taken: {} Milli Seconds.", (this.endTime - this.startTime) / 1000000);
                Main.end = true;
                return;
            } else if (this.pendingChildrenCount == 0) {
                LOGGER.info("Level Complete");
                // Update all the parents so that they can update their parents in return.
                for (Task parent : this.parents) {
                    parent.onFinalCompletion();
                }
            }

        }
    }


    public List<Task> getParents() {
        return parents;
    }

    public void setParents(List<Task> parents) {
        this.parents = parents;
    }

    public List<Task> getChildren() {
        return children;
    }

    public void setChildren(List<Task> children) {
        this.children = children;
    }

    public String getRequestUrl() {
        return requestUrl;
    }

    public void setRequestUrl(String requestUrl) {
        this.requestUrl = requestUrl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public boolean isWaitForAllParents() {
        return waitForAllParents;
    }

    public void setWaitForAllParents(boolean waitForAllParents) {
        this.waitForAllParents = waitForAllParents;
    }

    public int getPendingParentCount() {
        return pendingParentCount;
    }

    public int getPendingChildrenCount() {
        return pendingChildrenCount;
    }

    public UUID getUuid() {
        return uuid;
    }

    public boolean isFinishedExceptionally() {
        return finishedExceptionally;
    }

    public ClientType getClientType() {
        return clientType;
    }

    public void setClientType(ClientType clientType) {
        this.clientType = clientType;
    }

    public Duration getTimeOut() {
        return timeOut;
    }

    public void setTimeOut(Duration timeOut) {
        this.timeOut = timeOut;
    }

    public void setTransformerType(TransformerType transformerType) {
        this.transformerType = transformerType;
    }

    public void setResponseType(Class<? extends DtoBase> valueType) {
        this.valueType = valueType;
    }

    private class AsyncHandlerTask implements AsyncHandler<String> {

        /**
         * @param response
         * @param status
         * @return
         * @throws Exception
         */
        @Override
        public void onCompleted(String response, Status status) {
            Task.this.response = response;
            Task.this.requestStatus = requestStatus;
            Task.this.onPartialCompletion(false);
        }
    }


}
