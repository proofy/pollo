package org.outerj.pollo.action;

import org.outerj.pollo.PolloFrame;
import org.outerj.pollo.util.ResourceManager;
import org.outerj.pollo.xmleditor.model.XmlModel;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class CloseAction extends AbstractAction
{
    protected XmlModel xmlModel;
    protected PolloFrame polloFrame;

    public CloseAction(XmlModel xmlModel, PolloFrame polloFrame)
    {
        ResourceManager resMgr = ResourceManager.getManager(CloseAction.class);
        resMgr.configureAction(this);
        this.xmlModel = xmlModel;
        this.polloFrame = polloFrame;
    }

    public void actionPerformed(ActionEvent event)
    {
        try
        {
            xmlModel.closeAllViews(polloFrame);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
