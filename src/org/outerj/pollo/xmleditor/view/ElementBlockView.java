package org.outerj.pollo.xmleditor.view;

import org.outerj.pollo.xmleditor.NodeClickedEvent;
import org.outerj.pollo.xmleditor.XmlEditor;
import org.outerj.pollo.xmleditor.XmlTransferable;
import org.outerj.pollo.xmleditor.view.View;
import org.outerj.pollo.xmleditor.displayspec.AttributeSpec;
import org.outerj.pollo.xmleditor.displayspec.ElementSpec;
import org.outerj.pollo.xmleditor.displayspec.IDisplaySpecification;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventTarget;
import org.w3c.dom.events.MutationEvent;

import java.awt.*;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragSource;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;


public class ElementBlockView extends ChildrenBlockView
{
    protected ElementSpec elementSpec;
    protected String elementName;
    protected int titleHeight = 10;
    protected int collapseSignTop;
    protected final Element element;
    protected Shape elementShape;
    protected IDisplaySpecification displaySpec;
    protected final ViewStrategy viewStrategy;

    // everything for the attributes
    /** To store layout information for the attributes defined in the display specification */
    protected ArrayList attrViewInfoList = new ArrayList();
    /** To store layout information for the attributes not defined in the display specification */
    protected ArrayList extraAttrViewInfoList;
    protected boolean attributeLayoutUptodate = false;

    // some rendering constants
    protected static final int SPACING_HORIZONTAL = 7;
    protected static final int EMPTY_CONTENT_HEIGHT = 10;
    protected static final int COLLAPS_ICON_WIDTH = 15;
    protected static final int ATTR_SPACING = 9;
    protected static final int ATTR_NAME_VALUE_SPACING = 2;

    public ElementBlockView(View parentView, Element element, XmlEditor xmlEditor, ViewStrategy viewStrategy)
    {
        super(parentView, element, xmlEditor);
        this.element = element;
        this.viewStrategy = viewStrategy;

        // register this view as an eventlistener for changes to the element
        ((EventTarget)element).addEventListener("DOMAttrModified", this, false);

        // get the ElementSpec from the DisplaySpec
        displaySpec = xmlEditor.getDisplaySpec();
        this.elementSpec = displaySpec.getElementSpec(element);

        // create instances of AttrViewInfo for all the attributes we need to show
        int numberOfAttrs = elementSpec.attributesToShow.size();
        AttrViewInfo attrViewInfo;
        for (int i = 0; i < numberOfAttrs; i++)
        {
            attrViewInfo = new AttrViewInfo();
            attrViewInfo.attributeSpec = (AttributeSpec)elementSpec.attributesToShow.get(i);
            attrViewInfoList.add(attrViewInfo);
        }

        // make the string for the element name
        if (elementSpec.label != null)
        {
            this.elementName = elementSpec.label;
        }
        else
        {
            String prefix = element.getPrefix();
            String qname = element.getLocalName();
            if (prefix != null && prefix.length() > 0)
                qname = prefix + ":" + qname;
            this.elementName = qname;
        }
    }

    public void paint(Graphics2D gr, int startH, int startV)
    {
        viewStrategy.paintElementSurroundings(gr, startH, startV, this);

        if (hasChildren())
        {
            // draw + or - sign
            drawCollapseSign(gr, isCollapsed(), startH + BORDER_WIDTH, startV + collapseSignTop);

            super.paint(gr, startH, startV);
        }

        // draw icon
        int iconV = startV + (titleHeight / 2) - (ICON_SIZE / 2);
        int iconH = startH + BORDER_WIDTH + COLLAPSE_SIGN_SIZE + COLLAPSESIGN_ICON_SPACING;
        elementSpec.icon.paintIcon(xmlEditor, gr, iconH, iconV);

        int textHeight = max(new int[] {xmlEditor.getElementNameFontMetrics().getAscent(),
                xmlEditor.getAttributeNameFontMetrics().getAscent(),
                xmlEditor.getAttributeValueFontMetrics().getAscent()});

        int baseline = startV + (titleHeight / 2) + (textHeight / 2);

        // draw the element name
        gr.setFont(xmlEditor.getElementNameFont());
        int elementNameH = iconH + ICON_SIZE + 1;
        gr.setColor(elementSpec.textColor);
        gr.drawString(elementName, elementNameH, baseline);

        drawAttributes(gr, attrViewInfoList, startH, baseline);
        if (extraAttrViewInfoList != null)
            drawAttributes(gr, extraAttrViewInfoList, startH, baseline);
    }

    protected void drawAttributes(Graphics2D g, ArrayList myAttrViewInfoList, int startH, int baseline)
    {
        if (!attributeLayoutUptodate)
            layoutAttributes();

        // draw the attributes
        // the position of the attribute names and values was already calculated in the
        // layoutAttributes method. Here they just need to be drawn, but it is also checked
        // if they fit on the screen, otherwise they are clipped and '...' is appended.
        AttrViewInfo attrViewInfo;
        int numberOfAttrs = myAttrViewInfoList.size();
        int remainingAttrSpace; // the space remaining for attributes
        int requiredSpace;
        g.setFont(xmlEditor.getAttributeValueFont());
        int dotsWidth = g.getFontMetrics().stringWidth("...");
        for (int i = 0; i < numberOfAttrs; i++)
        {
            attrViewInfo = (AttrViewInfo)myAttrViewInfoList.get(i);
            if (attrViewInfo.visible)
            {
                // attribute color
                Color textColor = attrViewInfo.attributeSpec != null ? attrViewInfo.attributeSpec.textColor : Color.black;
                g.setColor(textColor);

                // attribute name
                g.setFont(xmlEditor.getAttributeNameFont());
                remainingAttrSpace = width - attrViewInfo.namePos;
                requiredSpace = g.getFontMetrics().stringWidth(attrViewInfo.name) + dotsWidth;
                if (requiredSpace > remainingAttrSpace)
                {
                    int c = clipText(attrViewInfo.name, remainingAttrSpace, g.getFontMetrics(), dotsWidth);
                    g.drawString(attrViewInfo.name.substring(0, c) + "...", startH + attrViewInfo.namePos, baseline);
                    break;
                }
                else
                {
                    g.drawString(attrViewInfo.name, startH + attrViewInfo.namePos, baseline);
                }

                // attribute value
                g.setFont(xmlEditor.getAttributeValueFont());
                remainingAttrSpace = width - attrViewInfo.valuePos;
                requiredSpace = g.getFontMetrics().stringWidth(attrViewInfo.value) + dotsWidth;
                if (requiredSpace > (remainingAttrSpace - ATTR_SPACING))
                {
                    int c = clipText(attrViewInfo.value, remainingAttrSpace, g.getFontMetrics(), dotsWidth);
                    g.drawString(attrViewInfo.value.substring(0, c) + "...", startH + attrViewInfo.valuePos, baseline);
                    break;
                }
                else
                {
                    g.drawString(attrViewInfo.value, startH + attrViewInfo.valuePos, baseline);
                }
            }
        }
    }

    /**
     * Layouts this view (and it's children) to fit to the given width.
     */
    public void layout(int width)
    {
        // init
        this.titleHeight = max(new int[] {xmlEditor.getElementNameFontMetrics().getHeight(),
                xmlEditor.getAttributeNameFontMetrics().getHeight(),
                xmlEditor.getAttributeValueFontMetrics().getHeight(), ICON_SIZE})
                + 4;
        this.width = width;

        // mark that the attributes need layouting
        attributeLayoutUptodate = false;

        collapseSignTop = (titleHeight / 2) - 5;

        // layout the children of this view
        super.layout(width);
    }

    /**
     * This method calculates the positions off the attributes. Called during initial layout
     * or when attributes have changed/removed/added.
     */
    public void layoutAttributes()
    {
        // Before trying to figure out this code, you should know this:
        //   - normally it is defined in the display specification in which order
        //     the attributes must be drawn.
        //   - however, we also want to show the other (if any) attributes

        FontMetrics attrNameFontMetrics  = xmlEditor.getAttributeNameFontMetrics();
        FontMetrics attrValueFontMetrics = xmlEditor.getAttributeValueFontMetrics();

        NamedNodeMap allAttributes = element.getAttributes();
        boolean[] processed = new boolean[allAttributes.getLength()];

        int elementNameWidth = xmlEditor.getElementNameFontMetrics().stringWidth(elementName);
        int attrPos = BORDER_WIDTH + COLLAPSE_SIGN_SIZE + COLLAPSESIGN_ICON_SPACING + ICON_SIZE + elementNameWidth + ATTR_SPACING;
        AttrViewInfo attrViewInfo;
        Attr attr;

        // part 1: handle the attributes defined in the display specification
        int numberOfAttrs = attrViewInfoList.size();
        for (int i = 0; i < numberOfAttrs; i++)
        {
            attrViewInfo = (AttrViewInfo)attrViewInfoList.get(i);
            int pos = findNamePoint(allAttributes, attrViewInfo.attributeSpec.nsUri,
                    attrViewInfo.attributeSpec.localName);

            if (pos < 0)
            {
                attrViewInfo.visible = false;
            }
            else
            {
                attr = (Attr)allAttributes.item(pos);
                attrViewInfo.value = attr.getValue();
                if (attrViewInfo.name == null)
                {
                    if (attrViewInfo.attributeSpec.label != null)
                    {
                        attrViewInfo.name = attrViewInfo.attributeSpec.label + ":";
                    }
                    else
                    {
                        String prefix = attr.getPrefix();
                        String qname = attr.getLocalName();
                        if (prefix != null)
                            qname = prefix + ":" + qname;

                        qname += ":"; // this is the colon seperating name and value
                        attrViewInfo.name = qname;
                    }
                }
                attrViewInfo.namePos = attrPos;
                attrPos += attrNameFontMetrics.stringWidth(attrViewInfo.name);
                attrPos += ATTR_NAME_VALUE_SPACING;
                attrViewInfo.valuePos = attrPos;
                attrPos += attrValueFontMetrics.stringWidth(attrViewInfo.value);
                attrPos += ATTR_SPACING;
                attrViewInfo.visible = true;

                // mark this attribute as processed
                processed[pos] = true;
            }
        }

        // part 2: handle the other attributes
        if (extraAttrViewInfoList != null)
            extraAttrViewInfoList.clear();

        numberOfAttrs = allAttributes.getLength();
        for (int i = 0; i < numberOfAttrs; i++)
        {
            if (processed[i] == false)
            {
                attr = (Attr)allAttributes.item(i);
                AttrViewInfo extraAttrViewInfo = new AttrViewInfo();

                String prefix = attr.getPrefix();
                String qname = attr.getLocalName();
                if (prefix != null)
                    qname = prefix + ":" + qname;
                qname += ":"; // this is the colon seperating name and value
                extraAttrViewInfo.name = qname;


                extraAttrViewInfo.value = attr.getValue();

                extraAttrViewInfo.namePos = attrPos;
                attrPos += attrNameFontMetrics.stringWidth(extraAttrViewInfo.name);
                attrPos += ATTR_NAME_VALUE_SPACING;
                extraAttrViewInfo.valuePos = attrPos;
                attrPos += attrValueFontMetrics.stringWidth(extraAttrViewInfo.value);
                attrPos += ATTR_SPACING;

                extraAttrViewInfo.visible = true;
                if (extraAttrViewInfoList == null)
                    extraAttrViewInfoList = new ArrayList();
                extraAttrViewInfoList.add(extraAttrViewInfo);
            }
        }

        attributeLayoutUptodate = true;
    }

    /**
     * This method is more or less copied from the Xerces codebase.
     * (from the file NamedNodeMapImpl).
     */
    protected int findNamePoint(NamedNodeMap nodeMap, String namespaceURI, String name)
    {
        int numberOfNodes = nodeMap.getLength();
        for (int i = 0; i < numberOfNodes; i++) {
            Node a = nodeMap.item(i);
            String aNamespaceURI = a.getNamespaceURI();
            String aLocalName = a.getLocalName();
            if (namespaceURI == null) {
                if (aNamespaceURI == null
                        &&
                        (name.equals(aLocalName)
                         ||
                         (aLocalName == null && name.equals(a.getNodeName()))))
                    return i;
            } else {
                if (namespaceURI.equals(aNamespaceURI)
                        &&
                        name.equals(aLocalName))
                    return i;
            }
        }
        return -1;
    }


    public int widthChanged(int amount)
    {
        this.width = this.width + amount;

        return super.widthChanged(amount);
    }


    // mouse events

    public void mousePressed(MouseEvent e, int startH, int startV)
    {
        // if clicked on the collapse/expand button
        if ((e.getY() > startV + collapseSignTop) && (e.getY() < startV + collapseSignTop + 10)
                && (e.getX() > startH + BORDER_WIDTH) && (e.getX() < startH + BORDER_WIDTH + 10))
        {
            if (isCollapsed())
                expand();
            else
                collapse();
        }
        else if (viewStrategy.selectsElement(e, startH, startV, this))
        {
            NodeClickedEvent nce = new NodeClickedEvent(element, e);
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
            if (e.getType().equalsIgnoreCase("DOMAttrModified"))
            {
                MutationEvent me = (MutationEvent)e;
                if (me.getTarget() == element)
                {
                    e.stopPropagation();
                    attributeLayoutUptodate = false;
                    xmlEditor.repaint(xmlEditor.getVisibleRect());
                }
            }
            else
            {
                super.handleEvent(e);
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
        return element;
    }


    public void removeEventListeners()
    {
        ((EventTarget)element).removeEventListener("DOMAttrModified", this, false);

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
            if (parentView == null)
            {
                // the mainView may not be dragged
                return;
            }
            xmlEditor.setDraggingNode(element, event.getDragAction() == DnDConstants.ACTION_MOVE ? true : false);
            if(event.getDragAction()== DnDConstants.ACTION_COPY)
            {
                xmlEditor.getDragSource().startDrag(event, DragSource.DefaultCopyDrop,
                        new XmlTransferable(createDocumentFragment(element)), xmlEditor);
            }
            else
            {
                xmlEditor.getDragSource().startDrag(event, DragSource.DefaultMoveDrop,
                        new XmlTransferable(createDocumentFragment(element)), xmlEditor);
            }
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

            xmlEditor.setDropData(xmlEditor.DROP_ACTION_APPEND_CHILD, element);
            if (event.isDataFlavorSupported(XmlTransferable.xmlFlavor))
                event.acceptDrag(event.getDropAction());
            /* CommandTransferable -- deprecated.
            else if (event.isDataFlavorSupported(CommandTransferable.commandFlavor))
                event.acceptDrag(DnDConstants.ACTION_MOVE);
            */
            return;
        }
        else if (p.getX() > startH && p.getX() < (startH + width) ) // maybe it's between two elements
        {
            super.dragOver(event, startH, startV);
        }
        else
        {
            xmlEditor.setDropData(xmlEditor.DROP_ACTION_NOT_ALLOWED, null);
            event.rejectDrag();
        }
    }


    /**
     * Given a maximum allowed width (in pixels), this method calculates how much text
     * will fit on one line, assuming that if it doesn't fit, three dots are appended (...).
     */
    protected int clipText(String text, int maxwidth, FontMetrics fontMetrics, int dotsWidth)
    {
        int totalWidth = dotsWidth;

        int i = 0;
        int length = text.length();
        for (; i < length; i++)
        {
            totalWidth += fontMetrics.charWidth(text.charAt(i));
            if (totalWidth > maxwidth)
                break;
        }

        return i;
    }

    public int getHeaderHeight()
    {
        return titleHeight;
    }

    public int getLeftMargin()
    {
        return BORDER_WIDTH + SPACING_HORIZONTAL;
    }

    protected int getDragLineLeftMargin()
    {
        return getLeftMargin() + BORDER_WIDTH;
    }

    public int getFirstLineCenterPos()
    {
        return collapseSignTop + HALF_COLLAPSE_SIGN_SIZE;
    }

    public int getFooterHeight()
    {
        return viewStrategy.getFooterHeight();
    }

    protected int getVerticalSpacing()
    {
        return viewStrategy.getVerticalSpacing();
    }

    protected int getDragSensitivityArea()
    {
        return viewStrategy.getDragSensitivityArea();
    }

    public String getHelp()
    {
        return elementSpec.help;
    }

    public String getLabel()
    {
        return elementName;
    }
}
