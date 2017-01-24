package org.outerj.pollo.xmleditor.schema;

import org.jaxen.SimpleNamespaceContext;
import org.outerj.pollo.util.URLFactory;
import org.outerj.pollo.xmleditor.exception.PolloException;
import org.outerj.pollo.xmleditor.util.NestedNodeMap;
import org.outerj.pollo.xmleditor.schema.ElementSchema.SubElement;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.NamespaceSupport;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.InputStream;
import java.util.*;


/**
 * Simple schema implementation. This class can provide the following information:
 * <ul>
 * <li>which attributes an element can have</li>
 * <li>(optionally) a list of possible attribute values to select form</li>
 * <li>which subelements an element can have</li>
 * <ul>
 * The schema information is read from an XML file with a custom syntax, for
 * an example see the sitemapschema.xml file.
 *
 * @author Bruno Dumon
 */
public class BasicSchema implements ISchema
{
    protected SimpleNamespaceContext namespaceContext = new SimpleNamespaceContext();
    protected NestedNodeMap elementSchemas;

    /**
     * Returns the list of attributes an element can have.
     */
    public Collection getAttributesFor(Element element)
    {
        ElementSchema elementSchema = (ElementSchema)elementSchemas.get(element);

        if (elementSchema == null)
            return new LinkedList();
        else
            return elementSchema.attributes;
    }

    /**
     * Returns true if the element <i>child</i> is allowed as child
     * of the element <i>parent</i>.
     */
    public boolean isChildAllowed(Element parent, Element child)
    {
        ElementSchema elementSchema = (ElementSchema)elementSchemas.get(parent);
        if (elementSchema != null)
        {
            return elementSchema.isAllowedAsSubElement(child);
        }
        else
        {
            return false;
        }
    }

    /**
     * Returns an array containing a list of possible values an attribute can have,
     * or null if such a list is not available.
     */
    public String [] getPossibleAttributeValues(Element element, String namespaceURI, String localName)
    {
        AttributeSchema attrSchema = getAttributeSchema(element, namespaceURI, localName);
        if (attrSchema != null)
        {
            return attrSchema.getPossibleValues(element);
        }
        return null;
    }


    public Collection getAllowedSubElements(Element element)
    {
        ElementSchema elementSchema = (ElementSchema)elementSchemas.get(element);
        if (elementSchema != null)
        {
            return elementSchema.subelements.values();
        }
        else
        {
            return Collections.EMPTY_LIST;
        }
    }

    public Collection getAllowedSubTexts(Element element)
    {
        ElementSchema elementSchema = (ElementSchema)elementSchemas.get(element);
        if (elementSchema != null)
        {
            return elementSchema.subtexts;
        }
        else
        {
            return Collections.EMPTY_LIST;
        }
    }


    // The rest is not part of the public interface

    protected void init(HashMap initParams)
        throws Exception
    {
        String source = (String)initParams.get("source");
        if (source == null || source.trim().equals(""))
        {
            throw new PolloException("[BasicSchema] The source init-param is not specified!");
        }

        SchemaHandler schemaHandler = new SchemaHandler();
        elementSchemas = new NestedNodeMap();

        SAXParserFactory parserFactory = SAXParserFactory.newInstance();
        parserFactory.setNamespaceAware(true);
        SAXParser parser = parserFactory.newSAXParser();
        InputStream is = URLFactory.createUrl(source).openStream();
        try
        {
            parser.parse(new InputSource(is), schemaHandler);
        }
        finally
        {
            try { is.close(); } catch (Exception e) {}
        }
    }

    protected void addElementSchema(String elementPath, ElementSchema elementSchema, NestedNodeMap.NamespaceResolver resolver) throws Exception
    {
        elementSchemas.put(elementPath, elementSchema, resolver);
    }

    protected AttributeSchema getAttributeSchema(Element element, String namespaceURI, String localName)
    {
        ElementSchema elementSchema = (ElementSchema)elementSchemas.get(element);

        if (elementSchema == null)
            return null;
        else
            return elementSchema.getAttributeSchema(namespaceURI, localName);
    }

    public Collection validate(Document document)
            throws ValidationNotSupportedException
    {
        throw new ValidationNotSupportedException();
    }


    /*
      Here comes the parser.
     */
    protected class SchemaHandler extends DefaultHandler
    {
        protected boolean inElement;
        protected boolean inAttribute;
        protected String elementPath, elementName, attributeName;
        protected int slashPos;

        protected ElementSchema currentElementSchema;
        protected AttributeSchema currentAttributeSchema;
        protected NamespaceSupport nsSupport = new NamespaceSupport();
        protected String[] nameParts = new String[3];



        public SchemaHandler()
        {
        }
        
        public void startPrefixMapping(String prefix, String uri)
            throws SAXException
        {
            nsSupport.declarePrefix(prefix, uri);
        }

        public void startElement(String namespaceURI, String localName,
                String qName, Attributes atts)
            throws SAXException
        {
            nsSupport.pushContext();
            if (localName.equals("element"))
            {
                inElement = true;
                elementPath = atts.getValue("name");
                currentElementSchema = new ElementSchema();
                slashPos = elementPath.lastIndexOf('/');
                if (slashPos != -1)
                    elementName = elementPath.substring(slashPos + 1);
                else
                    elementName = elementPath;
                nameParts = nsSupport.processName(elementName, nameParts, false);
                currentElementSchema.namespaceURI = nameParts[0].equals("") ? null : nameParts[0];
                currentElementSchema.localName = nameParts[1];
            }
            else if (localName.equals("attribute"))
            {
                if (!inElement) throw new SAXException("SchemaHandler: 'attribute' element only allowed inside an 'element' element.");
                inAttribute = true;
                attributeName = atts.getValue("name");
                nameParts = nsSupport.processName(attributeName, nameParts, false);
                currentAttributeSchema = new AttributeSchema(nameParts[0].equals("") ? null : nameParts[0], nameParts[1],
                        atts.getValue("readvaluesfrom"), namespaceContext);
                if (currentAttributeSchema.xpathExpr == null)
                {
                    String choosefrom = atts.getValue("choosefrom");
                    if (choosefrom != null)
                    {
                        StringTokenizer tokenizer = new StringTokenizer(choosefrom, ",");
                        currentAttributeSchema.values = new String[tokenizer.countTokens()];
                        int i = 0;
                        while (tokenizer.hasMoreTokens())
                        {
                            currentAttributeSchema.values[i] = tokenizer.nextToken();
                            i++;
                        }
                    }
                }
            }
            else if (localName.equals("allowedsubelements"))
            {
                if (!inElement) throw new SAXException("SchemaHandler: 'allowedsubelements' element only allowed inside an 'element' element.");
                String names = atts.getValue("names");
                StringTokenizer tokenizer = new StringTokenizer(names, ",");

                while (tokenizer.hasMoreTokens())
                {
                    nameParts = nsSupport.processName(tokenizer.nextToken(), nameParts, false);
                    SubElement subelement = currentElementSchema.createSubElement(nameParts[0], nameParts[1]);
                    currentElementSchema.subelements.put(subelement.namespaceURI, subelement.localName, subelement);
                }
            }
            else if (localName.equals("allowedsubtexts"))
            {
                if (!inElement) throw new SAXException("SchemaHandler: 'allowedsubtexts' element only allowed inside an 'element' element.");
                String values = atts.getValue("texts");
                StringTokenizer tokenizer = new StringTokenizer(values, ",");

                while (tokenizer.hasMoreTokens())
                {
                    String token = tokenizer.nextToken();
                    if (token.equals("#any"))
                        currentElementSchema.subtexts.add("");
                    else
                        currentElementSchema.subtexts.add(token);
                }
            }
            else if (localName.equals("xpath-ns-prefixes"))
            {
                int count = atts.getLength();
                for (int i = 0; i < count; i++)
                {
                    namespaceContext.addNamespace(atts.getQName(i), atts.getValue(i));
                }
            }
        }

        
        public void endElement(String namespaceURI, String localName, String qName)
            throws SAXException
        {
            if (localName.equals("element"))
            {
                inElement = false;
                try
                {
                    addElementSchema(elementPath, currentElementSchema, new NestedNodeMap.NamespaceResolver()
                    {
                        public String[] parseName(String name) throws Exception
                        {
                            String[] parts = new String[3];
                            nsSupport.processName(name, parts, false);
                            if (parts[0].equals("")) parts[0] = null;
                            return parts;
                        }
                    });
                }
                catch (Exception e)
                {
                    throw new SAXException(e.getMessage(), e);
                }
                currentElementSchema = null;
            }
            else if (localName.equals("attribute"))
            {
                inAttribute = false;
                currentElementSchema.attributes.add(currentAttributeSchema);
                currentAttributeSchema = null;
            }
            nsSupport.popContext();
        }
        
        public void characters(char[] ch, int start, int length)
            throws SAXException
        {
        }

        public void endDocument()
        {
        }

    }
}
