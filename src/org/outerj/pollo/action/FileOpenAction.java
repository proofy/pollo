package org.outerj.pollo.action;

import org.outerj.pollo.Pollo;
import org.outerj.pollo.PolloFrame;
import org.outerj.pollo.util.ExtensionFileFilter;
import org.outerj.pollo.util.ResourceManager;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.event.ActionEvent;
import java.io.File;

public class FileOpenAction extends AbstractAction
{
    Pollo pollo = Pollo.getInstance();
    PolloFrame polloFrame;


    public FileOpenAction(PolloFrame polloFrame)
    {
        ResourceManager resMgr = ResourceManager.getManager(FileOpenAction.class);
        resMgr.configureAction(this);

        this.polloFrame = polloFrame;
    }


    public void actionPerformed(ActionEvent e)
    {
        String defaultPath = pollo.getConfiguration().getFileOpenDialogPath();

        JFileChooser chooser = null;
        if (defaultPath == null)
            chooser = new JFileChooser();
        else
            chooser = new JFileChooser(new File(defaultPath));

        FileFilter defaultFilter = chooser.getFileFilter();
        ExtensionFileFilter filter1 = new ExtensionFileFilter(".xml", "XML files (*.xml)");
        chooser.addChoosableFileFilter(filter1);
        ExtensionFileFilter filter2 = new ExtensionFileFilter(".xmap", "Cocoon Sitemap files (*.xmap)");
        chooser.addChoosableFileFilter(filter2);

        chooser.setFileFilter(defaultFilter);

        int returnVal = chooser.showOpenDialog(polloFrame);
        if(returnVal == JFileChooser.APPROVE_OPTION)
        {
            File selectedFile = chooser.getSelectedFile();
            pollo.getConfiguration().setFileOpenDialogPath(selectedFile.getPath());
            pollo.openFile(selectedFile, polloFrame);
        }
    }
}
