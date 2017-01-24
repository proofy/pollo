package org.outerj.pollo.xmleditor.model;

import org.outerj.pollo.DomConnected;
import org.outerj.pollo.xmleditor.IconManager;
import org.w3c.dom.*;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;
import org.w3c.dom.events.MutationEvent;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.Stack;


/**
 * The undo-class enables the undoing of modifications to the DOM-tree.
 * This is achieved by registring this class as mutation-event listener on the DOM tree.
 *
 * Normally for each DOM-event an appropriate {@link org.outerj.pollo.xmleditor.model.Undo.Undoable}
 * is created. But sometimes one user action corresponds to multiple modifications
 * to the DOM-tree, such as a drag-and-drop causes both a removal and an insertion of
 * a node. Therefore the concept of UndoTransaction's is introduced. If you're going
 * to do multiple changes, first call {@link #startUndoTransaction(String description)},
 * and when you're done, call {@link #endUndoTransaction()}.
 *
 * There's also a Swing Action, called UndoAction, which can be requested by
 * {@link #getUndoAction()}. This action will change it's description appropriately
 * and enable/disable itself.
 *
 * @author Bruno Dumon
 *
 */
public class Undo implements EventListener, DomConnected
{
    protected Stack undos;
    protected boolean undoing = false;
    protected UndoAction undoAction;
    protected XmlModel xmlModel;
    protected UndoTransaction currentUndoTransaction = null;
    protected int undoLevels;

    public Undo(XmlModel xmlModel, int undoLevels)
    {
        this.xmlModel = xmlModel;
        this.undoLevels = undoLevels;

        undoAction = new UndoAction();
        undoAction.setEnabled(false);
    }

    protected void init()
    {
        Node node = xmlModel.getDocument();
        // note to myself: if event listener are added here, remove them again
        // in disconnectFromDom() !
        ((EventTarget)node).addEventListener("DOMAttrModified", this, true);
        ((EventTarget)node).addEventListener("DOMNodeInserted", this, true);
        ((EventTarget)node).addEventListener("DOMNodeRemoved", this, true);
        ((EventTarget)node).addEventListener("DOMCharacterDataModified", this, true);

        setUndoInfo(null);

        undos = new LimitedSizeStack(undoLevels);
    }

    public void handleEvent(Event event)
    {
        xmlModel.markModified();
        if (undoing)
        {
            // if the current mutation event is the result of an undo operation,
            // ignore it.
            undoing = false;
            return;
        }

        try
        {
            Undoable undo = null;
            if (event.getType().equalsIgnoreCase("DOMNodeInserted"))
            {
                undo = new NodeInsertedUndo((MutationEvent)event);
            }
            else if (event.getType().equalsIgnoreCase("DOMNodeRemoved"))
            {
                undo = new NodeRemovedUndo((MutationEvent)event);
            }
            else if (event.getType().equalsIgnoreCase("DOMAttrModified"))
            {
                undo = new AttrModifiedUndo((MutationEvent)event);
            }
            else if (event.getType().equalsIgnoreCase("DOMCharacterDataModified"))
            {
                undo = new CharacterDataModifiedUndo((MutationEvent)event);
            }
            else
            {
                System.out.println("[undo] WARNING: unprocessed dom event: " + event.getType());
            }


            if (currentUndoTransaction != null)
            {
                currentUndoTransaction.addUndoable(undo);
                setUndoInfo(currentUndoTransaction);
            }
            else
            {
                undos.push(undo);
                setUndoInfo(undo);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void undo()
    {
        Undoable undoable = null;
        try
        {
            undoable = (Undoable)undos.pop();
        }
        catch (EmptyStackException ese)
        {
            System.out.println("nothing to undo");
            return;
        }

        if (undos.empty())
        {
            setUndoInfo(null);
        }
        else
        {
            setUndoInfo((Undoable)undos.peek());
        }


        undoing = true;
        undoable.undo();
    }

    private final void setUndoInfo(Undoable undoable)
    {
        if (undoable == null)
        {
            undoAction.putValue(UndoAction.NAME, "Undo");
            undoAction.putValue(UndoAction.SHORT_DESCRIPTION, "Nothing to undo");
            undoAction.setEnabled(false);
        }
        else
        {
            String description = "Undo " + undoable.getDescription();
            undoAction.putValue(UndoAction.NAME, description);
            undoAction.putValue(UndoAction.SHORT_DESCRIPTION, description);
            undoAction.setEnabled(true);
        }
    }

    public void startUndoTransaction(String description)
    {
        UndoTransaction undoTransaction = new UndoTransaction(description);
        currentUndoTransaction = undoTransaction;
        undos.push(undoTransaction);
    }

    public void endUndoTransaction()
    {
        currentUndoTransaction = null;
    }

    public interface Undoable
    {
        public void undo();
        public String getDescription();
    }

    public class NodeInsertedUndo implements Undoable
    {
        public Node insertedNode;

        public NodeInsertedUndo(MutationEvent me)
        {
            insertedNode = (Node)me.getTarget();
        }

        public void undo()
        {
            Node parent = insertedNode.getParentNode();
            parent.removeChild(insertedNode);
        }

        public String getDescription()
        {
            if (insertedNode.getNodeType() == Node.COMMENT_NODE)
            {
                return "insert comment node";
            }
            else if (insertedNode.getNodeType() == Node.TEXT_NODE)
            {
                return "insert text node";
            }
            else if (insertedNode.getNodeType() == Node.CDATA_SECTION_NODE)
            {
                return "insert cdata section";
            }
            else if (insertedNode.getNodeType() == Node.PROCESSING_INSTRUCTION_NODE)
            {
                return "insert processing instruction";
            }
            else if (insertedNode.getNodeType() == Node.ELEMENT_NODE)
            {
                Element insertedEl = (Element)insertedNode;
                String prefix = insertedEl.getPrefix();
                String localName = insertedEl.getLocalName();
                String qname = prefix != null? prefix + ":" + localName : localName;
                return "insert node <" + qname + ">";
            }
            else
            {
                return "insert node of unkown type?!";
            }
        }
    }


    public class NodeRemovedUndo implements Undoable
    {
        public Node removedNode;
        public Node parentNode;
        public Node nextSibling;

        public NodeRemovedUndo(MutationEvent me)
        {
            removedNode = (Node)me.getTarget();
            parentNode = removedNode.getParentNode();
            nextSibling = removedNode.getNextSibling();
        }

        public void undo()
        {
            parentNode.insertBefore(removedNode, nextSibling);
        }

        public String getDescription()
        {
            switch (removedNode.getNodeType())
            {
                case Node.COMMENT_NODE:
                    return "remove comment node";
                case Node.TEXT_NODE:
                    return "remove text node";
                case Node.CDATA_SECTION_NODE:
                    return "remove cdata section";
                case Node.PROCESSING_INSTRUCTION_NODE:
                    return "remove processing instruction";
                case Node.ELEMENT_NODE:
                    Element removedEl = (Element)removedNode;
                    String prefix = removedEl.getPrefix();
                    String localName = removedEl.getLocalName();
                    String qname = prefix != null? prefix + ":" + localName : localName;
                    return "remove node <" + qname + ">";
                default:
                    return "remove unknown node type?!";
            }
        }
    }


    public class AttrModifiedUndo implements Undoable
    {
        protected int type;
        protected Element element;
        protected String namespaceURI;
        protected String localName;
        protected String prefix;
        protected String value;
        protected String newValue;

        public AttrModifiedUndo(MutationEvent me)
        {
            element = (Element)me.getTarget();
            type = me.getAttrChange();

            Attr attr = (Attr)me.getRelatedNode();

            // is an attribute is removed, attr will be null so we have
            // to get the attr some other way (though there's no way
            // to do this in a namespace-safe way, so FIXME)
            if (attr == null)
            {
                localName = me.getAttrName();
                namespaceURI = null;
                prefix = null;
            }
            else if (attr.getLocalName() != null)
            {
                namespaceURI = attr.getNamespaceURI();
                localName = attr.getLocalName();
                prefix = attr.getPrefix();
            }
            else
            {
                localName = attr.getName();
            }
            value = me.getPrevValue();
            if (attr != null)
                newValue = attr.getValue();
        }

        public void undo()
        {
            if (type == MutationEvent.ADDITION)
            {
                if (namespaceURI != null)
                    element.removeAttributeNS(namespaceURI, localName);
                else
                    element.removeAttribute(localName);
            }
            else
            {
                element.setAttributeNS(namespaceURI, localName, value);
            }
        }

        public String getDescription()
        {
            String qname = prefix != null? prefix + ":" + localName : localName;
            String elQName = element.getPrefix() != null ? element.getPrefix() +
                ":" + element.getLocalName() : element.getLocalName();
            switch(type)
            {
                case MutationEvent.ADDITION:
                    return "add attribute '" + qname + "' to <" + elQName + ">";
                case MutationEvent.MODIFICATION:
                    return "change attribute '" + qname + "' of <" + elQName +
                        "> to '" + newValue + "'";
                case MutationEvent.REMOVAL:
                    return "removal of attribute '" + qname + "' from <" +
                        elQName + ">";
            }
            return "Undo attribute modification";
        }
    }

    /**
     * Can be used for both CharacterData and ProcessingInstruction nodes.
     */
    public class CharacterDataModifiedUndo implements Undoable
    {
        protected String oldValue;
        protected Node characterData;

        public CharacterDataModifiedUndo(MutationEvent me)
        {
            characterData = (Node)me.getTarget();
            oldValue = me.getPrevValue();
        }

        public void undo()
        {
            if (characterData instanceof CharacterData)
                ((CharacterData)characterData).setData(oldValue);
            else if (characterData instanceof ProcessingInstruction)
                ((ProcessingInstruction)characterData).setData(oldValue);
            else
                throw new RuntimeException("[Undo] Unexpected node type: " + characterData);
        }

        public String getDescription()
        {
            switch (characterData.getNodeType())
            {
                case Node.TEXT_NODE:
                    return "change text";
                case Node.COMMENT_NODE:
                    return "change comment";
                case Node.CDATA_SECTION_NODE:
                    return "change CDATA section";
                case Node.PROCESSING_INSTRUCTION_NODE:
                    return "change processing instruction";
                default:
                    return "change I-don't-know-what";
            }
        }
    }


    public UndoAction getUndoAction()
    {
        return undoAction;
    }

    /**
     * An undo transaction groups several undoable events into one transaction.
     * E.g. if a user drags and drops a node, this causes a node remove and node
     * insert event, although this only corresponds to one user action. Therefore,
     * those actions are combined into one UndoTransaction so that they can be
     * undone in one step.
     * */
    public class UndoTransaction implements Undoable
    {
        protected String description;
        protected ArrayList transactionUndoables = new ArrayList();

        public UndoTransaction(String description)
        {
            this.description = description;
        }

        public void addUndoable(Undoable undoable)
        {
            transactionUndoables.add(undoable);
        }

        public void undo()
        {
            int totalUndoables = transactionUndoables.size();

            for (int i = totalUndoables - 1; i >= 0; i--)
            {
                undoing = true;
                Undoable undoable = (Undoable)transactionUndoables.get(i);
                undoable.undo();
            }
        }

        public String getDescription()
        {
            return description;
        }
    }

    public class UndoAction extends AbstractAction
    {
        protected UndoAction()
        {
            super("Undo", IconManager.getIcon("org/outerj/pollo/resource/Undo16.gif"));
        }

        public void actionPerformed(ActionEvent e)
        {
            undo();
        }
    }

    public void disconnectFromDom()
    {
        Node node = xmlModel.getDocument();
        ((EventTarget)node).removeEventListener("DOMAttrModified", this, true);
        ((EventTarget)node).removeEventListener("DOMNodeInserted", this, true);
        ((EventTarget)node).removeEventListener("DOMNodeRemoved", this, true);
        ((EventTarget)node).removeEventListener("DOMCharacterDataModified", this, true);
    }

    public void reconnectToDom()
    {
        disconnectFromDom();
        init();
    }

    /**
     * Extension of Java's stack class that enforces a limit to the number of items
     * in the stack. Once the limit is reached, the oldest items are removed.
     */
    public class LimitedSizeStack extends Stack
    {
        private final int maxSize;

        public LimitedSizeStack(int maxSize)
        {
            this.maxSize = maxSize;
        }

        public Object push(Object item)
        {
            if (size() == maxSize)
                removeElementAt(0);

            return super.push(item);
        }
    }
}
