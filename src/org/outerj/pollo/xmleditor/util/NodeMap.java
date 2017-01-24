package org.outerj.pollo.xmleditor.util;

import java.util.HashMap;


/**
 * Extension of java.util.HashMap that allows to get and put items
 * based on an namespaceURI and localName.
 *
 * @author Bruno Dumon
 */
public class NodeMap extends HashMap
{
    public void put(String namespaceURI, String localName, Object object)
    {
        put(getHashString(namespaceURI, localName), object);
    }

    public Object get(String namespaceURI, String localName)
    {
        return get(getHashString(namespaceURI, localName));
    }

    private final String getHashString(String uri, String localName)
    {
        if (uri == null) uri = "";
        StringBuffer fqn = new StringBuffer();
        fqn.append("{").append(uri).append("}").append(localName);
        return fqn.toString();
    }
}
