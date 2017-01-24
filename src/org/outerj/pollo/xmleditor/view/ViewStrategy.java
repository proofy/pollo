package org.outerj.pollo.xmleditor.view;

import java.awt.*;
import java.awt.event.MouseEvent;

public interface ViewStrategy
{
    void paintElementSurroundings(Graphics2D gr, int startH, int startV, ElementBlockView view);

    int getFooterHeight();

    int getVerticalSpacing();

    int getDragSensitivityArea();

    /**
     * Returns true when the coordinates of the given MouseEvent select the element.
     */
    boolean selectsElement(MouseEvent e, int startH, int startV, ElementBlockView view);

    void drawTextFrame(Graphics2D gr, int startH, int startV, TextView view);

    void drawCDataFrame(Graphics2D gr, int startH, int startV, CDataView view);

    void drawCommentFrame(Graphics2D gr, int startH, int startV, CommentView view);

    void drawPIFrame(Graphics2D gr, int startH, int startV, PIView view);

    void drawDocumentFrame(Graphics2D gr, int startH, int startV, DocumentBlockView view);

    int getPIFooter();

    void drawEntityReferenceFrame(Graphics2D gr, int startH, int startV, EntityReferenceView view);
}
