package org.outerj.pollo.xmleditor.displayspec;

import org.outerj.pollo.xmleditor.exception.PolloException;

import java.util.HashMap;
import java.util.WeakHashMap;

public class BasicDisplaySpecFactory implements IDisplaySpecificationFactory
{
    protected WeakHashMap displaySpecInstances = new WeakHashMap();

    public IDisplaySpecification getDisplaySpecification(HashMap initParams)
        throws PolloException
    {
        if (!displaySpecInstances.containsKey(initParams))
        {
            try
            {
                BasicDisplaySpecification newDisplaySpec = new BasicDisplaySpecification();
                newDisplaySpec.init(initParams);
                displaySpecInstances.put(initParams, newDisplaySpec);
            }
            catch (Exception e)
            {
                throw new PolloException("Could not create display specification.", e);
            }
        }

        return (IDisplaySpecification)displaySpecInstances.get(initParams);
    }
}
