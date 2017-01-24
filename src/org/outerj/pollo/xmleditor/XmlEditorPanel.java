package org.outerj.pollo.xmleditor;

import org.outerj.pollo.DomConnected;
import org.outerj.pollo.Pollo;
import org.outerj.pollo.config.PolloConfiguration;
import org.outerj.pollo.gui.ShadowBorder;
import org.outerj.pollo.xmleditor.action.ValidateAction;
import org.outerj.pollo.xmleditor.attreditor.AttributesPanel;
import org.outerj.pollo.xmleditor.chardataeditor.CharDataPanel;
import org.outerj.pollo.xmleditor.displayspec.IDisplaySpecification;
import org.outerj.pollo.xmleditor.model.XmlModel;
import org.outerj.pollo.xmleditor.plugin.IAttributeEditorPlugin;
import org.outerj.pollo.xmleditor.schema.ISchema;
import org.w3c.dom.Node;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.BevelBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;


/**
 * An XmlEditorPanel consists of three parts: an XmlEditor (showing the actual XML content),
 * an AttributesPanel for editing attribute values, and a NodeInsertionPanel to insert new
 * elements.
 *
 * @author Bruno Dumon
 */
public class XmlEditorPanel extends JPanel implements DomConnected, Disposable
{
    protected XmlEditor xmlEditor;
    protected XmlModel xmlModel;
    protected String xpathForRoot;
    protected ISchema schema;
    protected NodeInsertionPanel nodeInsertionPanel;
    protected NodeDetailsPanel nodeDetailsPanel;
    protected JSplitPane xmlEditorAndNodeInsertPanelSplit;
    protected JSplitPane xmlEditorAndValidationErrorsSplit;
    protected JSplitPane topBottomSplit;
    protected JSplitPane nodeDetailsAndHelpPanelSplit;
    protected Container xpathAndXmlEditorContainer;
    protected ValidationErrorsPanel validationErrorsPanel;
    protected AttributesPanel attrPanel;
    protected ValidateAction validateAction = new ValidateAction(this);
    protected QueryByXPathPanel queryByXPathPanel;

    public XmlEditorPanel(XmlModel model, String xpathForRoot, IDisplaySpecification displaySpec,
            ISchema schema, IAttributeEditorPlugin attrEditorPlugin, int forcedTreetype)
        throws Exception
    {
        this.xpathForRoot = xpathForRoot;

        setLayout(new BorderLayout());
        this.schema = schema;

        // create the xml content editor component
        xmlEditor = new XmlEditor(xpathForRoot, displaySpec, forcedTreetype);
        JScrollPane scrollPane = new JScrollPane(xmlEditor, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getVerticalScrollBar().setUnitIncrement(10);
        scrollPane.setBorder(ShadowBorder.getInstance());

        // create the details pane
        nodeDetailsPanel = new NodeDetailsPanel();
        xmlEditor.getSelectionInfo().addListener(nodeDetailsPanel);
        nodeDetailsPanel.setBorder(ShadowBorder.getInstance());

        attrPanel = new AttributesPanel(model, schema, attrEditorPlugin, xmlEditor, displaySpec);
        xmlEditor.getSelectionInfo().addListener(attrPanel);
        nodeDetailsPanel.add(Node.ELEMENT_NODE, attrPanel);

        CharDataPanel charDataPanel1 = new CharDataPanel(model, Node.CDATA_SECTION_NODE);
        xmlEditor.getSelectionInfo().addListener(charDataPanel1);
        nodeDetailsPanel.add(Node.CDATA_SECTION_NODE, charDataPanel1);

        CharDataPanel charDataPanel2 = new CharDataPanel(model, Node.TEXT_NODE);
        xmlEditor.getSelectionInfo().addListener(charDataPanel2);
        nodeDetailsPanel.add(Node.TEXT_NODE, charDataPanel2);

        CharDataPanel charDataPanel3 = new CharDataPanel(model, Node.COMMENT_NODE);
        xmlEditor.getSelectionInfo().addListener(charDataPanel3);
        nodeDetailsPanel.add(Node.COMMENT_NODE, charDataPanel3);

        CharDataPanel charDataPanel4 = new CharDataPanel(model, Node.PROCESSING_INSTRUCTION_NODE);
        xmlEditor.getSelectionInfo().addListener(charDataPanel4);
        nodeDetailsPanel.add(Node.PROCESSING_INSTRUCTION_NODE, charDataPanel4);

        // create the panel from which the user can select new nodes to insert
        nodeInsertionPanel = new NodeInsertionPanel(this);
        nodeInsertionPanel.setBorder(ShadowBorder.getInstance());

        // bind some keyevents of the xml editor
        ActionMap editorActionMap = xmlEditor.getActionMap();
        editorActionMap.put("insert-node-after", new AbstractAction()
                {
                    public void actionPerformed(ActionEvent e)
                    {
                        nodeInsertionPanel.activateInsertAfter();
                    }
                });
        editorActionMap.put("insert-node-before", new AbstractAction()
                {
                    public void actionPerformed(ActionEvent e)
                    {
                        nodeInsertionPanel.activateInsertBefore();
                    }
                });
        editorActionMap.put("insert-node-inside", new AbstractAction()
                {
                    public void actionPerformed(ActionEvent e)
                    {
                        nodeInsertionPanel.activateInsertInside();
                    }
                });
        editorActionMap.put("edit-details", new AbstractAction()
                {
                    public void actionPerformed(ActionEvent e)
                    {
                        nodeDetailsPanel.requestFocus();
                    }
                });

        // create the HelpPanel
        HelpPanel helpPanel = new HelpPanel(xmlEditor, attrPanel);
        helpPanel.setBorder(ShadowBorder.getInstance());

        // Create the container containing the QueryByXPath panel and the XmlEditor component
        xpathAndXmlEditorContainer = new Container();
        xpathAndXmlEditorContainer.setLayout(new BorderLayout());
        queryByXPathPanel = new QueryByXPathPanel(xmlEditor, attrPanel);
        queryByXPathPanel.setBorder(new CompoundBorder(new EmptyBorder(0, 0, 3, 0), ShadowBorder.getInstance()));
        xpathAndXmlEditorContainer.add(queryByXPathPanel, BorderLayout.NORTH);
        xpathAndXmlEditorContainer.add(scrollPane, BorderLayout.CENTER);

        // create first split pane (xmlEditor - nodeInsertionPanel)
        xmlEditorAndNodeInsertPanelSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, xpathAndXmlEditorContainer, nodeInsertionPanel);
        xmlEditorAndNodeInsertPanelSplit.setResizeWeight(1); // xml content editor gets extra space
        xmlEditorAndNodeInsertPanelSplit.setDividerLocation(Pollo.getInstance().getConfiguration().getSplitPane1Pos());
        xmlEditorAndNodeInsertPanelSplit.addPropertyChangeListener(new SplitPaneDividerListener());
        xmlEditorAndNodeInsertPanelSplit.setDividerSize(3);
        xmlEditorAndNodeInsertPanelSplit.setBorder(BorderFactory.createEmptyBorder());

        // create nodeDetailsPanel - helpPanel splitPane
        nodeDetailsAndHelpPanelSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, nodeDetailsPanel, helpPanel);
        nodeDetailsAndHelpPanelSplit.setDividerSize(3);
        nodeDetailsAndHelpPanelSplit.setBorder(BorderFactory.createEmptyBorder());
        nodeDetailsAndHelpPanelSplit.setDividerLocation(Pollo.getInstance().getConfiguration().getSplitPane3Pos());
        nodeDetailsAndHelpPanelSplit.addPropertyChangeListener(new SplitPaneDividerListener());
        nodeDetailsAndHelpPanelSplit.setOneTouchExpandable(true);

        // create top - bottom split pane
        topBottomSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, xmlEditorAndNodeInsertPanelSplit, nodeDetailsAndHelpPanelSplit);
        topBottomSplit.setResizeWeight(1); // xml content editor gets extra space
        topBottomSplit.setDividerLocation(Pollo.getInstance().getConfiguration().getSplitPane2Pos());
        topBottomSplit.addPropertyChangeListener(new SplitPaneDividerListener());
        topBottomSplit.setDividerSize(3);
        topBottomSplit.setBorder(BorderFactory.createEmptyBorder());
        topBottomSplit.setOneTouchExpandable(true);
        add(topBottomSplit, BorderLayout.CENTER);

        NodePathBar nodePathBar = new NodePathBar(xmlEditor, attrPanel);
        nodePathBar.setBorder(new CompoundBorder(new EmptyBorder(3, 0, 0, 0), BorderFactory.createBevelBorder(BevelBorder.LOWERED)));
        add(nodePathBar, BorderLayout.SOUTH);

        setXmlModel(model);
    }

    public class SplitPaneDividerListener implements PropertyChangeListener
    {
        public void propertyChange(PropertyChangeEvent evt)
        {
            if (evt.getPropertyName().equals(JSplitPane.DIVIDER_LOCATION_PROPERTY))
            {
                PolloConfiguration conf = Pollo.getInstance().getConfiguration();

                conf.setSplitPane1Pos(xmlEditorAndNodeInsertPanelSplit.getDividerLocation());
                conf.setSplitPane2Pos(topBottomSplit.getDividerLocation());
                conf.setSplitPane3Pos(nodeDetailsAndHelpPanelSplit.getDividerLocation());

                // also change the default window size, since the position of the divider is dependent on the
                // size of the current window
                JFrame frame = (JFrame)XmlEditorPanel.this.getTopLevelAncestor();
                conf.setWindowHeight(frame.getHeight());
                conf.setWindowWidth(frame.getWidth());
            }
        }
    }

    public void setXmlModel(XmlModel xmlModel)
    {
        this.xmlModel = xmlModel;
        xmlEditor.setXmlModel(xmlModel);
    }

    public XmlModel getXmlModel()
    {
        return xmlModel;
    }

    public XmlEditor getXmlEditor()
    {
        return xmlEditor;
    }

    public ISchema getSchema()
    {
        return schema;
    }

    public void showValidationErrorsPanel(Collection errors)
    {
        if (xmlEditorAndValidationErrorsSplit == null)
        {
            xmlEditorAndValidationErrorsSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
            xmlEditorAndValidationErrorsSplit.setDividerSize(3);
            xmlEditorAndValidationErrorsSplit.setBorder(BorderFactory.createEmptyBorder());
            xmlEditorAndValidationErrorsSplit.setResizeWeight(1); // xml content editor gets extra space
            xmlEditorAndValidationErrorsSplit.setDividerLocation(0.7);
        }

        getValidationErrorsPanel(); // to be sure that the panel gets instantiated

        if (xmlEditorAndNodeInsertPanelSplit.getLeftComponent() != xmlEditorAndValidationErrorsSplit)
        {
            xmlEditorAndNodeInsertPanelSplit.remove(xpathAndXmlEditorContainer);
            xmlEditorAndValidationErrorsSplit.setTopComponent(xpathAndXmlEditorContainer);
            xmlEditorAndValidationErrorsSplit.setBottomComponent(validationErrorsPanel);
            xmlEditorAndNodeInsertPanelSplit.setLeftComponent(xmlEditorAndValidationErrorsSplit);
        }
        validationErrorsPanel.showErrors(errors);
    }

    public void hideValidationErrorsPanel()
    {
        xmlEditorAndNodeInsertPanelSplit.remove(xmlEditorAndValidationErrorsSplit);
        xmlEditorAndValidationErrorsSplit.remove(xpathAndXmlEditorContainer);
        xmlEditorAndNodeInsertPanelSplit.setLeftComponent(xpathAndXmlEditorContainer);
    }

    public ValidateAction getValidateAction()
    {
        return validateAction;
    }

    public ValidationErrorsPanel getValidationErrorsPanel()
    {
        if (validationErrorsPanel == null)
        {
            validationErrorsPanel = new ValidationErrorsPanel(this, attrPanel);
            validationErrorsPanel.setBorder(ShadowBorder.getInstance());
        }
        return validationErrorsPanel;
    }


    /**
     * Removes event listeners.
     */
    public void disconnectFromDom()
    {
        xmlEditor.disconnectFromDom();
        nodeInsertionPanel.disconnectFromDom();
        nodeDetailsPanel.disconnectFromDom();
    }

    public void reconnectToDom()
    {
        xmlEditor.reconnectToDom();
        nodeInsertionPanel.reconnectToDom();
        nodeDetailsPanel.reconnectToDom();
    }

    public void dispose()
    {
        queryByXPathPanel.dispose();
        attrPanel.dispose();
        remove(xmlEditor);
    }
}
