package snowflakes.cuda.kask.eti.pg.gda.pl.commons;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Kuba on 2015-06-07.
 */
public class TimeLogger {

    public static final String SEPARATOR = " ";
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    private final String name;

    private TimeLogger(String name) {
        this.name = name;
    }

    public void log(String message) {
        Date timestamp = new Date();
        StringBuilder sb = new StringBuilder();
        sb.append(name)
                .append(SEPARATOR)
                .append(sdf.format(timestamp))
                .append(SEPARATOR)
                .append(timestamp.getTime())
                .append(SEPARATOR)
                .append(message);
        System.out.println(sb.toString());
    }

    public static TimeLogger getTimeLogger(String name) {
        return new TimeLogger(name);
    }
}
