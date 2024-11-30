package org.example;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class Progress {
    private final String name;
    private int current;
    private final int percent;
    private final int total;
    public Progress(String name, int total) {
        this.name = name;
        this.total = total;
        this.percent = Math.max(1, 1000 / total);
    }
    public Progress(String name, int total, int percent) {
        this.name = name;
        this.total = total;
        this.percent = percent;
    }

    public boolean printProgress(int nextValue) {
        int prevValue = current;
        current = nextValue;
        if (current * 100 / total >= prevValue * 100 / total + percent) {
            System.out.println("Processing '" + name + "'... (" + (current * 100 / total) + "%/100%)[" + current + "/" + total + "]");
            return true;
        }
        return false;
    }

    public boolean update(int nextValue) {
        if (nextValue > total) {
            return false;
        }
        printProgress(nextValue);
        return true;
    }
    public boolean increment(int delta) {
        return update(current + delta);
    }
}
