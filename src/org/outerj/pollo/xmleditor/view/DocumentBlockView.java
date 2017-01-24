package org.outerj.pollo.xmleditor.view;

import org.outerj.pollo.xmleditor.NodeClickedEvent;
import org.outerj.pollo.xmleditor.XmlEditor;
import org.outerj.pollo.xmleditor.XmlTransferable;
import org.outerj.pollo.xmleditor.view.View;
import org.outerj.pollo.xmleditor.displayspec.IDisplaySpecification;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.events.Event;

import java.awt.*;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.event.MouseEvent;


public class DocumentBlockView extends ChildrenBlockView
{
    protected int titleHeight = 10;
    protected int collapseSignTop;
    protected Document document;
    protected IDisplaySpecification displaySpec;
    protected String title;
    protected Color documentColor = new Color(200, 200, 200);
    protected final ViewStrategy viewStrategy;

    public DocumentBlockView(View parentView, Document document, XmlEditor xmlEditor, ViewStrategy viewStrategy)
    {
        super(parentView, document, xmlEditor);
        this.document = document;
        this.displaySpec = xmlEditor.getDisplaySpec();
        this.viewStrategy = viewStrategy;

        StringBuffer titleBuffer = new StringBuffer();
        titleBuffer.append("XML");
        if (document.getVersion() != null)
        {
            titleBuffer.append("   version=" + document.getVersion());
        }
        if (document.getEncoding() != null)
        {
            titleBuffer.append("   encoding=" + document.getEncoding());
        }
        titleBuffer.append("   standalone=" + document.getStandalone());

        title = titleBuffer.toString();
    }

    public void paint(Graphics2D gr, int startH, int startV)
    {
        gr.setColor(documentColor);

        gr.fill(new Rectangle(startH, startV, startH + width, startV + titleHeight));

        viewStrategy.drawDocumentFrame(gr, startH, startV, this);

        if (hasChildren())
        {
            super.paint(gr, startH, startV);
        }

        int baseline = startV + xmlEditor.getElementNameFontMetrics().getAscent() + 2;
        // draw the element name
        gr.setFont(xmlEditor.getElementNameFont());
        gr.setColor(Color.white);
        gr.drawString(title, startH + 20, baseline);
    }

    public void layout(int width)
    {
        // init
        this.titleHeight = xmlEditor.getElementNameFontMetrics().getHeight() + 4;
        this.width = width;

        // layout the children of this view
        super.layout(this.width);
    }


    public int widthChanged(int amount)
    {
        this.width = this.width + amount;

        return super.widthChanged(amount);
    }


    // mouse events

    public void mousePressed(MouseEvent e, int startH, int startV)
    {
        if ( e.getY() > startV && e.getY() < startV + titleHeight
               && e.getX() > startH)
        {
            NodeClickedEvent nce = new NodeClickedEvent(document, e);
            xmlEditor.fireNodeClickedEvent(nce);
            
            // make that the current element is indicated
            markAsSelected(startH, startV);
        }
        else
        {
            super.mousePressed(e, startH, startV);
        }
    }

    public void handleEvent(Event e)
    {
        try
        {
            super.handleEvent(e);
        }
        catch (Exception exc)
        {
            // this try-catch is necessary because if an exception occurs in this code,
            // it is catched somewhere in the xerces code and we wouldn't see it or know
            // what's going on.
            exc.printStackTrace();
        }
    }

    public Node getNode()
    {
        return document;
    }


    public void removeEventListeners()
    {
        super.removeEventListeners();
    }



    public void dragGestureRecognized(DragGestureEvent event, int startH, int startV)
    {
        // only allow dragging with left mouse button (otherwise got problems on linux
        // when showing context menus)
        if ((event.getTriggerEvent().getModifiers() & MouseEvent.BUTTON1_MASK) == 0)
            return;

        Point p = event.getDragOrigin();
        if ( p.getY() > startV && p.getY() < startV + titleHeight
               && p.getX() > startH)
        {
            // document view cannot be dragged
        }
        else
        {
            super.dragGestureRecognized(event, startH, startV);
        }
    }


    public void dragOver(DropTargetDragEvent event, int startH, int startV)
    {
        if (isCollapsed())
        {
            xmlEditor.setDropData(xmlEditor.DROP_ACTION_NOT_ALLOWED, null);
            event.rejectDrag();
            return;
        }

        Point p = event.getLocation();

        if (p.getY() > startV && p.getY() < startV + getHeaderHeight() && p.getX() > startH)
        { // it is on this element
            if (hasChildren())
            {
                // dropping is only allowed if this element has no children yet
                xmlEditor.setDropData(xmlEditor.DROP_ACTION_NOT_ALLOWED, null);
                event.rejectDrag();
                return;
            }

            // draw drag over effect
            Rectangle rect = new Rectangle(startH, startV, width, getHeaderHeight());
            xmlEditor.setDragOverEffectRedraw(new Rectangle(rect.x - 1, rect.y - 1, rect.width + 2, rect.height + 2));
            Graphics2D graphics = (Graphics2D)xmlEditor.getGraphics();
            graphics.setColor(new Color(255, 0, 0));
            graphics.setStroke(new BasicStroke(2));
            graphics.draw(rect);

            xmlEditor.setDropData(xmlEditor.DROP_ACTION_APPEND_CHILD, document);
            if (event.isDataFlavorSupported(XmlTransferable.xmlFlavor))
                event.acceptDrag(event.getDropAction());
            /* CommandTransferable -- deprecated.
            else if (event.isDataFlavorSupported(CommandTransferable.commandFlavor))
                event.acceptDrag(DnDConstants.ACTION_MOVE);
            */
            return;
        }
        else if (p.getX() > startH && p.getX() < (startH + width) ) // maybe it's between to elements
        {
            super.dragOver(event, startH, startV);
        }
        else
        {
            xmlEditor.setDropData(xmlEditor.DROP_ACTION_NOT_ALLOWED, null);
            event.rejectDrag();
        }
    }


    public int getHeaderHeight()
    {
        return titleHeight;
    }

    public int getFooterHeight()
    {
        return 2;
    }

    public int getLeftMargin()
    {
        return 2;
    }


    // the document node cannot be collapsed

    public void collapse() {}

    public void expand() {}

    public void collapseAll() {}

    public void expandAll() {}

    public boolean isCollapsed()
    {
        return false;
    }

    protected int getDragLineLeftMargin()
    {
        return getLeftMargin();
    }

    public int getFirstLineCenterPos()
    {
        // irrelevant, will never be called
        return 0;
    }

    protected int getVerticalSpacing()
    {
        return viewStrategy.getVerticalSpacing();
    }

    protected int getDragSensitivityArea()
    {
        return viewStrategy.getDragSensitivityArea();
    }
}
