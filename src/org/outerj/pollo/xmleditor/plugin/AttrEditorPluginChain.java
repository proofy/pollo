package org.outerj.pollo.xmleditor.plugin;

import org.w3c.dom.Element;
import org.outerj.pollo.xmleditor.Disposable;

import javax.swing.table.TableCellEditor;
import java.util.ArrayList;

public class AttrEditorPluginChain implements IAttributeEditorPlugin, Disposable
{
    protected ArrayList plugins = new ArrayList();

    public void add(IAttributeEditorPlugin plugin)
    {
        plugins.add(plugin);
    }

    public TableCellEditor getAttributeEditor(Element element,
            String namespaceURI, String localName)
    {
        for (int i = 0; i < plugins.size(); i++)
        {
            TableCellEditor editor = ((IAttributeEditorPlugin)plugins.get(i))
                .getAttributeEditor(element, namespaceURI, localName);
            if (editor != null)
                return editor;
        }
        return null;
    }

    public void dispose()
    {
        for (int i = 0; i < plugins.size(); i++)
        {
            Object plugin = plugins.get(i);
            if (plugin instanceof Disposable)
            {
                ((Disposable)plugin).dispose();
            }
        }
    }
}
