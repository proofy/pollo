package org.outerj.pollo.xmleditor.schema;

import org.outerj.pollo.xmleditor.exception.PolloException;

import java.util.HashMap;

/**
 * This factory returns instances of GenericSchema.
 *
 * @author Bruno Dumon
 */
public class GenericSchemaFactory implements ISchemaFactory
{
    public ISchema getSchema(HashMap initParams)
        throws PolloException
    {
        GenericSchema schema = new GenericSchema();
        schema.init(initParams);
        return schema;
    }
}
