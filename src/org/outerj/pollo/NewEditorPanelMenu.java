package org.outerj.pollo;

import org.outerj.pollo.util.ResourceManager;
import org.outerj.pollo.util.Utilities;
import org.outerj.pollo.xmleditor.model.XmlModel;

import javax.swing.*;
import javax.swing.event.MenuListener;
import javax.swing.event.MenuEvent;
import java.awt.event.ActionEvent;
import java.util.Iterator;
import java.util.List;

/**
 * A JMenu from which the user can create new EditorPanels.
 *
 * @author Bruno Dumon
 */
public class NewEditorPanelMenu extends JMenu implements MenuListener
{
    PolloFrame polloFrame;

    public NewEditorPanelMenu(PolloFrame polloFrame)
    {
        ResourceManager resMgr = ResourceManager.getManager(NewEditorPanelMenu.class);
        resMgr.configureMenu( this );
        this.polloFrame = polloFrame;
        addMenuListener(this);
    }

    public void menuSelected(MenuEvent e)
    {
        Utilities.destructMenus(getMenuComponents());
        removeAll();
        // create the items in the menu, one for each XmlModel
        List openFiles = Pollo.getInstance().getOpenFiles();
        Iterator it = openFiles.iterator();
        while (it.hasNext())
        {
            XmlModel xmlModel = (XmlModel)it.next();
            add(new JMenuItem(new NewEditorPanelAction(xmlModel)));
        }
    }

    public void menuDeselected(MenuEvent e)
    {
    }

    public void menuCanceled(MenuEvent e)
    {
    }

    public class NewEditorPanelAction extends AbstractAction
    {
        XmlModel xmlModel;

        public NewEditorPanelAction(XmlModel xmlModel)
        {
            this.xmlModel = xmlModel;

            // set the action name
            putValue(Action.NAME, xmlModel.getLongTitle());
        }

        public void actionPerformed(ActionEvent e)
        {
            EditorPanel editorPanel = Pollo.getInstance().createEditorPanel(xmlModel, polloFrame);
            if (editorPanel != null)
                polloFrame.addEditorPanel(editorPanel);
        }
    }

}
