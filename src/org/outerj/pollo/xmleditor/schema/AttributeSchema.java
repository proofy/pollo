package org.outerj.pollo.xmleditor.schema;

import org.jaxen.NamespaceContext;
import org.jaxen.dom.DocumentNavigator;
import org.jaxen.dom.DOMXPath;
import org.jaxen.XPath;
import org.jaxen.function.StringFunction;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.Iterator;
import java.util.List;

/**
 * This class represents the definition of an attribute
 * in a Schema.
 *
 * @author Bruno Dumon
 */
public final class AttributeSchema
{
    public NamespaceContext namespaceContext;
    public final String namespaceURI;
    public final String localName;
    public final String xpathExpr;
    public String [] values;
    public boolean required = false;

    public AttributeSchema(String namespaceURI, String localName, String xpathExpr, NamespaceContext namespaceContext)
    {
        this.namespaceContext = namespaceContext;
        this.localName = localName;
        this.namespaceURI = namespaceURI;
        this.xpathExpr = xpathExpr;
    }

    public String [] getPossibleValues(Element element)
    {
        if (xpathExpr == null)
        {
            if (values != null)
                return values;
            return null;
        }
        else
        {
            // execute xpath
            List nodes;
            try
            {
                XPath xpath = new DOMXPath(xpathExpr);
                xpath.setNamespaceContext(namespaceContext);
                nodes = xpath.selectNodes(element);
            }
            catch (Exception e)
            {
                System.out.println("Error executing xpath " + xpathExpr + ": " + e.toString());
                return null;
            }

            // construct return value array
            String [] values = new String[nodes.size()];
            Iterator nodesIt = nodes.iterator();
            int i = 0;
            DocumentNavigator navigator = new DocumentNavigator();
            while (nodesIt.hasNext())
            {
                Node node = (Node)nodesIt.next();
                values[i] = StringFunction.evaluate(node, navigator);
                i++;
            }
            return values;
        }
    }

    public boolean hasPickList()
    {
        if (xpathExpr != null || values != null)
            return true;
        else
            return false;
    }
}
