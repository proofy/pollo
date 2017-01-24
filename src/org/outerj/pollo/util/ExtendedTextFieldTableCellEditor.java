package org.outerj.pollo.util;

import javax.swing.table.TableCellEditor;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

/**
 * A TableCellEditor that consists of a textfield with (optionally) some
 * extra components next to it (e.g. a button to browse for a file name, etc.).
 *
 * <p>This behaviour is achieved by placing a textfield and the extra components on
 * a panel, and returning that panel as editor.
 *
 * <p>Using the methods in the Valuable interface, the contents of the textfield
 * can be manipulated in a uniform way as for the ExtendedComboBoxTableCellEditor.
 *
 * @author Bruno Dumon
 */
public class ExtendedTextFieldTableCellEditor extends AbstractCellEditor implements TableCellEditor, Valuable
{
    protected JPanel panel;
    protected PublicKeyBindingTextField textField;

    public ExtendedTextFieldTableCellEditor(Component extraStuff)
    {
        // since it is not really the panel itself which is the editor, but
        // rather the textfield inside it, some methods must be overided to
        // make it behave like it should.
        panel = new JPanel()
        {
            protected boolean processKeyBinding(KeyStroke ks, KeyEvent e,
                                                int condition, boolean pressed)
            {
                if (textField.processKeyBindingPublic(ks, e, condition, pressed))
                    return true;
                else
                    return super.processKeyBinding(ks, e, condition, pressed);
            }

            public void requestFocus()
            {
                textField.requestFocus();
            }
        };

        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));

        textField = new PublicKeyBindingTextField();
        textField.setBorder(null);
        panel.add(textField);
        panel.add(extraStuff);

        textField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                Component parent = panel.getParent();
                ExtendedTextFieldTableCellEditor.this.stopCellEditing();
                // moving the focus back to the parent manually is necessary for java 1.4
                parent.requestFocus();
            }
        });
    }

    public JTextField getTextField()
    {
        return textField;
    }

    public Valuable getValuable()
    {
        return this;
    }

    public void setValue(String value)
    {
        textField.setText(value);
    }

    public String getValue()
    {
        return textField.getText();
    }

    public void insertString(String value)
    {
        int pos = textField.getCaretPosition();
        try
        {
            textField.getDocument().insertString(pos, value, null);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }


    public Component getTableCellEditorComponent(JTable table, Object value,
                                                 boolean isSelected,
                                                 int row, int column)
    {
        textField.setText(value != null ? value.toString() : "");
        return panel;
    }

    public Object getCellEditorValue()
    {
        return textField.getText();
    }
}
