package org.outerj.pollo.xmleditor.view;

import org.outerj.pollo.xmleditor.XmlEditor;
import org.outerj.pollo.xmleditor.view.View;
import org.w3c.dom.Text;

import javax.swing.*;
import java.awt.*;


/**
  A View for Text nodes.
 */
public class TextView extends CharacterDataBlockView
{
    protected final ViewStrategy viewStrategy;
    protected static final Icon ICON = new ImageIcon(CDataView.class.getResource("/org/outerj/pollo/xmleditor/icons/text.png"));

    public TextView(View parentView, Text text, XmlEditor xmlEditor, ViewStrategy viewStrategy)
    {
        super(parentView, text, xmlEditor);
        this.viewStrategy = viewStrategy;
    }

    public void drawFrame(Graphics2D g, int startH, int startV)
    {
        viewStrategy.drawTextFrame(g, startH, startV, this);
    }

    public Icon getIcon()
    {
        return ICON;
    }

    public String getLabel()
    {
        return "text()";
    }
}
