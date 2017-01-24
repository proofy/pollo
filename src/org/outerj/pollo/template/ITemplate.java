package org.outerj.pollo.template;

import org.outerj.pollo.xmleditor.exception.PolloException;
import org.outerj.pollo.xmleditor.model.XmlModel;
import org.w3c.dom.Document;

public interface ITemplate
{
    public XmlModel createNewDocument(int undoLevels)
        throws PolloException;
}
