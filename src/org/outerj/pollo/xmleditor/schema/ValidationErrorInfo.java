package org.outerj.pollo.xmleditor.schema;

import org.w3c.dom.Node;

/**
 * This class contains information on a validation error.
 *
 * @author Bruno Dumon
 */
public class ValidationErrorInfo
{
    protected Node location;
    protected String message;
    protected String attrNamespaceURI;
    protected String attrLocalName;

    /**
     * @param location Node where the error occured, may be null if unknown
     * @param message the error message, should not be null
     * @param attrNamespaceURI if the error occured at an attribute, put here its URI, otherwise leave null
     * @param attrLocalName same as attrNamespaceURI
     */
    public ValidationErrorInfo(Node location, String message, String attrNamespaceURI, String attrLocalName)
    {
        this.location = location;
        this.message = message;
        this.attrNamespaceURI = attrNamespaceURI;
        this.attrLocalName = attrLocalName;
    }

    public Node getLocation()
    {
        return location;
    }

    public String getMessage()
    {
        return message;
    }

    public String getAttrNamespaceURI()
    {
        return attrNamespaceURI;
    }

    public String getAttrLocalName()
    {
        return attrLocalName;
    }

    public String toString()
    {
        return message;
    }
}
