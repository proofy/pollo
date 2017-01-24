package org.outerj.pollo.xmleditor.action;

import org.outerj.pollo.xmleditor.XmlEditor;
import org.outerj.pollo.util.ResourceManager;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * Moves focus to the XmlEditor.
 *
 * Meant to be binded to the escape key.
 */
public class FocusOnEditorAction extends AbstractAction
{
    protected XmlEditor xmlEditor;
    protected static final ResourceManager resourceManager = ResourceManager.getManager(FocusOnEditorAction.class);

    public FocusOnEditorAction(XmlEditor xmlEditor)
    {
        this.xmlEditor = xmlEditor;
        resourceManager.configureAction(this);
    }

    public void actionPerformed(ActionEvent e)
    {
        xmlEditor.requestFocus();
    }
}
