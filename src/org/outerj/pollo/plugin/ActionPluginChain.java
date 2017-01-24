package org.outerj.pollo.plugin;

import org.outerj.pollo.PolloFrame;
import org.outerj.pollo.xmleditor.model.XmlModel;
import org.w3c.dom.Node;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Combines a number of action plugins.
 */
public class ActionPluginChain implements IActionPlugin
{
    protected ArrayList actionPlugins = new ArrayList();

    public void add(IActionPlugin actionPlugin)
    {
        this.actionPlugins.add(actionPlugin);
    }

    public void addActionsToPluginMenu(JMenu menu, Node selectedNode)
    {
        Iterator it = actionPlugins.iterator();
        while (it.hasNext())
        {
            IActionPlugin actionPlugin = (IActionPlugin)it.next();
            actionPlugin.addActionsToPluginMenu(menu, selectedNode);
        }
    }

}
