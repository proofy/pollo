package org.outerj.pollo.engine.cocoon;

import org.outerj.pollo.xmleditor.schema.ISchema;
import org.outerj.pollo.xmleditor.model.XmlModel;
import org.outerj.pollo.xmleditor.exception.PolloException;
import org.outerj.pollo.xmleditor.plugin.IAttributeEditorPluginFactory;
import org.outerj.pollo.xmleditor.plugin.IAttributeEditorPlugin;
import org.outerj.pollo.PolloFrame;

import java.util.HashMap;

public class CocoonAttrEditorPluginFactory implements IAttributeEditorPluginFactory
{
    public IAttributeEditorPlugin getInstance(HashMap initParams, XmlModel xmlModel, ISchema schema, PolloFrame polloFrame)
        throws PolloException
    {
        CocoonAttrEditorPlugin plugin = new CocoonAttrEditorPlugin();
        plugin.init(initParams, xmlModel, schema, polloFrame);
        return plugin;
    }
}
