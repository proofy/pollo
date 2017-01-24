package org.outerj.pollo.xmleditor.view;

import org.w3c.dom.Node;
import org.w3c.dom.events.EventListener;

import java.awt.*;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.event.MouseEvent;

/**
 * A view contains the visual represantation of an element from
 * the corresponding document.
 *
 * A view in itself doesn't know it's absolute coordinates, therfore
 * it gets it's top-left coordinate in its paint/mousePressed method.
 *
 * @author Bruno Dumon
 */
public interface View extends EventListener
{
    public void paint(Graphics2D gr, int startHorizontal, int startVertical);

    /**
     * The layout method is responsible for layouting the view, eg
     * defining where everything should be placed etc, so that the paint
     * method doesn't need to do a lot of calculations.
     *
     * @param width the available width
     *
     */
    public void layout(int width);

    /**
     * Returns the height of this view.
     */
    public int getHeight();

    /**
     * Relayouts the view when it's height has changed.
     * Used by the collapsing features.
     */
    public void heightChanged(int amount);

    /**
     * Relayouts the view when it's width is changed, like
     * when the user resizes when the window. Returns the
     * new height.
     */
    public int widthChanged(int amount);

    /**
     * Determines whether this view needs to be repainted.
     */
    public boolean needsRepainting(int startVertical, int clipStartVertical, int clipEndVertical);

    public int getWidth();

    public void addChildView(View childView);

    public void mousePressed          (MouseEvent e,              int startH, int startV);
    public void dragGestureRecognized (DragGestureEvent event,    int startH, int startV);
    public void dragOver              (DropTargetDragEvent event, int startH, int startV);

    public Node getNode();

    public void removeEventListeners();

    public boolean isCollapsed();
    public void collapse();
    public void collapseAll();
    public void expand();
    public void expandAll();

    /**
     * This function will check if the view can be be made visible,
     * which consists of checking that none of it parents are collapsed.
     *
     * It will not make the view itself visible, to do that the method
     * scrollAlignTop/Bottom of XmlEditor could be used.
     *
     * @param recursive should be false
     */
    public void assureVisibility(boolean recursive);

    /**
     * This method returns the absolute vertical position of this view.
     * A view doesn't know it's absolute position, therefore it has to
     * ask its position at its parent, which again needs to ask it at
     * its parent, and so on until the root node is reached.
     */
    public int getVerticalPosition();

    /**
     * Returns the (absolute) vertical position of the given childView.
     */
    public int getVerticalPosition(View wantedChildView);

    /**
     * Analog to getVerticalPosition()
     */
    public int getHorizontalPosition();
    public int getHorizontalPosition(View wantedChildView);

    public View getNextSibling();

    public View getNextSibling(View childView);

    /**
     * @param visible true if the next visible view should be
     * returned (eg if an element is collapsed, then that would
     * be the next sibling instead of the first child).
     */
    public View getNext(boolean visible);

    /**
     * Get the next view but not a child view.
     */
    public View getNextButNotChild();

    public View getPreviousSibling();

    public View getPreviousSibling(View childView);

    public View getPrevious(boolean visible);

    public View getParent();

    public void markAsSelected(int startH, int startV);

    /**
     * For views with childviews, this should return recursively the last child
     * of the view. Thus the last child of the last child of the last child of...
     * Or return itself if it has no childs.
     */
    public View getLastChild(boolean visible);

    /**
     * Finds the View that corresponds to the given node. Returns null
     * if it is not found.
     */
    public View findNode(Node node);

    public boolean hasChildren();

    /**
     * Gets the vertical coordinate of the middle of the first line (which should
     * also be the middle of the collapse sign if there is one).
     */
    public int getFirstLineCenterPos();

    /**
     * Returns a help text to be displayed for this node. Returns null if no help is available.
     */
    public String getHelp();

    /**
     * Returns the label for this node, if it has any.
     */
    public String getLabel();
}
