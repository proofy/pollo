package org.outerj.pollo.xmleditor;

import org.w3c.dom.Node;
import org.outerj.pollo.xmleditor.attreditor.AttributesPanel;
import org.outerj.pollo.xmleditor.attreditor.AttributesTableModel;

import javax.swing.*;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;
import java.awt.*;

public class HelpPanel extends JPanel
{
    protected JTable attributesTable;
    protected AttributesTableModel attributesTableModel;
    protected XmlEditor xmlEditor;

    public HelpPanel(XmlEditor xmlEditor, AttributesPanel attrPanel)
    {
        attributesTable = attrPanel.getAttributesTable();
        attributesTableModel = attrPanel.getAttributesTableModel();
        this.xmlEditor = xmlEditor;

        setLayout(new BorderLayout());

        HelpArea helpArea = new HelpArea();
        add(helpArea, BorderLayout.CENTER);
    }

    class HelpArea extends JScrollPane implements ListSelectionListener, SelectionListener
    {
        protected JEditorPane editorPane;
        private static final String NO_HELP = "No help available.";

        public HelpArea()
        {
            editorPane = new JEditorPane();
            editorPane.setContentType("text/html");
            editorPane.setEditable(false);
            editorPane.setBorder(BorderFactory.createEmptyBorder());

            setViewportView(editorPane);
            setBorder(BorderFactory.createEmptyBorder());

            attributesTable.getSelectionModel().addListSelectionListener(this);
            xmlEditor.getSelectionInfo().addListener(this);
        }

        public void valueChanged(ListSelectionEvent event)
        {
            int row = attributesTable.getSelectedRow();
            if (row != -1)
                setHelp(attributesTableModel.getHelpText(row));
        }

        public void setHelp(String text)
        {
            if (text != null)
                editorPane.setText(text);
            else
                editorPane.setText(NO_HELP);
            editorPane.setCaretPosition(0);
        }

        public void nodeUnselected(Node node)
        {
            setHelp("No node selected.");
        }

        public void nodeSelected(Node node)
        {
            setHelp(xmlEditor.getSelectionInfo().getSelectedNodeView().getHelp());
        }

        public Dimension getPreferredSize()
        {
            return new Dimension(200, (int)super.getPreferredSize().getHeight());
        }
    }
}
