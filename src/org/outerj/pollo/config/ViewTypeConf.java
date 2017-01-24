package org.outerj.pollo.config;

import org.outerj.pollo.plugin.ActionPluginChain;
import org.outerj.pollo.plugin.IActionPlugin;
import org.outerj.pollo.xmleditor.displayspec.ChainedDisplaySpecification;
import org.outerj.pollo.xmleditor.displayspec.IDisplaySpecification;
import org.outerj.pollo.xmleditor.exception.PolloException;
import org.outerj.pollo.xmleditor.model.XmlModel;
import org.outerj.pollo.xmleditor.plugin.AttrEditorPluginChain;
import org.outerj.pollo.xmleditor.plugin.IAttributeEditorPlugin;
import org.outerj.pollo.xmleditor.plugin.DefaultAttributeEditorPlugin;
import org.outerj.pollo.xmleditor.schema.ChainedSchema;
import org.outerj.pollo.xmleditor.schema.ISchema;
import org.outerj.pollo.PolloFrame;
import org.outerj.pollo.EditorPanel;

import java.util.ArrayList;
import java.util.Iterator;

public class ViewTypeConf
{
    protected String name;
    protected String description;
    protected String className;
    protected ArrayList schemas = new ArrayList();
    protected ArrayList displaySpecs = new ArrayList();
    protected ArrayList attrEditorPlugins = new ArrayList();
    protected ArrayList actionPlugins = new ArrayList();

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public String getClassName()
    {
        return className;
    }

    public void setClassName(String className)
    {
        this.className = className;
    }

    public void addSchema(SchemaConfItem schema)
    {
        schemas.add(schema);
    }

    public void addDisplaySpec(DisplaySpecConfItem displaySpec)
    {
        displaySpecs.add(displaySpec);
    }

    public void addAttrEditorPlugin(AttrEditorPluginConfItem attrEditorPlugin)
    {
        attrEditorPlugins.add(attrEditorPlugin);
    }

    public void addActionPlugin(ActionPluginConfItem actionPlugin)
    {
        actionPlugins.add(actionPlugin);
    }

    public ISchema createSchemaChain()
        throws PolloException
    {
        ChainedSchema schemaChain = new ChainedSchema();

        Iterator it = schemas.iterator();
        while (it.hasNext())
        {
            SchemaConfItem conf = (SchemaConfItem)it.next();
            schemaChain.add(conf.createSchema());
        }

        return schemaChain;
    }

    public IDisplaySpecification createDisplaySpecChain()
        throws PolloException
    {
        ChainedDisplaySpecification displaySpecChain = new ChainedDisplaySpecification();

        Iterator it = displaySpecs.iterator();
        while (it.hasNext())
        {
            DisplaySpecConfItem conf = (DisplaySpecConfItem)it.next();
            displaySpecChain.add(conf.createDisplaySpec());
        }

        return displaySpecChain;
    }

    public IAttributeEditorPlugin createAttrEditorPluginChain(XmlModel xmlModel, ISchema schema, PolloFrame polloFrame)
        throws PolloException
    {
        AttrEditorPluginChain attrEditorPluginChain = new AttrEditorPluginChain();

        if (attrEditorPlugins.size() > 0)
        {
            Iterator it = attrEditorPlugins.iterator();
            while (it.hasNext())
            {
                AttrEditorPluginConfItem conf = (AttrEditorPluginConfItem)it.next();
                attrEditorPluginChain.add(conf.createPlugin(xmlModel, schema, polloFrame));
            }
        }
        else
        {
            DefaultAttributeEditorPlugin defaultPlugin = new DefaultAttributeEditorPlugin();
            defaultPlugin.init(null, xmlModel, schema);
            attrEditorPluginChain.add(defaultPlugin);
        }

        return attrEditorPluginChain;
    }

    public IActionPlugin createActionPlugins(EditorPanel editorPanel, PolloFrame polloFrame)
        throws PolloException
    {
        ActionPluginChain actionPluginChain = new ActionPluginChain();

        Iterator it = actionPlugins.iterator();
        while (it.hasNext())
        {
            ActionPluginConfItem conf = (ActionPluginConfItem)it.next();
            actionPluginChain.add(conf.createActionPlugin(editorPanel, polloFrame));
        }

        return actionPluginChain;
    }

    public String toString()
    {
        return description;
    }
}
