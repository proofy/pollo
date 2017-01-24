package org.outerj.pollo.plaf;

import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.plaf.metal.MetalTheme;
import javax.swing.*;

public class PolloLookAndFeel extends MetalLookAndFeel
{
    private static boolean isInstalled = false;
    private static boolean themeHasBeenSet = false;

    public PolloLookAndFeel()
    {
        if (!isInstalled)
        {
          UIManager.installLookAndFeel(new UIManager.LookAndFeelInfo("Pollo", "org.outerj.pollo.plaf.PolloLookAndFeel"));
          isInstalled = true;
        }
    }

    public String getID()
    {
      return "Pollo";
    }

    public String getName()
    {
      return "Pollo";
    }

    public String getDescription()
    {
      return "Look and Feel for Pollo.";
    }

    public boolean isNativeLookAndFeel()
    {
      return false;
    }

    public boolean isSupportedLookAndFeel()
    {
      return true;
    }

    protected void initClassDefaults(UIDefaults table)
    {
        super.initClassDefaults(table);

        final String packageName = "org.outerj.pollo.plaf.";
        table.put("SplitPaneUI", packageName + "PolloSplitPaneUI");
    }

    protected void createDefaultTheme()
    {
        if (!themeHasBeenSet)
        {
            setCurrentTheme(new PolloTheme());
        }
    }

    public static void setCurrentTheme(MetalTheme theme) {
      MetalLookAndFeel.setCurrentTheme(theme);
      themeHasBeenSet = true;
    }


}
