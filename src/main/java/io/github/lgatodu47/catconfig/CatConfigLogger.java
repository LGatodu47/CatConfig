package io.github.lgatodu47.catconfig;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.message.FormattedMessage;

/**
 * Interface that defines methods that the config will use to log messages.
 * You can make your own implementations or use the ones that are available
 * with the static methods.
 *
 * @since 0.2
 */
public interface CatConfigLogger {
    /**
     * Logs a message with the INFO level.
     * @param msg The message to log.
     * @param args The formatting arguments.
     */
    void info(String msg, Object... args);

    /**
     * Logs a message with the DEBUG level.
     * @param msg The message to log.
     * @param args The formatting arguments.
     */
    void debug(String msg, Object... args);

    /**
     * Logs a message with the WARN level.
     * @param msg The message to log.
     * @param args The formatting arguments.
     */
    void warn(String msg, Object... args);

    /**
     * Logs a message with the ERROR level.
     * @param msg The message to log.
     * @param args The formatting arguments.
     */
    void error(String msg, Object... args);

    /**
     * Logs a message with the INFO level.
     * @param msg The message to log.
     * @param t The throwable describing the error.
     * @param args The formatting arguments.
     */
    void error(String msg, Throwable t, Object... args);

    /**
     * Delegates the method calls of the interface to {@link Logger Log4J's Logger} implementation.
     * @param logger The Log4J Logger.
     * @return A new instance of CatConfigLogger.
     */
    static CatConfigLogger delegate(Logger logger) {
        return new CatConfigLogger() {
            @Override
            public void info(String msg, Object... args) {
                logger.info(msg, args);
            }

            @Override
            public void debug(String msg, Object... args) {
                logger.debug(msg, args);
            }

            @Override
            public void warn(String msg, Object... args) {
                logger.warn(msg, args);
            }

            @Override
            public void error(String msg, Object... args) {
                logger.error(msg, args);
            }

            @Override
            public void error(String msg, Throwable t, Object... args) {
                logger.error(new FormattedMessage(msg, args), t);
            }
        };
    }

    /**
     * Creates a default logger implementation named after the specified name.
     * @param name The name of the logger to create.
     * @return A named new instance of {@link Default the default implementation of CatConfigLogger}.
     */
    static CatConfigLogger named(String name) {
        return create("[" + name + "/${LEVEL}]: ");
    }

    /**
     * Creates a default logger implementation with the specified prefix.
     * @param prefix The prefix of the logger (what will be shown before the message).
     * @return A prefixed new instance of {@link Default the default implementation of CatConfigLogger}.
     */
    static CatConfigLogger create(String prefix) {
        return new Default(prefix);
    }

    /**
     * Default implementation of CatConfigLogger using default System PrintStreams.
     */
    final class Default implements CatConfigLogger {
        private final String prefix;

        private Default(String prefix) {
            this.prefix = prefix;
        }

        @Override
        public void info(String msg, Object... args) {
            log("INFO", msg, args);
        }

        @Override
        public void debug(String msg, Object... args) {
            log("DEBUG", msg, args);
        }

        @Override
        public void warn(String msg, Object... args) {
            log("WARN", msg, args);
        }

        @Override
        public void error(String msg, Object... args) {
            System.err.println(withLevel(prefix, "ERROR").concat(format(msg, args)));
        }

        @Override
        public void error(String msg, Throwable t, Object... args) {
            error(msg, args);
            t.printStackTrace();
        }

        private void log(String level, String msg, Object... args) {
            System.out.println(withLevel(prefix, level).concat(format(msg, args)));
        }

        private static String format(String str, Object... args) {
            if(args.length == 0) {
                return str;
            }

            StringBuilder sb = new StringBuilder();
            char[] chars = str.toCharArray();
            int argsIndex = 0;
            for (int i = 0; i < chars.length; i++) {
                char c = chars[i];
                if(argsIndex < args.length) {
                    if(c == '{' && (i != chars.length - 1 && chars[i + 1] == '}')) {
                        sb.append(args[argsIndex++]);
                        i++;
                        continue;
                    }
                }
                sb.append(c);
            }

            return sb.toString();
        }

        private static String withLevel(String str, String level) {
            return str.replace("${LEVEL}", level);
        }
    }
}
