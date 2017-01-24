package org.outerj.pollo.engine.cocoon;

import org.outerj.pollo.plugin.IActionPlugin;
import org.outerj.pollo.plugin.IActionPluginFactory;
import org.outerj.pollo.xmleditor.exception.PolloException;
import org.outerj.pollo.PolloFrame;
import org.outerj.pollo.EditorPanel;

import java.util.HashMap;

public class CocoonActionPluginFactory implements IActionPluginFactory
{
    /**
     * Note: when this method is called, editorPanelImpl will not yet be fully initialised, so during
     * initialisation the Action Plugin should not call methods on EditorPanel.
     */
    public IActionPlugin getActionPlugin(HashMap initParams, EditorPanel editorPanel, PolloFrame polloFrame)
            throws PolloException
    {
        try
        {
            CocoonActionPlugin plugin = new CocoonActionPlugin();
            plugin.init(initParams, editorPanel, polloFrame);
            return plugin;
        }
        catch (Exception e)
        {
            throw new PolloException("[CocoonActionPluginFactory] Could not create plugin.", e);
        }
    }

}
