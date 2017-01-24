package org.outerj.pollo.action;

import org.outerj.pollo.Pollo;
import org.outerj.pollo.PolloFrame;
import org.outerj.pollo.util.ResourceManager;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class ExitAction extends AbstractAction
{
    protected PolloFrame polloFrame;

    public ExitAction(PolloFrame polloFrame)
    {
        ResourceManager resMgr = ResourceManager.getManager(ExitAction.class);
        resMgr.configureAction(this);
        this.polloFrame = polloFrame;
    }

    public void actionPerformed(ActionEvent e)
    {
        Pollo.getInstance().exit(polloFrame);
    }
}
