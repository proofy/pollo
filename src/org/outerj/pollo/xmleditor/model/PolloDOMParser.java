package org.outerj.pollo.xmleditor.model;

import org.apache.xerces.parsers.DOMParser;
import org.apache.xerces.xni.*;

/**
 * A variant of the Xerces DOMParser which does not create text nodes
 * if the text only consists of whitespace.
 *
 * Normally the decission to ignore whitespace is based on the dtd or
 * schema, but for the purposes of Pollo this is more usefull.
 *
 * @author Bruno Dumon
 */
public class PolloDOMParser extends DOMParser
{
    public PolloDOMParser()
        throws Exception
    {
        setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        setFeature("http://apache.org/xml/features/dom/include-ignorable-whitespace", false);
        setFeature("http://xml.org/sax/features/external-general-entities", false);
        setFeature("http://xml.org/sax/features/external-parameter-entities", false);
    }

    public void characters(XMLString xmlString, Augmentations augmentations) throws XNIException
    {
        int maxPos = xmlString.offset + xmlString.length;
        for (int i = xmlString.offset; i < maxPos; i++)
        {
            if (xmlString.ch[i] != ' ' && xmlString.ch[i] != '\t' && xmlString.ch[i] != '\r' && xmlString.ch[i] != '\n')
            {
                super.characters(xmlString, augmentations);
                break;
            }
        }
    }
}
