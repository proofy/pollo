package org.outerj.pollo.engine.pollo;

import org.outerj.pollo.xmleditor.plugin.IAttributeEditorPluginFactory;
import org.outerj.pollo.xmleditor.plugin.IAttributeEditorPlugin;
import org.outerj.pollo.xmleditor.model.XmlModel;
import org.outerj.pollo.xmleditor.schema.ISchema;
import org.outerj.pollo.xmleditor.exception.PolloException;
import org.outerj.pollo.PolloFrame;

import java.util.HashMap;

public class DisplaySpecAttrEditorPluginFactory implements IAttributeEditorPluginFactory
{
    public IAttributeEditorPlugin getInstance(HashMap initParams, XmlModel xmlModel, ISchema schema, PolloFrame polloFrame)
            throws PolloException
    {
        DisplaySpecAttrEditorPlugin plugin = new DisplaySpecAttrEditorPlugin();
        plugin.init(schema, polloFrame);
        return plugin;
    }
}
