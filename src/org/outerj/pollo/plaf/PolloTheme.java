package org.outerj.pollo.plaf;

import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.FontUIResource;
import javax.swing.plaf.metal.DefaultMetalTheme;
import java.awt.*;

public class PolloTheme extends DefaultMetalTheme
{
    private final ColorUIResource primary1 = new ColorUIResource(102, 102, 153);
    private final ColorUIResource primary2 = new ColorUIResource(153, 153, 204);
    private final ColorUIResource primary3 = new ColorUIResource(245, 216, 153);

    private final ColorUIResource secondary1 = new ColorUIResource(102, 102, 102);
    private final ColorUIResource secondary2 = new ColorUIResource(153, 153, 153);
    private final ColorUIResource secondary3 = new ColorUIResource(212, 208, 200);

    public static final Color lightGrey = new Color(235, 235, 235);

    private FontUIResource controlFont = new FontUIResource("Dialog", Font.PLAIN, 12);
    private FontUIResource systemFont = new FontUIResource("Dialog", Font.PLAIN, 12);
    private FontUIResource userFont = new FontUIResource("Dialog", Font.PLAIN, 12);
    private FontUIResource smallFont = new FontUIResource("Dialog", Font.PLAIN, 10);

    public String getName()
    {
        return "Default Pollo Theme";
    }


    protected ColorUIResource getPrimary1()
    {
        return primary1;
    }

    protected ColorUIResource getPrimary2()
    {
        return primary2;
    }

    protected ColorUIResource getPrimary3()
    {
        return primary3;
    }


    protected ColorUIResource getSecondary1()
    {
        return secondary1;
    }

    protected ColorUIResource getSecondary2()
    {
        return secondary2;
    }

    protected ColorUIResource getSecondary3()
    {
        return secondary3;
    }

    public FontUIResource getControlTextFont()
    {
        return controlFont;
    }

    public FontUIResource getSystemTextFont()
    {
        return systemFont;
    }

    public FontUIResource getUserTextFont()
    {
        return userFont;
    }

    public FontUIResource getMenuTextFont()
    {
        return controlFont;
    }

    public FontUIResource getWindowTitleFont()
    {
        return controlFont;
    }

    public FontUIResource getSubTextFont()
    {
        return smallFont;
    }

}
