package org.outerj.pollo.xmleditor.displayspec;

import org.outerj.pollo.xmleditor.exception.PolloException;

import java.util.HashMap;

public class GenericDisplaySpecFactory implements IDisplaySpecificationFactory
{
    public IDisplaySpecification getDisplaySpecification(HashMap initParams)
        throws PolloException
    {
        GenericDisplaySpecification displaySpec = new GenericDisplaySpecification();
        displaySpec.init(initParams);
        return displaySpec;
    }
}
