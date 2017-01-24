package org.outerj.pollo.xmleditor.exception;

/**
 * Base class for pollo configuration exceptions.
 *
 * @author Bruno Dumon.
 */
public class PolloConfigurationException extends PolloException
{
    public PolloConfigurationException(String message)
    {
        super(message);
    }

    public PolloConfigurationException(String message, Exception nestedException)
    {
        super(message, nestedException);
    }
}
