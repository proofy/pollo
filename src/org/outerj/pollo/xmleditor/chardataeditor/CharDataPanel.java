package org.outerj.pollo.xmleditor.chardataeditor;

import org.outerj.pollo.DomConnected;
import org.outerj.pollo.gui.SomeLinesBorder;
import org.outerj.pollo.xmleditor.SelectionListener;
import org.outerj.pollo.xmleditor.model.XmlModel;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Node;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class CharDataPanel extends JPanel implements SelectionListener, EventListener, DomConnected
{
    protected XmlModel xmlModel;
    protected JTextArea charDataTextArea;
    protected JScrollPane scrollPane;
    protected Node currentNode;
    protected int nodetype;

    /**
     * @param nodetype must be one of the following constants defined in
     * org.w3c.dom.Node : CDATA_SECTION_NODE, TEXT_NODE, COMMENT_NODE,
     * PROCESSIG_INSTRUCTION_NODE. This parameter specifies to which type
     * of node this editor will react.
     */
    public CharDataPanel(XmlModel xmlModel, int nodetype)
    {
        this.xmlModel = xmlModel;
        this.nodetype = nodetype;

        this.setLayout(new BorderLayout());

        charDataTextArea = new JTextArea();
        charDataTextArea.setFont(new Font("Monospaced", 0, 12));
        charDataTextArea.setBorder(BorderFactory.createEmptyBorder());

        scrollPane = new JScrollPane(charDataTextArea);
        scrollPane.setBorder(new SomeLinesBorder(false, false, true, false));
        this.add(scrollPane, BorderLayout.CENTER);

        JButton applyButton = new JButton("Apply changes");
        applyButton.addActionListener(new ActionListener()
                {
                    public void actionPerformed(ActionEvent event)
                    {
                        saveChanges();
                    }
                });

        Box buttons = new Box(BoxLayout.X_AXIS);
        buttons.add(Box.createGlue());
        buttons.add(applyButton);

        this.add(buttons, BorderLayout.SOUTH);
    }

    /**
     * Implementation of the SelectionListener interface.
     */
    public void nodeSelected(Node node)
    {
        saveChanges();

        if (node.getNodeType() == nodetype)
        {
            String data;
            if (node instanceof CharacterData)
                data = ((CharacterData)node).getData();
            else if (node instanceof ProcessingInstruction)
                data = ((ProcessingInstruction)node).getData();
            else
                throw new RuntimeException("[CharDataPanel] Unsupported node: " + currentNode);
            charDataTextArea.setText(data);
            charDataTextArea.setCaretPosition(0);

            ((EventTarget)node).addEventListener("DOMCharacterDataModified", this, false);
            currentNode = node;

            setEnabled(true);
        }
        else
        {
            setEnabled(false);
        }
    }

    /**
     * Implementation of the SelectionListener interface.
     */
    public void nodeUnselected(Node node)
    {
        saveChanges();
        setEnabled(false);

        if (currentNode != null)
        {
            ((EventTarget)currentNode).removeEventListener("DOMCharacterDataModified", this, false);
            currentNode = null;
        }
    }

    public void saveChanges()
    {
        if (currentNode != null)
        {
            String data;
            if (currentNode instanceof CharacterData)
                data = ((CharacterData)currentNode).getData();
            else if (currentNode instanceof ProcessingInstruction)
                data = ((ProcessingInstruction)currentNode).getData();
            else
                throw new RuntimeException("[CharDataPanel] Unsupported node: " + currentNode);

            if (!data.equals(charDataTextArea.getText()))
            {
                if (currentNode instanceof CharacterData)
                    ((CharacterData)currentNode).setData(charDataTextArea.getText());
                else if (currentNode instanceof ProcessingInstruction)
                    ((ProcessingInstruction)currentNode).setData(charDataTextArea.getText());
                else
                    throw new RuntimeException("[CharDataPanel] Unsupported node: " + currentNode);
            }
        }
    }

    /**
     * Implementation of the org.w3c.dom.event.EventListener inteface
     */
    public void handleEvent(Event e)
    {
        try
        {
            if (e.getType().equalsIgnoreCase("DOMCharacterDataModified"))
            {
                if (currentNode instanceof CharacterData)
                    charDataTextArea.setText(((CharacterData)currentNode).getData());
                else if (currentNode instanceof ProcessingInstruction)
                    charDataTextArea.setText(((ProcessingInstruction)currentNode).getData());
                else
                    throw new RuntimeException("[CharDataPanel] Unsupported node: " + currentNode);
            }
            else
            {
                System.out.println("WARNING: unprocessed dom event:" + e.getType());
            }
        }
        catch (Exception exc)
        {
            // this try-catch is necessary because if an exception occurs in this code,
            // it is catched somewhere in the xerces code and we wouldn't see it or know
            // what's going on.
            exc.printStackTrace();
        }
    }

    public void requestFocus()
    {
        charDataTextArea.requestFocus();
    }

    public void disconnectFromDom()
    {
        if (currentNode != null)
        {
            ((EventTarget)currentNode).removeEventListener("DOMCharacterDataModified", this, false);
            currentNode = null;
        }
    }

    public void reconnectToDom()
    {
        disconnectFromDom();
    }
}
