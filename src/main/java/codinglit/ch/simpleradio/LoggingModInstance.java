package codinglit.ch.simpleradio;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** This class provides wrapper functions to allow substitution and ease-of-access for logging functions.
 */
public class LoggingModInstance {
    public static String ID;
    private static Logger LOGGER;

    public LoggingModInstance() {
        LOGGER = LogManager.getLogger(this);
    }

    public static void info(Object object, Object... substitutions) {
        LOGGER.info(String.format(String.valueOf(object), substitutions));
    }

    public static void warn(Object object, Object... substitutions) {
        LOGGER.warn(String.format(String.valueOf(object), substitutions));
    }
}
