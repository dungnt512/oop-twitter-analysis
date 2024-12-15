package twitter.entity;

import javafx.concurrent.Task;

public class TaskVoid extends Task<Void> {
    public void updateProgress(double v1, double v2) {
        super.updateProgress(v1, v2);
    }
    public void updateProgress(long v1, long v2) {
        super.updateProgress(v1, v2);
    }
    public void updateMessage(String message) {
        super.updateMessage(message);
    }

    @Override
    protected Void call() throws Exception {
        return null;
    }
}
