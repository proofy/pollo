package org.outerj.pollo.xmleditor.view;

import org.outerj.pollo.xmleditor.XmlEditor;
import org.outerj.pollo.xmleditor.view.View;
import org.w3c.dom.Comment;

import javax.swing.*;
import java.awt.*;


/**
  Implements a block view (block as opposed to inline) for Comment nodes.
 */
public class CommentView extends CharacterDataBlockView
{
    protected final ViewStrategy viewStrategy;
    protected static final Icon ICON = new ImageIcon(CDataView.class.getResource("/org/outerj/pollo/xmleditor/icons/comment.png"));

    public CommentView(View parentView, Comment comment, XmlEditor xmlEditor, ViewStrategy viewStrategy)
    {
        super(parentView, comment, xmlEditor);
        this.viewStrategy = viewStrategy;
    }

    public void drawFrame(Graphics2D g, int startH, int startV)
    {
        viewStrategy.drawCommentFrame(g, startH, startV, this);
    }

    public Icon getIcon()
    {
        return ICON;
    }

    public String getLabel()
    {
        return "comment()";
    }
}
