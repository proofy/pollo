package org.outerj.pollo.xmleditor.view;

import org.outerj.pollo.xmleditor.XmlEditor;

public class V1ViewFactory extends ViewFactoryBase
{
    private static final ViewStrategy VIEW_STRATEGY = new V1Strategy();

    public V1ViewFactory(XmlEditor xmlEditor)
    {
        super(xmlEditor, VIEW_STRATEGY);
    }
}
