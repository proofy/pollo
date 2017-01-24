package org.outerj.pollo;

import org.outerj.pollo.action.*;
import org.outerj.pollo.gui.RecentlyOpenedFilesMenu;
import org.outerj.pollo.gui.EmptyIcon;
import org.outerj.pollo.xmleditor.IconManager;
import org.outerj.pollo.util.ResourceManager;
import org.outerj.pollo.util.Utilities;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.PopupMenuListener;
import javax.swing.event.PopupMenuEvent;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

/**
 * PolloFrame is a top-level frame containing a number of
 * EditorPanel. Each EditorPanel is a view on an open file.
 *
 * There could be multiple PolloFrame's, each containing one or more
 * EditorPanels. Multiple EditorPanels may be showing the same file
 * (XmlModel instance).
 *
 * @author Bruno Dumon
 */
public class PolloFrame extends JFrame implements EditorPanelListener, ChangeListener
{
    /** The menu bar that is shown when there are no EditorPanels */
    protected JMenuBar noEditorPanelsMenuBar;

    /** The pollo instance to which this frame belongs. */
    protected Pollo pollo = Pollo.getInstance();

    /** A tabbed pane containing the editorpanel instances */
    protected DnDTabbedPane editorPanelTabs;

    /** The currently visible toolbar */
    protected JToolBar currentToolBar;

    protected Action fileOpenAction;
    protected Action fileNewAction;
    protected Action exitAction;
    protected Action helpAction;
    protected Action aboutAction;
    protected Action userPreferencesAction;
    protected Action closeAllViewsExceptThisAction;
    protected Action closeAllAction;

    public PolloFrame()
    {
        super("Pollo");
        ResourceManager resMgr = ResourceManager.getManager(PolloFrame.class);
        setTitle(resMgr.getString("Title"));
        
        setIconImage(IconManager.getIcon("org/outerj/pollo/resource/pollo_icon.gif").getImage());

        editorPanelTabs = new DnDTabbedPane();
        editorPanelTabs.addChangeListener(this);
        editorPanelTabs.addMouseListener(new TabsMouseListener());

        //check it's not a mac (returns string if it's a mac, null otherwise)
        if (System.getProperty("mrj.version") == null)
        {
            // no border and dark background
            editorPanelTabs.setBorder(new EmptyBorder(0, 0, 0, 0));
            editorPanelTabs.setBackground(new Color(153, 153, 153));
            editorPanelTabs.setOpaque(true);
        }

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(editorPanelTabs, BorderLayout.CENTER);

        // initialize actions
        fileOpenAction = new FileOpenAction(this);
        fileNewAction = new FileNewAction(this);
        helpAction = new HelpAction(this);
        aboutAction = new AboutAction(this);
        exitAction = new ExitAction(this);
        userPreferencesAction = new UserPreferencesAction(this, pollo.getConfiguration());
        closeAllViewsExceptThisAction = new CloseAllViewsExceptThisAction();
        closeAllAction = new CloseAllAction(this);

        // create and display menu bar
        createNoEditorPanelsMenuBar();
        setJMenuBar(noEditorPanelsMenuBar);

        // don't close the window automatically
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        // add a window listener
        addWindowListener(new WindowAdapter()
        {
            public void windowClosing(WindowEvent e)
            {
                close();
            }
        });

        setSize(pollo.getConfiguration().getWindowWidth(), pollo.getConfiguration().getWindowHeight());
    }

    /**
     * This method creates the menu bar that is shown when there are
     * no EditorPanels open in this frame.
     */
    protected void createNoEditorPanelsMenuBar()
    {
        ResourceManager resMgr = ResourceManager.getManager(PolloFrame.class);
        noEditorPanelsMenuBar = new JMenuBar();

        JMenu fileMenu = new JMenu(resMgr.getString( "FileMenu_Text" ));
        resMgr.setMnemonic( fileMenu, "FileMenu_MnemonicKey" );
        noEditorPanelsMenuBar.add(fileMenu);
        fileMenu.add(Utilities.createMenuItemFromAction(getFileNewAction()));
        fileMenu.addSeparator();
        fileMenu.add(Utilities.createMenuItemFromAction(getFileOpenAction()));
        fileMenu.add(new RecentlyOpenedFilesMenu(this));
        fileMenu.addSeparator();
        fileMenu.add(Utilities.createMenuItemFromAction(getUserPreferencesAction()));
        fileMenu.addSeparator();
        fileMenu.add(Utilities.createMenuItemFromAction(getExitAction()));

        // view menu
        JMenu viewMenu = new JMenu(resMgr.getString("ViewMenu_Text"));
        resMgr.setMnemonic( viewMenu, "ViewMenu_MnemonicKey" );
        noEditorPanelsMenuBar.add(viewMenu);
        viewMenu.add(Utilities.createMenuItemFromAction(Pollo.getInstance().getNewPolloFrameAction()));
        viewMenu.add(new NewEditorPanelMenu(this));

        // help menu
        noEditorPanelsMenuBar.add(Box.createHorizontalGlue());
        JMenu helpMenu = new JMenu(resMgr.getString("HelpMenu_Text"));
        resMgr.setMnemonic( helpMenu, "HelpMenu_MnemonicKey" );
        noEditorPanelsMenuBar.add(helpMenu);

        helpMenu.add(Utilities.createMenuItemFromAction(getHelpAction()));
        helpMenu.add(Utilities.createMenuItemFromAction(getAboutAction()));
    }

    public void addEditorPanel(EditorPanel editorPanel)
    {
        editorPanelTabs.add(editorPanel.getTitle(), editorPanel);
        editorPanel.addListener(this);
        editorPanelTabs.setSelectedComponent(editorPanel);
    }

    /**
     * This only removed the EditorPanel from this PolloFrame, it
     * does not actually close and clean up the EditorPanel. To do
     * that, use the close() method of EditorPanel.
     */
    public void removeEditorPanel(EditorPanel editorPanel)
    {
        editorPanelTabs.remove(editorPanel);
    }

    public java.util.List getEditorPanels()
    {
        Component[] components = editorPanelTabs.getComponents();
        ArrayList editorPanels = new ArrayList();
        for (int i = 0; i < components.length; i++)
            editorPanels.add(components[i]);
        return editorPanels;
    }

    public Action getFileOpenAction()
    {
        return fileOpenAction;
    }

    public Action getFileNewAction()
    {
        return fileNewAction;
    }

    public Action getExitAction()
    {
        return exitAction;
    }

    public Action getAboutAction()
    {
        return aboutAction;
    }

    public Action getHelpAction()
    {
        return helpAction;
    }

    public Action getUserPreferencesAction()
    {
        return userPreferencesAction;
    }

    public Action getCloseAllAction()
    {
        return closeAllAction;
    }

    /**
     * Implementation of EditorPanelListener interface.
     */
    public void editorPanelMenuChanged(EditorPanel source)
    {
        if (editorPanelTabs.getSelectedComponent() == source)
        {
            setJMenuBar(source.getMenuBar());
        }
    }

    /**
     * Implementation of EditorPanelListener interface.
     */
    public void editorPanelToolBarChanged(EditorPanel source)
    {
        if (editorPanelTabs.getSelectedComponent() == source)
        {
            if (currentToolBar != null)
                getContentPane().remove(currentToolBar);
            currentToolBar = source.getToolBar();
            if (currentToolBar != null)
                getContentPane().add(currentToolBar, BorderLayout.NORTH);
            getRootPane().revalidate();
        }
    }

    /**
     * Implementation of EditorPanelListener interface.
     */
    public void editorPanelClosing(EditorPanel source)
    {
        editorPanelTabs.remove(source);
        stateChanged(null);
    }

    public void editorPanelTitleChanged(EditorPanel source)
    {
        editorPanelTabs.setTitleAt(editorPanelTabs.indexOfComponent(source),
                source.getTitle());

        if (getCurrentEditorPanel() == source)
        {
            setFrameTitle();
        }
    }

    /**
     * Called when another tab is selected.
     */
    public void stateChanged(ChangeEvent e)
    {
        EditorPanel currentEditorPanel = getCurrentEditorPanel();
        if (currentEditorPanel != null)
        {
            setJMenuBar(currentEditorPanel.getMenuBar());
            JToolBar toolBar = currentEditorPanel.getToolBar();
            if (currentToolBar != null)
                getContentPane().remove(currentToolBar);
            currentToolBar = toolBar;
            if (currentToolBar != null)
                getContentPane().add(currentToolBar, BorderLayout.NORTH);
        }
        else
        {
            setJMenuBar(noEditorPanelsMenuBar);
            if (currentToolBar != null)
            {
                getContentPane().remove(currentToolBar);
                currentToolBar = null;
            }
        }
        setFrameTitle();
        getRootPane().revalidate(); // this is needed after changing the toolbar
    }

    public void setFrameTitle()
    {
        EditorPanel currentEditorPanel = getCurrentEditorPanel();
        if (currentEditorPanel != null)
        {
            String modifiedStar = currentEditorPanel.getXmlModel().isModified() ? "*" : "";
            setTitle("Pollo - " + modifiedStar + currentEditorPanel.getXmlModel().getLongTitle());
        }
        else
        {
            setTitle("Pollo");
        }
    }

    /**
     * Returns the currently active EditorPanel.
     */
    public EditorPanel getCurrentEditorPanel()
    {
        return (EditorPanel)editorPanelTabs.getSelectedComponent();
    }

    public void close()
    {
        try
        {
            // close all the EditorPanels

            // make a copy of the list of editorpanels
            ArrayList editorPanelsList = new ArrayList();
            for (int i = 0; i < editorPanelTabs.getTabCount(); i++)
            {
                editorPanelsList.add(editorPanelTabs.getComponent(i));
            }

            // try to close the editor panels. This can be canceled by the user
            // when asking him to save a changed file and he/she selects cancel.
            boolean allEditorPanelsClosed = true;
            for (int i = 0; i < editorPanelsList.size(); i++)
            {
                EditorPanel editorPanel = (EditorPanel)editorPanelsList.get(i);
                if (!editorPanel.close())
                {
                    allEditorPanelsClosed = false;
                    break;
                }
                else
                {
                    editorPanelTabs.remove(editorPanel);
                }
            }

            // if all EditorPanels were closed, we can close the window.
            if (allEditorPanelsClosed)
            {
                hide();
                dispose();
            }
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
        }
    }

    public class DnDTabbedPane extends JTabbedPane implements java.awt.dnd.DropTargetListener
    {
        public DnDTabbedPane()
        {
            super();
            java.awt.dnd.DropTarget dropTarget = new java.awt.dnd.DropTarget(this, this);
        }

        public void dragEnter(java.awt.dnd.DropTargetDragEvent dropTargetDragEvent)
        {
        }

        public void dragExit(java.awt.dnd.DropTargetEvent dropTargetEvent)
        {
        }

        public void dragOver(java.awt.dnd.DropTargetDragEvent dropTargetDragEvent)
        {
            // the line below works only from java 1.4
            //int index = indexAtLocation((int)dropTargetDragEvent.getLocation().getX(), (int)dropTargetDragEvent.getLocation().getY());

            int index =  getTabForCoordinate((int)dropTargetDragEvent.getLocation().getX(), (int)dropTargetDragEvent.getLocation().getY());
            if(index >= 0 && index<getTabCount() && index != getSelectedIndex())
            {
                setSelectedIndex(index);
            }
        }

        public void drop(java.awt.dnd.DropTargetDropEvent dropTargetDropEvent)
        {
        }

        public void dropActionChanged(java.awt.dnd.DropTargetDragEvent dropTargetDragEvent)
        {
        }

        public int getTabForCoordinate(int x, int y)
        {
            int index =  getUI().tabForCoordinate(this, x, y);
            return index;
        }

    }

    class TabsMouseListener implements MouseListener
    {
        public void mouseClicked(MouseEvent e)
        {
            if (SwingUtilities.isRightMouseButton(e))
            {
                int tabIndex = editorPanelTabs.getTabForCoordinate(e.getX(), e.getY());
                if (tabIndex != -1)
                {
                    final EditorPanel editorPanel = (EditorPanel)editorPanelTabs.getComponent(tabIndex);
                    JPopupMenu popupMenu = new JPopupMenu();
                    popupMenu.add(editorPanel.getCloseAction());
                    popupMenu.add(closeAllViewsExceptThisAction);
                    popupMenu.addPopupMenuListener(new PopupMenuListener()
                    {
                        public void popupMenuWillBecomeVisible(PopupMenuEvent e)
                        {
                        }

                        public void popupMenuWillBecomeInvisible(PopupMenuEvent e)
                        {
                            // remove all menu items to avoid memory leak
                            ((JPopupMenu)e.getSource()).removeAll();
                        }

                        public void popupMenuCanceled(PopupMenuEvent e)
                        {
                        }
                    });
                    popupMenu.show(editorPanelTabs, e.getX(), e.getY());
                }
            }
        }

        public void mousePressed(MouseEvent e)
        {
        }

        public void mouseReleased(MouseEvent e)
        {
        }

        public void mouseEntered(MouseEvent e)
        {
        }

        public void mouseExited(MouseEvent e)
        {
        }
    }

    class CloseAllViewsExceptThisAction extends AbstractAction
    {
        public CloseAllViewsExceptThisAction()
        {
            super("Close All Except This", EmptyIcon.get16Instance());
        }

        public void actionPerformed(ActionEvent e)
        {
            int selected = editorPanelTabs.getSelectedIndex();

            // make a copy of the list of editorpanels
            ArrayList editorPanelsList = new ArrayList();
            for (int i = 0; i < editorPanelTabs.getTabCount(); i++)
            {
                editorPanelsList.add(editorPanelTabs.getComponent(i));
            }

            // try to close the editor panels. This can be canceled by the user
            // when asking him to save a changed file and he/she selects cancel.
            for (int i = 0; i < editorPanelsList.size(); i++)
            {
                if (i != selected)
                {
                    EditorPanel editorPanel = (EditorPanel)editorPanelsList.get(i);
                    if (editorPanel.close())
                    {
                        editorPanelTabs.remove(editorPanel);
                    }
                }
            }
            // make sure the correct toolbars and menu items are shown -- apparently this can get
            // messed up while removing the editorpanels.
            stateChanged(null);
        }
    }
}
