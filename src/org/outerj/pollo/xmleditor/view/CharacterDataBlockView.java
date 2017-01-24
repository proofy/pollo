package org.outerj.pollo.xmleditor.view;

import org.outerj.pollo.xmleditor.NodeClickedEvent;
import org.outerj.pollo.xmleditor.XmlEditor;
import org.outerj.pollo.xmleditor.XmlTransferable;
import org.outerj.pollo.xmleditor.view.View;
import org.w3c.dom.CharacterData;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Node;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventTarget;

import javax.swing.*;
import java.awt.*;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragSource;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DnDConstants;
import java.awt.event.MouseEvent;


/**
 * Implements an abstract base class for 'block views' (block as opposed to inline) for
 * character data nodes (textnodes, commentnodes, cdata nodes).
 *
 * @author Bruno Dumon
 */
public abstract class CharacterDataBlockView extends BlockView
{
    protected int lineHeight = -1;
    protected int collapseSignTop = -1;
    protected int minimumHeight = -1;

    // text clipping related constants
    protected static final int NOT_CALCULATED    = -1;
    protected static final int NO_CLIPPING       = -2;

    // rendering related constants
    protected static final int LEFT_TEXT_MARGIN  = BORDER_WIDTH + COLLAPSE_SIGN_SIZE + COLLAPSESIGN_ICON_SPACING + ICON_SIZE + 2;
    protected static final int RIGHT_TEXT_MARGIN = 5;

    protected Node characterData; //CharacterData characterData;

    protected int numberOfLines;
    protected int [] lineInfo;
        // the lineInfo structure contains three int's for each line:
        // the offset (in the data array), length, and the clippinglength
    protected char [] data;

    /**
     * @param characterData should be a node of type CharacterData or ProcessingInstruction
     */
    public CharacterDataBlockView(View parentView, Node characterData, XmlEditor xmlEditor)
    {
        super(parentView, xmlEditor);
        this.characterData = characterData;

        // we keep a copy of the characterData's data in a char array, because thats easier and faster to work with
        if (characterData instanceof CharacterData)
            data = ((CharacterData)characterData).getData().toCharArray();
        else if (characterData instanceof ProcessingInstruction)
            data = ((ProcessingInstruction)characterData).getData().toCharArray();

        // register this view as an eventlistener for changes to the character data 
        ((EventTarget)characterData).addEventListener("DOMCharacterDataModified", this, false);
    }

    public abstract void drawFrame(Graphics2D g, int startH, int startV);

    public void paint(Graphics2D gr, int startH, int startV)
    {
        gr.setFont(xmlEditor.getCharacterDataFont());

        drawFrame(gr, startH, startV);

        gr.setColor(Color.black);

        // draw the collapse sign
        if (numberOfLines > 1)
            drawCollapseSign(gr, isCollapsed, startH + BORDER_WIDTH, startV + (lineHeight / 2) - HALF_COLLAPSE_SIGN_SIZE);

        // draw Icon
        int iconV = startV + 2;
        int iconH = startH + BORDER_WIDTH + COLLAPSE_SIGN_SIZE + COLLAPSESIGN_ICON_SPACING;
        getIcon().paintIcon(xmlEditor, gr, iconH, iconV);

        // draw the characterdata text
        int verticalOffset = startV + getHeader() + xmlEditor.getCharacterDataFontMetrics().getAscent();
        // if the view is collapsed and there is a header, draw no lines, otheriwse draw 1 line
        int doHowMuchLines = isCollapsed() ? (getHeader() != 0 ? 0 : 1) : numberOfLines;
        for (int i = 0; i < doHowMuchLines; i++)
        {
            if (lineInfo[(i*3)+2] == NOT_CALCULATED) // clipping not calculated
            {
                lineInfo[(i*3)+2] = clipText(data, lineInfo[(i*3)], lineInfo[(i*3)+1],
                        width - LEFT_TEXT_MARGIN - RIGHT_TEXT_MARGIN);
            }

            if (lineInfo[(i*3)+2] == NO_CLIPPING) // draw all text
            {
                gr.drawChars(data, lineInfo[(i*3)], lineInfo[(i*3)+1], startH + LEFT_TEXT_MARGIN, verticalOffset);
            }
            else // draw part of the text and three dots after it
            {
                gr.drawChars(data, lineInfo[(i*3)], lineInfo[(i*3)+2], startH + LEFT_TEXT_MARGIN, verticalOffset);
                gr.drawString("...", startH + LEFT_TEXT_MARGIN
                        + xmlEditor.getCharacterDataFontMetrics().charsWidth(data, lineInfo[(i*3)], lineInfo[(i*3)+2]), verticalOffset);
            }
            verticalOffset += lineHeight;
        }
    }

    /**
      Layouts this view (and it's children) to fit to
      the given width.

      */
    public void layout(int width)
    {
        // initialize variables
        lineHeight = xmlEditor.getCharacterDataFontMetrics().getHeight();
        minimumHeight = max(new int[] {lineHeight, ICON_SIZE + 2});
        collapseSignTop = (lineHeight / 2) - HALF_COLLAPSE_SIGN_SIZE;

        // fill the lineInfo structure
        numberOfLines = countNumberOfLines(data);
        lineInfo = new int[numberOfLines * 3];

        int pos = 0;
        for (int i = 0; i < numberOfLines; i++)
        {
            lineInfo[(i*3)] = pos; // first field: starting position of the line (in the data array)
            pos = searchNextLineBreak(data, pos);
            lineInfo[(i*3)+1] = pos - lineInfo[(i*3)]; // second field: length of the line (number of chars)
            lineInfo[(i*3)+2] = NOT_CALCULATED; // clipping length, will be calculated in paint method

            pos +=1;
        }

        this.width = width;
    }




    public void heightChanged(int amount)
    {
        if (parentView != null)
            parentView.heightChanged(amount);
        else
        {
            resetSize();
        }
    }

    public int widthChanged(int amount)
    {
        this.width = this.width + amount;

        // mark the clipping lenght as not calculated, will be recalculated as necessary
        // in the paint method
        for (int i = 0; i < numberOfLines; i++)
        {
            lineInfo[(i*3)+2] = NOT_CALCULATED;
        }

        return getHeight();
    }


    public void addChildView(View childView)
    {
        throw new RuntimeException("This is not supported on CharacterDataBlockView");
    }

    // mouse events

    public void mousePressed(MouseEvent e, int startH, int startV)
    {
        // if clicked on the collapse/expand button
        if (isCollapsable() && (e.getY() > startV + collapseSignTop) && (e.getY() < startV + collapseSignTop + 10)
                && (e.getX() > startH + BORDER_WIDTH) && (e.getX() < startH + BORDER_WIDTH + 10))
        {
            if (isCollapsed())
                expand();
            else
                collapse();
        }
        else if (e.getX() > startH)
        {
            NodeClickedEvent nce = new NodeClickedEvent(characterData, e);
            xmlEditor.fireNodeClickedEvent(nce);
            
            // make that the current element is indicated
            markAsSelected(startH, startV);
        }
    }


    public int getHeight()
    {
        if (!isCollapsed)
            return max(new int[] {getHeader() + (numberOfLines * lineHeight) + getFooter(), minimumHeight});
        else
            return minimumHeight;
    }


    public void handleEvent(Event e)
    {
        try
        {
            if (e.getType().equalsIgnoreCase("DOMCharacterDataModified"))
            {
                if (characterData instanceof CharacterData)
                    data = ((CharacterData)characterData).getData().toCharArray();
                else if (characterData instanceof ProcessingInstruction)
                    data = ((ProcessingInstruction)characterData).getData().toCharArray();
                int oldHeight = getHeight();
                layout(width);
                int newHeight = getHeight();
                applyHeightChange(newHeight - oldHeight);
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
        return characterData;
    }


    public void removeEventListeners()
    {
        ((EventTarget)characterData).removeEventListener("DOMCharacterDataModified", this, false);
    }


    public void dragGestureRecognized(DragGestureEvent event, int startH, int startV)
    {
        DocumentFragment documentFragment = xmlEditor.getXmlModel().getDocument().createDocumentFragment();
        documentFragment.appendChild(characterData.cloneNode(true));
        xmlEditor.setDraggingNode(characterData, event.getDragAction() == DnDConstants.ACTION_MOVE ? true : false);
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
        // dropping is not allowed on a characterData node
        xmlEditor.setDropData(xmlEditor.DROP_ACTION_NOT_ALLOWED, null);
        event.rejectDrag();
    }


    /**
     * Given a maximum allowed width (in pixels), this method calculates how much text
     * will fit on one line, assuming that if it doesn't fit, three dots are appended (...).
     */
    protected int clipText(char [] text, final int offset, final int length, int maxwidth)
    {
        if (xmlEditor.getCharacterDataFontMetrics().charsWidth(text, offset, length) < maxwidth)
            return NO_CLIPPING;

        final String dots = "...";
        int totalWidth = xmlEditor.getCharacterDataFontMetrics().stringWidth(dots);

        int i = 0;
        for (; i < length; i++)
        {
            totalWidth += xmlEditor.getCharacterDataFontMetrics().charWidth(text[offset + i]);
            if (totalWidth > maxwidth)
                break;
        }

        return i;
    }

    /**
     * Counts the number of lines in the text array.
     */
    public int countNumberOfLines(char [] text)
    {
        int linecount = 0;

        for (int i = 0; i < text.length; i++)
        {
            if (text[i] == '\n')
            {
                linecount++;
            }
        }
        // note: do +1 because after the last line there is no \n
        return linecount + 1;
    }

    /**
     * Finds the position of the last line break, or if the end of the array is reached,
     * returns that position.
     */
    public int searchNextLineBreak(char [] data, int offset)
    {
        int linebreak = offset;

        while ((linebreak < data.length) && data[linebreak] != '\n')
        {
            linebreak++;
        }

        return linebreak;
    }

    public boolean isCollapsable()
    {
        return numberOfLines > 1;
    }

    /**
     * Allows subclasses to specify a region below the text.
     */
    protected int getFooter()
    {
        return 0;
    }

    /**
     * Allows subclasses to specify a region above the text.
     */
    protected int getHeader()
    {
        return 0;
    }

    public int getFirstLineCenterPos()
    {
        return collapseSignTop + HALF_COLLAPSE_SIGN_SIZE;
    }

    public abstract Icon getIcon();
}
