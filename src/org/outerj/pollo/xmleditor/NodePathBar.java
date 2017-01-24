package org.outerj.pollo.xmleditor;

import org.outerj.pollo.xmleditor.attreditor.AttributesPanel;
import org.outerj.pollo.xmleditor.attreditor.AttributesTableModel;
import org.outerj.pollo.xmleditor.view.View;
import org.w3c.dom.Node;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 * A JLabel that displays the XPath-path to the currently selected node.
 * To achieve this goal, it registers itself as selection event listener
 * on the XmlEditor and on the JTable containing the attributes.
 *
 * @author Bruno Dumon
 */
public class NodePathBar extends JLabel implements SelectionListener, ListSelectionListener
{
    protected JTable attributesTable;
    protected AttributesTableModel attrTableModel;
    protected XmlEditor xmlEditor;

    public NodePathBar(XmlEditor xmlEditor, AttributesPanel attrPanel)
    {
        super("Welcome to Pollo!");

        xmlEditor.getSelectionInfo().addListener(this);
        attrPanel.getAttributesTable().getSelectionModel().addListSelectionListener(this);

        attributesTable = attrPanel.getAttributesTable();
        attrTableModel = attrPanel.getAttributesTableModel();
        this.xmlEditor = xmlEditor;
    }

    public void nodeUnselected(Node node)
    {
        setText("No node selected.");
    }

    public void nodeSelected(Node node)
    {
        setText(getPath());
    }

    public String getPath()
    {
        StringBuffer path = new StringBuffer(50);
        View view = xmlEditor.getSelectionInfo().getSelectedNodeView();

        while (view != null)
        {
            String label = view.getLabel();
            if (label != null)
            {
                path.insert(0, label);
                path.insert(0, '/');
            }
            view = view.getParent();
        }
        return path.toString();
    }

    public void valueChanged(ListSelectionEvent event)
    {
        int row = attributesTable.getSelectedRow();
        if (row != -1)
        {
            String path = getPath();
            path += "/@" + attrTableModel.getValueAt(row, 0);
            setText(path);
        }
    }
}
