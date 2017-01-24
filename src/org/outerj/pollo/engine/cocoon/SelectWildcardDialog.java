package org.outerj.pollo.engine.cocoon;

import org.w3c.dom.Element;
import org.outerj.pollo.xmleditor.model.XmlModel;
import org.jaxen.SimpleNamespaceContext;
import org.jaxen.XPath;
import org.jaxen.dom.DOMXPath;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import java.util.ArrayList;
import java.util.Iterator;
import java.awt.*;
import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

/**
 * Dialog that shows the nearest wilcard URI matcher pattern and lets the user select
 * one of the stars to insert a reference to them.
 *
 * @author Bruno Dumon
 */
public class SelectWildcardDialog extends JDialog
{
    protected final String SITEMAP_NS = "http://apache.org/cocoon/sitemap/1.0";
    protected final XmlModel xmlModel;
    protected SimpleNamespaceContext namespaceContext;
    protected String wildcardMatcherName;
    protected boolean isWilcardMatcherDefault;
    protected JPanel patternPanel;
    protected String resultString;

    public SelectWildcardDialog(Frame parent, XmlModel xmlModel)
    {
        super(parent, "Select wildcard matcher pattern");
        this.xmlModel = xmlModel;

        namespaceContext = new SimpleNamespaceContext();
        namespaceContext.addNamespace("map", SITEMAP_NS);

        JPanel panel = new JPanel();
        panel.setBorder(new EmptyBorder(12, 12, 12, 12));
        panel.setLayout(new BorderLayout(12, 12));
        setContentPane(panel);

        Box titleBox = new Box(BoxLayout.Y_AXIS);
        JLabel title = new JLabel("Below the closest wildcard URI matcher pattern is shown.");
        JLabel title2 = new JLabel("Click on any of the stars to insert a reference to the string matched by them.");
        titleBox.add(title);
        titleBox.add(title2);
        panel.add(titleBox, BorderLayout.NORTH);

        Border border = BorderFactory.createBevelBorder(BevelBorder.LOWERED);
        patternPanel = new JPanel();
        patternPanel.setLayout(new BoxLayout(patternPanel, BoxLayout.X_AXIS));
        patternPanel.setBorder(border);
        panel.add(patternPanel, BorderLayout.CENTER);

        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt)
            {
                resultString = null;
                hide();
            }
        });
        Box buttonBox = new Box(BoxLayout.X_AXIS);
        buttonBox.add(Box.createHorizontalGlue());
        buttonBox.add(closeButton);
        panel.add(buttonBox, BorderLayout.SOUTH);


        setModal(true);
    }

    public String showIt(Element element)
    {
        if (!getWildcardMatcherInfos())
        {
            JOptionPane.showMessageDialog(getParent(), "Could not find the declaration for the wildcard URI matcher.");
            return null;
        }

        resultString = null;
        int nestingLevel = 0;
        patternPanel.removeAll();
        boolean foundPattern = false;

        Element parent = (Element)(element.getParentNode() instanceof Element ? element.getParentNode() : null);
        while (parent != null)
        {
            if (parent.getNamespaceURI().equals(SITEMAP_NS))
            {
                if (parent.getLocalName().equals("act"))
                {
                    nestingLevel++;
                }
                else if (parent.getLocalName().equals("match") && (wildcardMatcherName.equals(parent.getAttribute("type")) || ((parent.getAttribute("type") == null || parent.getAttribute("type").equals("")) && isWilcardMatcherDefault)))
                {
                    String pattern = parent.getAttribute("pattern");
                    if (pattern != null && pattern.length() > 0)
                    {
                        foundPattern = true;
                        ArrayList parts = splitInParts(pattern);
                        Iterator partsIt = parts.iterator();
                        int starOccurence = 0;
                        while (partsIt.hasNext())
                        {
                            String part = (String)partsIt.next();
                            if (part.charAt(0) != '*')
                            {
                                JLabel label = new JLabel(part);
                                patternPanel.add(label);
                            }
                            else
                            {
                                starOccurence++;
                                HyperlinkButton button = new HyperlinkButton(part);
                                button.setActionCommand(nestingLevel + "," + starOccurence);
                                final int finalNestingLevel = nestingLevel;
                                final int finalOccurence = starOccurence;
                                button.addActionListener(new ActionListener() {
                                    public void actionPerformed(ActionEvent evt)
                                    {
                                        StringBuffer result = new StringBuffer();
                                        result.append("{");
                                        for (int i = 0; i < finalNestingLevel; i++)
                                        {
                                            result.append("../");
                                        }
                                        result.append(finalOccurence);
                                        result.append("}");
                                        resultString = result.toString();
                                        hide();
                                    }
                                });
                                patternPanel.add(button);
                            }
                        }
                        break;
                    }
                    nestingLevel++;
                }
            }
            parent = (Element)(parent.getParentNode() instanceof Element ? parent.getParentNode() : null);
        }

        if (foundPattern == false)
        {
            JOptionPane.showMessageDialog(getParent(), "Could not find a parent wildcard URI matcher element.");
            return null;
        }

        pack();
        setLocationRelativeTo(getParent());
        show();
        return resultString;
    }

    /**
     * Gets information about what the component name is for the wilcard URI matcher.
     * <p>
     * Returns false if it could not be found or is incorrect.
     */
    public boolean getWildcardMatcherInfos()
    {
        try
        {
            String matcherDeclExpr = "/map:sitemap/map:components/map:matchers/map:matcher[@src='org.apache.cocoon.matching.WildcardURIMatcher']";
            XPath matcherDeclXPath = new DOMXPath(matcherDeclExpr);
            matcherDeclXPath.setNamespaceContext(namespaceContext);
            Object object = matcherDeclXPath.selectSingleNode(xmlModel.getDocument().getDocumentElement());
            if (object instanceof Element)
            {
                Element element = (Element)object;
                wildcardMatcherName = element.getAttribute("name");
                if (wildcardMatcherName == null)
                    return false;

                Element parent = (Element)element.getParentNode();
                if (wildcardMatcherName.equals(parent.getAttribute("default")))
                    isWilcardMatcherDefault = true;
                else
                    isWilcardMatcherDefault = false;

                return true;
            }
            else
                return false;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Splits a wildcard matcher pattern in parts consisting of either
     * stars or normal text. It follows the same rules as the Cocoon wildcard
     * matcher: stars can be escaped with a backslash, and more than 2 stars
     * in a row means the same as 2 stars.
     *
     * @return An arraylist with Strings, where each string is either static text
     * or a number of stars.
     */
    public ArrayList splitInParts(String pattern)
    {
        char [] text = pattern.toCharArray();

        StringBuffer currentPart = new StringBuffer();
        ArrayList parts = new ArrayList();
        boolean inStars = false;
        boolean inEscape = false;

        for (int i = 0; i < text.length; i++)
        {
            if (text[i] == '*')
            {
                if (inEscape)
                {
                    currentPart.append(text[i]);
                    inEscape = false;
                }
                else
                {
                    if (currentPart.length() > 0 && !inStars)
                    {
                        // start a new part
                        parts.add(currentPart.toString());
                        currentPart.setLength(0);
                    }
                    currentPart.append(text[i]);
                    inStars = true;
                }

            }
            else
            {
                if (inStars)
                {
                    // start a new part
                    parts.add(currentPart.toString());
                    currentPart.setLength(0);
                    inStars = false;
                }

                if (text[i] == '\\')
                {
                    inEscape = !inEscape;
                    currentPart.append(text[i]);
                }
                else
                {
                    currentPart.append(text[i]);
                    inEscape = false;
                }
            }
        }

        if (currentPart.length() > 0)
            parts.add(currentPart.toString());

        return parts;
    }

    /**
     * Hyperlink-like version of JButton.
     */
    protected class HyperlinkButton extends JButton implements MouseListener
    {
        public HyperlinkButton(String label)
        {
            super(label);
            setMargin(new Insets(0, 0, 0, 0));
            setBorderPainted(false);
            setFocusPainted(false);
            setForeground(Color.blue);
            setRequestFocusEnabled(false);
            addMouseListener(this);
        }

        public void mouseClicked(MouseEvent e)
        {
        }

        public void mousePressed(MouseEvent e)
        {
        }

        public void mouseReleased(MouseEvent e)
        {
        }

        public void mouseEntered(MouseEvent e)
        {
            setForeground(Color.red);
        }

        public void mouseExited(MouseEvent e)
        {
            setForeground(Color.blue);
        }
    }

}
