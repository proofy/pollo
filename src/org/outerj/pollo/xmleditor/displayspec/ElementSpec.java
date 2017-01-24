package org.outerj.pollo.xmleditor.displayspec;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

/**
 * Class that holds information about how to show an element.
 * ElementSpec's are managed by a DisplaySpecification.
 *
 * @author Bruno Dumon
 */
public class ElementSpec implements Comparable
{
    public String nsUri;
    public String localName;
    public Color backgroundColor;
    public Color textColor;
    public String label;
    public String help;
    public ArrayList attributesToShow = new ArrayList();
    public Icon icon;

    public int compareTo(Object o)
    {
        if (o instanceof ElementSpec)
            return getLabel().compareToIgnoreCase(((ElementSpec)o).getLabel());
        else
            throw new ClassCastException("Can only compare to ElementSpec's!");
    }

    public final String getLabel()
    {
        if (label != null)
            return label;
        else
            return localName;
    }

    public AttributeSpec getAttributeSpec(String namespaceURI, String localName)
    {
        for (int i = 0; i < attributesToShow.size(); i++)
        {
            AttributeSpec attributeSpec = (AttributeSpec)attributesToShow.get(i);
            if (equals(namespaceURI, attributeSpec.nsUri) && equals(localName, attributeSpec.localName))
                return attributeSpec;
        }
        return null;
    }

    private final boolean equals(String s1, String s2)
    {
        if (s1 == null && s2 == null)
            return true;
        else if (s1 != null && s1.equals(s2))
            return true;
        return false;
    }
}
