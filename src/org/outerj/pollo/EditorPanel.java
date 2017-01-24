package org.outerj.pollo;

import org.outerj.pollo.xmleditor.model.View;
import org.outerj.pollo.xmleditor.model.XmlModel;

import javax.swing.*;

public abstract class EditorPanel extends JPanel implements View
{
    /**
     * Returns the menu bar to be shown when this EditorPanel is active.
     */
    public abstract JMenuBar getMenuBar();

    public abstract JToolBar getToolBar();

    public abstract boolean close();

    public abstract XmlModel getXmlModel();

    public abstract void addListener(EditorPanelListener listener);

    /**
     * The title to be shown on the tab for this editor panel.
     */
    public abstract String getTitle();

    public abstract void refreshUserPreferences();

    public abstract Action getCloseAction();
}
