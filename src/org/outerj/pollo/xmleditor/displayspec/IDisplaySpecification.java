package org.outerj.pollo.xmleditor.displayspec;

import org.w3c.dom.Element;

import java.awt.*;

/**
 * Interface that must be implemented by display specifications.
 * A display specification contains the information about how elements
 * should be rendered on the screen. It plays the same role as CSS
 * does for browsers.
 *
 * @author Bruno Dumon
 */
public interface IDisplaySpecification
{
    public static final int POLLO_TREE = 1;
    public static final int CLASSIC_TREE = 2;

    /**
     * Get the background fill color.
     */
    public Color getBackgroundColor();

    /**
     * Gets the element specification, this is an object
     * containing attributes for how this element should be
     * rendered.
     *
     * @param parent can be null
     */
    public ElementSpec getElementSpec(String namespaceURI, String localName, Element parent);

    public ElementSpec getElementSpec(Element element);

    public int getTreeType();
}
