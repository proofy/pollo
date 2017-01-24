package org.outerj.pollo.xmleditor.schema;

import org.outerj.pollo.xmleditor.exception.PolloException;

import java.util.HashMap;

/**
 * Factory for MsvSchema's.
 *
 * @author Bruno Dumon
 */
public class MsvSchemaFactory implements ISchemaFactory
{

    public ISchema getSchema(HashMap initParams)
        throws PolloException
    {
        MsvSchema newSchema = new MsvSchema();

        try
        {
            newSchema.init(initParams);
        }
        catch (Exception e)
        {
            throw new PolloException("[MsvSchema] MsvSchema could not be created. ", e);
        }
        return newSchema;
    }
}
