package org.outerj.pollo.xmleditor.action;

import org.outerj.pollo.gui.ErrorDialog;
import org.outerj.pollo.gui.EmptyIcon;
import org.outerj.pollo.xmleditor.SelectionListener;
import org.outerj.pollo.xmleditor.XmlEditor;
import org.outerj.pollo.xmleditor.model.PolloDOMParser;
import org.outerj.pollo.xmleditor.model.Undo;
import org.w3c.dom.*;
import org.xml.sax.InputSource;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

public class UncommentAction extends AbstractAction implements SelectionListener
{
    protected XmlEditor xmlEditor;
    protected int behaviour;

    public UncommentAction(XmlEditor xmlEditor)
    {
        super("Uncomment", EmptyIcon.getInstance());
        this.xmlEditor = xmlEditor;

        xmlEditor.getSelectionInfo().addListener(this);
        setEnabled(false);
    }

    public void actionPerformed(ActionEvent event)
    {
        Node selectedNode = xmlEditor.getSelectedNode();
        
        if (selectedNode.getParentNode()  instanceof Document)
        {
            JOptionPane.showMessageDialog(xmlEditor.getTopLevelAncestor(),
                    "Sorry, uncomment is not supported at this place.");    
            return;
        }

        String data = ((Comment)selectedNode).getData();

        // put the data inside a wrapper tag. This wrapper tag declares all the namespaces known
        // at this location in the document.
        StringBuffer wrapperTag = new StringBuffer();
        wrapperTag.append("<wrapper ");
        HashMap namespaceDeclarations = xmlEditor.getXmlModel().findNamespaceDeclarations((Element)selectedNode.getParentNode());
        Iterator it = namespaceDeclarations.entrySet().iterator();
        while (it.hasNext())
        {
            Entry entry = (Entry)it.next();
            wrapperTag.append("xmlns:");
            wrapperTag.append(entry.getKey());
            wrapperTag.append("='");
            // FIXME this value should probably be checked for problematic characters (such as &)
            wrapperTag.append(entry.getValue());
            wrapperTag.append("' ");
        }
        wrapperTag.append(">");

        StringReader commentReader = new StringReader(wrapperTag.toString() + data + "</wrapper>");
        Document parsedComment = null;

        try
        {
            /* JAXP
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            documentBuilderFactory.setNamespaceAware(true);
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            parsedComment = documentBuilder.parse(new InputSource(commentReader));
            */
            PolloDOMParser parser = new PolloDOMParser();
            parser.setFeature("http://apache.org/xml/features/dom/defer-node-expansion",false);
            parser.setFeature("http://xml.org/sax/features/namespaces", true);
            parser.parse(new InputSource(commentReader));
            parsedComment = parser.getDocument();
        }
        catch (Exception e)
        {
            ErrorDialog errorDialog = new ErrorDialog((Frame)xmlEditor.getTopLevelAncestor(),
                    "Could not parse the contents of this comment.", e);
            errorDialog.show();
            return;
        }

        Element parent = (Element)selectedNode.getParentNode();

        Undo undo = xmlEditor.getXmlModel().getUndo();
        undo.startUndoTransaction("Uncomment");

        NodeList newNodes = parsedComment.getDocumentElement().getChildNodes();
        Document document = xmlEditor.getXmlModel().getDocument();
        for (int i = 0; i < newNodes.getLength(); i++)
        {
            // currently only element and comment nodes are supported.
            if (XmlEditor.isNodeTypeSupported(newNodes.item(i).getNodeType()))
            {
                Node newNode = document.importNode(newNodes.item(i), true);
                parent.insertBefore(newNode, selectedNode);
            }
        }

        parent.removeChild(selectedNode);
        undo.endUndoTransaction();
    }

    public void nodeUnselected(Node node)
    {
        setEnabled(false);
    }

    public void nodeSelected(Node node)
    {
        if (node instanceof Comment)
            setEnabled(true);
    }
}
