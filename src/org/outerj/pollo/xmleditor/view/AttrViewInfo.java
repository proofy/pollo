package org.outerj.pollo.xmleditor.view;

import org.outerj.pollo.xmleditor.displayspec.AttributeSpec;

/**
 * This class is used to store layout-information about how/where to render
 * an attribute. This class is used for the attributes that were defined in
 * the display specification. For other attributes, the class ExtraAttrViewInfo
 * is used instead.
 *
 * @author Bruno Dumon
 */
public class AttrViewInfo
{
    public AttributeSpec attributeSpec;

    public int namePos;
    public int valuePos;
    public String value;
    public String name;
    public boolean visible;
}
