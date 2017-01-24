package org.outerj.pollo.xmleditor.schema;

import org.outerj.pollo.xmleditor.exception.PolloException;

/**
 * This exception is thrown by an ISchema implementation that
 * doesn't support validation.
 */
public class ValidationNotSupportedException extends PolloException
{

    public ValidationNotSupportedException()
    {
        super("The current schema implementation does not support validation", null);
    }
}
