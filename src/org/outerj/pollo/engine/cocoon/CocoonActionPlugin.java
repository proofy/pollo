package org.outerj.pollo.engine.cocoon;

import org.outerj.pollo.Pollo;
import org.outerj.pollo.PolloFrame;
import org.outerj.pollo.EditorPanelImpl;
import org.outerj.pollo.EditorPanel;
import org.outerj.pollo.plugin.IActionPlugin;
import org.outerj.pollo.xmleditor.exception.PolloException;
import org.outerj.pollo.xmleditor.model.XmlModel;
import org.outerj.pollo.xmleditor.XmlEditorPanel;
import org.outerj.pollo.xmleditor.XmlEditor;
import org.outerj.pollo.xmleditor.view.View;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Attr;
import org.jaxen.XPath;
import org.jaxen.SimpleNamespaceContext;
import org.jaxen.dom.DOMXPath;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.HashMap;

public class CocoonActionPlugin implements IActionPlugin
{
    protected final String SITEMAP_NS = "http://apache.org/cocoon/sitemap/1.0";
    protected EditorPanel editorPanel;
    protected PolloFrame polloFrame;
    protected XmlModel xmlModel;
    protected XmlEditor xmlEditor;
    protected SimpleNamespaceContext namespaceContext;
    protected GotoMatcherAction gotoMatcherAction;


    /**
     * This map contains mappings betweeen sitemap element names and the
     * name of the xml element that declares this type of component, e.g.
     * generate and generator
     */
    protected static final HashMap sitemapComponentMap = new HashMap();

    static
    {
        sitemapComponentMap.put("generate", "generator");
        sitemapComponentMap.put("transform", "transformer");
        sitemapComponentMap.put("read", "reader");
        sitemapComponentMap.put("serialize", "serializer");
        sitemapComponentMap.put("select", "selector");
        sitemapComponentMap.put("match", "matcher");
        sitemapComponentMap.put("act", "action");
    }

    public void init(HashMap initParams, EditorPanel editorPanel, PolloFrame polloFrame)
        throws PolloException
    {
        this.editorPanel = editorPanel;
        this.polloFrame = polloFrame;

        namespaceContext = new SimpleNamespaceContext();
        namespaceContext.addNamespace("map", SITEMAP_NS);
    }

    private final void lateInitialisation()
    {
        // The initialisation of this ActionPlugin is done 'late' because during the
        // init method, the EditorPanel is not yet fully initialised, e.g. the XmlEditor
        // does not exist yet then

        if (this.xmlModel == null)
        {
            xmlModel = editorPanel.getXmlModel();
        }
        if (this.xmlEditor == null)
        {
            // Note: the method getXmlEditorPanel() is not in the EditorPanel interface,
            // because that interface should remain independent of the type of views in
            // the EditorPanel
            xmlEditor = ((EditorPanelImpl)editorPanel).getXmlEditorPanel().getXmlEditor();
        }
        if (this.gotoMatcherAction == null)
        {
            gotoMatcherAction = new GotoMatcherAction();
        }
    }

    public void addActionsToPluginMenu(JMenu menu, Node selectedNode)
    {
        lateInitialisation();

        menu.add(gotoMatcherAction);

        if (selectedNode != null && selectedNode instanceof Element)
        {
            Element element = (Element)selectedNode;
            if (element.getLocalName().equals("generate") && SITEMAP_NS.equals(element.getNamespaceURI()))
            {
                menu.add(new EditSourceAction("Edit generator source", element, xmlModel, polloFrame));
            }

            if (SITEMAP_NS.equals(element.getNamespaceURI()) && sitemapComponentMap.containsKey(element.getLocalName()))
            {
                menu.add(new GotoComponentDeclarationAction(element));
            }
        }
    }

    public class EditSourceAction extends AbstractAction
    {
        PolloFrame polloFrame;
        Element element;
        XmlModel xmlModel;

        public EditSourceAction(String name, Element element, XmlModel xmlModel, PolloFrame polloFrame)
        {
            super(name);

            this.polloFrame = polloFrame;
            this.element = element;
            this.xmlModel = xmlModel;
        }

        public void actionPerformed(ActionEvent e)
        {
            String filename = element.getAttribute("src");
            if (filename == null)
            {
                JOptionPane.showMessageDialog(polloFrame, "The element has no 'src' attribute");
                return;
            }

            File relativePath = xmlModel.getFile();
            if (relativePath != null)
                relativePath = relativePath.getParentFile();
            File file = new File(relativePath, filename); // relativePath is allowed to be null
            if (!file.exists())
            {
                JOptionPane.showMessageDialog(polloFrame, "The file " + file.getAbsolutePath() + " does not exist.");
                return;
            }

            Pollo.getInstance().openFile(file, polloFrame);
        }
    }


    public class GotoComponentDeclarationAction extends AbstractAction
    {
        Element element;

        public GotoComponentDeclarationAction(Element element)
        {
            super("Goto component declaration");
            this.element = element;
        }

        public void actionPerformed(ActionEvent evt)
        {
            try
            {
                String componentName = element.getLocalName();
                String componentType = element.getAttribute("type");
                if (componentType != null && componentType.equals(""))
                    componentType = null;
                String componentDeclartionName = (String)sitemapComponentMap.get(componentName);


                if (componentType == null)
                {
                    // find out what the default type is
                    String typeXPathString = "/map:sitemap/map:components/map:" + componentDeclartionName + "s/@default";
                    XPath xpath = new DOMXPath(typeXPathString);
                    xpath.setNamespaceContext(namespaceContext);
                    Attr attr = (Attr)xpath.selectSingleNode(xmlModel.getDocument().getDocumentElement());
                    if (attr != null)
                        componentType = attr.getValue();
                    else
                    {
                        JOptionPane.showMessageDialog(polloFrame, "This element has no type attribute and there is no default " + componentDeclartionName + " defined.");
                        return;
                    }
                }

                String declarationXPathString = "/map:sitemap/map:components/map:" + componentDeclartionName + "s/map:" + componentDeclartionName + "[@name='" + componentType + "']";
                XPath xpath = new DOMXPath(declarationXPathString);
                xpath.setNamespaceContext(namespaceContext);
                Element foundElement = (Element)xpath.selectSingleNode(xmlModel.getDocument().getDocumentElement());
                if (foundElement != null)
                {
                    View view = (View)xmlEditor.getRootView().findNode(foundElement);
                    view.assureVisibility(false);
                    int startV = view.getVerticalPosition();
                    int startH = view.getHorizontalPosition();
                    view.markAsSelected(startH, startV);
                    xmlEditor.scrollAlignTop(startV, view.getHeight());
                }
                else
                {
                    JOptionPane.showMessageDialog(polloFrame, "No component declaration found for this type of " + componentDeclartionName);
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    public class GotoMatcherAction extends AbstractAction
    {
        public GoToMatcherDialog dialog;

        public GotoMatcherAction()
        {
            super("Goto matcher...");
            dialog = new GoToMatcherDialog(polloFrame, xmlModel);
        }

        public void actionPerformed(ActionEvent evt)
        {
            Element selected = dialog.showIt();
            if (selected != null)
            {
                View view = (View)xmlEditor.getRootView().findNode(selected);
                view.assureVisibility(false);
                int startV = view.getVerticalPosition();
                int startH = view.getHorizontalPosition();
                view.markAsSelected(startH, startV);
                xmlEditor.scrollAlignTop(startV, view.getHeight());
            }
        }
    }
}
