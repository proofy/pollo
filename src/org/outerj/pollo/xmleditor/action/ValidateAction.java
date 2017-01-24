package org.outerj.pollo.xmleditor.action;

import com.sun.msv.verifier.ValidationUnrecoverableException;
import org.outerj.pollo.xmleditor.XmlEditorPanel;
import org.outerj.pollo.xmleditor.schema.ISchema;
import org.outerj.pollo.gui.EmptyIcon;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Collection;
import java.util.Collections;

/**
 * A Swing Action that validates the document against its schema.
 *
 * @author Bruno Dumon
 */
public class ValidateAction extends AbstractAction
{
    protected XmlEditorPanel xmlEditorPanel;

    public ValidateAction(XmlEditorPanel xmlEditorPanel)
    {
        super("Validate document", EmptyIcon.getInstance());
        this.xmlEditorPanel = xmlEditorPanel;
        setEnabled(true);
    }

    public void actionPerformed(ActionEvent e)
    {
        ISchema schema = xmlEditorPanel.getSchema();
        Collection errors = null;
        try
        {
            errors = schema.validate(xmlEditorPanel.getXmlModel().getDocument());
        }
        catch (ValidationUnrecoverableException e1)
        {
            JOptionPane.showMessageDialog(xmlEditorPanel.getTopLevelAncestor(),
                    "An unrecoverable exception occured during validation:\n\n" + e1.getMessage(),
                    "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        catch (Exception e2)
        {
            JOptionPane.showMessageDialog(xmlEditorPanel.getTopLevelAncestor(),
                    "An unexpected exception occured during validation:\n\n" + e2.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (errors.isEmpty())
        {
            xmlEditorPanel.getValidationErrorsPanel().showErrors(Collections.EMPTY_LIST);
            JOptionPane.showMessageDialog(xmlEditorPanel.getTopLevelAncestor(),
                    "This document is valid.", "Validation Success", JOptionPane.INFORMATION_MESSAGE);
        }
        else
        {
            xmlEditorPanel.showValidationErrorsPanel(errors);
        }
    }

}
