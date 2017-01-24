package org.outerj.pollo.texteditor;

import org.outerj.pollo.xmleditor.model.XmlModel;
import org.outerj.pollo.xmleditor.schema.ISchema;
import org.outerj.pollo.xmleditor.Disposable;
import org.xml.sax.SAXParseException;

import javax.swing.*;
import java.awt.*;

public class XmlTextEditorPanel extends JPanel implements Disposable
{
    protected XmlTextEditor xmlTextEditor;
    protected CheckPanel checkPanel;

    public XmlTextEditorPanel(XmlModel xmlModel, ISchema schema)
    {
        xmlTextEditor = new XmlTextEditor();
        xmlTextEditor.setDocument(xmlModel.getTextDocument());
        xmlTextEditor.addExtraKeyBindings();

        setLayout(new BorderLayout());
        add(xmlTextEditor, BorderLayout.CENTER);

        checkPanel = new CheckPanel(xmlModel, xmlTextEditor);
        add(checkPanel, BorderLayout.SOUTH);
    }

    public void jumpToBeginning()
    {
        xmlTextEditor.setCaretPosition(0);
    }

    public void showParseException(SAXParseException e)
    {
        checkPanel.showParseException(e);
    }

    public XmlTextEditor getEditor()
    {
        return xmlTextEditor;
    }

    public XmlTextDocument getDocument()
    {
        return xmlTextEditor.getXmlTextDocument();
    }

    public void dispose()
    {
        // by removing xmlTextEditor, we are sure that it isn't any longer
        // referenced by the static variable 'focusedComponent' in JEditTextArea
        remove(xmlTextEditor);
        // although apperently it isn't called, so I do it here manually
        xmlTextEditor.removeNotify();
    }
}
