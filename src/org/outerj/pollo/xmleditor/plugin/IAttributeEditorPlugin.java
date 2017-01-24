package org.outerj.pollo.xmleditor.plugin;

import org.w3c.dom.Element;

import javax.swing.table.TableCellEditor;

public interface IAttributeEditorPlugin
{
    public TableCellEditor getAttributeEditor(Element element,
            String namespaceURI, String localName);
}
