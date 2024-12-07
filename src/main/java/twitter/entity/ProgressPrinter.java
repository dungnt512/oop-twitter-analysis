package twitter.entity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ProgressPrinter {
    private final int MAX_PERCENT = 100;
    private final String name;
    private int current = 0;
    private long currentTime = 0;
    private final int percent;
    private final int total;
    public ProgressPrinter(String name, int total) {
        this.name = name;
        this.total = total;
        this.currentTime = System.nanoTime();
        this.percent = Math.max(1, 1000 / total);
    }
    public ProgressPrinter(String name, int total, int percent) {
        if (percent < 1) {
            System.out.println("Value of 'percent' cannot be less than 1. Set 'percent' to 1");
            percent = 1;
        }
        this.name = name;
        this.total = total;
        this.percent = percent;
        this.currentTime = System.nanoTime();
    }

    public void printProgress(int nextValue, boolean forced) {
        assert percent > 0;
        if (forced || nextValue == total || nextValue * MAX_PERCENT / total >= current * MAX_PERCENT / total + percent) {
            long prevTime = currentTime;
            currentTime = System.nanoTime();
            long remaining = (long)((double)(currentTime - prevTime) / (nextValue - current) * (total - nextValue));
            current = nextValue;
            System.out.println("Processing '" + name + "'... (" + (current * MAX_PERCENT / total) + "%/100%)[" + current + "/" + total + "] --- about "
                    + TimePrinter.getConvertedApproximateTime(remaining) + " left");
        }
    }

    public boolean update(int nextValue) {
        if (nextValue > total) {
            return false;
        }
        printProgress(nextValue, false);
        return true;
    }
    public boolean increment(int delta) {
        return update(current + delta);
    }
}
