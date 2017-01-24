package org.outerj.pollo.xmleditor.schema;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.Collection;

/**
 * This schema implementation chaines a number of other schemas.
 * This allows combining multiple schemas to one. Usefull for XML
 * documents containing elements from different schemas (eg WSDL).
 *
 * @author Bruno Dumon
 */
public class ChainedSchema implements ISchema
{
    protected ArrayList schemas = new ArrayList();

    public void add(ISchema schema)
    {
        schemas.add(schema);
    }

    /**
     * This will combine the attributes from all the schemas
     * in the chain.
     */
    public Collection getAttributesFor(Element element)
    {
        Collection completeCollection = null;
        for (int i = 0; i < schemas.size(); i++)
        {
            Collection result = ((ISchema)schemas.get(i)).getAttributesFor(element);
            if (i == 0)
                completeCollection = result;
            else
                completeCollection.add(result);
        }
        return completeCollection;
    }

    /**
     * If one of the schemas in the chain says its allowed, then it
     * is allowed.
     */
    public boolean isChildAllowed(Element parent, Element child)
    {
        boolean childAllowed = false;
        for (int i = 0; i < schemas.size(); i++)
        {
            boolean result = ((ISchema)schemas.get(i)).isChildAllowed(parent, child);
            childAllowed = childAllowed || result;
        }
        return childAllowed;
    }

    /**
     * This returns the result from the first encountered schema that does
     * not return null.
     */
    public String [] getPossibleAttributeValues(Element element, String namespaceURI, String localName)
    {
        for (int i = 0; i < schemas.size(); i++)
        {
            String [] result = ((ISchema)schemas.get(i)).getPossibleAttributeValues(element, namespaceURI, localName);
            if (result != null)
                return result;
        }
        return null;
    }

    /**
     * This combines the elements from all the schemas in the chain.
     */
    public Collection getAllowedSubElements(Element element)
    {
        Collection completeCollection = null;
        for (int i = 0; i < schemas.size(); i++)
        {
            Collection result = ((ISchema)schemas.get(i)).getAllowedSubElements(element);
            if (i == 0)
                completeCollection = result;
            else
                completeCollection.add(result);
        }
        return completeCollection;
    }

    public Collection getAllowedSubTexts(Element element)
    {
        Collection completeCollection = null;
        for (int i = 0; i < schemas.size(); i++)
        {
            Collection result = ((ISchema)schemas.get(i)).getAllowedSubTexts(element);
            if (i == 0)
                completeCollection = result;
            else
                completeCollection.add(result);
        }
        return completeCollection;
    }


    public Collection validate(Document document)
        throws ValidationNotSupportedException, Exception
    {
        if (schemas.size() > 0)
            return ((ISchema)schemas.get(0)).validate(document);
        else
            throw new ValidationNotSupportedException();
    }

    public ISchema getSchema(int position)
    {
        if (schemas.size() >= position + 1)
            return (ISchema)schemas.get(position);
        else
            return null;
    }

}
