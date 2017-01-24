package org.outerj.pollo.util;

import java.awt.*;

public class ColorFormat
{
    public static Color parseHexColor(String hexColor) throws Exception
    {
        if (hexColor.length() != 6)
            throw new Exception("Invalid color code: \"" + hexColor + "\"");

        String c1 = hexColor.substring(0, 2);
        String c2 = hexColor.substring(2, 4);
        String c3 = hexColor.substring(4, 6);

        try
        {
            int c1c = Integer.parseInt(c1, 16);
            int c2c = Integer.parseInt(c2, 16);
            int c3c = Integer.parseInt(c3, 16);
            return new Color(c1c, c2c, c3c);
        }
        catch (NumberFormatException e)
        {
            throw new Exception("Error in color \"" + hexColor + "\": " + e.getMessage());
        }
    }

    public static String formatHex(Color color)
    {
        return padHex(Integer.toHexString(color.getRed()))
                + padHex(Integer.toHexString(color.getGreen()))
                + padHex(Integer.toHexString(color.getBlue()));
    }

    private static String padHex(String hex)
    {
        if (hex.length() < 2)
            return "0" + hex;
        return hex;
    }
}
