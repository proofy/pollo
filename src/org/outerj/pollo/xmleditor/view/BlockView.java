package org.outerj.pollo.xmleditor.view;

import org.outerj.pollo.xmleditor.XmlEditor;
import org.outerj.pollo.xmleditor.view.View;
import org.w3c.dom.Node;

import java.awt.*;


/**
 * This class contains functionality that's shared between block views
 * (such as ElementBlockView and CommentView).
 *
 * Many of the methods here are defined in the View interface, so look
 * there for documentation.
 */
public abstract class BlockView implements View
{
    protected boolean isCollapsed = false;
    protected View parentView;
    protected XmlEditor xmlEditor;
    protected int width;

    public static final BasicStroke STROKE_LIGHT = new BasicStroke(1f);
    public static final BasicStroke STROKE_HEAVY = new BasicStroke(3f);
    public static final int COLLAPSE_SIGN_SIZE = 8;
    public static final int HALF_COLLAPSE_SIGN_SIZE = COLLAPSE_SIGN_SIZE / 2;
    public static final int ICON_SIZE = 16;
    public static final int COLLAPSESIGN_ICON_SPACING = 3;
    public static final int BORDER_WIDTH = 6;

    public BlockView(View parentView, XmlEditor xmlEditor)
    {
        this.parentView = parentView;
        this.xmlEditor = xmlEditor;
    }

    public void drawCollapseSign(Graphics g, boolean isCollapsed, int startH, int startV)
    {
        g.setColor(xmlEditor.getBackground());
        g.fillRect(startH, startV, COLLAPSE_SIGN_SIZE, COLLAPSE_SIGN_SIZE);
        g.setColor(Color.black);
        g.drawRect(startH, startV, COLLAPSE_SIGN_SIZE, COLLAPSE_SIGN_SIZE);
        if (isCollapsed)
        {
            // draw '+' sign
            g.drawLine(startH + 2, startV + 4, startH + 6, startV + 4);
            g.drawLine(startH + 4, startV + 2, startH + 4, startV + 6);
        }
        else
        {
            // draw '-' sign
            g.drawLine(startH + 2, startV + 4, startH + 6, startV + 4);
        }
    }

    /**
     * Changes the height of the editing widget
     */
    protected void resetSize()
    {
        xmlEditor.setSize(new Dimension(xmlEditor.getWidth(), getHeight()));
        xmlEditor.repaint(xmlEditor.getVisibleRect());
    }

    public boolean needsRepainting(int startV, int clipStartVertical, int clipEndVertical)
    {
        int absStartVertical = startV;
        int absEndVertical = absStartVertical + getHeight();


        if ((absStartVertical >= clipStartVertical && absEndVertical <= clipEndVertical)
                || (absStartVertical <= clipStartVertical && absEndVertical >= clipStartVertical)
                || (absStartVertical <= clipEndVertical && absEndVertical >= clipEndVertical)
                || (absStartVertical <= clipStartVertical && absEndVertical >= clipEndVertical))
            return true;
        else
            return false;
    }

    /**
     * Does parentView.heightChanged if the parentView is not null, otherwise
     * resizes the containing JComponent (xmlEditor).
     */
    public void applyHeightChange(int amount)
    {
        if (parentView != null)
            parentView.heightChanged(amount);
        else
        {
            resetSize();
        }
    }

    /**
     * Marks this node as selected. This involves putting a thick
     * border around it, and letting the XmlEditor know that this view
     * is now the selected view. As a consequence, the XmlEditor will
     * then generated NodeSelected events.
     */
    public void markAsSelected(int startH, int startV)
    {
        xmlEditor.setSelectedNode(getNode(), this);

        Rectangle redrawRect = new Rectangle(startH-2, startV-2, width+4, getHeight()+4);
        if (xmlEditor.getSelectedViewRect() != null)
            redrawRect.add(xmlEditor.getSelectedViewRect());
        xmlEditor.setSelectedViewRect(new Rectangle(startH-2, startV-2, width+4, getHeight()+4));
        Rectangle visibleRect = xmlEditor.getVisibleRect();
        redrawRect.y = visibleRect.y > redrawRect.y ? visibleRect.y : redrawRect.y;
        redrawRect.height = visibleRect.y + visibleRect.height - redrawRect.y;

        xmlEditor.repaint(redrawRect);
    }

    /**
     * Indicates if this view is collapsed.
     */
    public boolean isCollapsed()
    {
        return isCollapsed;
    }

    /**
     * Indicates if this view supports collapsing (and expanding).
     * Default implementation always returns false.
     */
    public boolean isCollapsable()
    {
        return false;
    }

    /**
     * Collapses this view. If the view is not hidden inside another collapsed
     * view, then the height change caused by this collapsing will be propagated upwards
     * in the view tree. Otherwise, {@link #invalidateHeight} is called.
     */
    public void collapse()
    {
        if (!isCollapsable())
            return;

        if (isCollapsed())
            return;

        boolean trackHeight = (parentView == null) ||
            (parentView != null && parentView.isCollapsed() != true);
        int oldHeight = 0, newHeight = 0;

        if (trackHeight) oldHeight = this.getHeight();
        isCollapsed = true;
        if (trackHeight) newHeight = this.getHeight();

        if (trackHeight)
            applyHeightChange(newHeight - oldHeight);

        invalidateHeight();
    }

    /**
     * Same as for {@link #collapse}.
     */
    public void expand()
    {
        if (!isCollapsable())
            return;

        if (!isCollapsed())
            return;

        boolean trackHeight = (parentView == null) ||
            (parentView != null && parentView.isCollapsed() != true);
        int oldHeight = 0, newHeight = 0;

        if (trackHeight) oldHeight = this.getHeight();
        isCollapsed = false;
        if (trackHeight) newHeight = this.getHeight();

        if (trackHeight)
        {
            applyHeightChange(newHeight - oldHeight);
        }

        invalidateHeight();
    }

    /**
     * This implementation only collapses the current node, thus doesn't
     * work recursively. Subclasses must overide this behaviour if needed.
     */
    public void collapseAll()
    {
        collapse();
    }

    /**
     * This implementation only expands the current node, thus doesn't
     * work recursively. Subclasses must overide this behaviour if needed.
     */
    public void expandAll()
    {
        expand();
    }

    /**
     * This sets a flag that the height of this view is to be recalculated
     * the next time it is requested.
     * Default implementation is empty.
     */
    public void invalidateHeight()
    {
    }

    /**
     * Implementation of the View inteface.
     */
    public int getVerticalPosition()
    {
        if (parentView == null)
        {
            // we have reached the root
            return 0;
        }
        else
        {
            return parentView.getVerticalPosition(this);
        }
    }

    /**
     * Implementation of the View interface. Subclasses should overide
     * this method if applicable.
     */
    public int getVerticalPosition(View wantedChildView)
    {
        throw new RuntimeException("The method getVerticalPosition(View) is not supported by this view type.");
    }

    /**
     * Implementation of the View interface.
     */
    public int getHorizontalPosition()
    {
        if (parentView == null)
        {
            return 0;
        }
        else
        {
            return parentView.getHorizontalPosition(this);
        }
    }

    /**
     * Implementation of the View interface. Subclasses should overide
     * this method if applicable.
     */
    public int getHorizontalPosition(View wantedChildView)
    {
        throw new RuntimeException("The method getHorizontalPosition(View) is not supported by this view type.");
    }


    public View getNextSibling()
    {
        if (parentView != null)
        {
            View view = parentView.getNextSibling(this);
            if (view != null)
                return view;
            if (view == null && parentView != null)
                return parentView.getNextSibling();
        }
        return null;
    }

    public View getNextSibling(View childView)
    {
        throw new RuntimeException("The method getNextSibling(View) is not supported by this view type.");
    }

    /**
     * Returns the next View node. In this implementation it returns the next sibling,
     * or if it hasn't got one, it returns the next sibling of its parent.
     */
    public View getNext(boolean visible)
    {
        View view = getNextSibling();
        if (view == null)
        {
            if (parentView != null)
            {
                View view2 = parentView.getNextSibling();
                if (view2 != null)
                    return view2;
                else
                    return this;
            }
            return this;
        }
        else
        {
            return view;
        }
    }

    /**
     * The difference between this method and getNext() is that this
     * one is not overidden in ElementBlockView.
     *
     * This method is used when a node is removed, so it may
     * not return itself.
     */
    public View getNextButNotChild()
    {
        View view = getNextSibling();
        if (view == null)
        {
            if (parentView != null)
            {
                View view2 = parentView.getNextSibling();
                if (view2 != null)
                    return view2;
                else
                    return parentView;
            }
            return null;
        }
        else
        {
            return view;
        }
    }


    public View getPreviousSibling()
    {
        if (parentView != null)
        {
            View view = parentView.getPreviousSibling(this);
            if (view != null)
                return view;
        }
        return null;
    }

    public View getPreviousSibling(View childView)
    {
        throw new RuntimeException("The method getPreviousSibling(View) is not supported by this view type.");
    }

    /**
     * Returns the previous View node. In this implementation it returns the
     * previous sibling, or if it hasn't got one, it returns the previous
     * sibling of its parent.
     */
    public View getPrevious(boolean visible)
    {
        View view = getPreviousSibling();
        if (view == null)
        {
            if (parentView != null)
                return parentView;
            return this;
        }
        return view.getLastChild(visible);
    }

    public View getParent()
    {
        return parentView;
    }

    /**
     * Implementation of the View interface.
     */
    public View getLastChild(boolean visible)
    {
        return this;
    }

    /**
     * Implementation of the View interface.
     */
    public View findNode(Node node)
    {
        if (node == getNode())
            return this;
        else
            return null;
    }

    /**
     * Implementation of the View interface.
     */
    public void assureVisibility(boolean recursive)
    {
        if (recursive)
            expand();

        if (parentView != null)
            parentView.assureVisibility(true);
    }

    public int getWidth()
    {
        return width;
    }

    public boolean hasChildren()
    {
        return false;
    }

    public String getHelp()
    {
        return null;
    }

    public String getLabel()
    {
        return null;
    }

    protected int max(int[] values)
    {
        int largestvalue = values[0];
        for (int i = 1; i < values.length; i++)
            if (values[i] > largestvalue)
                largestvalue = values[i];
        return largestvalue;
    }
}
