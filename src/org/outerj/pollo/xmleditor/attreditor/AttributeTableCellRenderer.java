package org.outerj.pollo.xmleditor.attreditor;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import org.outerj.pollo.xmleditor.attreditor.AttributesTableModel.TempAttrEditInfo;
import org.outerj.pollo.xmleditor.IconManager;
import org.outerj.pollo.gui.EmptyIcon;

import java.awt.*;

/**
 * TableCellRenderer used by the attributes panel. If an attribute has no
 * value, the attribute name will appear 'disabled' (usually light grey)
 * and for its value a dash (-) will be shown.
 *
 * @author Bruno Dumon
 */
public class AttributeTableCellRenderer extends DefaultTableCellRenderer
{
    protected TempAttrEditInfo taei = null;
    protected int column = 0;
    protected Icon requiredAttributeIcon = IconManager.getIcon("org/outerj/pollo/xmleditor/attreditor/required_attribute.gif");
    protected Icon unrequiredAttributeSpacerIcon = new EmptyIcon(requiredAttributeIcon.getIconHeight(), requiredAttributeIcon.getIconWidth());
    protected Color disabledForeground = UIManager.getColor("Label.disabledForeground");

    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column)
    {
        super.getTableCellRendererComponent(table, null, isSelected, hasFocus, row, column);

        AttributesTableModel tableModel = (AttributesTableModel)table.getModel();
        taei = tableModel.getTempAttrEditInfo(row);
        this.column = column;
        setIcon(null);

        switch (column)
        {
            case 0:
                setText(taei.getLabel());
                if (taei.value == null)
                    setForeground(disabledForeground);
                else
                    setForeground(Color.black);

                if (taei.attrSchema != null && taei.attrSchema.required)
                    setIcon(requiredAttributeIcon);
                else
                    setIcon(unrequiredAttributeSpacerIcon);

                break;
            case 1:
                if (taei.value == null)
                {
                    setForeground(disabledForeground);
                    setText(" - ");
                }
                else
                {
                    setForeground(Color.black);
                    setText(taei.value);
                }
                break;
        }
        return this;
    }

    protected void setValue(Object value)
    {
        // empty on purpose
    }

    public void paint(Graphics g)
    {
        super.paint(g);
        if (column == 1 && taei.attrSchema != null && taei.attrSchema.hasPickList())
        {
            g.setColor(UIManager.getColor("Label.disabledForeground"));

            int arrowWidth = 10;
            int x = getWidth() - arrowWidth - 2;
            int y = (getHeight() / 2) - 2;

            g.translate(x, y);

            g.drawLine( 0, 0, arrowWidth - 1, 0 );
            g.drawLine( 1, 1, 1 + (arrowWidth - 3), 1 );
            g.drawLine( 2, 2, 2 + (arrowWidth - 5), 2 );
            g.drawLine( 3, 3, 3 + (arrowWidth - 7), 3 );
            g.drawLine( 4, 4, 4 + (arrowWidth - 9), 4 );

            g.translate(-x, -y);
        }
    }

}
