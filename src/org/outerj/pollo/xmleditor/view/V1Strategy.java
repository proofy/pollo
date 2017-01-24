package org.outerj.pollo.xmleditor.view;

import java.awt.*;
import java.awt.event.MouseEvent;

public class V1Strategy implements ViewStrategy
{
    protected static final int END_MARKER_HEIGHT = 10;
    protected static final Color TEXT_BACKGROUND_COLOR = Color.white;
    protected static final Color CDATA_BACKGROUND_COLOR = Color.white;
    protected static final int COMMENT_FLAP_SIZE = 6;
    protected static final Color COMMENT_BACKGROUND_COLOR = new Color(255, 249, 186); // some kind of yellow
    protected static final Color PI_BACKGROUND_COLOR = new Color(255, 180, 180);
    protected static final Color ENTITY_REFERENCE_BACKGROUND_COLOR = Color.white;

    public void paintElementSurroundings(Graphics2D gr, int startH, int startV, ElementBlockView view)
    {
        // make the shape
        if (view.hasChildren())
        {
            view.elementShape = getElementShape(gr, startH, startV, view);
        }
        else
        {
            // just draw a rectangle
            view.elementShape = new Rectangle(startH, startV, view.width, view.titleHeight);
        }

        gr.setColor(view.elementSpec.backgroundColor);
        gr.fill(view.elementShape);

        gr.setColor(Color.black);
        if (view.xmlEditor.getSelectedNode() == view.element)
            gr.setStroke(BlockView.STROKE_HEAVY);
        else
            gr.setStroke(BlockView.STROKE_LIGHT);

        gr.draw(view.elementShape);

        gr.setStroke(BlockView.STROKE_LIGHT);
    }

    protected Shape getElementShape(Graphics2D g, int startH, int startV, ElementBlockView view)
    {
        // because of a clipping bug in Java (which is probably caused by a coordinate-size limitation
        // of the underlying windowing systems), java cannot correctly clip large shapes, therefore
        // we'll clip the largest parts ourselves.

        int clipStartVertical = (int)g.getClipBounds().getY();
        int clipEndVertical = clipStartVertical + (int)g.getClipBounds().getHeight();

        final int margin = 4; // a margin to account for the border widths

        // case 1: only the middle part is visible
        if (!view.isCollapsed() && (startV + view.titleHeight + margin < clipStartVertical) && (startV + view.titleHeight + view.getContentHeight() - margin > clipEndVertical))
        {
            return new Rectangle(startH, clipStartVertical - margin , view.BORDER_WIDTH, clipEndVertical - clipStartVertical + margin + margin);
        }
        // the top and the middle parts are visible, bottom part is not
        else if (!view.isCollapsed() && startV + margin > clipStartVertical && (startV + view.titleHeight + view.getContentHeight() - margin > clipEndVertical))
        {
            Polygon poly = new Polygon();
            // starting at the top left point
            poly.addPoint(startH, startV);
            poly.addPoint(startH + view.width, startV);
            poly.addPoint(startH + view.width, startV + view.titleHeight);
            poly.addPoint(startH + view.BORDER_WIDTH, startV + view.titleHeight);
            poly.addPoint(startH + view.BORDER_WIDTH, clipEndVertical + margin);
            poly.addPoint(startH, clipEndVertical + margin);
            return poly;
        }
        // the middle and the bottom parts are visible, the top part is not
        else if (!view.isCollapsed() && (startV + view.titleHeight + margin < clipStartVertical) && (startV + view.titleHeight + margin < clipStartVertical))
        {
            Polygon poly = new Polygon();
            // starting at the top left point
            poly.addPoint(startH, clipStartVertical - margin);
            poly.addPoint(startH + view.BORDER_WIDTH, clipStartVertical - margin);
            poly.addPoint(startH + view.BORDER_WIDTH, startV + view.titleHeight + view.getContentHeight());
            poly.addPoint(startH + view.BORDER_WIDTH + 4, startV + view.titleHeight + view.getContentHeight());
            poly.addPoint(startH, startV + view.titleHeight + view.getContentHeight() + END_MARKER_HEIGHT);
            return poly;
        }
        // all other cases: return the whole shape. If the java clipping bug would eventually be solved, this
        // is the only part we need to keep.
        else
        {
            Polygon poly = new Polygon();
            // starting at the top left point
            poly.addPoint(startH, startV);
            poly.addPoint(startH + view.width, startV);
            poly.addPoint(startH + view.width, startV + view.titleHeight);
            if (!view.isCollapsed())
            {
                poly.addPoint(startH + view.BORDER_WIDTH, startV + view.titleHeight);
                poly.addPoint(startH + view.BORDER_WIDTH, startV + view.titleHeight + view.getContentHeight());
                poly.addPoint(startH + view.BORDER_WIDTH + 4, startV + view.titleHeight + view.getContentHeight());
                poly.addPoint(startH, startV + view.titleHeight + view.getContentHeight() + END_MARKER_HEIGHT);
            }
            else
            {
                poly.addPoint(startH + view.BORDER_WIDTH + 4, startV + view.titleHeight);
                poly.addPoint(startH, startV + view.titleHeight + END_MARKER_HEIGHT);
            }
            return poly;
        }
    }

    public int getFooterHeight()
    {
        return END_MARKER_HEIGHT;
    }

    public int getVerticalSpacing()
    {
        return 5;
    }

    public int getDragSensitivityArea()
    {
        // irrelevant here, because getVerticalSpacing() != 0
        return 0;
    }

    public boolean selectsElement(MouseEvent e, int startH, int startV, ElementBlockView view)
    {
        return /* title */ (e.getY() > startV && e.getY() < startV + view.titleHeight
              && e.getX() > startH + view.BORDER_WIDTH)
               || (/* vertical bar */ !view.isCollapsed()
               && e.getY() > startV && e.getY() < startV + view.titleHeight + view.getContentHeight() + END_MARKER_HEIGHT
               && e.getX() >= startH && e.getX() <= startH + view.BORDER_WIDTH);
    }

    public void drawTextFrame(Graphics2D g, int startH, int startV, TextView view)
    {
        Rectangle frame = new Rectangle(startH, startV, view.width, view.getHeight());

        g.setColor(TEXT_BACKGROUND_COLOR);
        g.fill(frame);

        g.setColor(Color.black);
        if (view.xmlEditor.getSelectedNode() == view.characterData)
            g.setStroke(BlockView.STROKE_HEAVY);
        else
            g.setStroke(BlockView.STROKE_LIGHT);

        g.draw(frame);
        g.setStroke(BlockView.STROKE_LIGHT);
    }

    public void drawCDataFrame(Graphics2D gr, int startH, int startV, CDataView view)
    {
        Rectangle frame = new Rectangle(startH, startV, view.width, view.getHeight());

        gr.setColor(CDATA_BACKGROUND_COLOR);
        gr.fill(frame);

        gr.setColor(new Color(68, 68, 255)); // blue border, to distinguish from text nodes.

        if (view.xmlEditor.getSelectedNode() == view.characterData)
            gr.setStroke(BlockView.STROKE_HEAVY);
        else
            gr.setStroke(BlockView.STROKE_LIGHT);

        gr.draw(frame);
        gr.setStroke(BlockView.STROKE_LIGHT);
        gr.setColor(Color.black);
    }

    public void drawCommentFrame(Graphics2D gr, int startH, int startV, CommentView view)
    {
        Polygon frame = new Polygon();
        frame.addPoint(startH, startV);
        frame.addPoint(startH + view.width - COMMENT_FLAP_SIZE, startV);
        frame.addPoint(startH + view.width, startV + COMMENT_FLAP_SIZE);
        frame.addPoint(startH + view.width, startV + view.getHeight());
        frame.addPoint(startH, startV + view.getHeight());

        gr.setColor(COMMENT_BACKGROUND_COLOR);
        gr.fill(frame);

        gr.setColor(Color.black);

        if (view.xmlEditor.getSelectedNode() == view.characterData)
            gr.setStroke(BlockView.STROKE_HEAVY);
        else
            gr.setStroke(BlockView.STROKE_LIGHT);

        gr.draw(frame);
        gr.drawLine(startH + view.width - COMMENT_FLAP_SIZE, startV, startH + view.width - COMMENT_FLAP_SIZE, startV + COMMENT_FLAP_SIZE);
        gr.drawLine(startH + view.width - COMMENT_FLAP_SIZE, startV + COMMENT_FLAP_SIZE, startH + view.width, startV + COMMENT_FLAP_SIZE);

        gr.setStroke(BlockView.STROKE_LIGHT);
    }

    public void drawPIFrame(Graphics2D gr, int startH, int startV, PIView view)
    {
        Polygon frame = new Polygon();
        frame.addPoint(startH, startV + 2);
        frame.addPoint(startH + 2, startV);
        frame.addPoint(startH + view.width - 2, startV);
        frame.addPoint(startH + view.width, startV + 2);
        frame.addPoint(startH + view.width, startV + view.getHeight() - 2);
        frame.addPoint(startH + view.width - 2, startV + view.getHeight());
        frame.addPoint(startH + 2, startV + view.getHeight());
        frame.addPoint(startH, startV + view.getHeight() - 2);

        gr.setColor(PI_BACKGROUND_COLOR);
        gr.fill(frame);

        gr.setColor(Color.black);
        if (view.xmlEditor.getSelectedNode() == view.characterData)
            gr.setStroke(BlockView.STROKE_HEAVY);
        else
            gr.setStroke(BlockView.STROKE_LIGHT);

        gr.draw(frame);
        gr.setStroke(BlockView.STROKE_LIGHT);

        // draw the PI target
        gr.drawString(view.title, startH + view.LEFT_TEXT_MARGIN, startV + view.xmlEditor.getCharacterDataFontMetrics().getAscent());

        if (!view.isCollapsed())
        {
            int linepos = startV + view.xmlEditor.getCharacterDataFontMetrics().getHeight();
            gr.drawLine(startH, linepos, startH + view.width, linepos);
        }
    }

    public int getPIFooter()
    {
        return 3;
    }

    public void drawEntityReferenceFrame(Graphics2D gr, int startH, int startV, EntityReferenceView view)
    {
        Rectangle frame = new Rectangle(startH, startV, view.width, view.getHeight());

        gr.setColor(ENTITY_REFERENCE_BACKGROUND_COLOR);
        gr.fill(frame);

        gr.setColor(Color.black);
        if (view.xmlEditor.getSelectedNode() == view.entityReference)
            gr.setStroke(BlockView.STROKE_HEAVY);
        else
            gr.setStroke(BlockView.STROKE_LIGHT);

        gr.draw(frame);
        gr.setStroke(BlockView.STROKE_LIGHT);
    }

    public void drawDocumentFrame(Graphics2D gr, int startH, int startV, DocumentBlockView view)
    {
        if (view.xmlEditor.getSelectedNode() == view.document)
        {
            Rectangle surroundingRect = new Rectangle(startH, startV, startH + view.width, startV + view.getHeight());
            gr.setColor(Color.black);
            gr.setStroke(BlockView.STROKE_HEAVY);
            gr.draw(surroundingRect);
        }

        gr.setStroke(BlockView.STROKE_LIGHT);
    }
}
