package org.outerj.pollo.engine.ant;

import org.outerj.pollo.xmleditor.schema.ISchema;
import org.outerj.pollo.xmleditor.model.XmlModel;
import org.outerj.pollo.xmleditor.exception.PolloException;
import org.outerj.pollo.xmleditor.plugin.IAttributeEditorPluginFactory;
import org.outerj.pollo.xmleditor.plugin.IAttributeEditorPlugin;
import org.outerj.pollo.PolloFrame;

import java.util.HashMap;

public class AntAttrEditorPluginFactory implements IAttributeEditorPluginFactory
{
    public IAttributeEditorPlugin getInstance(HashMap initParams, XmlModel xmlModel, ISchema schema, PolloFrame polloFrame)
        throws PolloException
    {
        AntAttrEditorPlugin plugin = new AntAttrEditorPlugin();
        plugin.init(initParams, xmlModel, schema, polloFrame);
        return plugin;
    }
}
