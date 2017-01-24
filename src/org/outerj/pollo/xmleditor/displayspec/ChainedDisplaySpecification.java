package org.outerj.pollo.xmleditor.displayspec;

import org.w3c.dom.Element;

import java.awt.*;
import java.util.ArrayList;

/**
 * A wrapper class around a display specification that allows chaining it.
 * The way chaining works is that if the current instance returns null,
 * the next instance is consulted for a value. Note that the methods in
 * IDisplaySpecification are supposed to always return a value, so make
 * sure that on the end of the chain there's a display specification that
 * always return something for each method.
 *
 * @author Bruno Dumon
 */
public class ChainedDisplaySpecification implements IDisplaySpecification
{
    protected ArrayList displaySpecs = new ArrayList();

    public void add(IDisplaySpecification displaySpec)
    {
        displaySpecs.add(displaySpec);
    }

    public Color getBackgroundColor()
    {
        for (int i = 0; i < displaySpecs.size(); i++)
        {
            Color result = ((IDisplaySpecification)displaySpecs.get(i)).getBackgroundColor();
            if (result != null)
                return result;
        }
        return null;
    }

    public ElementSpec getElementSpec(String namespaceURI, String localName, Element parent)
    {
        for (int i = 0; i < displaySpecs.size(); i++)
        {
            ElementSpec result = ((IDisplaySpecification)displaySpecs.get(i))
                .getElementSpec(namespaceURI, localName, parent);
            if (result != null)
                return result;
        }
        return null;
    }

    public ElementSpec getElementSpec(Element element)
    {
        for (int i = 0; i < displaySpecs.size(); i++)
        {
            ElementSpec result = ((IDisplaySpecification)displaySpecs.get(i))
                .getElementSpec(element);
            if (result != null)
                return result;
        }
        return null;
    }

    public int getTreeType()
    {
        for (int i = 0; i < displaySpecs.size(); i++)
        {
            int result = ((IDisplaySpecification)displaySpecs.get(i)).getTreeType();
            if (result != -1)
                return result;
        }
        return IDisplaySpecification.POLLO_TREE;
    }
}
