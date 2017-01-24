package org.outerj.pollo.action;

import org.outerj.pollo.Pollo;
import org.outerj.pollo.PolloFrame;

import org.outerj.pollo.util.ResourceManager;

import javax.swing.*;

import java.awt.event.ActionEvent;


/**
 * Action to create a new file.
 *
 * @author Bruno Dumon
 */


public class FileNewAction extends AbstractAction
{
    PolloFrame polloFrame;
    Pollo pollo = Pollo.getInstance();

    public FileNewAction(PolloFrame polloFrame)
    {
        ResourceManager resMgr = ResourceManager.getManager(FileNewAction.class);
        resMgr.configureAction(this);

        this.polloFrame = polloFrame;
        this.pollo = pollo;
    }


    public void actionPerformed(ActionEvent e)
    {
        pollo.newFileWizard(polloFrame);
    }

}
