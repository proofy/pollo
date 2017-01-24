package org.outerj.pollo.plaf;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.plaf.BorderUIResource;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicSplitPaneDivider;
import javax.swing.plaf.metal.MetalSplitPaneUI;

public class PolloSplitPaneUI extends MetalSplitPaneUI
{
    private static final Border border = new BorderUIResource.EmptyBorderUIResource(0, 0, 0, 0);
    public PolloSplitPaneUI ()
    {
    }

    public static ComponentUI createUI(JComponent x)
    {
        return new PolloSplitPaneUI();
    }

    public BasicSplitPaneDivider createDefaultDivider()
    {
        BasicSplitPaneDivider divider = new BasicSplitPaneDivider(this);
        divider.setBorder(border);
        return divider;
    }

}
