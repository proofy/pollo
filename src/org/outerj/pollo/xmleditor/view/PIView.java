package org.outerj.pollo.xmleditor.view;

import org.outerj.pollo.xmleditor.XmlEditor;
import org.outerj.pollo.xmleditor.view.View;
import org.w3c.dom.ProcessingInstruction;

import javax.swing.*;
import java.awt.*;


/**
 * A View for processing instructions.
 */
public class PIView extends CharacterDataBlockView
{
    protected final String title;
    protected final ViewStrategy viewStrategy;
    protected static final Icon ICON = new ImageIcon(CDataView.class.getResource("/org/outerj/pollo/xmleditor/icons/processinginstruction.png"));

    public PIView(View parentView, ProcessingInstruction pi, XmlEditor xmlEditor, ViewStrategy viewStrategy)
    {
        super(parentView, pi, xmlEditor);
        title = new String("Processing Instruction target: " + pi.getTarget());
        this.viewStrategy = viewStrategy;
    }

    public void drawFrame(Graphics2D g, int startH, int startV)
    {
        viewStrategy.drawPIFrame(g, startH, startV, this);
    }

    public int getHeader()
    {
        return xmlEditor.getCharacterDataFontMetrics().getHeight();
    }

    public int getFooter()
    {
        return viewStrategy.getPIFooter();
    }

    public Icon getIcon()
    {
        return ICON;
    }

    public String getLabel()
    {
        return "processing-instruction";
    }
}
