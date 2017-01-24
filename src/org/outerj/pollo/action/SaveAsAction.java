package org.outerj.pollo.action;

import org.outerj.pollo.PolloFrame;
import org.outerj.pollo.gui.ErrorDialog;
import org.outerj.pollo.util.ResourceManager;
import org.outerj.pollo.xmleditor.model.XmlModel;

import javax.swing.*;
import java.awt.event.ActionEvent;


public class SaveAsAction extends AbstractAction
{
    private XmlModel xmlModel;
    protected PolloFrame polloFrame;

    public SaveAsAction(XmlModel xmlModel, PolloFrame polloFrame)
    {
        ResourceManager resMgr = ResourceManager.getManager(SaveAsAction.class);
        resMgr.configureAction(this);

        this.xmlModel = xmlModel;
        this.polloFrame = polloFrame;
    }

    public void actionPerformed(ActionEvent event)
    {
        try
        {
            xmlModel.saveAs(polloFrame);
        }
        catch (Exception e)
        {
            ErrorDialog errorDialog = new ErrorDialog(polloFrame, "Error saving document.", e);
            errorDialog.show();
        }
    }
}
