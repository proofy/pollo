package org.outerj.pollo.xmleditor.model;

import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.jaxen.SimpleNamespaceContext;
import org.jaxen.XPath;
import org.jaxen.dom.DOMXPath;
import org.outerj.pollo.texteditor.XMLTokenMarker;
import org.outerj.pollo.texteditor.XmlTextDocument;
import org.w3c.dom.*;
import org.xml.sax.InputSource;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Segment;
import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;


/**
 * In-memory representation of an XML file. The XML file can be in two
 * formats: as a DOM-tree ('parsed mode'), or as XmlTextDocument ('text mode').
 * All views on the XmlModel should reflect this, they should be all in
 * parsed mode or text mode.
 *
 * This class also has methods for loading and storing the file.
 *
 * There are some utility functions for searching namespace declarations and
 * getting nodes using xpath expressions.
 *
 * @author Bruno Dumon
 */
public class XmlModel
{
    public static final int PARSED_MODE = 1;
    public static final int TEXT_MODE   = 2;

    protected Document domDocument;
    protected XmlTextDocument textDocument;
    protected int mode;

    protected File file;
    protected Undo undo;
    protected ArrayList registeredViewsList = new ArrayList();
    protected ArrayList xmlModelListeners = new ArrayList();

    protected boolean modified;
    protected boolean modifiedWhileInTextMode;
    protected TextDocumentModifiedListener textModifiedListener;

    protected static int untitledCount = 0;
    protected int untitledNumber = -1;

    public static final int FILENAME_CHANGED = 1;
    public static final int LAST_VIEW_CLOSED = 2;
    public static final int FILE_CHANGED     = 3;
    public static final int FILE_SAVED       = 4;
    public static final int SWITCH_TO_TEXT_MODE   = 5;
    public static final int SWITCH_TO_PARSED_MODE = 6;

    /**
     * Constructor. By default, this will create an empty file in text mode.
     */
    public XmlModel(int undoLevels)
    {
        textDocument = new XmlTextDocument();
        textDocument.setTokenMarker(new XMLTokenMarker());
        textModifiedListener = new TextDocumentModifiedListener();
        textDocument.addDocumentListener(textModifiedListener);
        mode = TEXT_MODE;
        undo = new Undo(this, undoLevels);
    }

    /**
     * Reades the xml document given by the inputSource. File is an
     * optional parameter that is used for saving the document and
     * displaying the file name to the user. It may be null, in wich case
     * the document will be shown as 'Untitled'.
     *
     * By default, the document will be parsed and hence the XmlModel
     * will be in parsed mode. If parsing fails, the XmlModel will be
     * in text mode.
     *
     * The inputstream provided by the InputSource must be closed by
     * the caller.
     *
     */
    public void readFromResource(InputSource inputSource, File file)
        throws Exception
    {
        this.file = file;
        try
        {
            PolloDOMParser parser = new PolloDOMParser();
            setFeatures(parser);
            parser.parse(inputSource);
            domDocument = parser.getDocument();
            undo.reconnectToDom();
            mode = PARSED_MODE;
        }
        catch (Exception e)
        {
            // parsing failed, read the document as text
            try
            {
                // fallback only supported if it is a file because we need to open
                // a new inputstream
                if (file != null)
                {
                    // FIXME encoding!!
                    InputStream is = new FileInputStream(file);
                    try
                    {
                        InputStreamReader reader = new InputStreamReader(is);
                        StringBuffer text = new StringBuffer();
                        final int BUFFER_SIZE = 5000;
                        char[] buffer = new char[BUFFER_SIZE];

                        int l;
                        do
                        {
                            l = reader.read(buffer, 0, BUFFER_SIZE);
                            if (l != -1)
                                text.append(buffer, 0, l);
                        }
                        while (l != -1);
                        setTextDocumentText(text.toString());
                        mode = TEXT_MODE;
                    }
                    finally
                    {
                        try { is.close(); } catch (Exception e3) {}
                    }
                }
            }
            catch (Exception e2)
            {
                throw new Exception("Could not read from the file: " + e2.toString());
            }
        }
        modified = false;
    }

    public void readFromResource(File file)
        throws Exception
    {
        FileInputStream fis = new FileInputStream(file);
        try
        {
            readFromResource(new InputSource(fis), file);
        }
        finally
        {
            try { fis.close(); } catch (Exception e) {}
        }
    }

    public void setFeatures(PolloDOMParser parser)
        throws Exception
    {
        parser.setFeature("http://apache.org/xml/features/dom/defer-node-expansion",false);
        parser.setFeature("http://xml.org/sax/features/namespaces", true);
    }

    public Document getDocument()
    {
        return domDocument;
    }

    public XmlTextDocument getTextDocument()
    {
        return textDocument;
    }

    /**
     * Sets the text of the textdocument.
     */
    public void setTextDocumentText(String text)
    {
        try
        {
            textDocument.stopUndo();
            textModifiedListener.stop();
            textDocument.beginCompoundEdit();
            textDocument.remove(0, textDocument.getLength());
            textDocument.insertString(0, text, null);
        }
        catch(BadLocationException bl)
        {
            bl.printStackTrace();
        }
        finally
        {
            textDocument.endCompoundEdit();
            textDocument.startUndo();
            textModifiedListener.start();
        }
    }

    /**
     * When set to true, the document will be reparsed when
     * switching to parsed mode, otherwise not.
     */
    public void setModifiedWhileInTextMode(boolean modified)
    {
        modifiedWhileInTextMode = modified;
    }

    /**
     * Returns the contents of the text document as a String.
     */
    public String getTextDocumentText()
    {
        try
        {
            return textDocument.getText(0, textDocument.getLength());
        }
        catch(BadLocationException bl)
        {
            bl.printStackTrace();
            return null;
        }
    }

    public void store(String filename)
        throws Exception
    {
        FileOutputStream output = null;
        try
        {
            output = new FileOutputStream(filename);
            if (mode == PARSED_MODE)
            {
                XMLSerializer serializer = new XMLSerializer(output, createOutputFormat());
                serializer.serialize(domDocument);
            }
            else if (mode == TEXT_MODE)
            {
                String encoding = textDocument.getEncoding();
                if (encoding == null)
                    encoding = "UTF-8";
                Writer writer = null;
                if (encoding != null)
                    writer = new OutputStreamWriter(output, encoding);
                else
                    writer = new OutputStreamWriter(output);
                Segment seg = new Segment();
                textDocument.getText(0, textDocument.getLength(), seg);
                try
                {
                    writer.write(seg.array, seg.offset, seg.count);
                }
                finally
                {
                    writer.close();
                }
            }
            else
            {
                throw new RuntimeException("XmlModel is in an invalid mode.");
            }
        }
        finally
        {
            try { output.close(); } catch (Exception e) {}
        }
        
        modified = false;
        notify(FILE_SAVED);
    }

    public void store()
        throws Exception
    {
        store(file.getAbsolutePath());
    }

    public OutputFormat createOutputFormat()
    {
        String encoding = domDocument.getEncoding();
        OutputFormat outputFormat = new OutputFormat(domDocument, encoding != null ? encoding : "ISO-8859-1", true);
        outputFormat.setIndent(2);
        outputFormat.setLineWidth(0);
        outputFormat.setLineSeparator(System.getProperty("line.separator"));

        return outputFormat;
    }

    public String toXMLString()
        throws Exception
    {
        StringWriter writer = new StringWriter();

        XMLSerializer serializer = new XMLSerializer(writer, createOutputFormat());
        serializer.serialize(domDocument);

        return writer.toString();
    }

    public Element getNextElementSibling(Element element)
    {
        if (mode != PARSED_MODE)
            throw new RuntimeException("getNextElementSibling may not be called when the document is not in parsed mode.");
        // search the next sibling of type element (null is also allowed)
        Element nextElement = null;
        Node nextNode = element;
        while ((nextNode = nextNode.getNextSibling()) != null)
        {
            if (nextNode.getNodeType() == Node.ELEMENT_NODE)
            {
                nextElement = (Element)nextNode;
                break;
            }
        }
        return nextElement;
    }


    /**
     * Finds the namespace with which the prefix is associated, or null
     * if not found.
     *
     * @param element Element from which to start searching
     */
    public String findNamespaceForPrefix(Element element, String prefix)
    {
        if (mode != PARSED_MODE)
            throw new RuntimeException("findNamespaceForPrefix may not be called when the document is not in parsed mode.");
        if (element == null || prefix == null)
            return null;

        if (prefix.equals("xml"))
            return "http://www.w3.org/XML/1998/namespace";

        if (prefix.equals("xmlns"))
            return "http://www.w3.org/2000/xmlns/";

        Element currentEl = element;
        String searchForAttr = "xmlns:" + prefix;

        do
        {
            String attrValue = currentEl.getAttribute(searchForAttr);
            if (attrValue != null && attrValue.length() > 0)
            {
                return attrValue;
            }

            if (currentEl.getParentNode().getNodeType() == currentEl.ELEMENT_NODE)
                currentEl = (Element)currentEl.getParentNode();
            else
                currentEl = null;
        }
        while (currentEl != null);

        return null;
    }


    /**
     * Finds a prefix declaration for the given namespace, or null if
     * not found.
     *
     * @param element Element from which to start searching
     *
     * @return null if no prefix is found, an empty string if it is the
     * default namespace, and otherwise the found prefix
     */
    public String findPrefixForNamespace(Element element, String ns)
    {
        if (mode != PARSED_MODE)
            throw new RuntimeException("findPrefixForNamespace may not be called when the document is not in parsed mode.");
        if (element == null || ns == null)
            return null;

        if (ns.equals("http://www.w3.org/XML/1998/namespace"))
            return "xml";

        Element currentEl = element;

        do
        {
            NamedNodeMap attrs = currentEl.getAttributes();

            for (int i = 0; i < attrs.getLength(); i++)
            {
                Attr attr = (Attr)attrs.item(i);
                if (attr.getValue().equals(ns))
                {
                    if (attr.getPrefix() != null && attr.getPrefix().equals("xmlns"))
                    {
                        return attr.getLocalName();
                    }
                    else if (attr.getLocalName().equals("xmlns"))
                    {
                        return "";
                    }
                }
            }
            if (currentEl.getParentNode().getNodeType() == currentEl.ELEMENT_NODE)
                currentEl = (Element)currentEl.getParentNode();
            else
                currentEl = null;
        }
        while (currentEl != null);

        return null;
    }

    /**
      Returns a list of all the namespace prefixes that are known in the given context.

      @param element Element from which to start searching
     */
    public HashMap findNamespaceDeclarations(Element element)
    {
        if (mode != PARSED_MODE)
            throw new RuntimeException("findNamespaceDeclarations may not be called when the document is not in parsed mode.");

        HashMap namespaces = new HashMap();
        Element currentEl = element;

        do
        {
            NamedNodeMap attrs = currentEl.getAttributes();

            for (int i = 0; i < attrs.getLength(); i++)
            {
                Attr attr = (Attr)attrs.item(i);
                if (attr.getPrefix() != null && attr.getPrefix().equals("xmlns") )
                {
                    // only the first declartion found counts.
                    if (!namespaces.containsKey(attr.getLocalName()))
                        namespaces.put(attr.getLocalName(), attr.getValue());
                }
            }
            if (currentEl.getParentNode().getNodeType() == currentEl.ELEMENT_NODE)
                currentEl = (Element)currentEl.getParentNode();
            else
                currentEl = null;
        }
        while (currentEl != null);

        return namespaces;
    }


    /**
      Finds a default namespace declaration.
     */
    public String findDefaultNamespace(Element element)
    {
        if (mode != PARSED_MODE)
            throw new RuntimeException("findDefaultNamespace may not be called when the document is not in parsed mode.");

        if (element == null)
            return null;

        Element currentEl = element;
        do
        {
            String xmlns = currentEl.getAttribute("xmlns");
            if (xmlns != null)
                return xmlns;

            if (currentEl.getParentNode().getNodeType() == currentEl.ELEMENT_NODE)
                currentEl = (Element)currentEl.getParentNode();
            else
                currentEl = null;
        }
        while (currentEl != null);

        return null;
    }

    public Element getNode(String xpathExpr)
    {
        if (mode != PARSED_MODE)
            throw new RuntimeException("getNode may not be called when the document is not in parsed mode.");

        try
        {
            XPath xpath = new DOMXPath(xpathExpr);
            SimpleNamespaceContext namespaceContext = new SimpleNamespaceContext();
            namespaceContext.addElementNamespaces(xpath.getNavigator(), domDocument.getDocumentElement());
            xpath.setNamespaceContext(namespaceContext);
            Element el =  (Element)xpath.selectSingleNode(domDocument.getDocumentElement());
            if (el == null)
                System.out.println("xpath returned null: " + xpathExpr);
            return el;
        }
        catch (Exception e)
        {
            System.out.println("error executing xpath: " + xpathExpr);
            return null;
        }
    }

    public Undo getUndo()
    {
        return undo;
    }

    public File getFile()
    {
        return file;
    }

    public String getShortTitle()
    {
        if (file == null)
        {
            if (untitledNumber == -1)
            {
                untitledCount++;
                untitledNumber = untitledCount;
            }

            return "Untitled" + untitledNumber;
        }
        else
        {
            return file.getName();
        }
    }

    public String getLongTitle()
    {
        if (file == null)
        {
            return getShortTitle();
        }
        else
        {
            return file.getAbsolutePath();
        }
    }

    public void switchToParsedMode()
        throws Exception
    {
        if (mode == PARSED_MODE)
            return;

        if (modifiedWhileInTextMode || domDocument == null)
        {
            PolloDOMParser parser = new PolloDOMParser();
            setFeatures(parser);
            Segment seg = new Segment();
            textDocument.getText(0, textDocument.getLength(), seg);
            parser.parse(new InputSource(new CharArrayReader(seg.array, seg.offset, seg.count)));
            domDocument = parser.getDocument();
            undo.reconnectToDom();
        }
        mode = PARSED_MODE;
        notify(SWITCH_TO_PARSED_MODE);
    }

    public void switchToTextMode()
        throws Exception
    {
        if (mode == TEXT_MODE)
            return;
        String xml = toXMLString();
        setTextDocumentText(xml);
        mode = TEXT_MODE;
        modifiedWhileInTextMode = false;
        notify(SWITCH_TO_TEXT_MODE);
    }

    public int getCurrentMode()
    {
        return mode;
    }

    public boolean isInParsedMode()
    {
        return (mode == PARSED_MODE);
    }

    public boolean isInTextMode()
    {
        return (mode == TEXT_MODE);
    }

    public void markModified()
    {
        if (modified == false)
        {
            modified = true;
            notify(FILE_CHANGED);
        }
    }

    public void registerView(View view)
    {
        registeredViewsList.add(view);
    }

    public void addListener(XmlModelListener listener)
    {
        xmlModelListeners.add(listener);
    }

    public void removeListener(XmlModelListener listener)
    {
        xmlModelListeners.remove(listener);
    }

    /**
      @return false if the user cancelled the operation
     */
    public boolean closeView(View view)
        throws Exception
    {
        if (!registeredViewsList.contains(view))
            throw new RuntimeException("Tried to call XmlModel.closeView for a view that was not registered.");

        if (registeredViewsList.size() == 1)
        {
            // this was the last view on the model
            if (!askToSave(view.getParentForDialogs())) return false;

            // last view was closed, notified XmlModelListeners of this fact
            notify(LAST_VIEW_CLOSED);
        }
        registeredViewsList.remove(view);
        return true;
    }

    public void notify(int eventtype)
    {
        Iterator xmlModelListenersIt = xmlModelListeners.iterator();
        while (xmlModelListenersIt.hasNext())
        {
            XmlModelListener listener = (XmlModelListener)xmlModelListenersIt.next();
            switch (eventtype)
            {
                case FILENAME_CHANGED:
                    listener.fileNameChanged(this);
                    break;
                case LAST_VIEW_CLOSED:
                    listener.lastViewClosed(this);
                    break;
                case FILE_CHANGED:
                    listener.fileChanged(this);
                    break;
                case FILE_SAVED:
                    listener.fileSaved(this);
                    break;
                case SWITCH_TO_TEXT_MODE:
                    listener.switchToTextMode(this);
                    break;
                case SWITCH_TO_PARSED_MODE:
                    listener.switchToParsedMode(this);
                    break;
            }
        }
    }

    public void save(Component parent)
        throws Exception
    {
        if (file == null)
        {
            saveAs(parent);
        }
        if (file != null)
            store();
    }

    public void saveAs(Component parent)
        throws Exception
    {
        // ask for a filename
        JFileChooser fileChooser = new JFileChooser();
        switch (fileChooser.showSaveDialog(parent))
        {
            case JFileChooser.APPROVE_OPTION:
                file = fileChooser.getSelectedFile();
                break;
            case JFileChooser.CANCEL_OPTION:
                break;
            case JFileChooser.ERROR_OPTION:
                break;
        }
        if (file != null)
        {
            notify(FILENAME_CHANGED);
            save(parent);
        }
    }

    public boolean closeAllViews(Component parent)
        throws Exception
    {
        if (!askToSave(parent)) return false;

        Iterator registeredViewsIt = registeredViewsList.iterator();

        while (registeredViewsIt.hasNext())
        {
            View view = (View)registeredViewsIt.next();
            view.stop();
        }

        registeredViewsList.clear();
        notify(LAST_VIEW_CLOSED);

        return true;
    }


    /**
     * @return false if the user pressed cancel
     */
    public boolean askToSave(Component parent)
        throws Exception
    {
        if (modified)
        {
            String message = "This file is not yet saved. Save it before closing?";
            if (file != null)
                message = "The file " + file.getAbsolutePath() + " was modified. Save it?";
            switch (JOptionPane.showConfirmDialog(parent, message, "Pollo message",
                        JOptionPane.YES_NO_CANCEL_OPTION))
            {
                case JOptionPane.YES_OPTION:
                    save(parent);
                    break;
                case JOptionPane.NO_OPTION:
                    break;
                case JOptionPane.CANCEL_OPTION:
                    return false;
            }
        }
        return true;
    }


    public boolean isModified()
    {
        return modified;
    }

    public class TextDocumentModifiedListener implements DocumentListener
    {
        protected boolean enabled = false;

        public void changedUpdate(DocumentEvent e)
        {
            if (enabled)
            {
                markModified();
                modifiedWhileInTextMode = true;
            }
        }

        public void insertUpdate(DocumentEvent e)
        {
            if (enabled)
            {
                markModified();
                modifiedWhileInTextMode = true;
            }
        }

        public void removeUpdate(DocumentEvent e)
        {
            if (enabled)
            {
                markModified();
                modifiedWhileInTextMode = true;
            }
        }

        public void start()
        {
            enabled = true;
        }

        public void stop()
        {
            enabled = false;
        }
    }
}
