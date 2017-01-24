package org.outerj.pollo.xmleditor.action;

import org.outerj.pollo.xmleditor.SelectionListener;
import org.outerj.pollo.xmleditor.XmlEditor;
import org.outerj.pollo.util.ResourceManager;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * An action to insert comment, text or cdata nodes before, after
 * or as child of the currently selected node.
 *
 * <p>It would have been more OO to create 12 different Actions instead of this one,
 * but I'll save that cleanup for later.
 */
public class InsertCharacterDataAction extends AbstractAction implements SelectionListener
{
    public static final int INSERT_BEFORE  = 1;
    public static final int INSERT_AFTER   = 2;
    public static final int INSERT_INSIDE  = 3;

    public static final int TYPE_TEXT      = 4;
    public static final int TYPE_COMMENT   = 5;
    public static final int TYPE_CDATA     = 6;
    public static final int TYPE_PI        = 7;

    protected XmlEditor xmlEditor;
    protected int behaviour;
    protected int type;
    protected static final ResourceManager resourceManager = ResourceManager.getManager(InsertCharacterDataAction.class);

    public InsertCharacterDataAction(XmlEditor xmlEditor, int behaviour, int type)
    {
        this.xmlEditor = xmlEditor;
        this.behaviour = behaviour;
        this.type      = type;
        setEnabled(false);
        xmlEditor.getSelectionInfo().addListener(this);

        String propertyPrefix;
        if (type == TYPE_TEXT)
            propertyPrefix = "insertText";
        else if (type == TYPE_COMMENT)
            propertyPrefix = "insertComment";
        else if (type == TYPE_CDATA)
            propertyPrefix = "insertCData";
        else if (type == TYPE_PI)
            propertyPrefix = "insertPI";
        else
            throw new RuntimeException("[InsertCharacterDataAction] Invalid type: " + type);

        if (behaviour == INSERT_BEFORE)
            propertyPrefix += "BeforeAction";
        else if (behaviour == INSERT_AFTER)
            propertyPrefix += "AfterAction";
        else if (behaviour == INSERT_INSIDE)
            propertyPrefix += "InsideAction";
        else
            throw new RuntimeException("[InsertCharacterDataAction] Invalid behaviour: " + behaviour);

        resourceManager.configureAction(propertyPrefix, this);

    }

    public void actionPerformed(ActionEvent e)
    {
        Node selectedNode = xmlEditor.getSelectedNode();

        Node parent = selectedNode.getParentNode();

        if (parent instanceof Document
                && (behaviour == INSERT_BEFORE || behaviour == INSERT_AFTER)
                && (type != TYPE_COMMENT && type != TYPE_PI))
        {
            JOptionPane.showMessageDialog(xmlEditor.getTopLevelAncestor(),
                    "That cannot be inserted at this place.");  
            return;
        }

        Node newNode = null;
        switch (type)
        {
            case TYPE_TEXT:
                newNode = xmlEditor.getXmlModel().getDocument().
                    createTextNode("I'm a new text node.");
                break;
            case TYPE_COMMENT:
                newNode = xmlEditor.getXmlModel().getDocument().
                    createComment("I'm a new comment node.");
                break;
            case TYPE_CDATA:
                newNode = xmlEditor.getXmlModel().getDocument().
                    createCDATASection("I'm a new CDATA section.");
                break;
            case TYPE_PI:
                String target = JOptionPane.showInputDialog(xmlEditor.getTopLevelAncestor(),
                        "Processing instruction target?", "Create Processing Instruction",
                        JOptionPane.QUESTION_MESSAGE);
                if (target == null || target.trim().equals(""))
                    return;

                if (!org.apache.xerces.dom.DocumentImpl.isXMLName(target))
                {
                    JOptionPane.showMessageDialog(xmlEditor.getTopLevelAncestor(),
                            "That is not a valid XML target name.",
                            "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                newNode = xmlEditor.getXmlModel().getDocument().createProcessingInstruction(target, "");
                break;
        }


        if (behaviour == INSERT_BEFORE)
        {
            parent.insertBefore(newNode, selectedNode);
        }
        else if (behaviour == INSERT_AFTER)
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
        else if (behaviour == INSERT_INSIDE)
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
        if (behaviour == INSERT_INSIDE)
        {
            if (node.getNodeType() == Node.DOCUMENT_NODE)
            {
                if (!(type == TYPE_COMMENT || type == TYPE_PI))
                    setEnabled(false);
                else
                    setEnabled(true);
            }
            else if (node.getNodeType() != Node.ELEMENT_NODE)
            {
                setEnabled(false);
            }
            else
            {
                setEnabled(true);
            }
        }
        else if (node.getNodeType() == Node.DOCUMENT_NODE)
        {
            if (!(type == TYPE_COMMENT || type == TYPE_PI))
                setEnabled(false);
            else
                setEnabled(true);
        }
        else
        {
            setEnabled(true);
        }
    }
}
