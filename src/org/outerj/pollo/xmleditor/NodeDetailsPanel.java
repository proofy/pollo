package org.outerj.pollo.xmleditor;

import org.outerj.pollo.DomConnected;
import org.w3c.dom.Node;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Iterator;

/**
 * This panel shows other panels based on the selected node. These other panels
 * are e.g. the AttributesPanel (used to edit attributes) when an element is selected,
 * or a CharDataPanel (to edit the text of a character data node) when a comment is
 * selected.
 *
 * To make this work, the panel must be registered as a listener for selection events.
 *
 * @author Bruno Dumon.
 */
public class NodeDetailsPanel extends JPanel implements SelectionListener
{
    protected HashMap panels = new HashMap(); // hashed on the node type (constants defined in org.w3c.dom.Node interface)
    public static final String UNKOWN_NODE_TYPE = "10000";
    protected CardLayout cardLayout;
    protected JPanel currentPanel;

    public NodeDetailsPanel()
    {
        cardLayout = new CardLayout();
        this.setLayout(cardLayout);

        JPanel unkownNodeTypePanel = new JPanel();
        JLabel helpMessage = new JLabel("Selected a node to edit it here.");
        unkownNodeTypePanel.add(helpMessage);
        this.add(unkownNodeTypePanel, UNKOWN_NODE_TYPE);
    }

    public void add(int nodetype, JPanel panel)
    {
        panels.put(String.valueOf(nodetype), panel);
        this.add(panel, String.valueOf(nodetype));
        panel.layout();
    }

    /**
     * Implementation of the SelectionListener interface.
     */
    public void nodeSelected(Node node)
    {
        String nodetype = String.valueOf(node.getNodeType());
        if (panels.containsKey(nodetype))
        {
            currentPanel = (JPanel)panels.get(nodetype);
            cardLayout.show(this, nodetype);
        }
        else
        {
            currentPanel = (JPanel)panels.get(UNKOWN_NODE_TYPE);
            cardLayout.show(this, UNKOWN_NODE_TYPE);
        }
    }

    /**
     * Implementation of the SelectionListener interface.
     */
    public void nodeUnselected(Node node)
    {
        currentPanel = (JPanel)panels.get(UNKOWN_NODE_TYPE);
        cardLayout.show(this, UNKOWN_NODE_TYPE);
    }

    public void requestFocus()
    {
        currentPanel.requestFocus();
    }

    public void disconnectFromDom()
    {
        Iterator cleanIt = panels.values().iterator();

        while (cleanIt.hasNext())
        {
            Object object = (Object)cleanIt.next();
            if (object instanceof DomConnected)
            {
                ((DomConnected)object).disconnectFromDom();
            }
        }

        currentPanel = (JPanel)panels.get(UNKOWN_NODE_TYPE);
        cardLayout.show(this, UNKOWN_NODE_TYPE);
    }

    public void reconnectToDom()
    {
        Iterator cleanIt = panels.values().iterator();

        while (cleanIt.hasNext())
        {
            Object object = (Object)cleanIt.next();
            if (object instanceof DomConnected)
            {
                ((DomConnected)object).reconnectToDom();
            }
        }

        currentPanel = (JPanel)panels.get(UNKOWN_NODE_TYPE);
        cardLayout.show(this, UNKOWN_NODE_TYPE);
    }
}
