package org.outerj.pollo.config;

import org.outerj.pollo.xmleditor.ComponentManager;
import org.outerj.pollo.xmleditor.exception.PolloException;
import org.outerj.pollo.xmleditor.schema.ISchema;
import org.outerj.pollo.xmleditor.schema.ISchemaFactory;

public class SchemaConfItem extends ConfItem
{
    public ISchema createSchema()
        throws PolloException
    {
        ISchema schema = null;
        try
        {
            ISchemaFactory schemaFactory = (ISchemaFactory)ComponentManager.getFactoryInstance(getFactoryClass());
            schema = schemaFactory.getSchema(getInitParams());
        }
        catch (Exception e)
        {
            throw new PolloException("[SchemaConfItem] Error creating schema.", e);
        }
        return schema;
    }
}
