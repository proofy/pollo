package org.outerj.pollo.xmleditor.util;

import org.w3c.dom.Element;

public class DomUtils
{
    /**
     * Given a prefix and local name, returns the qualified name.
     * The prefix may be null, in which case the localName is returned as is.
     */
    public static String getQName(String prefix, String localName)
    {
        if (prefix != null && prefix.length() != 0)
        {
            localName = prefix + ":" + localName;
        }
        return localName;
    }

    public static String getQName(Element element)
    {
        String prefix = element.getPrefix();
        String localName = element.getLocalName();

        if (prefix != null)
            return prefix + ":" + localName;
        else
            return localName;
    }
}
