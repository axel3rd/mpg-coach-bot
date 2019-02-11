package org.blondin.mpg.test.io;

import java.io.ByteArrayOutputStream;

import org.apache.log4j.ConsoleAppender;

/**
 * Log4j ConsoleAppender in charge to ensure log test binding
 */
public class ConsoleTestAppender extends ConsoleAppender {

    /** Lout Out execution */
    private static ByteArrayOutputStream logOut = new ByteArrayOutputStream();

    /** Lout Err execution */
    private static ByteArrayOutputStream logErr = new ByteArrayOutputStream();

    /** Binded out logger */
    private static TeePrintStream teeOut = new TeePrintStream(logOut, System.out);

    /** Binded err logger */
    private static TeePrintStream teeErr = new TeePrintStream(logErr, System.err);

    static {
        // Bind out/err
        // Note : With slf4j logger usage, the associated Stream could be initialized once => TeePrintStream is static and associated Stream reseted
        // each method (see setUp)
        System.setOut(teeOut);
        System.setErr(teeErr);
    }

    /**
     * reset internal logs
     */
    public static void logTestReset() {
        logOut.reset();
        logErr.reset();
    }

    /**
     * return output stream
     * 
     * @return output stream
     */
    public static ByteArrayOutputStream getLogOut() {
        // Log wrapper should be flushed to ensure the end usage/content
        teeOut.flush();
        return logOut;
    }

    /**
     * return error stream
     * 
     * @return error stream
     */
    public static ByteArrayOutputStream getLogErr() {
        // Log wrapper should be flushed to ensure the end usage/content
        teeErr.flush();
        return logErr;
    }

    /**
     * verify if sdtout stream is our output stream
     */
    public static void checkLogBinding() {
        if (!(System.out instanceof TeePrintStream)) { 
            throw new UnsupportedOperationException(String.format("Log4j configuration appender is '%s' and not '%s', please review configuration",
                    System.out.getClass().getName(), ConsoleTestAppender.class.getName()));

        }
    }
}
