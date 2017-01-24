package org.outerj.pollo.plugin;

import org.outerj.pollo.PolloFrame;
import org.outerj.pollo.xmleditor.model.XmlModel;
import org.w3c.dom.Node;

import javax.swing.*;

public interface IActionPlugin
{
    /**
     * @param menu menu to which menu items should be added
     * @param selectedNode currently selected node, may be null
     */
    public void addActionsToPluginMenu(JMenu menu, Node selectedNode);

}
