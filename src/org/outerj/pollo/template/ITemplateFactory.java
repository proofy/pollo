package org.outerj.pollo.template;

import org.outerj.pollo.xmleditor.exception.PolloException;
import java.util.HashMap;

/**
 * @author Bruno Dumon
 */
public interface ITemplateFactory
{
    public ITemplate getTemplate(HashMap initParams)
        throws PolloException;
}
