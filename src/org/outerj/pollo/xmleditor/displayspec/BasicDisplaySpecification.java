package org.outerj.pollo.xmleditor.displayspec;

import org.outerj.pollo.util.URLFactory;
import org.outerj.pollo.util.ColorFormat;
import org.outerj.pollo.xmleditor.ElementColorIcon;
import org.outerj.pollo.xmleditor.exception.PolloException;
import org.outerj.pollo.xmleditor.util.NestedNodeMap;
import org.w3c.dom.Element;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.mxp1.MXParser;
import org.apache.commons.lang.exception.NestableException;

import javax.swing.*;
import java.awt.*;
import java.io.InputStream;
import java.util.HashMap;
import java.net.URL;

/**
 * An implementation of the IDisplaySpecification interface.
 * The displayspecification is read from an XML file, see the
 * files included with Pollo for examples.
 *
 * @author Bruno Dumon
 */
public class BasicDisplaySpecification implements IDisplaySpecification
{
    /** Contains the instances of the ElementSpec class */
    protected NestedNodeMap elementSpecs = new NestedNodeMap();

    /** Color to use as the background of the XmlEditor. */
    protected Color backgroundColor;

    protected int treeType = -1;


    protected void init(HashMap initParams)
        throws Exception
    {
        String source = (String)initParams.get("source");
        if (source == null || source.trim().equals(""))
        {
            throw new PolloException("[BasicDisplaySpecification] The 'source' init-param is not specified!");
        }

        // parse the XML file.
        InputStream is = URLFactory.createUrl(source).openStream();
        try
        {
            Parser parser = new Parser();
            parser.parse(is);
        }
        catch (Exception e)
        {
            throw new NestableException("Error reading display specification file " + source, e);
        }
        finally
        {
            try { is.close(); } catch (Exception e) {}
        }
    }

    public Color getBackgroundColor()
    {
        return backgroundColor;
    }

    public void addElementSpec(String elementPath, ElementSpec elementSpec, NestedNodeMap.NamespaceResolver resolver) throws Exception
    {
        elementSpecs.put(elementPath, elementSpec, resolver);
    }

    public ElementSpec getElementSpec(String uri, String localName, Element parent)
    {
        ElementSpec elementSpec = (ElementSpec)elementSpecs.get(uri, localName, parent);
        return elementSpec;
    }

    public ElementSpec getElementSpec(Element element)
    {
        ElementSpec elementSpec = (ElementSpec)elementSpecs.get(element);
        return elementSpec;
    }

    public class Parser implements NestedNodeMap.NamespaceResolver {
        XmlPullParser parser;

        public void parse(InputStream is) throws Exception
        {
            parser = new MXParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true);
            parser.setInput(is, null);
            int eventType = parser.getEventType();

            while (eventType != XmlPullParser.END_DOCUMENT)
            {
                if (eventType == XmlPullParser.START_TAG)
                {
                    if (!parser.getName().equals("displayspec"))
                        throw new Exception("Root element should be called \"displayspec\"");

                    // read tree type
                    String treeTypeString = parser.getAttributeValue(XmlPullParser.NO_NAMESPACE, "treetype");
                    if (treeTypeString != null)
                    {
                        if (treeTypeString.equals("classic"))
                            treeType = IDisplaySpecification.CLASSIC_TREE;
                        else if (treeTypeString.equals("pollo"))
                            treeType = IDisplaySpecification.POLLO_TREE;
                        else
                            throw new Exception("Incorrect value for treetype attribute: \"" + treeTypeString + "\" at line " + parser.getLineNumber());
                    }

                    // read background color
                    String backgroundColorString = parser.getAttributeValue(XmlPullParser.NO_NAMESPACE, "background-color");
                    if (backgroundColorString != null)
                    {
                        backgroundColor = ColorFormat.parseHexColor(backgroundColorString);
                    }

                    readElements();
                    break;
                }
                eventType = parser.next();
            }
        }

        private void readElements() throws Exception
        {
            parser.next();
            int eventType = parser.getEventType();

            while (eventType != XmlPullParser.END_TAG)
            {
                if (eventType == XmlPullParser.START_TAG)
                {
                    if (!parser.getName().equals("element"))
                        throw new Exception("Unexpected element: " + parser.getName() + " on line " + parser.getLineNumber());
                    readElement();
                }
                eventType = parser.next();
            }
        }

        private void readElement() throws Exception
        {
            ElementSpec elementSpec = new ElementSpec();

            // parse name attribute
            String elementPath = parser.getAttributeValue(XmlPullParser.NO_NAMESPACE, "name");
            if (elementPath == null)
                throw new Exception("Attribute \"name\" missing on element \"element\" on line " + parser.getLineNumber());

            int slashPos = elementPath.lastIndexOf('/');
            String elementName;
            if (slashPos != -1)
                elementName = elementPath.substring(slashPos + 1);
            else
                elementName = elementPath;
            String[] nameParts = parseName(elementName);
            elementSpec.nsUri = nameParts[0];
            elementSpec.localName = nameParts[1];

            // parse color attribute
            String textColor = parser.getAttributeValue(XmlPullParser.NO_NAMESPACE, "color");
            if (textColor != null)
                elementSpec.textColor = ColorFormat.parseHexColor(textColor);
            else
                elementSpec.textColor = Color.black;

            // parse background-color attribute
            String backgroundColor = parser.getAttributeValue(XmlPullParser.NO_NAMESPACE, "background-color");
            if (backgroundColor != null)
                elementSpec.backgroundColor = ColorFormat.parseHexColor(backgroundColor);
            else
                elementSpec.backgroundColor = Color.white;

            // label attribute
            elementSpec.label = parser.getAttributeValue(XmlPullParser.NO_NAMESPACE, "label");

            // icon attribute
            String iconLocation = parser.getAttributeValue(XmlPullParser.NO_NAMESPACE, "icon");
            if (iconLocation != null)
            {
                try
                {
                    URL iconURL = URLFactory.createUrl(iconLocation);
                    elementSpec.icon = new ImageIcon(iconURL);
                }
                catch (Exception e)
                {
                    throw new NestableException("Could not read icon \"" + iconLocation + "\" specified on line " + parser.getLineNumber(), e);
                }
            }

            parser.next();
            int eventType = parser.getEventType();

            while (eventType != XmlPullParser.END_TAG)
            {
                if (eventType == XmlPullParser.START_TAG)
                {
                    if (parser.getName().equals("help"))
                    {
                        elementSpec.help = readText();
                    }
                    else if (parser.getName().equals("attributes"))
                    {
                        readAttributes(elementSpec);
                    }
                    else
                    {
                        throw new Exception("Unexpected element: " + parser.getName() + " on line " + parser.getLineNumber());
                    }
                }
                eventType = parser.next();
            }

            if (elementSpec.icon == null)
                elementSpec.icon = new ElementColorIcon(elementSpec.backgroundColor);

            addElementSpec(elementPath, elementSpec, this);
        }

        private void readAttributes(ElementSpec elementSpec) throws Exception
        {
            parser.next();
            int eventType = parser.getEventType();

            while (eventType != XmlPullParser.END_TAG)
            {
                if (eventType == XmlPullParser.START_TAG)
                {
                    if (!parser.getName().equals("attribute"))
                        throw new Exception("Unexpected element: " + parser.getName() + " on line " + parser.getLineNumber());
                    readAttribute(elementSpec);
                }
                eventType = parser.next();
            }
        }

        private void readAttribute(ElementSpec elementSpec) throws Exception
        {
            AttributeSpec attributeSpec = new AttributeSpec();

            // parse name attribute
            String attributeName = parser.getAttributeValue(XmlPullParser.NO_NAMESPACE, "name");
            if (attributeName == null)
                throw new Exception("Attribute \"name\" missing on element \"element\" on line " + parser.getLineNumber());

            String[] nameParts = parseName(attributeName);
            attributeSpec.nsUri = nameParts[0];
            attributeSpec.localName = nameParts[1];

            // parse color attribute
            String textColor = parser.getAttributeValue(XmlPullParser.NO_NAMESPACE, "color");
            if (textColor != null)
                attributeSpec.textColor = ColorFormat.parseHexColor(textColor);
            else
                attributeSpec.textColor = Color.black;

            // label attribute
            attributeSpec.label = parser.getAttributeValue(XmlPullParser.NO_NAMESPACE, "label");

            parser.next();
            int eventType = parser.getEventType();

            while (eventType != XmlPullParser.END_TAG)
            {
                if (eventType == XmlPullParser.START_TAG)
                {
                    if (parser.getName().equals("help"))
                    {
                        attributeSpec.help = readText();
                    }
                    else
                    {
                        throw new Exception("Unexpected element: " + parser.getName() + " on line " + parser.getLineNumber());
                    }
                }
                eventType = parser.next();
            }

            elementSpec.attributesToShow.add(attributeSpec);
        }

        private String readText() throws Exception
        {
            parser.next();
            int eventType = parser.getEventType();
            StringBuffer text = new StringBuffer();

            while (eventType != XmlPullParser.END_TAG)
            {
                if (eventType == XmlPullParser.TEXT)
                    text.append(parser.getText());
                eventType = parser.next();
            }

            if (text.length() > 0)
                return text.toString();
            else
                return null;
        }

        public String[] parseName(String name) throws Exception
        {
            int colonPos = name.indexOf(':');
            if (colonPos == -1)
            {
                return new String[] {null, name};
            }
            else
            {
                String prefix = name.substring(0, colonPos);
                String namespaceURI = parser.getNamespace(prefix);
                if (namespaceURI == null)
                    throw new Exception("Undeclared namespace prefix \"" + prefix + "\"");
                return new String[] {namespaceURI, name.substring(colonPos + 1)};
            }
        }
    }

    public int getTreeType()
    {
        return treeType;
    }
}
