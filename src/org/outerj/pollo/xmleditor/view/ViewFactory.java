package org.outerj.pollo.xmleditor.view;

import org.w3c.dom.Node;

public interface ViewFactory
{
    public View createView(Node node, View parentView);
}
