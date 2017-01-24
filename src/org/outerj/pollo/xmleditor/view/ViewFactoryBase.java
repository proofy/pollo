package org.outerj.pollo.xmleditor.view;

import org.w3c.dom.*;
import org.outerj.pollo.xmleditor.XmlEditor;

public class ViewFactoryBase implements ViewFactory
{
    protected XmlEditor xmlEditor;
    protected ViewStrategy viewStrategy;

    public ViewFactoryBase(XmlEditor xmlEditor, ViewStrategy viewStrategy)
    {
        this.xmlEditor = xmlEditor;
        this.viewStrategy = viewStrategy;
    }

    public View createView(Node node, View parentView)
    {
        if (node.getNodeType() == Node.ELEMENT_NODE)
        {
            ElementBlockView view = new ElementBlockView(parentView, (Element)node, xmlEditor, viewStrategy);
            createViewsRecursive(node, view);
            return view;
        }
        else if (node.getNodeType() == Node.DOCUMENT_NODE)
        {
            DocumentBlockView view = new DocumentBlockView(parentView, (Document)node, xmlEditor, viewStrategy);
            createViewsRecursive(node, view);
            return view;
        }
        else if (node.getNodeType() == Node.COMMENT_NODE)
        {
            return new CommentView(parentView, (Comment)node, xmlEditor, viewStrategy);
        }
        else if (node.getNodeType() == Node.TEXT_NODE)
        {
            return new TextView(parentView, (Text)node, xmlEditor, viewStrategy);
        }
        else if (node.getNodeType() == Node.CDATA_SECTION_NODE)
        {
            return new CDataView(parentView, (CDATASection)node, xmlEditor, viewStrategy);
        }
        else if (node.getNodeType() == Node.PROCESSING_INSTRUCTION_NODE)
        {
            return new PIView(parentView, (ProcessingInstruction)node, xmlEditor, viewStrategy);
        }
        else if (node.getNodeType() == Node.ENTITY_REFERENCE_NODE)
        {
            return new EntityReferenceView(parentView, (EntityReference)node, xmlEditor, viewStrategy);
        }
        return null;
    }

    private void createViewsRecursive(Node parentNode, View parentView)
    {
        NodeList children = parentNode.getChildNodes();

        for (int i = 0; i < children.getLength(); i++)
        {
            Node node = children.item(i);
            View childView = createView(node, parentView);
            if (childView != null)
                parentView.addChildView(childView);
        }
    }
}
