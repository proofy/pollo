package org.outerj.pollo.gui;

import org.outerj.pollo.util.ResourceManager;
import org.outerj.pollo.xmleditor.model.XmlModel;
import org.outerj.pollo.PolloFrame;
import org.outerj.pollo.Pollo;
import org.outerj.pollo.EditorPanel;

import javax.swing.*;
import javax.swing.event.MenuListener;
import javax.swing.event.MenuEvent;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.Iterator;
import java.util.List;

/**
 * A JMenu from which the user can select a recently opened file.
 *
 * @author Bruno Dumon
 */
public class RecentlyOpenedFilesMenu extends JMenu implements MenuListener
{
    PolloFrame polloFrame;

    public RecentlyOpenedFilesMenu(PolloFrame polloFrame)
    {
        ResourceManager resMgr = ResourceManager.getManager(RecentlyOpenedFilesMenu.class);
        resMgr.configureMenu( this );
        this.polloFrame = polloFrame;
        addMenuListener(this);
    }

    public void menuSelected(MenuEvent e)
    {
        removeAll();
        // create the items in the menu, one for each XmlModel
        List recentFiles = Pollo.getInstance().getConfiguration().getRecentlyOpenedFiles();
        for (int i = recentFiles.size() - 1; i >= 0; i--)
        {
            add(new OpenRecentFileAction((String)recentFiles.get(i)));
        }
    }

    public void menuDeselected(MenuEvent e)
    {
    }

    public void menuCanceled(MenuEvent e)
    {
    }

    public class OpenRecentFileAction extends AbstractAction
    {
        String fullpath;

        public OpenRecentFileAction(String fullpath)
        {
            super(fullpath);
            this.fullpath = fullpath;
        }

        public void actionPerformed(ActionEvent e)
        {
            File file = new File(fullpath);
            if (!file.exists())
            {
                ResourceManager resMgr = ResourceManager.getManager(RecentlyOpenedFilesMenu.class);

                String message = resMgr.getString("FileDoesNotExistMessageDialog_message");
                message += fullpath;

                String title = resMgr.getString("FileDoesNotExistMessageDialog_title");

                JOptionPane.showMessageDialog(polloFrame, 
                    message, title, JOptionPane.ERROR_MESSAGE);
                return;
            }
            Pollo.getInstance().openFile(file, polloFrame);
        }
    }

}
