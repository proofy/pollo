package org.outerj.pollo.xmleditor.action;

import org.outerj.pollo.xmleditor.SelectionListener;
import org.outerj.pollo.xmleditor.XmlEditor;
import org.outerj.pollo.util.ResourceManager;
import org.w3c.dom.*;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class PasteAction extends AbstractAction implements SelectionListener
{
    public static final int PASTE_BEFORE  = 1;
    public static final int PASTE_AFTER   = 2;
    public static final int PASTE_ASCHILD = 3;

    protected XmlEditor xmlEditor;
    protected int behaviour;
    protected static final ResourceManager resourceManager = ResourceManager.getManager(PasteAction.class);

    public PasteAction(XmlEditor xmlEditor, int behaviour)
    {
        this.xmlEditor = xmlEditor;
        this.behaviour = behaviour;
        setEnabled(false);
        xmlEditor.getSelectionInfo().addListener(this);

        String propertyPrefix;
        if (behaviour == PASTE_BEFORE)
            propertyPrefix = "pasteBeforeAction";
        else if (behaviour == PASTE_AFTER)
            propertyPrefix = "pasteAfterAction";
        else if (behaviour == PASTE_ASCHILD)
            propertyPrefix = "pasteInsideAction";
        else
            throw new RuntimeException("[PasteAction] Invalid behaviour: " + behaviour);

        resourceManager.configureAction(propertyPrefix, this);
    }

    public void actionPerformed(ActionEvent e)
    {
        DocumentFragment clipboard = xmlEditor.getClipboard();
        if (clipboard == null)
        {
            JOptionPane.showMessageDialog(xmlEditor.getTopLevelAncestor(),
                    "Clipboard is empty", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Node newNode = clipboard.getFirstChild();
        Node selectedNode = xmlEditor.getSelectedNode();

        Node parent = selectedNode.getParentNode();

        newNode = xmlEditor.getXmlModel().getDocument().importNode(newNode, true);

        if ((selectedNode instanceof Document || parent instanceof Document)
                && !(newNode instanceof Comment || newNode instanceof ProcessingInstruction)
                && ((selectedNode instanceof Document) && (((Document)selectedNode).getDocumentElement() != null)))
        {
            JOptionPane.showMessageDialog(xmlEditor.getTopLevelAncestor(),
                    "An XML document can have only one root element."); 
            return;
        }

        if (parent != null && behaviour == PASTE_BEFORE)
        {
            parent.insertBefore(newNode, selectedNode);
        }
        else if (parent != null && behaviour == PASTE_AFTER)
        {
            Node nextSibling = selectedNode.getNextSibling();
            if (nextSibling != null)
            {
                parent.insertBefore(newNode, nextSibling);
            }
            else
            {
                parent.appendChild(newNode);
            }
        }
        else if (behaviour == PASTE_ASCHILD)
        {
            selectedNode.appendChild(newNode);
        }
    }

    public void nodeUnselected(Node node)
    {
        setEnabled(false);
    }

    public void nodeSelected(Node node)
    {
        if (behaviour == PASTE_ASCHILD && !(node.getNodeType() == Node.ELEMENT_NODE || node.getNodeType() == Node.DOCUMENT_NODE))
            setEnabled(false);
        else
            setEnabled(true);
    }
}
