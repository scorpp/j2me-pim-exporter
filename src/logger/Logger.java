package logger;

/**
 * Logger, writes to System.out for now.
 */
public class Logger {

    public static final int ERROR = 0;
    public static final int WARN = 1;
    public static final int INFO = 2;
    public static final int DEBUG = 3;

    private static final String[] LEVELS_STR = {"ERROR", "WARN ", "INFO ", "DEBUG"};

    private Class clazz;

    public Logger(Class clazz) {
        this.clazz = clazz;
    }

    public void info(String msg) {
        log(INFO, msg);
    }

    public void error(String msg) {
        log(ERROR, msg);
    }

    public void warn(String msg) {
        log(WARN, msg);
    }

    public void debug(String msg) {
        log(DEBUG, msg);
    }

    public void log(int level, String msg) {
        StringBuffer sb = new StringBuffer('[').append(LEVELS_STR[level]).append(']').append(' ');
        sb.append(Thread.currentThread().getName()).append(' ');
        sb.append(getSimpleClassName(clazz)).append(' ');
        sb.append(msg);

        System.out.println(sb.toString());
    }

    private static String getSimpleClassName(Class clazz) {
        int lastDot = clazz.getName().lastIndexOf('.');
        if (lastDot == -1) {
            return clazz.getName();
        }

        return clazz.getName().substring(lastDot + 1);
    }
}
