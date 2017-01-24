package org.outerj.pollo.gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicSplitPaneUI;
import java.awt.*;

public class BorderLessSplitPane extends JSplitPane
{
    public BorderLessSplitPane(int newOrientation, Component newLeftComponent, Component newRightComponent)
    {
        super(newOrientation, newLeftComponent, newRightComponent);
    }

    public BorderLessSplitPane(int newOrientation)
    {
        super(newOrientation);
    }

    public void updateUI()
    {
        super.updateUI();
        BasicSplitPaneUI ui = (BasicSplitPaneUI) getUI();
        ui.getDivider().setBorder(new EmptyBorder(0, 0, 0, 0));
        setBorder(new EmptyBorder(0, 0, 0, 0));
    }
}
