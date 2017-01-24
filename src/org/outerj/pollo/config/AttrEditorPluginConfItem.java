package org.outerj.pollo.config;

import org.outerj.pollo.xmleditor.ComponentManager;
import org.outerj.pollo.xmleditor.exception.PolloException;
import org.outerj.pollo.xmleditor.model.XmlModel;
import org.outerj.pollo.xmleditor.plugin.IAttributeEditorPlugin;
import org.outerj.pollo.xmleditor.plugin.IAttributeEditorPluginFactory;
import org.outerj.pollo.xmleditor.schema.ISchema;
import org.outerj.pollo.PolloFrame;

public class AttrEditorPluginConfItem extends ConfItem
{
    public IAttributeEditorPlugin createPlugin(XmlModel xmlModel, ISchema schema, PolloFrame polloFrame)
        throws PolloException
    {
        IAttributeEditorPlugin plugin = null;
        try
        {
            IAttributeEditorPluginFactory pluginFactory =
                (IAttributeEditorPluginFactory)ComponentManager.getFactoryInstance(getFactoryClass());
            plugin = pluginFactory.getInstance(getInitParams(), xmlModel, schema, polloFrame);
        }
        catch (Exception e)
        {
            throw new PolloException("[AttrEditorPluginConfItem] Error creating plugin.", e);
        }
        return plugin;
    }
}
