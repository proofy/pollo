package org.outerj.pollo.xmleditor.util;

import org.w3c.dom.Element;
import org.xml.sax.helpers.NamespaceSupport;

import java.util.HashMap;


/**
 * Stores objects based on an (XML) element path (e.g. element/nestedElement).
 * The functionality is 'backwards compatible' with that of {@link NodeMap}.
 *
 * <p>
 * The path consits of a series of element names separated by slashes. The path
 * should not begin with a slash. The elements may have namespace prefixes.
 *
 * <p>
 * It is possible to store an object at a path and another object at a subpath
 * of that path, for example you can store an object for 'element' and for
 * 'element/nestedElement'.
 *
 * @author Bruno Dumon
 */
public class NestedNodeMap
{
    protected final HashMap hashMap = new HashMap();

    public void put(String namespaceURI, String localName, Object object)
    {
        NodeEntry newNodeEntry = new NodeEntry();
        newNodeEntry.value = object;
        hashMap.put(getHashString(namespaceURI, localName), newNodeEntry);
    }

    public interface NamespaceResolver
    {
        String[] parseName(String name) throws Exception;
    }

    /**
     * Puts an object in the map, whose key is a (possibly) a nested
     * element path, e.g. "parentelement/childelement". Thus the element
     * names are separated by slashes, but the element path should not start
     * with a slash.
     */
    public void put(String nestedElementPath, Object value, NamespaceResolver nsResolver) throws Exception
    {
        String currentElement = null;
        String restOfThePath = null;
        int pos = nestedElementPath.lastIndexOf('/');
        if (pos != -1)
        {
            restOfThePath = nestedElementPath.substring(0, pos);
            currentElement = nestedElementPath.substring(pos + 1);
        }
        else
        {
            currentElement = nestedElementPath;
        }

        // split element name in namespaceURI and localName parts
        String[] nameParts = nsResolver.parseName(currentElement);
        String namespaceURI = nameParts[0];
        String localName = nameParts[1];

        NodeEntry nodeEntry = null;
        // if there's an existing entry, we'll extend the information in there
        nodeEntry = (NodeEntry) hashMap.get(getHashString(namespaceURI, localName));
        if (nodeEntry == null)
            nodeEntry = new NodeEntry();

        if (restOfThePath != null)
        {
            if (nodeEntry.nestedNodes == null)
                nodeEntry.nestedNodes = new NestedNodeMap();
            nodeEntry.nestedNodes.put(restOfThePath, value, nsResolver);
        }
        else
        {
            nodeEntry.value = value;
        }
        hashMap.put(getHashString(namespaceURI, localName), nodeEntry);
    }

    public Object get(String namespaceURI, String localName)
    {
        NodeEntry nodeEntry = (NodeEntry) hashMap.get(getHashString(namespaceURI, localName));
        if (nodeEntry != null)
            return nodeEntry.value;
        else
            return null;
    }

    /**
     * This method will search for the object that best matches the given
     * element, it will take into account the parent nodes of the element.
     */
    public Object get(Element element)
    {
        NodeEntry nodeEntry = (NodeEntry) hashMap.get(getHashString(element.getNamespaceURI(), element.getLocalName()));
        if (nodeEntry != null)
        {
            if (element.getParentNode() instanceof Element)
            {
                if (nodeEntry.nestedNodes != null)
                {
                    Object value = nodeEntry.nestedNodes.get((Element) element.getParentNode());
                    if (value != null)
                        return value;
                    else
                        return nodeEntry.value;
                }
            }
            return nodeEntry.value;
        }
        return null;
    }

    /**
     * This method is usefull if you don't have an 'Element' object yet
     * for looking up the object, but you do have an Element object for the
     * parent of this node.
     *
     * @param parent can be null
     */
    public Object get(String namespaceURI, String localName, Element parent)
    {
        NodeEntry nodeEntry = (NodeEntry) hashMap.get(getHashString(namespaceURI, localName));
        if (nodeEntry != null)
        {
            if (nodeEntry.nestedNodes != null && parent != null)
            {
                Object value = nodeEntry.nestedNodes.get((Element) parent);
                if (value != null)
                    return value;
            }
            return nodeEntry.value;
        }
        return null;
    }

    private final String getHashString(String uri, String localName)
    {
        if (uri == null) uri = "";
        StringBuffer fqn = new StringBuffer();
        fqn.append("{").append(uri).append("}").append(localName);
        return fqn.toString();
    }

    public class NodeEntry
    {
        public Object value;
        public NestedNodeMap nestedNodes;
    }
}
