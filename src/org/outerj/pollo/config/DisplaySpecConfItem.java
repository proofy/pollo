package org.outerj.pollo.config;

import org.outerj.pollo.xmleditor.ComponentManager;
import org.outerj.pollo.xmleditor.displayspec.IDisplaySpecification;
import org.outerj.pollo.xmleditor.displayspec.IDisplaySpecificationFactory;
import org.outerj.pollo.xmleditor.exception.PolloException;

public class DisplaySpecConfItem extends ConfItem
{
    public IDisplaySpecification createDisplaySpec()
        throws PolloException
    {
        IDisplaySpecification displaySpec = null;
        try
        {
            IDisplaySpecificationFactory displaySpecFactory = (IDisplaySpecificationFactory)
                ComponentManager.getFactoryInstance(getFactoryClass());

            displaySpec = displaySpecFactory.getDisplaySpecification(getInitParams());
        }
        catch (Exception e)
        {
            throw new PolloException("Error creating display specification.", e);
        }
        return displaySpec;
    }
}
