package org.outerj.pollo.xmleditor.schema;

import org.outerj.pollo.xmleditor.exception.PolloException;

import java.util.HashMap;

/**
 * Schema factories should implement this interface.
 *
 * @author Bruno Dumon
 */
public interface ISchemaFactory
{
    public ISchema getSchema(HashMap initParams)
        throws PolloException;
}
