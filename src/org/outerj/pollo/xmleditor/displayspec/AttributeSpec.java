package org.outerj.pollo.xmleditor.displayspec;

import java.awt.*;

/**
 * Class that holds information about how to display an
 * attribute. Since no such properties are maintained
 * anymore for attributes, this class is here because of
 * historical reasons.
 *
 * AttributeSpec's are managed by a DisplaySpecification.
 *
 * @author Bruno Dumon
 */
public class AttributeSpec
{
    public String nsUri;
    public String localName;
    public Color textColor;
    public String help;
    public String label;
}
