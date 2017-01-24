package org.outerj.pollo.xmleditor.plugin;

import org.outerj.pollo.xmleditor.model.XmlModel;
import org.outerj.pollo.xmleditor.schema.ISchema;
import org.w3c.dom.Element;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import java.util.HashMap;

/**
 * Default implementation of the AttributeEditorPlugin abstract class.
 *
 * @author Bruno Dumon
 */
public class DefaultAttributeEditorPlugin implements IAttributeEditorPlugin
{
    protected XmlModel xmlModel;
    protected ISchema schema;
    protected AttributeEditorSupport editorSupport;

    public void init(HashMap initParams, XmlModel xmlModel, ISchema schema)
    {
        this.xmlModel = xmlModel;
        this.schema = schema;
        this.editorSupport = new AttributeEditorSupport(schema);
    }

    public TableCellEditor getAttributeEditor(Element element, String namespaceURI, String localName)
    {
        editorSupport.reset(element, namespaceURI, localName);
        return editorSupport.getEditor();
    }
}
