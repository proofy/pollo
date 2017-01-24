package org.outerj.pollo.xmleditor.util;

import java.util.HashSet;


/**
 * Extension of java.util.HashSet that allows to work with
 * an namespaceURI and localName.
 *
 * @author Bruno Dumon
 */
public class NodeSet extends HashSet
{
    public void add(String namespaceURI, String localName)
    {
        add(getHashString(namespaceURI, localName));
    }

    public boolean contains(String namespaceURI, String localName)
    {
        return contains(getHashString(namespaceURI, localName));
    }


    private final String getHashString(String uri, String localName)
    {
        if (uri == null) uri = "";
        StringBuffer fqn = new StringBuffer();
        fqn.append("{").append(uri).append("}").append(localName);
        return fqn.toString();
    }
}
