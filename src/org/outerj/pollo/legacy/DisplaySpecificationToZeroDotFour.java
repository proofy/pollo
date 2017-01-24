package org.outerj.pollo.legacy;

import org.w3c.dom.*;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.ContentHandler;
import org.outerj.pollo.util.ColorFormat;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.awt.*;
import java.util.StringTokenizer;

/**
 * Command-line utility class to convert pre-0.4 Pollo display specification
 * files to the 0.4 format.
 *
 * This code was written in half an hour, and is butt-ugly, but at least worked
 * for all Pollo's default display specifications.
 *
 * To use it, pass to parameters on the command line: infile and outfile.
 */
public class DisplaySpecificationToZeroDotFour
{
    public static void main(String[] args) throws Exception
    {
        new DisplaySpecificationToZeroDotFour().convert(args[0], args[1]);
    }
    public void convert(String fromPath, String toPath) throws Exception
    {
        File fromFile = new File(fromPath);
        File toFile = new File(toPath);

        SAXTransformerFactory tf = (SAXTransformerFactory)TransformerFactory.newInstance();
        TransformerHandler th = tf.newTransformerHandler();
        th.setResult(new StreamResult(toFile));

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        DocumentBuilder db = dbf.newDocumentBuilder();

        Document doc = db.parse(fromFile);

        Element root = doc.getDocumentElement();

        th.startDocument();

        AttributesImpl rootAttrs = new AttributesImpl();
        streamNSAttributes(root, th, rootAttrs);
        th.startElement("", "displayspec", "displayspec", rootAttrs);


        NodeList childNodes = root.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++)
        {
            Node node = childNodes.item(i);
            if (node instanceof Element && node.getLocalName().equals("element"))
            {
                Element element = (Element)node;

                Element backgroundColorEl = null;
                Element showAttributesEl = null;

                NodeList childs = element.getChildNodes();
                for (int j = 0; j < childs.getLength(); j++)
                {
                    Node subNode = childs.item(j);
                    if (subNode instanceof Element && subNode.getLocalName().equals("background-color"))
                        backgroundColorEl = (Element)subNode;
                    else if (subNode instanceof Element && subNode.getLocalName().equals("showattributes"))
                        showAttributesEl = (Element)subNode;
                }

                Color backgroundColor = null;
                if (backgroundColorEl != null)
                {
                    String red = backgroundColorEl.getAttribute("red");
                    String green = backgroundColorEl.getAttribute("green");
                    String blue = backgroundColorEl.getAttribute("blue");

                    backgroundColor = new Color(Integer.parseInt(red), Integer.parseInt(green), Integer.parseInt(blue));
                }

                AttributesImpl attrs = new AttributesImpl();
                String name = element.getAttribute("name");
                attrs.addAttribute("", "name", "name", "CDATA", name);
                if (backgroundColor != null)
                    attrs.addAttribute("", "background-color", "background-color", "CDATA", ColorFormat.formatHex(backgroundColor));
                streamNSAttributes(element, th, attrs);
                th.startElement("", "element", "element", attrs);

                if (showAttributesEl != null)
                {
                    String attrsToShow = showAttributesEl.getAttribute("names");
                    if (attrsToShow.length() > 0)
                    {
                        th.startElement("", "attributes", "attributes", new AttributesImpl());
                        StringTokenizer tokenizer = new StringTokenizer(attrsToShow, ",");
                        while (tokenizer.hasMoreTokens())
                        {
                            String attrName = tokenizer.nextToken();
                            AttributesImpl attrAttrs = new AttributesImpl();
                            attrAttrs.addAttribute("", "name", "name", "CDATA", attrName);
                            th.startElement("", "attribute", "attribute", attrAttrs);
                            th.endElement("", "attribute", "attribute");
                        }
                        th.endElement("", "attributes", "attributes");
                    }
                }

                th.endElement("", "element", "element");
            }
        }

        th.endElement("", "displayspec", "displayspec");
        th.endDocument();
    }

    public void streamNSAttributes(Element element, ContentHandler ch, AttributesImpl attrs) throws Exception
    {
        NamedNodeMap map = element.getAttributes();
        for (int i = 0; i < map.getLength(); i++)
        {
            Attr attr = (Attr)map.item(i);
            if (attr.getName().startsWith("xmlns:"))
            {
                String name = attr.getName();
                int pos = name.indexOf(":");
                String prefix = name.substring(pos + 1);
                attrs.addAttribute("http://www.w3.org/XML/1998/namespace", prefix, "xmlns:" + prefix, "CDATA", attr.getValue());
            }
        }
    }
}
