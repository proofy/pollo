package org.outerj.pollo.xmleditor.view;

import org.w3c.dom.*;
import org.w3c.dom.events.Event;
import org.outerj.pollo.xmleditor.*;
import org.outerj.pollo.xmleditor.view.View;

import java.awt.*;
import java.awt.dnd.*;
import java.awt.event.MouseEvent;

/**
 * View for rendering entity references.
 */
public class EntityReferenceView extends BlockView
{
    protected EntityReference entityReference;
    protected Font font;
    protected FontMetrics fontMetrics;
    protected final ViewStrategy viewStrategy;

    public EntityReferenceView(View parentView, EntityReference entityReference, XmlEditor xmlEditor, ViewStrategy viewStrategy)
    {
        super(parentView, xmlEditor);
        this.entityReference = entityReference;
        this.viewStrategy = viewStrategy;
    }

    public void paint(Graphics2D g, int startH, int startV)
    {
        drawFrame(g, startH, startV);

        int verticalOffset = startV + fontMetrics.getAscent();
        int horizontalOffset = startH + BORDER_WIDTH + COLLAPSE_SIGN_SIZE + COLLAPSESIGN_ICON_SPACING + 2;
        g.setFont(font);
        g.setColor(Color.red);
        g.drawString("&", horizontalOffset, verticalOffset);

        horizontalOffset += fontMetrics.stringWidth("&");
        g.setColor(Color.black);
        g.drawString(entityReference.getNodeName(), horizontalOffset, verticalOffset);

        horizontalOffset += fontMetrics.stringWidth(entityReference.getNodeName());
        g.setColor(Color.red);
        g.drawString(";", horizontalOffset, verticalOffset);
    }

    public void drawFrame(Graphics2D gr, int startH, int startV)
    {
        viewStrategy.drawEntityReferenceFrame(gr, startH, startV, this);
    }

    public void layout(int width)
    {
        // initialize variables
        if (font == null)
            font = new Font("Monospaced", 0, 12);
        if (fontMetrics == null)
        {
            Graphics graphics = xmlEditor.getGraphics();
            graphics.setFont(font);
            fontMetrics = graphics.getFontMetrics();
        }
        this.width = width;

    }

    public int getHeight()
    {
        return fontMetrics.getHeight();
    }

    public void heightChanged(int amount)
    {
        if (parentView != null)
            parentView.heightChanged(amount);
        else
            resetSize();
    }

    public int widthChanged(int amount)
    {
        this.width = this.width + amount;
        return getHeight();
    }

    public void addChildView(View childView)
    {
        throw new RuntimeException("This is not supported on EntityReferenceView");
    }

    public void mousePressed(MouseEvent e, int startH, int startV)
    {
        NodeClickedEvent nce = new NodeClickedEvent(entityReference, e);
        xmlEditor.fireNodeClickedEvent(nce);
        markAsSelected(startH, startV);
    }

    public void dragGestureRecognized(DragGestureEvent event, int startH, int startV)
    {
        DocumentFragment documentFragment = xmlEditor.getXmlModel().getDocument().createDocumentFragment();
        documentFragment.appendChild(entityReference.cloneNode(true));
        xmlEditor.setDraggingNode(entityReference, event.getDragAction() == DnDConstants.ACTION_MOVE ? true : false);
        if(event.getDragAction() == DnDConstants.ACTION_COPY)
        {
            xmlEditor.getDragSource().startDrag(event, DragSource.DefaultCopyDrop,
                    new XmlTransferable(documentFragment), xmlEditor);
        }
        else
        {
            xmlEditor.getDragSource().startDrag(event, DragSource.DefaultMoveDrop,
                    new XmlTransferable(documentFragment), xmlEditor);
        }
    }

    public void dragOver(DropTargetDragEvent event, int startH, int startV)
    {
        // dropping is not allowed on an EntityReference node
        xmlEditor.setDropData(xmlEditor.DROP_ACTION_NOT_ALLOWED, null);
        event.rejectDrag();
    }

    public Node getNode()
    {
        return entityReference;
    }

    public void removeEventListeners()
    {
    }

    public void handleEvent(Event evt)
    {
        System.out.println("WARNING: unprocessed dom event:" + evt.getType());
    }

    public int getFirstLineCenterPos()
    {
        return getHeight() / 2;
    }
}
