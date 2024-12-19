package twitter.entity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class TimePrinter {
    private long nanoSeconds;
    private long days;
    private int hours;
    private int minutes;
    private int seconds;

    final private int NANOSECOND_PER_SECOND = 1000000000;
    final private int SECONDS_PER_MINUTE = 60;
    final private int MINUTES_PER_HOUR = 60;
    final private int HOURS_PER_DAY = 24;

    public TimePrinter(long nanoSecond) {
        this.nanoSeconds = nanoSecond;
        nanoSecond /= NANOSECOND_PER_SECOND;
        seconds = (int) (nanoSecond % SECONDS_PER_MINUTE);
        nanoSecond /= SECONDS_PER_MINUTE;
        minutes = (int) (nanoSecond % MINUTES_PER_HOUR);
        nanoSecond /= MINUTES_PER_HOUR;
        hours = (int) (nanoSecond % HOURS_PER_DAY);
        nanoSecond /= HOURS_PER_DAY;
        days = (int) nanoSecond;
    }

    private long timeToNanoSeconds(long days, long hours, long minutes, long seconds) {
        return ((((days * HOURS_PER_DAY) + hours) * MINUTES_PER_HOUR + minutes) * SECONDS_PER_MINUTE + seconds) * NANOSECOND_PER_SECOND;
    }
    private int booleanToInt(boolean b) {
        return b ? 1 : 0;
    }
    public String getTime() {
        return days + " days " + hours + " hours " + minutes + " minutes " + seconds + " seconds";
    }
    public String getApproximateTime() {
        if (days > 0) {
            return (days + booleanToInt(
                    nanoSeconds - timeToNanoSeconds(days, 0, 0, 0) >
                            timeToNanoSeconds(days + 1, 0, 0, 0) - nanoSeconds)) + " days";
        }
        if (hours > 0) {
            return (hours + booleanToInt(
                    nanoSeconds - timeToNanoSeconds(days, hours, 0, 0) >
                            timeToNanoSeconds(days, hours + 1, 0, 0) - nanoSeconds)) + " hours";
        }
        if (minutes > 0) {
            return (minutes + booleanToInt(
                    nanoSeconds - timeToNanoSeconds(days, hours, minutes, 0) >
                            timeToNanoSeconds(days, hours, minutes + 1, 0) - nanoSeconds)) + " minutes";
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

    public int getHours() {
        return hours;
    }

    public void setHours(int hours) {
        this.hours = hours;
    }
}
