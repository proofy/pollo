package org.outerj.pollo.xmleditor.view;

import org.outerj.pollo.xmleditor.XmlEditor;

public class V2ViewFactory extends ViewFactoryBase
{
    public V2ViewFactory(XmlEditor xmlEditor)
    {
        super(xmlEditor, new V2Strategy(xmlEditor));
    }
}
