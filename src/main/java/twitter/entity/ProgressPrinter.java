package twitter.entity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

public class ProgressPrinter {
    private final int MAX_PERCENT = 100;
    private final String name;
    private long current = 0;
    private long currentTime;
    private final long startTime;
    private final int percent;
    private final long total;
    private int lastPercent = 0;
    private String lastMessage;

    public ProgressPrinter(String name, long total) {
        this(name, total, 1);
    }
    public ProgressPrinter(String name, long total, int percent) {
        if (percent < 1) {
            System.out.println("Value of 'percent' cannot be less than 1. Set 'percent' to 1");
            percent = 1;
        }
        this.name = name;
        this.total = total;
        this.percent = percent;
        this.startTime = System.nanoTime();
        this.currentTime = startTime;
        this.lastMessage = "Processing '" + name + "'... (" + 0 + "%/100%)[" + current + "/" + total + "]";
    }

    public void printProgress(long nextValue, boolean forced) {
        assert percent > 0;
//        if (forced || nextValue == total || nextValue * MAX_PERCENT / total >= current * MAX_PERCENT / total + percent) {
        if (forced || nextValue == total || nextValue > current) {
            int currentPercent = (int)(nextValue * MAX_PERCENT / total);
            if (forced) {
                currentPercent = MAX_PERCENT;
            }
            long prevTime = currentTime;
            currentTime = System.nanoTime();
            long remaining = Long.MAX_VALUE;
            if (currentPercent == MAX_PERCENT) remaining = 0;
            else {
                if (nextValue > current) {
                    double rate = (double) (currentTime - prevTime) / (nextValue - current);
                    rate = (rate * 49 + (double) (currentTime - startTime) / nextValue) / 50;
                    remaining = (long) (rate * (total - nextValue));
                }
            }

            current = nextValue;
            String message = "Processing '" + name + "'... (" + currentPercent + "%/100%)[" + current + "/" + total + "]";
            if (remaining < Long.MAX_VALUE) {
                message += " - about " + TimePrinter.getConvertedApproximateTime(remaining) + " left";
            }
            lastPercent = currentPercent;
            lastMessage = message;
//            System.out.println(message);
        }
    }

    public boolean update(long nextValue) {
        if (nextValue > total) {
            return false;
        }
        current = nextValue;
        currentTime = System.nanoTime();
//        printProgress(nextValue, false);
        return true;
    }
    public boolean increment(long delta) {
        return update(current + delta);
    }

    public int getMAX_PERCENT() {
        return MAX_PERCENT;
    }

    public String getName() {
        return name;
    }

    public long getCurrent() {
        return current;
    }

    public long getTotal() {
        return total;
    }

    public int getLastPercent() {
        return lastPercent;
    }

    public String getLastMessage() {
        return lastMessage;
    }
}
