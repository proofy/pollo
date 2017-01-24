package org.outerj.pollo.plugin;

import org.outerj.pollo.xmleditor.exception.PolloException;
import org.outerj.pollo.EditorPanel;
import org.outerj.pollo.PolloFrame;

import java.util.HashMap;

/**
 * Interface for factories of action plugins.
 */
public interface IActionPluginFactory
{
    public IActionPlugin getActionPlugin(HashMap initParams, EditorPanel editorPanel, PolloFrame polloFrame)
        throws PolloException;
}
