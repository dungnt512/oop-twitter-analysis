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
    public static TaskVoid testTask() {
        return new TaskVoid() {
            @Override
            protected Void call() throws Exception {
                updateMessage("Processing...");
                int step = 100;
                for (int i = 0; i <= step; i++) {
                    updateProgress(i, step);
                    Thread.sleep(25);
                }
                updateMessage("Process complete!");
                return null;
            }
        };
    }
    @Override
    protected Void call() throws Exception {
        return null;
    }
}
