package org.outerj.pollo.xmleditor.plugin;

import org.outerj.pollo.xmleditor.exception.PolloException;
import org.outerj.pollo.xmleditor.model.XmlModel;
import org.outerj.pollo.xmleditor.schema.ISchema;
import org.outerj.pollo.PolloFrame;

import java.util.HashMap;

public class DefaultAttributeEditorPluginFactory implements IAttributeEditorPluginFactory
{
    public IAttributeEditorPlugin getInstance(HashMap initParams, XmlModel xmlModel, ISchema schema, PolloFrame polloFrame)
        throws PolloException
    {
        DefaultAttributeEditorPlugin plugin = new DefaultAttributeEditorPlugin();
        plugin.init(initParams, xmlModel, schema);
        return plugin;
    }
}
