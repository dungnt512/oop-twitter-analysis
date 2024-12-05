package twitter.entity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TimePrinter {
    long days;
    int hours;
    int minutes;
    int seconds;

    public TimePrinter(long nanoSecond) {
        nanoSecond /= 1000000000;
        seconds = (int) nanoSecond % 60;
        nanoSecond /= 60;
        minutes = (int) nanoSecond % 60;
        nanoSecond /= 60;
        hours = (int) nanoSecond % 24;
        nanoSecond /= 24;
        days = (int) nanoSecond;
    }

    public String getTime() {
        return days + " days " + hours + " hours " + minutes + " minutes " + seconds + " seconds";
    }
    public String getApproximateTime() {
        if (days > 0) {
            return days + " days";
        }
        if (hours > 0) {
            return hours + " hours";
        }
        if (minutes > 0) {
            return minutes + " minutes";
        }
        return seconds + " seconds";
    }

    public static String getConvertedTime(long nanoSecond) {
        TimePrinter time = new TimePrinter(nanoSecond);
        return time.getTime();
    }
    public static String getConvertedApproximateTime(long nanoSecond) {
        TimePrinter time = new TimePrinter(nanoSecond);
        return time.getApproximateTime();
    }
}
