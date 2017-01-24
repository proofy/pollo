package org.outerj.pollo.xmleditor.attreditor;

import org.outerj.pollo.DomConnected;
import org.outerj.pollo.gui.FocusHighlightComponent;
import org.outerj.pollo.gui.SomeLinesBorder;
import org.outerj.pollo.gui.SmallButton;
import org.outerj.pollo.xmleditor.Disposable;
import org.outerj.pollo.xmleditor.SelectionListener;
import org.outerj.pollo.xmleditor.IconManager;
import org.outerj.pollo.xmleditor.displayspec.IDisplaySpecification;
import org.outerj.pollo.xmleditor.displayspec.ElementSpec;
import org.outerj.pollo.xmleditor.model.XmlModel;
import org.outerj.pollo.xmleditor.plugin.IAttributeEditorPlugin;
import org.outerj.pollo.xmleditor.schema.ISchema;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

/**
 * A Panel containing a JTable to edit attribute values, together with
 * buttons to add/delete attributes.
 * <p>
 * The JTable uses an {@link org.outerj.pollo.xmleditor.attreditor.AttributesTableModel AttributesTableModel}
 * to show the attributes.
 *
 * @author Bruno Dumon
 */
public class AttributesPanel extends JPanel implements ActionListener, SelectionListener, DomConnected
{
    protected JTable attributesTable;
    protected AttributesTableModel attributesTableModel;
    protected AttributeTableCellRenderer attributeTableCellRenderer = new AttributeTableCellRenderer();
    protected JButton addAttrButton;
    protected JButton deleteAttrButton;
    protected XmlModel xmlModel;
    protected ISchema schema;
    protected IAttributeEditorPlugin attrEditorPlugin;
    protected JComponent parentFocusComponent;
    protected IDisplaySpecification displaySpecification;

    /**
     * @param parentFocusComponent a component to which to move the focus after pressing escape. Can be null.
     */
    public AttributesPanel(XmlModel xmlModel, ISchema schema, IAttributeEditorPlugin attrEditorPlugin,
                           JComponent parentFocusComponent, IDisplaySpecification displaySpecification)
    {
        this.xmlModel = xmlModel;
        this.schema = schema;
        this.attrEditorPlugin = attrEditorPlugin;
        this.parentFocusComponent = parentFocusComponent;
        this.displaySpecification = displaySpecification;

        // construct the interface components
        attributesTableModel = new AttributesTableModel(schema, xmlModel);
        attributesTable = new AttributesTable(attributesTableModel, schema);
        attributesTable.setBorder(BorderFactory.createEmptyBorder());
        attributesTable.addFocusListener(new FocusHighlightComponent(attributesTable.getTableHeader()));

        attributesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        TableColumn column = null;
        column = attributesTable.getColumnModel().getColumn(0);
        column.setPreferredWidth(40);
        column = attributesTable.getColumnModel().getColumn(1);
        column.setPreferredWidth(300);

        JScrollPane scrollPane = new JScrollPane(attributesTable);
        scrollPane.setBorder(new SomeLinesBorder(false, false, false, true));

        setLayout(new BorderLayout());
        add(scrollPane, BorderLayout.CENTER);

        addAttrButton = new SmallButton(IconManager.getIcon("org/outerj/pollo/xmleditor/attreditor/add_attribute.gif"));
        addAttrButton.setActionCommand("add");
        addAttrButton.addActionListener(this);
        addAttrButton.setToolTipText("Add attribute...");

        deleteAttrButton = new SmallButton(IconManager.getIcon("org/outerj/pollo/xmleditor/attreditor/remove_attribute.gif"));
        deleteAttrButton.setActionCommand("delete");
        deleteAttrButton.addActionListener(this);
        deleteAttrButton.setToolTipText("Remove attribute");

        Box buttonsBox = new Box(BoxLayout.Y_AXIS);
        buttonsBox.add(addAttrButton);
        buttonsBox.add(deleteAttrButton);
        add(buttonsBox, BorderLayout.EAST);

        setEnabled(false);
    }

    public void actionPerformed(ActionEvent e)
    {
        TableCellEditor cellEditor = attributesTable.getCellEditor();
        if (cellEditor != null)
        {
            cellEditor.stopCellEditing();
        }

        if (e.getActionCommand().equals("add"))
        {
            String newAttrName = JOptionPane.showInputDialog(getTopLevelAncestor(),
                    "Please enter the (optionally qualified) attribute name:", "New attribute",
                    JOptionPane.QUESTION_MESSAGE);

            if (newAttrName == null || newAttrName.trim().equals(""))
            {
                return;
            }
            if (!org.apache.xerces.dom.DocumentImpl.isXMLName(newAttrName))
            {
                JOptionPane.showMessageDialog(getTopLevelAncestor(),
                    "That is not a valid XML attribute name.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            int prefixpos = newAttrName.indexOf(":");
            String prefix = null;
            String localName = null;
            String ns = null;
            if (prefixpos != -1)
            {
                prefix = newAttrName.substring(0, prefixpos);
                localName = newAttrName.substring(prefixpos + 1, newAttrName.length());
                ns = xmlModel.findNamespaceForPrefix(attributesTableModel.getElement(), prefix);
                if (ns == null && !prefix.equals("xmlns"))
                {
                    JOptionPane.showMessageDialog(getTopLevelAncestor(),
                            "No namespace declaration found for namespace prefix " + prefix
                            + ". Attribute will not be added.",
                            "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
            else
            {
                // oooops... namespace defaulting does not apply to attributes !!
                // ns = xmlModel.findDefaultNamespace(attributesTableModel.getElement());
                localName = newAttrName;
            }

            attributesTableModel.addAttribute(ns, prefix, localName);
        }
        else if (e.getActionCommand().equals("delete"))
        {
            int row = attributesTable.getSelectedRow();
            if (row == -1)
            {
                System.out.println("no attribute selected");
            }
            else
            {
                attributesTableModel.deleteAttribute(row);
            }
        }
    }

    /**
     * Implementation of the SelectionListener interface.
     */
    public void nodeSelected(Node node)
    {
        TableCellEditor cellEditor = attributesTable.getCellEditor();
        if (cellEditor != null)
        {
            cellEditor.stopCellEditing();
        }

        if (node instanceof Element)
        {
            ElementSpec elementSpec = displaySpecification.getElementSpec((Element)node);
            attributesTableModel.setElement((Element)node, elementSpec);
            setEnabled(true);
        }
        else
        {
            // if it's not an element node
            attributesTableModel.setElement(null, null);
            setEnabled(false);
        }

        attributesTable.clearSelection();
    }

    /**
     * Implementation of the SelectionListener interface.
     */
    public void nodeUnselected(Node node)
    {
        setEnabled(false);
        attributesTableModel.setElement(null, null);
        attributesTable.clearSelection();
    }


    /**
     * Extension of JTable. Purpose is to be able to provide other cell editors.
     */
    public class AttributesTable extends JTable
    {
        protected ISchema schema;

        public AttributesTable(AttributesTableModel model, ISchema schema)
        {
            super(model);
            this.schema = schema;

            // the following will automatically transfer the focus to the
            // cell editor when one starts typing. It also helps to solve
            // another issue I encountered: when tapping space (while in the
            // cell editor), the 'toggle expand' action was also triggered
            // (because it is bound to Space via the menu)
            //setSurrendersFocusOnKeystroke(true);
            // Ooops.. this method is not available in java 1.3, therefore overided
            //  processKeyBinding instead (see below)

            // move focus back to the main editor widget when escape is pressed. Normally this would not
            // be needed here, but apparently JTable does not propagate events further.
            getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "movefocustomain");
            getActionMap().put("movefocustomain", new AbstractAction() {
                public void actionPerformed(ActionEvent e)
                {
                    if (parentFocusComponent !=  null)
                        parentFocusComponent.requestFocus();
                }
            });
        }

        
        public TableCellEditor getCellEditor(int row, int column)
        {
            // if the schema has a list of values for this attribute, show that,
            // or otherwise show the default cell editor.
            AttributesTableModel model = (AttributesTableModel)getModel();
            AttributesTableModel.TempAttrEditInfo taei = model.getTempAttrEditInfo(row);
            TableCellEditor editor = attrEditorPlugin.getAttributeEditor(model.getElement(), taei.uri, taei.name);
            if (editor != null)
                return editor;
            else
                return super.getCellEditor(row, column);
        }

        public TableCellRenderer getCellRenderer(int row, int column)
        {
            return attributeTableCellRenderer;
        }

        public void changeSelection(int rowIndex, int columnIndex, boolean toggle, boolean extend)
        {
            // Only cells in the second column should be selectable
            super.changeSelection(rowIndex, 1, toggle, extend);
        }

        protected boolean processKeyBinding(KeyStroke ks, KeyEvent e,
                        int condition, boolean pressed)
        {
            boolean retValue = super.processKeyBinding(ks, e, condition, pressed);
            if (retValue == true)
            {
                Component editorComponent = getEditorComponent();
                if (editorComponent != null)
                        editorComponent.requestFocus();
            }
            return retValue;
        }

    }


    public void setEnabled(boolean enabled)
    {
        super.setEnabled(enabled);

        attributesTable.setEnabled(enabled);
        addAttrButton.setEnabled(enabled);
        deleteAttrButton.setEnabled(enabled);
    }

    public void requestFocus()
    {
        attributesTable.requestFocus();
    }

    public boolean highlightAttribute(String namespaceURI, String localName)
    {
        int maxAttrs = attributesTableModel.getRowCount();
        for (int i = 0; i < maxAttrs; i++)
        {
            AttributesTableModel.TempAttrEditInfo taei = attributesTableModel.getTempAttrEditInfo(i);
            if (((taei.uri == null && namespaceURI == null)
                        || (namespaceURI != null && namespaceURI.equals(taei.uri)))
                    && taei.name.equals(localName))
            {
                attributesTable.changeSelection(i, 0, false, false);
                return true;
            }
        }
        return false;
    }

    public JTable getAttributesTable()
    {
        return attributesTable;
    }

    public AttributesTableModel getAttributesTableModel()
    {
        return attributesTableModel;
    }

    public void disconnectFromDom()
    {
        attributesTableModel.disconnectFromDom();
    }

    public void reconnectToDom()
    {
        attributesTableModel.reconnectToDom();
    }

    public void dispose()
    {
        if (attrEditorPlugin instanceof Disposable)
            ((Disposable)attrEditorPlugin).dispose();
    }
}
