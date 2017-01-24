package org.outerj.pollo.xmleditor.plugin;

import org.w3c.dom.Element;
import org.outerj.pollo.util.*;
import org.outerj.pollo.xmleditor.schema.ISchema;

import javax.swing.table.TableCellEditor;
import javax.swing.*;
import java.awt.*;


/**
 * This class provides some basic support for writing new AttributeEditorPlugin's.
 * It will display either a textfield or combobox (if the schema contains predefined
 * values for the attribute). It is possible to add additional components, usually
 * JButtons, to the right of the textfield or combobox. The code behind these buttons
 * can modify the contents of the textfield or combobox through the provided 'Valuable'
 * (see getValuable()).
 *
 * <p>
 * Usage: see the example plugins for Cocoon or Ant.
 *
 * @author Bruno Dumon
 */
public class AttributeEditorSupport
{
    protected ExtendedTextFieldTableCellEditor textFieldEditor = null;
    protected ExtendedComboBoxTableCellEditor comboBoxEditor = null;

    protected Box extraTextFieldComponents = null;
    protected Box extraComboBoxComponents = null;

    protected short mode;
    private static final short TEXTFIELD_MODE = 1;
    private static final short COMBOBOX_MODE = 2;

    protected ISchema schema;

    public AttributeEditorSupport(ISchema schema)
    {
        this.schema = schema;

        // create the textfield editor
        extraTextFieldComponents = new Box(BoxLayout.X_AXIS);
        textFieldEditor = new ExtendedTextFieldTableCellEditor(extraTextFieldComponents);

        // create the combobox editor
        extraComboBoxComponents = new Box(BoxLayout.X_AXIS);
        comboBoxEditor = new ExtendedComboBoxTableCellEditor(extraComboBoxComponents);
    }

    /**
     * Prepares the editorSupport for a new attribute. This will, based on the schema,
     * decide wether to use a combobox or textfield. It will also remove any components
     * previously added using addComponent().
     */
    public void reset(Element element, String namespaceURI, String localName)
    {
        extraTextFieldComponents.removeAll();
        extraComboBoxComponents.removeAll();

        String [] values = schema.getPossibleAttributeValues(element, namespaceURI, localName);
        if (values != null)
        {
            comboBoxEditor.setModel(new DefaultComboBoxModel(values));
            mode = COMBOBOX_MODE;
        }
        else
        {
            mode = TEXTFIELD_MODE;
        }
    }

    /**
     * Returns the 'Valuable', this is needed to change the text in either the
     * textfield or the combobox. This method should be called after the reset method.
     */
    public Valuable getValuable()
    {
        switch (mode)
        {
            case COMBOBOX_MODE:
                return comboBoxEditor.getValuable();
            case TEXTFIELD_MODE:
                return textFieldEditor.getValuable();
        }
        throw new Error("[AttributeEditorSupport] mode has incorrect value.");
    }

    /**
     * Adds a component, such as a JButton, to the right of the combobox or
     * textfield.
     */
    public void addComponent(Component component)
    {
        switch (mode)
        {
            case COMBOBOX_MODE:
                extraComboBoxComponents.add(component);
                break;
            case TEXTFIELD_MODE:
                extraTextFieldComponents.add(component);
                break;
            default:
                throw new Error("[AttributeEditorSupport] mode has incorrect value.");
        }
    }

    /**
     * Returns the actual TableCellEditor.
     */
    public TableCellEditor getEditor()
    {
        switch (mode)
        {
            case COMBOBOX_MODE:
                return comboBoxEditor;
            case TEXTFIELD_MODE:
                return textFieldEditor;
        }
        throw new Error("[AttributeEditorSupport] mode has incorrect value.");
    }

    public JTextField [] getTextFields()
    {
        return new JTextField [] { comboBoxEditor.getTextField(), textFieldEditor.getTextField() };
    }
}
