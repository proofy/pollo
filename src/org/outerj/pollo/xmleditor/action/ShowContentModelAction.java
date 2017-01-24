package org.outerj.pollo.xmleditor.action;

import com.sun.msv.grammar.*;
import com.sun.msv.grammar.util.ExpressionPrinter;
import org.outerj.pollo.gui.ErrorDialog;
import org.outerj.pollo.gui.EmptyIcon;
import org.outerj.pollo.xmleditor.*;
import org.outerj.pollo.xmleditor.exception.PolloException;
import org.outerj.pollo.xmleditor.schema.ChainedSchema;
import org.outerj.pollo.xmleditor.schema.ISchema;
import org.outerj.pollo.xmleditor.schema.MsvSchema;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * A Swing Action that shows the content model of the selected node.
 * This only works for MsvSchema's.
 *
 * This action automatically enables/disables itself.
 *
 * @author Bruno Dumon
 */
public class ShowContentModelAction extends AbstractAction implements SelectionListener
{
    protected XmlEditorPanel xmlEditorPanel;
    protected XmlEditor xmlEditor;
    protected ContentModelDialog contentModelDialog;

    public ShowContentModelAction(XmlEditorPanel xmlEditorPanel)
    {
        super("Show content model...", EmptyIcon.getInstance());
        this.xmlEditorPanel = xmlEditorPanel;
        this.xmlEditor = xmlEditorPanel.getXmlEditor();
        xmlEditor.getSelectionInfo().addListener(this);
        setEnabled(false);
    }

    public void actionPerformed(ActionEvent e)
    {
        ISchema schema = xmlEditorPanel.getSchema();
        Node selectedNode = xmlEditor.getSelectedNode();
        if (!(selectedNode instanceof Element))
        {
            JOptionPane.showMessageDialog(xmlEditorPanel.getTopLevelAncestor(), "An element must be selected to show the content model.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (schema instanceof ChainedSchema)
        {
            ISchema schema1 = ((ChainedSchema) schema).getSchema(0);
            if (schema1 instanceof MsvSchema)
            {
                try
                {
                    Expression expression = ((MsvSchema) schema1).getExpression(xmlEditor.getXmlModel().getDocument(), (Element)selectedNode);
                    System.out.println(ExpressionPrinter.printContentModel(expression));
                    ExpressionTreeBuilder etb = new ExpressionTreeBuilder();
                    TreeNode root = etb.createContentModelTree(expression, ((Element)selectedNode).getLocalName());
                    if (contentModelDialog == null)
                        contentModelDialog = new ContentModelDialog((Frame)xmlEditor.getTopLevelAncestor());
                    contentModelDialog.show(root);
                    return;
                }
                catch (Exception err)
                {
                    ErrorDialog errorDialog = new ErrorDialog((Frame)xmlEditor.getTopLevelAncestor(),
                            "Error while trying to show the content model.", err);
                    errorDialog.show();
                    return;
                }
            }
        }

        JOptionPane.showMessageDialog(xmlEditor.getTopLevelAncestor(), "Can only show the content model for MSV supported schema languages.",
                "Error", JOptionPane.ERROR_MESSAGE);
    }

    public void nodeUnselected(Node node)
    {
        setEnabled(false);
    }

    public void nodeSelected(Node node)
    {
        setEnabled(true);
    }

    public class ContentModelDialog extends JDialog implements ActionListener
    {
        protected JTree contentModelTree;

        public ContentModelDialog(Frame parent)
            throws PolloException
        {
            super(parent, "Content Model", true);

            JPanel panel = new JPanel();
            panel.setBorder(new EmptyBorder(12, 12, 12, 12));
            setContentPane(panel);
            panel.setLayout(new BorderLayout(12, 12));

            JLabel label = new JLabel("Content model:");
            panel.add(label, BorderLayout.NORTH);

            contentModelTree = new JTree();
            contentModelTree.putClientProperty("JTree.lineStyle", "Angled");
            contentModelTree.setCellRenderer(new MyRenderer());
            panel.add(new JScrollPane(contentModelTree), BorderLayout.CENTER);

            JButton closeButton = new JButton("Close");
            closeButton.setActionCommand("close");
            closeButton.addActionListener(this);
            Box box = new Box(BoxLayout.X_AXIS);
            box.add(Box.createHorizontalGlue());
            box.add(closeButton);
            panel.add(box, BorderLayout.SOUTH);

            panel.setPreferredSize(new Dimension(250, 300));

            pack();
        }

        public void show(TreeNode root)
        {
            contentModelTree.setModel(new DefaultTreeModel(root));
            setLocationRelativeTo(getParent());
            super.show();
        }

        public void actionPerformed(ActionEvent e)
        {
            if (e.getActionCommand().equals("close"))
            {
                hide();
            }
        }

        /**
         * A custom treecellrenderer for the JTree showing the content model.
         */
        class MyRenderer extends DefaultTreeCellRenderer
        {
            Icon attributeIcon;
            Icon elementIcon;
            Icon dataIcon;
            Icon valueIcon;

            public MyRenderer()
                throws PolloException
            {
                attributeIcon = IconManager.getIcon("org/outerj/pollo/xmleditor/icons/attribute.png");
                elementIcon = IconManager.getIcon("org/outerj/pollo/xmleditor/icons/element.png");
                dataIcon = IconManager.getIcon("org/outerj/pollo/xmleditor/icons/data.png");
                valueIcon = IconManager.getIcon("org/outerj/pollo/xmleditor/icons/value.png");
            }

            public Component getTreeCellRendererComponent(
                    JTree tree,
                    Object value,
                    boolean sel,
                    boolean expanded,
                    boolean leaf,
                    int row,
                    boolean hasFocus)
            {

                super.getTreeCellRendererComponent(
                        tree, value, sel,
                        expanded, leaf, row,
                        hasFocus);

                DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
                Object userData = node.getUserObject();
                if (userData instanceof NodeData)
                {
                    NodeData nodeData = (NodeData) userData;
                    switch (nodeData.type)
                    {
                        case NodeData.ATTRIBUTE:
                            setIcon(attributeIcon);
                            break;
                        case NodeData.ELEMENT:
                            setIcon(elementIcon);
                            break;
                        case NodeData.DATA:
                            setIcon(dataIcon);
                            break;
                        case NodeData.VALUE:
                            setIcon(valueIcon);
                        default:
                            setIcon(null);
                            break;
                    }
                }
                else
                {
                    // don't want the default icons to show up
                    setIcon(null);
                }
                return this;
            }

        }

    }

    public final class NodeData
    {
        public static final int ATTRIBUTE = 1;
        public static final int ELEMENT = 2;
        public static final int DATA = 3;
        public static final int VALUE = 4;

        public final String title;
        public final int type;

        public NodeData(String title, int type)
        {
            this.title = title;
            this.type = type;
        }

        public String toString()
        {
            return title;
        }
    }

    /**
     * Creates a tree-structure from an MSV Expression. This tree can then
     * be displayed by using Swing's JTree.
     *
     * Loosely based on the ExpressionPrinter from MSV.
     */
    public class ExpressionTreeBuilder implements ExpressionVisitorVoid
    {
        protected DefaultMutableTreeNode currentNode;

        public DefaultMutableTreeNode createContentModelTree(Expression exp, String elementLocalName)
        {
            currentNode = new DefaultMutableTreeNode(new NodeData(elementLocalName, NodeData.ELEMENT));
            exp.visit(this);
            return currentNode;
        }

        protected void createBinary(BinaryExp exp, String op)
        {
            boolean moveup = false;
            DefaultMutableTreeNode parent = (DefaultMutableTreeNode) currentNode.getParent();

            if (parent != null && !op.equals(((DefaultMutableTreeNode) currentNode.getParent()).getUserObject().toString()))
            {
                DefaultMutableTreeNode node = new DefaultMutableTreeNode(op);
                currentNode.add(node);
                currentNode = node;
                moveup = true;
            }

            exp.exp1.visit(this);
            exp.exp2.visit(this);

            if (moveup)
            {
                currentNode = (DefaultMutableTreeNode) currentNode.getParent();
            }
        }

        public void onAttribute(AttributeExp exp)
        {
            DefaultMutableTreeNode node = new DefaultMutableTreeNode(new NodeData(exp.nameClass.toString(), NodeData.ATTRIBUTE));
            currentNode.add(node);
            currentNode = node;
            exp.exp.visit(this);
            currentNode = (DefaultMutableTreeNode) node.getParent();
        }

        private void optional(Expression exp)
        {
            if (exp instanceof OneOrMoreExp)
            {
                OneOrMoreExp ome = (OneOrMoreExp) exp;
                DefaultMutableTreeNode node = new DefaultMutableTreeNode("one or more");
                currentNode.add(node);
                currentNode = node;
                ome.exp.visit(this);
                currentNode = (DefaultMutableTreeNode) node.getParent();
            }
            else
            {
                DefaultMutableTreeNode node = new DefaultMutableTreeNode("optional");
                currentNode.add(node);
                currentNode = node;
                exp.visit(this);
                currentNode = (DefaultMutableTreeNode) node.getParent();
            }
        }

        public void onChoice(ChoiceExp exp)
        {
            if (exp.exp1 == Expression.epsilon)
            {
                optional(exp.exp2);
                return;
            }
            if (exp.exp2 == Expression.epsilon)
            {
                optional(exp.exp1);
                return;
            }

            createBinary(exp, "choice");
        }

        public void onConcur(ConcurExp exp)
        {
            createBinary(exp, "concur");
        }

        public void onInterleave(InterleaveExp exp)
        {
            createBinary(exp, "interleave");
        }

        public void onElement(ElementExp exp)
        {
            DefaultMutableTreeNode node = new DefaultMutableTreeNode(new NodeData(exp.getNameClass().toString(), NodeData.ELEMENT));
            currentNode.add(node);
        }

        public void onOneOrMore(OneOrMoreExp exp)
        {
            DefaultMutableTreeNode node = new DefaultMutableTreeNode("one or more");
            currentNode.add(node);
            currentNode = node;
            exp.exp.visit(this);
            currentNode = (DefaultMutableTreeNode) node.getParent();
        }

        public void onMixed(MixedExp exp)
        {
            DefaultMutableTreeNode node = new DefaultMutableTreeNode("mixed");
            currentNode.add(node);
            currentNode = node;
            exp.exp.visit(this);
            currentNode = (DefaultMutableTreeNode) node.getParent();
        }

        public void onList(ListExp exp)
        {
            DefaultMutableTreeNode node = new DefaultMutableTreeNode("list");
            currentNode.add(node);
            currentNode = node;
            exp.exp.visit(this);
            currentNode = (DefaultMutableTreeNode) node.getParent();
        }

        public void onEpsilon()
        {
            DefaultMutableTreeNode node = new DefaultMutableTreeNode("#epsilon");
            currentNode.add(node);
        }

        public void onNullSet()
        {
            DefaultMutableTreeNode node = new DefaultMutableTreeNode("#nullSet");
            currentNode.add(node);
        }

        public void onAnyString()
        {
            DefaultMutableTreeNode node = new DefaultMutableTreeNode("<anyString>");
            currentNode.add(node);
        }

        public void onSequence(SequenceExp exp)
        {
            createBinary(exp, "sequence");
        }

        public void onData(DataExp exp)
        {
            DefaultMutableTreeNode node = new DefaultMutableTreeNode(new NodeData(exp.name.localName, NodeData.DATA));
            currentNode.add(node);
        }

        public void onValue(ValueExp exp)
        {
            DefaultMutableTreeNode node = new DefaultMutableTreeNode(new NodeData("" + exp.value, NodeData.VALUE));
            currentNode.add(node);
        }

        public void onOther(OtherExp exp)
        {
            DefaultMutableTreeNode node = new DefaultMutableTreeNode(exp.printName());
            currentNode.add(node);
            currentNode = node;
            exp.exp.visit(this);
            currentNode = (DefaultMutableTreeNode) node.getParent();
        }

        public void onRef(ReferenceExp exp)
        {
            exp.exp.visit(this);
        }
    }
}
