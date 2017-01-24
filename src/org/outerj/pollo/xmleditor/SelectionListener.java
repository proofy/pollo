package org.outerj.pollo.xmleditor;

import org.w3c.dom.Node;

public interface SelectionListener
{
    public void nodeUnselected(Node node);

    public void nodeSelected(Node node);
}
