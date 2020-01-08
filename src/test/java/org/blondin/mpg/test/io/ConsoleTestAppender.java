package org.blondin.mpg.test.io;

import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Core;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;

/**
 * Log4j2 ConsoleAppender in charge to ensure log test binding
 */
@Plugin(name = "ConsoleTest", category = Core.CATEGORY_NAME, elementType = Appender.ELEMENT_TYPE)
public class ConsoleTestAppender extends AbstractAppender {

    private static StringBuilder logs = new StringBuilder();

    protected ConsoleTestAppender(String name, Filter filter) {
        super(name, filter, null, true, Property.EMPTY_ARRAY);
    }

    @PluginFactory
    public static ConsoleTestAppender createAppender(@PluginAttribute("name") String name, @PluginElement("Filter") Filter filter) {
        return new ConsoleTestAppender(name, filter);
    }

    /**
     * reset internal logs
     */
    public static void logTestReset() {
        logs = new StringBuilder();
    }

    /**
     * return logs
     * 
     * @return logs
     */
    public static String getLogOut() {
        return logs.toString();
    }

    @Override
    public void append(LogEvent event) {
        logs.append(event.getMessage().getFormattedMessage());
        logs.append(System.getProperty("line.separator"));
    }
}
