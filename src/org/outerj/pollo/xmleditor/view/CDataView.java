package org.outerj.pollo.xmleditor.view;

import org.outerj.pollo.xmleditor.XmlEditor;
import org.outerj.pollo.xmleditor.view.View;
import org.w3c.dom.CDATASection;

import javax.swing.*;
import java.awt.*;


/**
  A view for CDATA nodes.
 */
public class CDataView extends CharacterDataBlockView
{
    protected final ViewStrategy viewStrategy;
    protected static final Icon ICON = new ImageIcon(CDataView.class.getResource("/org/outerj/pollo/xmleditor/icons/cdata.png"));

    public CDataView(View parentView, CDATASection cdata, XmlEditor xmlEditor, ViewStrategy viewStrategy)
    {
        super(parentView, cdata, xmlEditor);
        this.viewStrategy = viewStrategy;
    }

    public void drawFrame(Graphics2D g, int startH, int startV)
    {
        viewStrategy.drawCDataFrame(g, startH, startV, this);
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
