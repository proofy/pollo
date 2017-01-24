package org.outerj.pollo;

import org.outerj.pollo.action.*;
import org.outerj.pollo.config.ViewTypeConf;
import org.outerj.pollo.config.PolloConfiguration;
import org.outerj.pollo.gui.*;
import org.outerj.pollo.plugin.IActionPlugin;
import org.outerj.pollo.texteditor.XmlTextEditorPanel;
import org.outerj.pollo.texteditor.XmlTextEditor;
import org.outerj.pollo.texteditor.SyntaxDocument;
import org.outerj.pollo.util.ResourceManager;
import org.outerj.pollo.util.Utilities;
import org.outerj.pollo.xmleditor.XmlEditor;
import org.outerj.pollo.xmleditor.XmlEditorPanel;
import org.outerj.pollo.xmleditor.IconManager;
import org.outerj.pollo.xmleditor.Disposable;
import org.outerj.pollo.xmleditor.displayspec.IDisplaySpecification;
import org.outerj.pollo.xmleditor.model.View;
import org.outerj.pollo.xmleditor.model.XmlModel;
import org.outerj.pollo.xmleditor.model.XmlModelListener;
import org.outerj.pollo.xmleditor.plugin.IAttributeEditorPlugin;
import org.outerj.pollo.xmleditor.schema.ISchema;
import org.xml.sax.SAXParseException;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.MenuListener;
import javax.swing.event.MenuEvent;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Iterator;

public class EditorPanelImpl extends EditorPanel implements View, XmlModelListener, Disposable
{
    protected XmlModel xmlModel;
    protected PolloFrame polloFrame;
    protected XmlEditorPanel xmlEditorPanel;
    protected XmlTextEditorPanel xmlTextEditorPanel;
    protected JComponent currentModePanel; // reference to either xmlEditorPanel or xmlTextEditorPanel
    protected ArrayList listeners = new ArrayList();
    protected String title;
    protected JMenuBar domModeMenuBar;
    protected JMenuBar textModeMenuBar;
    protected JToolBar domModeToolBar;
    protected JToolBar textModeToolBar;
    protected ModeSwitchDropDown textToolBarSwitch = new ModeSwitchDropDown();
    protected ModeSwitchDropDown domToolBarSwitch = new ModeSwitchDropDown();
    protected IActionPlugin actionPlugin;
    protected SaveAction saveAction;
    protected SaveAsAction saveAsAction;
    protected CloseAction closeAction;
    protected CloseViewAction closeViewAction;
    protected CloseAllExceptThisAction closeAllExceptThisAction;
    protected static ResourceManager resMgr = ResourceManager.getManager(EditorPanelImpl.class);

    public EditorPanelImpl(XmlModel xmlModel, ViewTypeConf viewTypeConf, PolloFrame polloFrame, int forcedTreeType)
        throws Exception
    {
        this.xmlModel = xmlModel;
        this.polloFrame = polloFrame;

        IDisplaySpecification idisplayspecification = viewTypeConf.createDisplaySpecChain();
        ISchema ischema = viewTypeConf.createSchemaChain();
        IAttributeEditorPlugin iattributeeditorplugin = viewTypeConf.createAttrEditorPluginChain(xmlModel, ischema, polloFrame);
        actionPlugin = viewTypeConf.createActionPlugins(this, polloFrame);

        xmlEditorPanel = new XmlEditorPanel(xmlModel, null, idisplayspecification, ischema, iattributeeditorplugin, forcedTreeType);
        xmlTextEditorPanel = new XmlTextEditorPanel(xmlModel, ischema);

        // no borders
        xmlTextEditorPanel.setBorder(BorderFactory.createEmptyBorder());
        xmlEditorPanel.setBorder(BorderFactory.createEmptyBorder());
        this.setBorder(new EmptyBorder(3, 3, 3, 3));

        applyUserPreferences();

        // determine the start mode
        if (xmlModel.isInTextMode())
            currentModePanel = xmlTextEditorPanel;
        else
            currentModePanel = xmlEditorPanel;

        // initialize actions
        saveAction = new SaveAction(xmlModel, polloFrame);
        if (!xmlModel.isModified())
            saveAction.setEnabled(false);
        saveAsAction = new SaveAsAction(xmlModel, polloFrame);
        closeAction = new CloseAction(xmlModel, polloFrame);
        closeViewAction = new CloseViewAction(polloFrame, this);
        closeAllExceptThisAction = new CloseAllExceptThisAction(xmlModel, polloFrame);

        // add the component to the panel
        this.setLayout(new BorderLayout());
        add(currentModePanel, BorderLayout.CENTER);

        xmlModel.addListener(this);

        title = xmlModel.getShortTitle();
        if (xmlModel.isModified())
            title = "*" + title;

        createMenus();
        createToolBars();
    }

    protected void createMenus()
    {
        domModeMenuBar = new JMenuBar();
        textModeMenuBar = new JMenuBar();

        // create the file menu
        JMenu domFileMenu = new JMenu("File");
        resMgr.configureMenu("domFileMenu", domFileMenu);
        JMenu textFileMenu = new JMenu("File");
        resMgr.configureMenu("textFileMenu", textFileMenu);
        domFileMenu.add(Utilities.createMenuItemFromAction(polloFrame.getFileNewAction()));
        textFileMenu.add(Utilities.createMenuItemFromAction(polloFrame.getFileNewAction()));
        domFileMenu.addSeparator();
        textFileMenu.addSeparator();
        domFileMenu.add(Utilities.createMenuItemFromAction(polloFrame.getFileOpenAction()));
        textFileMenu.add(Utilities.createMenuItemFromAction(polloFrame.getFileOpenAction()));
        domFileMenu.add(new RecentlyOpenedFilesMenu(polloFrame));
        textFileMenu.add(new RecentlyOpenedFilesMenu(polloFrame));
        domFileMenu.addSeparator();
        textFileMenu.addSeparator();
        domFileMenu.add(Utilities.createMenuItemFromAction(closeAction));
        textFileMenu.add(Utilities.createMenuItemFromAction(closeAction));
        domFileMenu.add(Utilities.createMenuItemFromAction(polloFrame.getCloseAllAction()));
        textFileMenu.add(Utilities.createMenuItemFromAction(polloFrame.getCloseAllAction()));
        domFileMenu.add(Utilities.createMenuItemFromAction(closeAllExceptThisAction));
        textFileMenu.add(Utilities.createMenuItemFromAction(closeAllExceptThisAction));
        domFileMenu.addSeparator();
        textFileMenu.addSeparator();
        domFileMenu.add(Utilities.createMenuItemFromAction(saveAction));
        textFileMenu.add(Utilities.createMenuItemFromAction(saveAction));
        domFileMenu.add(Utilities.createMenuItemFromAction(saveAsAction));
        textFileMenu.add(Utilities.createMenuItemFromAction(saveAsAction));
        domFileMenu.addSeparator();
        textFileMenu.addSeparator();
        domFileMenu.add(Utilities.createMenuItemFromAction(polloFrame.getUserPreferencesAction()));
        textFileMenu.add(Utilities.createMenuItemFromAction(polloFrame.getUserPreferencesAction()));
        domFileMenu.addSeparator();
        textFileMenu.addSeparator();
        domFileMenu.add(Utilities.createMenuItemFromAction(polloFrame.getExitAction()));
        textFileMenu.add(Utilities.createMenuItemFromAction(polloFrame.getExitAction()));

        domModeMenuBar.add(domFileMenu);
        textModeMenuBar.add(textFileMenu);

        // create edit menu for the dom menu bar
        JMenu domEditMenu = new JMenu("Edit");
        resMgr.configureMenu("domEditMenu", domEditMenu);
        domEditMenu.add(Utilities.createMenuItemFromAction(xmlModel.getUndo().getUndoAction()));
        domEditMenu.addSeparator();

        XmlEditor xmleditor = xmlEditorPanel.getXmlEditor();
        domEditMenu.add(Utilities.createMenuItemFromAction(xmleditor.getCopyAction()));
        domEditMenu.add(Utilities.createMenuItemFromAction(xmleditor.getCutAction()));
        JMenu domEditPasteMenu = new JMenu("Paste");
        resMgr.configureMenu("domEditPasteMenu", domEditPasteMenu);
        domEditPasteMenu.add(Utilities.createMenuItemFromAction(xmleditor.getPasteBeforeAction()));
        domEditPasteMenu.add(Utilities.createMenuItemFromAction(xmleditor.getPasteAfterAction()));
        domEditPasteMenu.add(Utilities.createMenuItemFromAction(xmleditor.getPasteInsideAction()));
        domEditMenu.add(domEditPasteMenu);

        domEditMenu.addSeparator();
        domEditMenu.add(Utilities.createMenuItemFromAction(xmleditor.getCommentOutAction()));
        domEditMenu.add(Utilities.createMenuItemFromAction(xmleditor.getUncommentAction()));
        domModeMenuBar.add(domEditMenu);

        // Insert menu for the dom menu bar
        JMenu domInsertMenu = new JMenu("Insert");
        resMgr.configureMenu("domInsertMenu", domInsertMenu);
        JMenu domTextMenu = new JMenu("Text Node");
        resMgr.configureMenu("domTextMenu", domTextMenu);
        domTextMenu.add(Utilities.createMenuItemFromAction(xmleditor.getInsertTextBeforeAction()));
        domTextMenu.add(Utilities.createMenuItemFromAction(xmleditor.getInsertTextAfterAction()));
        domTextMenu.add(Utilities.createMenuItemFromAction(xmleditor.getInsertTextInsideAction()));
        domInsertMenu.add(domTextMenu);
        JMenu domCommentMenu = new JMenu("Comment Node");
        resMgr.configureMenu("domCommentMenu", domCommentMenu);
        domCommentMenu.add(Utilities.createMenuItemFromAction(xmleditor.getInsertCommentBeforeAction()));
        domCommentMenu.add(Utilities.createMenuItemFromAction(xmleditor.getInsertCommentAfterAction()));
        domCommentMenu.add(Utilities.createMenuItemFromAction(xmleditor.getInsertCommentInsideAction()));
        domInsertMenu.add(domCommentMenu);
        JMenu domCDataMenu = new JMenu("CDATA section");
        resMgr.configureMenu("domCDataMenu", domCDataMenu );
        domCDataMenu.add(Utilities.createMenuItemFromAction(xmleditor.getInsertCDataBeforeAction()));
        domCDataMenu.add(Utilities.createMenuItemFromAction(xmleditor.getInsertCDataAfterAction()));
        domCDataMenu.add(Utilities.createMenuItemFromAction(xmleditor.getInsertCDataInsideAction()));
        domInsertMenu.add(domCDataMenu);
        JMenu domPiMenu = new JMenu("Processing Instruction");
        resMgr.configureMenu("domPiMenu", domPiMenu);
        domPiMenu.add(Utilities.createMenuItemFromAction(xmleditor.getInsertPIBeforeAction()));
        domPiMenu.add(Utilities.createMenuItemFromAction(xmleditor.getInsertPIAfterAction()));
        domPiMenu.add(Utilities.createMenuItemFromAction(xmleditor.getInsertPIInsideAction()));
        domInsertMenu.add(domPiMenu);
        domModeMenuBar.add(domInsertMenu);

        // tree menu for the dom menu bar
        JMenu domTreeMenu = new JMenu("Tree");
        resMgr.configureMenu("domTreeMenu", domTreeMenu);
        domTreeMenu.add(Utilities.createMenuItemFromAction(xmleditor.getCollapseAction()));
        domTreeMenu.add(Utilities.createMenuItemFromAction(xmleditor.getExpandAction()));
        domTreeMenu.addSeparator();
        domTreeMenu.add(Utilities.createMenuItemFromAction(xmleditor.getCollapseAllAction()));
        domTreeMenu.add(Utilities.createMenuItemFromAction(xmleditor.getExpandAllAction()));
        domTreeMenu.addSeparator();
        domTreeMenu.add(Utilities.createMenuItemFromAction(xmleditor.getToggleAction()));
        domTreeMenu.addSeparator();
        domTreeMenu.add(Utilities.createMenuItemFromAction(xmleditor.getRenderViewToFileAction()));
        domModeMenuBar.add(domTreeMenu);

        // schema menu for the dom menu bar
        JMenu schemaMenu = new JMenu("Schema");
        resMgr.configureMenu("schemaMenu", schemaMenu);
        schemaMenu.add(Utilities.createMenuItemFromAction(xmlEditorPanel.getValidateAction()));
        schemaMenu.add(Utilities.createMenuItemFromAction(new org.outerj.pollo.xmleditor.action.ShowContentModelAction(xmlEditorPanel)));
        domModeMenuBar.add(schemaMenu);

        // edit menu for the text menu bar
        JMenu editMenu = new JMenu("Edit");
        resMgr.configureMenu("editMenu", editMenu);
        editMenu.add(Utilities.createMenuItemFromAction(xmlTextEditorPanel.getDocument().getUndoAction()));
        editMenu.add(Utilities.createMenuItemFromAction(xmlTextEditorPanel.getDocument().getRedoAction()));
        editMenu.addSeparator();
        editMenu.add(Utilities.createMenuItemFromAction(xmlTextEditorPanel.getEditor().getCutAction()));
        editMenu.add(Utilities.createMenuItemFromAction(xmlTextEditorPanel.getEditor().getCopyAction()));
        editMenu.add(Utilities.createMenuItemFromAction(xmlTextEditorPanel.getEditor().getPasteAction()));
        editMenu.addSeparator();
        editMenu.add(Utilities.createMenuItemFromAction(xmlTextEditorPanel.getEditor().getFindAction()));
        textModeMenuBar.add(editMenu);

        // create the action plugin menu (for dom mode)
        JMenu actionPluginMenu = new ActionPluginMenu("Plugin actions");
        resMgr.configureMenu("actionPluginMenu", actionPluginMenu);
        domModeMenuBar.add(actionPluginMenu);

        // view menu for dom and text menu bar
        JMenu domViewMenu = new JMenu("View");
        resMgr.configureMenu("domViewMenu", domViewMenu);
        JMenu textViewMenu = new JMenu("View");
        resMgr.configureMenu("textViewMenu", textViewMenu);
        domModeMenuBar.add(domViewMenu);
        textModeMenuBar.add(textViewMenu);
        domViewMenu.add(closeViewAction);
        textViewMenu.add(closeViewAction);
        domViewMenu.add(new NewEditorPanelMenu(polloFrame));
        textViewMenu.add(new NewEditorPanelMenu(polloFrame));
        domViewMenu.add(Utilities.createMenuItemFromAction(Pollo.getInstance().getNewPolloFrameAction()));
        textViewMenu.add(Utilities.createMenuItemFromAction(Pollo.getInstance().getNewPolloFrameAction()));
        domViewMenu.addSeparator();
        domViewMenu.add(Utilities.createMenuItemFromAction(xmleditor.getFocusOnEditorAction()));

        // help menu for the dom and text mode menu bar
        domModeMenuBar.add(Box.createHorizontalGlue());
        textModeMenuBar.add(Box.createHorizontalGlue());

        JMenu domHelpMenu = new JMenu("Help");
        resMgr.configureMenu("domHelpMenu", domHelpMenu);
        JMenu textHelpMenu = new JMenu("Help");
        resMgr.configureMenu("textHelpMenu", textHelpMenu);

        domHelpMenu.add(Utilities.createMenuItemFromAction(polloFrame.getHelpAction()));
        textHelpMenu.add(Utilities.createMenuItemFromAction(polloFrame.getHelpAction()));
        domHelpMenu.add(Utilities.createMenuItemFromAction(polloFrame.getAboutAction()));
        textHelpMenu.add(Utilities.createMenuItemFromAction(polloFrame.getAboutAction()));

        domModeMenuBar.add(domHelpMenu);
        textModeMenuBar.add(textHelpMenu);

    }

    protected void createToolBars()
    {
        domModeToolBar = new JToolBar();
        domModeToolBar.setFloatable(false);
        domModeToolBar.add(new ToolButton(polloFrame.getFileOpenAction()));
        domModeToolBar.add(new ToolButton(polloFrame.getFileNewAction()));

        domModeToolBar.addSeparator();
        domModeToolBar.add(new ToolButton(xmlModel.getUndo().getUndoAction()));

        domModeToolBar.addSeparator();
        XmlEditor xmlEditor = xmlEditorPanel.getXmlEditor();
        domModeToolBar.add(new ToolButton(xmlEditor.getCutAction()));
        domModeToolBar.add(new ToolButton(xmlEditor.getCopyAction()));

        PopupToolButton domPasteButton = new PopupToolButton("Paste:", "Paste", IconManager.getIcon("org/outerj/pollo/Paste16.gif"));
        domPasteButton.addAction(xmlEditor.getPasteBeforeAction());
        domPasteButton.addAction(xmlEditor.getPasteInsideAction());
        domPasteButton.addAction(xmlEditor.getPasteAfterAction());
        domModeToolBar.add(domPasteButton);

        domModeToolBar.addSeparator();
        PopupToolButton domTextButton = new PopupToolButton("Insert text:", "Insert Text Node", IconManager.getIcon("org/outerj/pollo/resource/font.gif"));
        domTextButton.addAction(xmlEditor.getInsertTextBeforeAction());
        domTextButton.addAction(xmlEditor.getInsertTextInsideAction());
        domTextButton.addAction(xmlEditor.getInsertTextAfterAction());
        domModeToolBar.add(domTextButton);

        PopupToolButton domCommentButton = new PopupToolButton("Insert comment:", "Insert Comment Node", IconManager.getIcon("org/outerj/pollo/resource/comment.gif"));
        domCommentButton.addAction(xmlEditor.getInsertCommentBeforeAction());
        domCommentButton.addAction(xmlEditor.getInsertCommentInsideAction());
        domCommentButton.addAction(xmlEditor.getInsertCommentAfterAction());
        domModeToolBar.add(domCommentButton);

        domModeToolBar.addSeparator();
        domModeToolBar.add(domToolBarSwitch);

        textModeToolBar = new JToolBar();
        textModeToolBar.setFloatable(false);

        textModeToolBar.add(new ToolButton(polloFrame.getFileOpenAction()));
        textModeToolBar.add(new ToolButton(polloFrame.getFileNewAction()));

        textModeToolBar.addSeparator();
        XmlTextEditor xmlTextEditor = xmlTextEditorPanel.getEditor();
        textModeToolBar.add(xmlTextEditor.getCutAction());
        textModeToolBar.add(xmlTextEditor.getCopyAction());
        textModeToolBar.add(xmlTextEditor.getPasteAction());

        textModeToolBar.addSeparator();
        textModeToolBar.add(textToolBarSwitch);
    }

    public XmlEditorPanel getXmlEditorPanel()
    {
        return xmlEditorPanel;
    }

    public JMenuBar getMenuBar()
    {
        if (xmlModel.isInParsedMode())
            return domModeMenuBar;
        else if (xmlModel.isInTextMode())
            return textModeMenuBar;
        else
            throw new Error("[EditorPanelImpl] XmlModel is neither in parsed nor in text mode.");
    }

    public JToolBar getToolBar()
    {
        if (xmlModel.isInParsedMode())
        {
            domToolBarSwitch.setToTreeMode();
            return domModeToolBar;
        }
        else if (xmlModel.isInTextMode())
        {
            textToolBarSwitch.setToTextMode();
            return textModeToolBar;
        }
        else
            throw new Error("[EditorPanelImpl] XmlModel is neither in parsed nor in text mode.");
    }

    public XmlModel getXmlModel()
    {
        return xmlModel;
    }

    /**
     * Stop is called by the XmlModel when it wants to close all
     * views on an XmlModel.
     */
    public void stop()
    {
        fireClosingEvent();
        dispose();
    }

    /**
     * Close is a request from the containing PolloFrame, therefore we
     * dont' fire a closing event.
     */
    public boolean close()
    {
        try
        {
            if (xmlModel.closeView(this))
            {
                dispose();
                return true;
            }
            else
            {
                return false;
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return false;
    }

    public void addListener(EditorPanelListener listener)
    {
        listeners.add(listener);
    }

    public void fireMenuBarChangedEvent()
    {
        Iterator listenerIt = listeners.iterator();
        while (listenerIt.hasNext())
        {
            ((EditorPanelListener)listenerIt.next()).editorPanelMenuChanged(this);
        }
    }

    public void fireToolBarChangedEvent()
    {
        Iterator listenerIt = listeners.iterator();
        while (listenerIt.hasNext())
        {
            ((EditorPanelListener)listenerIt.next()).editorPanelToolBarChanged(this);
        }
    }

    public void fireClosingEvent()
    {
        Iterator listenerIt = listeners.iterator();
        while (listenerIt.hasNext())
            ((EditorPanelListener)listenerIt.next()).editorPanelClosing(this);
    }

    public void fireTitleChangedEvent()
    {
        Iterator listenerIt = listeners.iterator();
        while (listenerIt.hasNext())
            ((EditorPanelListener)listenerIt.next()).editorPanelTitleChanged(this);
    }

    public void lastViewClosed(XmlModel sourceXmlModel)
    {
    }

    public void fileNameChanged(XmlModel sourceXmlModel)
    {
        title = xmlModel.getShortTitle();
        fireTitleChangedEvent();
    }

    public void fileChanged(XmlModel sourceXmlModel)
    {
        title = "*" + title;
        fireTitleChangedEvent();
        saveAction.setEnabled(true);
    }

    public void fileSaved(XmlModel sourceXmlModel)
    {
        title = xmlModel.getShortTitle();
        fireTitleChangedEvent();
        saveAction.setEnabled(false);
    }

    public void switchToTextMode(XmlModel sourceXmlModel)
    {
        xmlTextEditorPanel.jumpToBeginning();
        remove(currentModePanel);
        currentModePanel = xmlTextEditorPanel;
        add(currentModePanel, BorderLayout.CENTER);
        fireMenuBarChangedEvent();
        fireToolBarChangedEvent();
        getRootPane().repaint();
    }

    public void switchToParsedMode(XmlModel sourceXmlModel)
    {
        xmlEditorPanel.reconnectToDom();
        remove(currentModePanel);
        currentModePanel = xmlEditorPanel;
        add(currentModePanel, BorderLayout.CENTER);
        fireMenuBarChangedEvent();
        fireToolBarChangedEvent();
        getRootPane().repaint();
    }

    public String getTitle()
    {
        return title;
    }

    public Component getParentForDialogs()
    {
        return this.getTopLevelAncestor();
    }

    public void dispose()
    {
        xmlEditorPanel.disconnectFromDom();
        xmlEditorPanel.dispose();
        xmlModel.removeListener(this);

        // disconnect actions from menu, this is important for action instances
        // that are shared between all EditorPanel's, because Swing attaches
        // event listener to those actions, which would cause memory leaks
        Utilities.destructMenus(domModeMenuBar.getComponents());
        Utilities.destructMenus(textModeMenuBar.getComponents());
        Utilities.destructMenus(domModeToolBar.getComponents());
        Utilities.destructMenus(textModeToolBar.getComponents());

        // the texteditor should remove its event listener from the document, do
        // this by setting a new document on it.
        xmlTextEditorPanel.getEditor().setDocument(new SyntaxDocument());
        xmlTextEditorPanel.dispose();
    }

    public void refreshUserPreferences()
    {
        applyUserPreferences();
        xmlEditorPanel.getXmlEditor().relayout();
        if (xmlEditorPanel.getXmlEditor().isShowing())
            xmlEditorPanel.getXmlEditor().repaint();
    }

    protected void applyUserPreferences()
    {
        // configure xmlEditorPanel
        Pollo pollo = Pollo.getInstance();
        PolloConfiguration configuration = pollo.getConfiguration();
        xmlEditorPanel.getXmlEditor().setElementNameFont(new Font("Default", configuration.getElementNameFontStyle(),
            configuration.getElementNameFontSize()));
        xmlEditorPanel.getXmlEditor().setAttributeNameFont(new Font("Default", configuration.getAttributeNameFontStyle(),
            configuration.getAttributeNameFontSize()));
        xmlEditorPanel.getXmlEditor().setAttributeValueFont(new Font("Default", configuration.getAttributeValueFontStyle(),
            configuration.getAttributeValueFontSize()));
        xmlEditorPanel.getXmlEditor().setCharacterDataFont(new Font("Monospaced", 0, configuration.getTextFontSize()));
        xmlEditorPanel.getXmlEditor().setAntialiasing(configuration.isTextAntialiasing());
    }

    protected class ActionPluginMenu extends JMenu implements MenuListener
    {
        public ActionPluginMenu(String name)
        {
            super(name);
            addMenuListener(this);
        }

        public void menuSelected(MenuEvent e)
        {
            Utilities.destructMenus(getMenuComponents());
            removeAll();
            actionPlugin.addActionsToPluginMenu(this, xmlEditorPanel.getXmlEditor().getSelectedNode());
            if (getMenuComponentCount() == 0)
            {
                JMenuItem menuItem = new JMenuItem("No plugin actions available");
                menuItem.setEnabled(false);
                add(menuItem);
            }
        }

        public void menuDeselected(MenuEvent e)
        {
        }

        public void menuCanceled(MenuEvent e)
        {
        }
    }

    public class ModeSwitchDropDown extends JComboBox
    {
        static final int TREE_VIEW_INDEX = 0;
        static final int TEXT_VIEW_INDEX = 1;

        public ModeSwitchDropDown()
        {
            addItem("Tree view");
            addItem("Text view");

            // set maximum size to preferred size, otherwise the dropdown will take
            // up all available width
            Dimension dim = getPreferredSize();
            setMaximumSize(dim);

            setRequestFocusEnabled(false);

            addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    if (getSelectedIndex() == TREE_VIEW_INDEX)
                    {
                        if (currentModePanel != xmlEditorPanel)
                        {
                            try
                            {
                                xmlModel.switchToParsedMode();
                            }
                            catch (SAXParseException saxparseexception)
                            {
                                setSelectedIndex(TEXT_VIEW_INDEX); // make text view selected again since we're staying in there
                                xmlTextEditorPanel.showParseException(saxparseexception);
                                JOptionPane.showMessageDialog(polloFrame, "The document contains well formedness errors.");
                            }
                            catch (Exception exception1)
                            {
                                setSelectedIndex(TEXT_VIEW_INDEX);
                                ErrorDialog errordialog1 = new ErrorDialog(polloFrame, "Could not parse the text to a DOM tree.", exception1);
                                errordialog1.show();
                            }
                        }
                    }
                    else if (getSelectedIndex() == TEXT_VIEW_INDEX)
                    {
                        if (currentModePanel != xmlTextEditorPanel)
                        {
                            try
                            {
                                xmlModel.switchToTextMode();
                            }
                            catch (Exception exception)
                            {
                                setSelectedIndex(TREE_VIEW_INDEX);
                                ErrorDialog errordialog = new ErrorDialog(polloFrame, "Could not serialize the DOM tree to text.", exception);
                                errordialog.show();
                            }
                        }
                    }
                }
            });

        }

        public void setToTextMode()
        {
            setSelectedIndex(1);
        }

        public void setToTreeMode()
        {
            setSelectedIndex(0);
        }

    }

    public Action getCloseAction()
    {
        return closeViewAction;
    }
}
