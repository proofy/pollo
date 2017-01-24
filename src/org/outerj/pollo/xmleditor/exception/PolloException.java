package org.outerj.pollo.xmleditor.exception;

import org.apache.commons.lang.exception.NestableException;

/**
 * Base class for pollo exceptions.
 *
 * @author Bruno Dumon
 */
public class PolloException extends NestableException
{
    public PolloException(String message)
    {
        super(message);
    }

    public PolloException(String message, Exception nestedException)
    {
        super(message, nestedException);
    }
}
