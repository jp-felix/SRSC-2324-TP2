
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class SecureLogFormatter extends Formatter {

    private static final DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Override
    public String format(LogRecord record) {
        StringBuilder builder = new StringBuilder(1000);
        builder.append(df.format(new Date(record.getMillis()))).append(" ");
        builder.append("[").append(record.getLevel()).append("] ");
        builder.append(formatMessage(record)).append("\n");
        return builder.toString();
    }

    @Override
    public String getHead(java.util.logging.Handler h) {
        return super.getHead(h);
    }

    @Override
    public String getTail(java.util.logging.Handler h) {
        return super.getTail(h);
    }
}
