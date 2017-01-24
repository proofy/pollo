package org.outerj.pollo.engine.pollo;

import org.outerj.pollo.xmleditor.plugin.IAttributeEditorPlugin;
import org.outerj.pollo.xmleditor.plugin.AttributeEditorSupport;
import org.outerj.pollo.xmleditor.schema.ISchema;
import org.outerj.pollo.xmleditor.IconManager;
import org.outerj.pollo.util.Valuable;
import org.outerj.pollo.util.ColorFormat;
import org.outerj.pollo.PolloFrame;
import org.w3c.dom.Element;

import javax.swing.table.TableCellEditor;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class DisplaySpecAttrEditorPlugin implements IAttributeEditorPlugin
{
    protected AttributeEditorSupport editorSupport;
    protected JButton chooseColorButton;
    protected Valuable currentValuable;

    public void init(ISchema schema, final PolloFrame polloFrame)
    {
        this.editorSupport = new AttributeEditorSupport(schema);

        chooseColorButton = new JButton(IconManager.getIcon("org/outerj/pollo/engine/pollo/color.png"));
        chooseColorButton.setMargin(new Insets(0, 0, 0, 0));
        chooseColorButton.setRequestFocusEnabled(false);
        chooseColorButton.setToolTipText("Select a color.");
        chooseColorButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                String currentColorString = currentValuable.getValue();
                Color currentColor = Color.white;
                try
                {
                    currentColor = ColorFormat.parseHexColor(currentColorString);
                }
                catch (Exception e1)
                {
                    // ignore invalid color
                }
                Color color = JColorChooser.showDialog(polloFrame, "Select a color", currentColor);
                if (color != null)
                    currentValuable.setValue(ColorFormat.formatHex(color));
            }
        });

    }

    public TableCellEditor getAttributeEditor(Element element,
                                              String namespaceURI, String localName)
    {
        editorSupport.reset(element, namespaceURI, localName);

        currentValuable = editorSupport.getValuable();

        if (localName.endsWith("color"))
        {
            editorSupport.addComponent(chooseColorButton);
        }

        return editorSupport.getEditor();
    }
}
