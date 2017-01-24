package org.outerj.pollo.xmleditor.schema;

import org.outerj.pollo.xmleditor.util.NodeMap;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.HashSet;

/**
 * This class represents the definition of an element
 * in a Schema.
 *
 * @author Bruno Dumon
 */
public final class ElementSchema
{
    public String namespaceURI;
    public String localName;

    public final ArrayList attributes = new ArrayList();
    public final NodeMap subelements = new NodeMap();
    public final HashSet subtexts = new HashSet();

    public final boolean isAllowedAsSubElement(Element element)
    {
        SubElement subelement = (SubElement)subelements.get(element.getNamespaceURI(), element.getLocalName());
        if (subelement == null)
        {
            return false;
        }
        else
        {
            return true;
        }
    }

    public final boolean containsSubElement(String namespaceURI, String localName)
    {
        Object subelement = subelements.get(namespaceURI, localName);
        if (subelement == null)
        {
            return false;
        }
        else
        {
            return true;
        }
    }

    public final boolean containsSubText(String text)
    {
        return subtexts.contains(text);
    }

    public AttributeSchema getAttributeSchema(String namespaceURI, String localName)
    {
        Iterator attrSchemaIt = attributes.iterator();
        while (attrSchemaIt.hasNext())
        {
            AttributeSchema attrSchema = (AttributeSchema)attrSchemaIt.next();
            if (((attrSchema.namespaceURI == null && namespaceURI == null)
                        || (attrSchema.namespaceURI != null
                            && attrSchema.namespaceURI.equals(namespaceURI)))
                    && attrSchema.localName.equals(localName))
            {
                return attrSchema;
            }
        }
        return null;
    }

    public final class SubElement
    {
        public final String namespaceURI;
        public final String localName;

        public SubElement(String namespaceURI, String localName)
        {
            this.namespaceURI = namespaceURI;
            this.localName = localName;
        }
    }

    public SubElement createSubElement(String namespaceURI, String localName)
    {
        return new SubElement(namespaceURI, localName);
    }

    public void dump()
    {
        System.out.println("element: " + localName + " (" + namespaceURI + ")");
        Iterator subelementsIt = subelements.values().iterator();
        while (subelementsIt.hasNext())
        {
            SubElement subElement = (SubElement)subelementsIt.next();
            System.out.println("  -> " + subElement.localName + " (" + subElement.namespaceURI + ")");
        }
    }
}
