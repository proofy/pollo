package org.outerj.pollo.template;

import org.outerj.pollo.xmleditor.exception.PolloException;

import java.util.HashMap;

public class FileTemplateFactory implements ITemplateFactory
{
    public ITemplate getTemplate(HashMap initParams)
        throws PolloException
    {
        FileTemplate template = new FileTemplate();
        template.init(initParams);
        return template;
    }
}
