package org.outerj.pollo.engine.cocoon;

import org.outerj.pollo.util.Valuable;
import org.outerj.pollo.xmleditor.IconManager;
import org.outerj.pollo.xmleditor.Disposable;
import org.outerj.pollo.xmleditor.model.XmlModel;
import org.outerj.pollo.xmleditor.plugin.AttributeEditorSupport;
import org.outerj.pollo.xmleditor.plugin.IAttributeEditorPlugin;
import org.outerj.pollo.xmleditor.schema.ISchema;
import org.outerj.pollo.PolloFrame;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.swing.*;
import javax.swing.text.Document;
import javax.swing.text.Segment;
import javax.swing.text.BadLocationException;
import javax.swing.event.CaretListener;
import javax.swing.event.CaretEvent;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusListener;
import java.awt.event.FocusEvent;
import java.io.File;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Attribute Editor Plugin for Cocoon Sitemap files.
 *
 * <p>
 * Currently supports:
 * <ul>
 *  <li>browsing for files in src attributes
 *  <li>Inserting a reference to a wildcard matcher pattern
 * </ul>
 *
 * @author Bruno Dumon, Al Byers (initial file dialog code)
 */
public class CocoonAttrEditorPlugin implements IAttributeEditorPlugin, Disposable
{
    protected XmlModel xmlModel;
    protected PolloFrame polloFrame;

    protected AttributeEditorSupport editorSupport;
    protected Valuable currentValuable;
    protected Element currentElement;

    protected JButton browseForFileButton;
    protected JFileChooser fileChooser;
    protected JCheckBox relativePathCheckBox;
    protected static HashSet elementsWithSrcChooser = new HashSet();

    protected JButton insertWildcardReferenceButton;
    protected SelectWildcardDialog selectWildcardDialog;

    protected JWindow popup;
    protected JToolTip popupTip;
    protected int screenWidth;

    static
    {
        elementsWithSrcChooser.add("generate");
        elementsWithSrcChooser.add("transform");
        elementsWithSrcChooser.add("serialize");
        elementsWithSrcChooser.add("read");
        elementsWithSrcChooser.add("part");
        elementsWithSrcChooser.add("mount");
    }

    public void init(HashMap initParams, XmlModel xmlModel, ISchema schema, PolloFrame polloFrame)
    {
        this.xmlModel = xmlModel;
        this.polloFrame = polloFrame;
        editorSupport = new AttributeEditorSupport(schema);

        // create the filechooser for inserting a file name
        fileChooser = new JFileChooser();
        // customise the file chooser
        fileChooser.setDialogTitle("Browse");
        fileChooser.setApproveButtonText("Select");
        JPanel fileChooserOptions = new JPanel();
        fileChooserOptions.setLayout(new BorderLayout());
        relativePathCheckBox = new JCheckBox("Insert path relative to location of the sitemap.");
        fileChooserOptions.add(relativePathCheckBox, BorderLayout.CENTER);
        // FIXME this line of code if *very* dependent on the underlying JFileChooser implementation
        //((JComponent)fileChooser.getComponent(2)).add(fileChooserOptions);

        // create the button for inserting a file name
        browseForFileButton = new JButton(IconManager.getIcon("org/outerj/pollo/engine/cocoon/browse.png"));
        browseForFileButton.setMargin(new Insets(0, 0, 0, 0));
        browseForFileButton.setRequestFocusEnabled(false);
        browseForFileButton.setToolTipText("Browse for a file");
        browseForFileButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                String srcValue = currentElement.getAttribute("src");
                File srcFile = new File(srcValue);
                fileChooser.setCurrentDirectory(srcFile);
                fileChooser.setSelectedFile(srcFile);

                int result = fileChooser.showOpenDialog(CocoonAttrEditorPlugin.this.polloFrame);
                if (result == JFileChooser.APPROVE_OPTION)
                {
                    String selectedFileName = fileChooser.getSelectedFile().getAbsolutePath();
                    String value = selectedFileName;
                    if (relativePathCheckBox.isSelected())
                    {
                        File sitemapFile = CocoonAttrEditorPlugin.this.xmlModel.getFile();
                        if (sitemapFile != null)
                        {
                            String path = sitemapFile.getParentFile().getAbsolutePath() + System.getProperty("file.separator");
                            if (selectedFileName.startsWith(path))
                            {
                                value = selectedFileName.substring(path.length());
                            }
                        }
                    }
                    currentValuable.setValue(value);
                }

            }
        });

        // create the button & stuff for inserting a wilcard reference
        selectWildcardDialog = new SelectWildcardDialog(polloFrame, xmlModel);
        insertWildcardReferenceButton = new JButton(IconManager.getIcon("org/outerj/pollo/engine/cocoon/wildcard.png"));
        insertWildcardReferenceButton.setMargin(new Insets(0, 0, 0, 0));
        insertWildcardReferenceButton.setRequestFocusEnabled(false);
        insertWildcardReferenceButton.setToolTipText("Insert reference to a wildcard");
        insertWildcardReferenceButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt)
            {
                try
                {
                    String result = selectWildcardDialog.showIt(currentElement);
                    if (result != null)
                    {
                        currentValuable.insertString(result);
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        });

        // register a caretlistener to display context sensitive popups
        JTextField [] textFields = editorSupport.getTextFields();
        for (int i = 0; i < textFields.length; i++)
        {
            JTextField textField = textFields[i];
            CocoonContextPopup cocoonContextPopup = new CocoonContextPopup(textField);
            textField.addCaretListener(cocoonContextPopup);
            textField.addFocusListener(cocoonContextPopup);
        }

        // create the popup window (must be disposed!)
        popup = new JWindow(polloFrame);
        popupTip = new JToolTip();
        popup.getContentPane().add(popupTip);

        // cache the screen width
        screenWidth = (int)Toolkit.getDefaultToolkit().getScreenSize().getWidth();
    }

    public TableCellEditor getAttributeEditor(Element element, String namespaceURI, String localName)
    {
        editorSupport.reset(element, namespaceURI, localName);

        currentElement = element;
        currentValuable = editorSupport.getValuable();

        if (elementsWithSrcChooser.contains(element.getLocalName()) && "src".equalsIgnoreCase(localName))
        {
            editorSupport.addComponent(browseForFileButton);
        }
        editorSupport.addComponent(insertWildcardReferenceButton);

        return editorSupport.getEditor();
    }

    public void dispose()
    {
        popup.dispose();
    }

    public class CocoonContextPopup implements CaretListener, FocusListener
    {
        JTextField textField;
        boolean popupVisible = false;

        static final int BEGIN = 0;
        static final int FIRST_DOT = 1;
        static final int SECOND_DOT = 2;
        static final int SLASH = 3;

        public CocoonContextPopup(JTextField textField)
        {
            this.textField = textField;
        }

        public void hidePopup()
        {
            popupVisible = false;
            popup.setVisible(false);
        }

        public void showPopup()
        {
            popupVisible = true;
            popup.setVisible(true);
        }

        public void caretUpdate(CaretEvent event)
        {
            if (!textField.isShowing())
            {
                hidePopup();
                return;
            }

            try
            {
                if (event.getDot() == event.getMark())
                {
                    Document document = textField.getDocument();
                    Segment segment = new Segment();
                    document.getText(0, document.getLength(), segment);

                    // search backward to a '{', except if first a '}' is encountered
                    int foundpos = -1;
                    for (int i = event.getDot() - 1; i >= 0; i--)
                    {
                        char c = segment.array[segment.offset + i];
                        if (c == '{')
                        {
                            foundpos = i;
                        }
                        else if (c == '}')
                        {
                            break;
                        }
                    }

                    // count the '../../' level
                    if (foundpos != -1)
                    {
                        int state = 0;
                        int level = 0;
                        int documentLength = document.getLength();
                        for (int i = foundpos + 1; i < documentLength; i++)
                        {
                            char c = segment.array[segment.offset + i];
                            if (c == '.')
                            {
                                if (state == BEGIN)
                                    state = FIRST_DOT;
                                else if (state == FIRST_DOT)
                                    state = SECOND_DOT;
                                else
                                    break;
                            }
                            else if ((c == '/') && (state == SECOND_DOT))
                            {
                                state = BEGIN;
                                level++;
                            }
                            else if (c == '}')
                                break;
                            else
                                break;
                        }

                        // and now find the component for this
                        Node parent = currentElement;
                        Element foundElement = null;
                        do
                        {
                            parent = parent.getParentNode();
                            if (parent instanceof Element && (parent.getLocalName().equals("match") || parent.getLocalName().equals("act")))
                            {
                                if (level == 0)
                                {
                                    foundElement = (Element)parent;
                                    break;
                                }
                                else
                                    level--;
                            }
                        }
                        while (parent != null && parent instanceof Element);

                        // configure and display the tooltip
                        if (foundElement != null)
                        {
                            StringBuffer popupText = new StringBuffer();
                            popupText.append(foundElement.getLocalName());
                            if (foundElement.getLocalName().equals("match"))
                            {
                                String type = foundElement.getAttribute("type");
                                String pattern = foundElement.getAttribute("pattern");
                                if (type != null && !type.equals(""))
                                    popupText.append("  type=\"" + type).append("\"");
                                if (pattern != null && !pattern.equals(""))
                                    popupText.append("  pattern=\"" + pattern).append("\"");
                            }
                            else if (foundElement.getLocalName().equals("act"))
                            {
                                String type = foundElement.getAttribute("type");
                                String set = foundElement.getAttribute("set");
                                String src = foundElement.getAttribute("src");
                                if (type != null && !type.equals(""))
                                    popupText.append("  type=\"").append(type).append("\"");
                                if (set != null && !set.equals(""))
                                    popupText.append("  set=\"").append(set).append("\"");
                                if (src != null && !src.equals(""))
                                    popupText.append("  src=\"").append(src).append("\"");
                            }
                            popupTip.setTipText(popupText.toString());
                        }
                        else
                        {
                            popupTip.setTipText("No element found at this position");
                        }

                        popupTip.invalidate();
                        popup.pack();
                        Rectangle openBracketLocation = textField.modelToView(foundpos);
                        if (openBracketLocation != null)
                        {
                            Point popupPoint = new Point(openBracketLocation.x, openBracketLocation.y);
                            if (popupPoint.x < 0)
                                popupPoint.x = 0;
                            SwingUtilities.convertPointToScreen(popupPoint, textField);
                            if (popupPoint.x + popup.getWidth() > screenWidth)
                                popupPoint.x = screenWidth - popup.getWidth();
                            popup.setLocation(popupPoint.x, popupPoint.y + textField.getHeight() + 3);
                            showPopup();
                        }
                        return;
                    }
                }
            }
            catch (BadLocationException e)
            {
                e.printStackTrace();
            }
            hidePopup();
        }

        public void focusGained(FocusEvent e)
        {
            if (popupVisible)
            {
                popup.setVisible(true);
            }
        }

        public void focusLost(FocusEvent e)
        {
            popup.setVisible(false);
        }
    }
}
