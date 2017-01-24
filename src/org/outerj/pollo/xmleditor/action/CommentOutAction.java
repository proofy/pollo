package org.outerj.pollo.xmleditor.action;

import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.outerj.pollo.gui.ErrorDialog;
import org.outerj.pollo.gui.EmptyIcon;
import org.outerj.pollo.xmleditor.SelectionListener;
import org.outerj.pollo.xmleditor.XmlEditor;
import org.outerj.pollo.xmleditor.model.Undo;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.StringWriter;

/**
 * A Swing Action to comment out the selected node. This serializes the selected
 * node to XML, removes the node, and creates a new comment node containing the XML.
 *
 * @author Bruno Dumon
 */
public class CommentOutAction extends AbstractAction implements SelectionListener
{
    protected XmlEditor xmlEditor;

    public CommentOutAction(XmlEditor xmlEditor)
    {
        super("Comment out", EmptyIcon.getInstance());

        this.xmlEditor = xmlEditor;

        xmlEditor.getSelectionInfo().addListener(this);
        setEnabled(false);
    }

    public void actionPerformed(ActionEvent event)
    {
        Node selectedNode = xmlEditor.getSelectedNode();

        if (selectedNode instanceof Document)
        {
            JOptionPane.showMessageDialog(xmlEditor.getTopLevelAncestor(),
                    "You cannot comment out the XML document itself."); 
            return;
        }
        StringWriter commentWriter = new StringWriter();

        try
        {
            OutputFormat outputFormat = new OutputFormat();
            outputFormat.setIndenting(true);
            outputFormat.setIndent(2);
            outputFormat.setOmitXMLDeclaration(true);

            XMLSerializer serializer = new XMLSerializer(commentWriter, outputFormat);
            serializer.serialize((Element)selectedNode);

            /* JAXP
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty("omit-xml-declaration", "yes");
            transformer.transform(new DOMSource(selectedNode), new StreamResult(commentWriter));
            */
        }
        catch (Exception e)
        {
            ErrorDialog errorDialog = new ErrorDialog((Frame)xmlEditor.getTopLevelAncestor(),
                    "Could not serialize the selected node.", e);
            errorDialog.show();
            return;
        }

        Node parent = selectedNode.getParentNode();

        Node newNode = xmlEditor.getXmlModel().getDocument().createComment(commentWriter.toString());

        Undo undo = xmlEditor.getXmlModel().getUndo();
        undo.startUndoTransaction("Comment out node of the node <" + selectedNode.getLocalName() + ">");
        parent.insertBefore(newNode, selectedNode);
        parent.removeChild(selectedNode);
        undo.endUndoTransaction();

    }

    public void nodeUnselected(Node node)
    {
        setEnabled(false);
    }

    public void nodeSelected(Node node)
    {
        if (!(node instanceof Comment))
            setEnabled(true);
    }
}
