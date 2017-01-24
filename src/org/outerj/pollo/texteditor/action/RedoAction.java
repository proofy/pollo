package org.outerj.pollo.texteditor.action;

import org.outerj.pollo.texteditor.XmlTextDocument;
import org.outerj.pollo.gui.EmptyIcon;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

public class RedoAction extends AbstractAction
{
    protected XmlTextDocument xmlTextDocument;

    public RedoAction(XmlTextDocument xmlTextDocument)
    {
        super("Redo", EmptyIcon.getInstance());
        this.xmlTextDocument = xmlTextDocument;
    }

    public void actionPerformed(ActionEvent e)
    {
        xmlTextDocument.redo();
    }

}
