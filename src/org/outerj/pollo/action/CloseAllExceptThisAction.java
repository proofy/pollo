package org.outerj.pollo.action;

import org.outerj.pollo.PolloFrame;
import org.outerj.pollo.Pollo;
import org.outerj.pollo.xmleditor.model.XmlModel;
import org.outerj.pollo.util.ResourceManager;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class CloseAllExceptThisAction extends AbstractAction
{
    protected PolloFrame polloFrame;
    protected XmlModel xmlModel;

    public CloseAllExceptThisAction(XmlModel xmlModel, PolloFrame polloFrame)
    {
        ResourceManager resMgr = ResourceManager.getManager(CloseAllExceptThisAction.class);
        resMgr.configureAction(this);
        this.polloFrame = polloFrame;
        this.xmlModel = xmlModel;
    }

    public void actionPerformed(ActionEvent e)
    {
        Pollo.getInstance().closeAllFiles(polloFrame, xmlModel);
    }
}