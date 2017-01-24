package org.outerj.pollo.xmleditor;

import org.w3c.dom.Node;

import java.awt.event.MouseEvent;


/**
  Event that is fired when the user clicks on a node in the editor.

  TODO: maybe it would be better to use real DOM events for this? Although
  I think these types of events should be associated with the view and not the model.
 */
public class NodeClickedEvent
{
    Node node;
    MouseEvent mouseEvent;


    /**
      @param element the element which has been clicked on.
     */
    public NodeClickedEvent(Node node, MouseEvent mouseEvent)
    {
        this.node = node;
        this.mouseEvent = mouseEvent;
    }

    public MouseEvent getMouseEvent()
    {
        return mouseEvent;
    }

    public Node getNode()
    {
        return node;
    }
}
