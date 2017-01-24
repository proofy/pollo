package org.outerj.pollo.xmleditor;

/**
 * This interface defines the constants to be used as appender names for the
 * log4j logging system.
 *
 * @author Bruno Dumon
 */
public interface AppenderDefinitions
{
    /** appender for all info related to (user) configuration */
    public static final String CONFIG = "config";
    public static final String MAIN = "main";
    public static final String GUI = "gui";
}
