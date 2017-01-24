package org.outerj.pollo.xmleditor.view;

import org.outerj.pollo.xmleditor.XmlEditor;
import org.outerj.pollo.xmleditor.XmlTransferable;
import org.outerj.pollo.xmleditor.view.View;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventTarget;
import org.w3c.dom.events.MutationEvent;

import java.awt.*;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Iterator;


public abstract class ChildrenBlockView extends BlockView
{
    private int contentHeight;
    private Node node;
    protected ArrayList childViewList = new ArrayList(5);

    protected static final int NOT_CALCULATED    = -1;

    public ChildrenBlockView(View parentView, Node node, XmlEditor xmlEditor)
    {
        super(parentView, xmlEditor);
        this.node = node;

        // register this view as an eventlistener for changes to the element
        ((EventTarget)node).addEventListener("DOMNodeInserted", this, false);
        ((EventTarget)node).addEventListener("DOMNodeRemoved", this, false);
    }


    public void paint(Graphics2D gr, int startH, int startV)
    {
        if (childViewList.size()  > 0)
        {
            // now draw the children, but only those that need updating
            Iterator childrenIt = childViewList.iterator();

            int clipStartVertical = (int)gr.getClipBounds().getY();
            int clipEndVertical = clipStartVertical + (int)gr.getClipBounds().getHeight();
            if (!isCollapsed())
            {
                int childVertPos = startV + getHeaderHeight() + getVerticalSpacing();
                while (childrenIt.hasNext())
                {
                    View view = (View)childrenIt.next();
                    if (view.needsRepainting(childVertPos, clipStartVertical, clipEndVertical))
                        view.paint(gr, startH + getLeftMargin(), childVertPos);
                    childVertPos += view.getHeight() + getVerticalSpacing();
                }
            }
        }
    }


    public void layout(int width)
    {
        // layout the children of this view
        if (childViewList.size() > 0)
        {
            Iterator childrenIt = childViewList.iterator();
            while (childrenIt.hasNext())
            {
                View view = (View)childrenIt.next();
                view.layout(width - getLeftMargin());
            }
        }

        invalidateHeight();
    }



    /**
     * Recalculates the height of this view and recursively of its parent views
     * when the height has changed because of element removal/addition or when
     * collapsing/expanding.
     */
    public void heightChanged(int amount)
    {
        if (!isCollapsed())
        {
            contentHeight = contentHeight + amount;
            if (parentView != null)
                parentView.heightChanged(amount);
            else
                resetSize();
        }
    }

    public int widthChanged(int amount)
    {
        Iterator childrenIt = childViewList.iterator();
        while (childrenIt.hasNext())
        {
            View view = (View)childrenIt.next();
            view.widthChanged(amount);
        }

        return getHeight();
    }


    public void addChildView(View childView)
    {
        childViewList.add(childView);
    }

    // mouse events

    public void mousePressed(MouseEvent e, int startH, int startV)
    {
        Iterator childrenIt = childViewList.iterator();
        int childVertPos = startV + getHeaderHeight() + getVerticalSpacing();
        while (childrenIt.hasNext())
        {
            View childView = (View)childrenIt.next();
            if (e.getY() > childVertPos && e.getY() < childVertPos + childView.getHeight())
            {
                childView.mousePressed(e, startH + getLeftMargin(), childVertPos);
                break;
            }
            childVertPos += childView.getHeight() + getVerticalSpacing();
        }
    }

    public int getHeight()
    {
        if (!isCollapsed() && childViewList.size() > 0)
            return getHeaderHeight() + getContentHeight() + getFooterHeight();
        else if (isCollapsed())
            return getHeaderHeight() + getFooterHeight(); 
        else
            return getHeaderHeight(); 
    }

    public int getContentHeight()
    {
        if (contentHeight == NOT_CALCULATED)
        {
            Iterator childrenIt = childViewList.iterator();

            if (childViewList.size() > 0)
            {
                int totalChildrenHeight = 0;

                while (childrenIt.hasNext())
                {
                    View view = (View)childrenIt.next();
                    totalChildrenHeight += view.getHeight() + getVerticalSpacing();
                }

                contentHeight = totalChildrenHeight + getVerticalSpacing();
            }
            else
            {
                contentHeight = 0;
            }
        }
        return contentHeight;
    }

    public void invalidateHeight()
    {
        contentHeight = NOT_CALCULATED;
    }

    public boolean isCollapsable()
    {
        if (childViewList.size() > 0)
            return true;
        else
            return false;
    }

    public void handleEvent(Event e)
    {
        try
        {
            if (e.getType().equalsIgnoreCase("DOMNodeInserted"))
            {
                if (((MutationEvent)e).getRelatedNode() != node)
                    return;
                e.stopPropagation();
                createViewsForNewChildren();
            }
            else if (e.getType().equalsIgnoreCase("DOMNodeRemoved"))
            {
                if (((Node)e.getTarget()).getParentNode() == node)
                {
                    removeViewForRemovedChild((Node)e.getTarget());
                    e.stopPropagation();
                }
            }
            else
            {
                System.out.println("WARNING: unprocessed dom event:" + e.getType());
            }
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
        return node;
    }


    /**
      This method updates the list of childviews: if elements were inserted
      in the dom, new corresponding views are created. Also updates the
      layout.
     */
    public void createViewsForNewChildren()
    {
        NodeList children = node.getChildNodes();
        int nodeChildNodeCounter = 0;
        boolean heightChanged = false;
        int oldHeight = getHeight();

        for (int i = 0; i < children.getLength(); i++)
        {
            Node node = children.item(i);
            if (XmlEditor.isNodeTypeSupported(node.getNodeType()))
            {
                View correspondingView = null;
                try
                {
                    correspondingView = (View)childViewList.get(nodeChildNodeCounter);
                }
                catch (IndexOutOfBoundsException e) { }

                if (correspondingView == null || correspondingView.getNode() != node)
                {
                    View childView = xmlEditor.createView(node, this);
                    childView.layout(width - getLeftMargin());
                    childViewList.add(nodeChildNodeCounter, childView);
                    heightChanged = true;
                }
                nodeChildNodeCounter++;
            }
        }

        if (heightChanged)
        {
            invalidateHeight();
            int newHeight = getHeight();
            int diff = newHeight - oldHeight;
            applyHeightChange(diff);
        }
    }

    public void removeViewForRemovedChild(Node removedChild)
    {
        NodeList children = node.getChildNodes();
        int relevantChildNodeCounter = 0; // only nodes that are displayed count (currently e.g. not PI's)
        boolean heightChanged = false;
        int oldHeight = getHeight();

        for (int i = 0; i < children.getLength(); i++)
        {
            Node node = children.item(i);
            if ((node == removedChild))
            {
                View correspondingView = (View)childViewList.get(relevantChildNodeCounter);
                heightChanged = true;
                correspondingView.removeEventListeners();
                childViewList.remove(relevantChildNodeCounter);
                break;
                
            }
            if (XmlEditor.isNodeTypeSupported(node.getNodeType()))
                relevantChildNodeCounter++;
        }
        if (heightChanged)
        {
            invalidateHeight();
            int newHeight = getHeight();
            int diff = newHeight - oldHeight;
            applyHeightChange(diff);
        }
    }

    public void removeEventListeners()
    {
        ((EventTarget)node).removeEventListener("DOMNodeInserted", this, false);
        ((EventTarget)node).removeEventListener("DOMNodeRemoved", this, false);

        Iterator childViewListIt = childViewList.iterator();
        while (childViewListIt.hasNext())
        {
            View childView = (View)childViewListIt.next();
            childView.removeEventListeners();
        }
    }


    protected DocumentFragment createDocumentFragment(Node node)
    {
        DocumentFragment documentFragment = xmlEditor.getXmlModel().getDocument().createDocumentFragment();
        documentFragment.appendChild(node.cloneNode(true));
        return documentFragment;
    }


    public void dragGestureRecognized(DragGestureEvent event, int startH, int startV)
    {
        Iterator childrenIt = childViewList.iterator();
        int childVertPos = startV + getHeaderHeight() + getVerticalSpacing();
        Point p = event.getDragOrigin();
        while (childrenIt.hasNext())
        {
            View childView = (View)childrenIt.next();
            if (p.getY() > childVertPos && p.getY() < childVertPos + childView.getHeight())
            {
                childView.dragGestureRecognized(event, startH + getLeftMargin(), childVertPos);
                break;
            }
            childVertPos += childView.getHeight() + getVerticalSpacing();
        }
    }


    public void dragOver(DropTargetDragEvent event, int startH, int startV)
    {
        // here we handle the dragging between the nodes

        // There is some difference in handling for the cases where getVerticalSpacing is 0 or not 0.
        // If it is not null, we will be checking if we drag in the spacing area, otherwise if we drag
        // in a "sensitivity" area. Especially, the handling where elements ends is different: if
        // getVerticalSpacing() returns 0, nested elements will vertically end on the same Y position,
        // and hence we'll need to check the X coordinate to determine of which element the dragged node
        // will become a child.

        Iterator childrenIt = childViewList.iterator();
        int childVertPos = startV + getHeaderHeight() + getVerticalSpacing();
        int previousChildVertPos = childVertPos;
        int lastChildStartV = childVertPos;
        View childView = null;
        View previousChildView = null;
        Point p = event.getLocation();
        while (childrenIt.hasNext())
        {
            childView = (View)childrenIt.next();

            if (getVerticalSpacing() == 0 && p.getY() > (childVertPos - getDragSensitivityArea()) && p.getY() < childVertPos + getDragSensitivityArea())
            {
                if (previousChildView != null &&  previousChildView.hasChildren() && !previousChildView.isCollapsed() && p.getX() > startH + getDragLineLeftMargin())
                {
                    previousChildView.dragOver(event, startH + getLeftMargin(), previousChildVertPos);
                    return;
                }
                else if (event.isDataFlavorSupported(XmlTransferable.xmlFlavor))
                {
                    drawDragOverEffectLine(startH, childVertPos - 1);
                    xmlEditor.setDropData(xmlEditor.DROP_ACTION_INSERT_BEFORE, childView.getNode());
                    event.acceptDrag(event.getDropAction());
                }
                return;
            }
            else if (getVerticalSpacing() != 0 && p.getY() > (childVertPos - getVerticalSpacing()) && p.getY() < childVertPos)
            {
                if (event.isDataFlavorSupported(XmlTransferable.xmlFlavor))
                {
                    drawDragOverEffectLine(startH, childVertPos - 3);
                    xmlEditor.setDropData(xmlEditor.DROP_ACTION_INSERT_BEFORE, childView.getNode());
                    event.acceptDrag(event.getDropAction());
                }
                return;
            }
            else if (p.getY() > childVertPos && p.getY() < childVertPos + childView.getHeight())
            {
                childView.dragOver(event, startH + getLeftMargin(), childVertPos);
                break;
            }
            else
            {
                xmlEditor.setDropData(xmlEditor.DROP_ACTION_NOT_ALLOWED, null);
                event.rejectDrag();
            }

            previousChildVertPos = childVertPos;
            previousChildView = childView;
            lastChildStartV = childVertPos;
            childVertPos += childView.getHeight() + getVerticalSpacing();
        }

        // after the last one
        if ((getVerticalSpacing() == 0 && p.getY() > (childVertPos - getDragSensitivityArea()) && p.getY() < childVertPos + getDragSensitivityArea())
        || (getVerticalSpacing() != 0 && p.getY() > (childVertPos - getVerticalSpacing()) && p.getY() < childVertPos))
        {
            if (getVerticalSpacing() == 0 && childView.hasChildren() && !childView.isCollapsed() && p.getX() > startH + getLeftMargin() + getDragLineLeftMargin())
            {
                childView.dragOver(event, startH + getLeftMargin(), previousChildVertPos);
            }
            else
            {
                if (getVerticalSpacing() == 0 && childView.hasChildren() && !childView.isCollapsed())
                {
                    drawDragOverAngle(startH, lastChildStartV, childView);
                }
                else
                {
                    drawDragOverEffectLine(startH, childVertPos - (getVerticalSpacing() == 0 ? 1 : 3));
                }
                xmlEditor.setDropData(xmlEditor.DROP_ACTION_APPEND_CHILD, childView.getNode().getParentNode());
                event.acceptDrag(event.getDropAction());
            }
        }
    }

    protected void drawDragOverEffectLine(int startH, int vertPos)
    {
        int horPos = (getVerticalSpacing() == 0 ? getDragLineLeftMargin() : getLeftMargin());
        Shape shape = new Rectangle(startH + horPos, vertPos, width - horPos, 2);

        xmlEditor.setDragOverEffectRedraw(shape);
        Graphics2D graphics = (Graphics2D)xmlEditor.getGraphics();
        graphics.setColor(new Color(255, 0, 0));
        graphics.fill(shape);
    }

    protected void drawDragOverAngle(int startH, int childStartV, View lastChildView)
    {
        Polygon poly = new Polygon();
        int x = startH + getLeftMargin() + BORDER_WIDTH + HALF_COLLAPSE_SIGN_SIZE;
        int topY = childStartV + lastChildView.getFirstLineCenterPos() + HALF_COLLAPSE_SIGN_SIZE;
        poly.addPoint(x, topY);
        poly.addPoint(x + 2, topY);
        poly.addPoint(x + 2, childStartV + lastChildView.getHeight());
        poly.addPoint(startH + width, childStartV + lastChildView.getHeight());
        poly.addPoint(startH + width, childStartV + lastChildView.getHeight() + 2);
        poly.addPoint(x, childStartV + lastChildView.getHeight() + 2);

        xmlEditor.setDragOverEffectRedraw(poly);
        Graphics2D graphics = (Graphics2D)xmlEditor.getGraphics();
        graphics.setColor(new Color(255, 0, 0));
        graphics.fill(poly);
    }

    public void collapseAll()
    {
        // collapse myself
        collapse();

        // collapse my child views
        Iterator childrenIt = childViewList.iterator();
        while (childrenIt.hasNext())
        {
            View view = (View)childrenIt.next();
            view.collapseAll();
        }
    }

    public void expandAll()
    {
        // expand my child views
        Iterator childrenIt = childViewList.iterator();
        while (childrenIt.hasNext())
        {
            View view = (View)childrenIt.next();
            view.expandAll();
        }

        // expand myself
        expand();

    }

    public abstract int getHeaderHeight();

    public abstract int getFooterHeight();

    public abstract int getLeftMargin();

    public boolean hasChildren()
    {
        return childViewList.size() > 0;
    }

    /**
     * Overidden method from BlockView class.
     */
    public int getVerticalPosition(View wantedView)
    {
        int startV = getVerticalPosition();

        Iterator childrenIt = childViewList.iterator();

        if (!isCollapsed())
        {
            int childVertPos = startV + getHeaderHeight() + getVerticalSpacing();
            while (childrenIt.hasNext())
            {
                View view = (View)childrenIt.next();
                if (view == wantedView)
                    return childVertPos;
                childVertPos += view.getHeight() + getVerticalSpacing();
            }
        }
        else
        {
            throw new RuntimeException("Cannot give the position of the childview is its parent is collapsed!");
        }
        throw new RuntimeException("The given view is not a childview.");
    }

    /**
     * Overidden method from BlockView class.
     */
    public int getHorizontalPosition(View wantedChildView)
    {
        return getHorizontalPosition() + getLeftMargin();
    }

    /**
     * Overidden method from BlockView class.
     */
    public View getNextSibling(View wantedChildView)
    {
        Iterator childrenIt = childViewList.iterator();
        while (childrenIt.hasNext())
        {
            View view = (View)childrenIt.next();
            if (view == wantedChildView)
            {
                try
                {
                    return (View)childrenIt.next();
                }
                catch (java.util.NoSuchElementException e)
                {
                    return null;
                }
            }
        }
        throw new RuntimeException("The given view is not a childview.");
    }

    /**
     * Overidden method from BlockView class.
     */
    public View getNext(boolean visible)
    {
        if (childViewList.size() > 0 && (visible ? !isCollapsed() : true))
        {
            return (View)childViewList.get(0);
        }
        else
        {
            return super.getNext(visible);
        }
    }

    /**
     * Overidden method from BlockView class.
     */
    public View getPreviousSibling(View wantedChildView)
    {
        Iterator childrenIt = childViewList.iterator();
        View previousView = null;
        while (childrenIt.hasNext())
        {
            View view = (View)childrenIt.next();
            if (view == wantedChildView)
            {
                return previousView;
            }
            previousView = view;
        }
        throw new RuntimeException("The given view is not a childview.");
    }


    public View getLastChild(boolean visible)
    {
        if (isCollapsed())
            return this;

        if (childViewList.size() > 0)
        {
            return ((View)childViewList.get(childViewList.size() - 1)).getLastChild(visible);
        }
        return this;
    }

    public View findNode(Node soughtNode)
    {
        if (soughtNode == node)
            return this;

        Node previousParent = soughtNode;
        while(true)
        {
            Node parentNode = previousParent.getParentNode();
            if (parentNode == null)
            {
                return null;
            }

            if (parentNode == node)
            {
                // zoek onder mijn kinderen
                Iterator childrenIt = childViewList.iterator();
                while (childrenIt.hasNext())
                {
                    View view = (View)childrenIt.next();
                    if (view.getNode() == previousParent)
                        return view.findNode(soughtNode);
                }
            }
            previousParent = parentNode;
        }
    }

    /**
     * @return the spacing between children.
     */
    protected abstract int getVerticalSpacing();

    /**
     * Only applicable if getVerticalSpacing() returns 0.
     */
    protected abstract int getDragSensitivityArea();

    protected abstract int getDragLineLeftMargin();
}
