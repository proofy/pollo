package org.outerj.pollo.xmleditor.view;

import org.outerj.pollo.xmleditor.view.View;
import org.outerj.pollo.xmleditor.view.ElementBlockView;
import org.outerj.pollo.xmleditor.XmlEditor;

import java.awt.*;
import java.awt.event.MouseEvent;

public class V2Strategy implements ViewStrategy
{
    protected static final Color SELECTED_BACKGROUND_COLOR = new Color(255, 221, 118);
    protected final XmlEditor xmlEditor;

    public V2Strategy(XmlEditor xmlEditor)
    {
        this.xmlEditor = xmlEditor;
    }

    public void paintElementSurroundings(Graphics2D gr, int startH, int startV, ElementBlockView view)
    {
        gr.setColor(Color.black);

        if (view.xmlEditor.getSelectedNode() == view.element)
        {
            int skipH = view.BORDER_WIDTH + view.COLLAPSE_SIGN_SIZE + view.COLLAPSESIGN_ICON_SPACING - 1;
            gr.setColor(SELECTED_BACKGROUND_COLOR);
            gr.fillRect(startH + skipH, startV, view.width - skipH, view.titleHeight);
            gr.setColor(Color.black);
            if (xmlEditor.hasFocus())
            {
                gr.drawRect(startH + skipH, startV, view.width - skipH, view.titleHeight);
            }
        }

        if (!view.isCollapsed() && view.hasChildren())
        {
            int totalChildrenHeight = 0;

            for (int i = 0; i < view.childViewList.size() - 1; i++)
            {
                View childView = (View)view.childViewList.get(i);
                totalChildrenHeight += childView.getHeight();
            }

            View lastChildView = (View)view.childViewList.get(view.childViewList.size() - 1);
            totalChildrenHeight += lastChildView.getFirstLineCenterPos();

            int left = startH + view.getLeftMargin() + view.BORDER_WIDTH + view.HALF_COLLAPSE_SIGN_SIZE;
            gr.drawLine(left, startV + view.collapseSignTop + view.COLLAPSE_SIGN_SIZE,
                    left, startV + view.getHeaderHeight() + totalChildrenHeight);
        }

        if (view.hasChildren())
        {
            int startX = startH + view.BORDER_WIDTH + view.COLLAPSE_SIGN_SIZE;
            int endX = startX + view.COLLAPSESIGN_ICON_SPACING - 1;
            int y = startV + view.collapseSignTop + view.HALF_COLLAPSE_SIGN_SIZE;
            gr.drawLine(startX, y, endX, y);
        }
        else
        {
            int startX = startH + view.BORDER_WIDTH + view.HALF_COLLAPSE_SIGN_SIZE;
            int endX = startX + view.HALF_COLLAPSE_SIGN_SIZE + view.COLLAPSESIGN_ICON_SPACING - 1;
            int y = startV + view.collapseSignTop + view.HALF_COLLAPSE_SIGN_SIZE;
            gr.drawLine(startX, y, endX, y);
        }
    }

    public int getFooterHeight()
    {
        return 0;
    }

    public int getVerticalSpacing()
    {
        return 0;
    }

    public int getDragSensitivityArea()
    {
        return 3;
    }

    public boolean selectsElement(MouseEvent e, int startH, int startV, ElementBlockView view)
    {
         return (e.getY() > startV && e.getY() < startV + view.titleHeight
               && e.getX() > startH + view.BORDER_WIDTH);
    }

    public void drawTextFrame(Graphics2D gr, int startH, int startV, TextView view)
    {
        drawBlockViewFrame(gr, startH, startV, view);
    }

    public void drawCDataFrame(Graphics2D gr, int startH, int startV, CDataView view)
    {
        drawBlockViewFrame(gr, startH, startV, view);
    }

    public void drawCommentFrame(Graphics2D gr, int startH, int startV, CommentView view)
    {
        drawBlockViewFrame(gr, startH, startV, view);
    }

    public void drawPIFrame(Graphics2D gr, int startH, int startV, PIView view)
    {
        drawBlockViewFrame(gr, startH, startV, view);

        // draw the PI target
        gr.setColor(Color.black);
        gr.drawString(view.title, startH + view.LEFT_TEXT_MARGIN, startV + view.xmlEditor.getCharacterDataFontMetrics().getAscent());
    }

    protected void drawBlockViewFrame(Graphics2D gr, int startH, int startV, CharacterDataBlockView view)
    {
        if (view.xmlEditor.getSelectedNode() == view.characterData)
        {
            drawSelectionRectangle(gr, startH, startV, view);
        }

        gr.setColor(Color.black);

        // draw little horizontal line
        int y = startV + view.collapseSignTop + view.HALF_COLLAPSE_SIGN_SIZE;
        if (view.numberOfLines > 1)
        {
            // to the right of the collapse icon
            int startX = startH + view.BORDER_WIDTH + view.COLLAPSE_SIGN_SIZE;
            int endX = startX + view.COLLAPSESIGN_ICON_SPACING - 1;
            gr.drawLine(startX, y, endX, y);
        }
        else
        {
            // starting from the parent's vertical line
            int startX = startH + view.BORDER_WIDTH + view.HALF_COLLAPSE_SIGN_SIZE;
            int endX = startX + view.HALF_COLLAPSE_SIGN_SIZE + view.COLLAPSESIGN_ICON_SPACING - 1;
            gr.drawLine(startX, y, endX, y);
        }
    }

    protected void drawSelectionRectangle(Graphics2D gr, int startH, int startV, BlockView view)
    {
        int skipH = view.BORDER_WIDTH + view.COLLAPSE_SIGN_SIZE + view.COLLAPSESIGN_ICON_SPACING - 1;
        Rectangle frame = new Rectangle(startH + skipH, startV, view.width - skipH, view.getHeight());
        gr.setColor(SELECTED_BACKGROUND_COLOR);
        gr.fill(frame);

        if (xmlEditor.hasFocus())
        {
            gr.setColor(Color.black);
            gr.draw(frame);
        }
    }

    public int getPIFooter()
    {
        return 0;
    }

    public void drawEntityReferenceFrame(Graphics2D gr, int startH, int startV, EntityReferenceView view)
    {
        if (view.xmlEditor.getSelectedNode() == view.entityReference)
        {
            drawSelectionRectangle(gr, startH, startV, view);
        }

        // only draw a little horizontal line
        gr.setColor(Color.black);
        int startX = startH + view.BORDER_WIDTH + view.HALF_COLLAPSE_SIGN_SIZE;
        int endX = startX + view.HALF_COLLAPSE_SIGN_SIZE + view.COLLAPSESIGN_ICON_SPACING - 1;
        int y = startV + view.getHeight() / 2;
        gr.drawLine(startX, y, endX, y);
    }

    public void drawDocumentFrame(Graphics2D gr, int startH, int startV, DocumentBlockView view)
    {
        gr.setColor(Color.black);
        if (view.xmlEditor.getSelectedNode() == view.document && xmlEditor.hasFocus())
        {
            Rectangle surroundingRect = new Rectangle(startH, startV, startH + view.width, startV + view.getHeight());
            gr.draw(surroundingRect);
        }

        if (view.hasChildren())
        {
            int totalChildrenHeight = 0;

            for (int i = 0; i < view.childViewList.size() - 1; i++)
            {
                View childView = (View)view.childViewList.get(i);
                totalChildrenHeight += childView.getHeight();
            }

            View lastChildView = (View)view.childViewList.get(view.childViewList.size() - 1);
            totalChildrenHeight += lastChildView.getFirstLineCenterPos();

            View firstChildView = (View)view.childViewList.get(0);

            int left = startH + view.getLeftMargin() + view.BORDER_WIDTH + view.HALF_COLLAPSE_SIGN_SIZE;
            int top = startV + view.getHeaderHeight() + firstChildView.getFirstLineCenterPos();
            gr.drawLine(left, top, left, startV + view.getHeaderHeight() + totalChildrenHeight);
        }
    }
}
