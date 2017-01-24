package org.outerj.pollo.action;

import org.outerj.pollo.PolloFrame;
import org.outerj.pollo.gui.AboutDialog;
import org.outerj.pollo.util.ResourceManager;

import javax.swing.*;
import java.awt.event.ActionEvent;


public class AboutAction extends AbstractAction
{
    protected PolloFrame polloFrame;

    public AboutAction(PolloFrame polloFrame)
    {
        ResourceManager resMgr = ResourceManager.getManager(AboutAction.class);
        resMgr.configureAction(this);
        this.polloFrame = polloFrame;
    }

    public void actionPerformed(ActionEvent e)
    {
        AboutDialog aboutdialog = new AboutDialog(polloFrame);
        aboutdialog.show();
    }
}
