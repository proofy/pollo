package org.outerj.pollo.xmleditor;

import org.outerj.pollo.xmleditor.exception.PolloException;

import java.util.HashMap;

/**
 * Sometime in the future this might evolve into a real component manager, but
 * for the moment it only serves as a factory for factories.
 *
 * @author Bruno Dumon
 */
public class ComponentManager
{
    protected static HashMap factories = new HashMap();

    public static Object getFactoryInstance(String factoryClassName)
        throws PolloException
    {
        if (!factories.containsKey(factoryClassName))
        {
            try
            {
                Object newFactory = Class.forName(factoryClassName).newInstance();
                factories.put(factoryClassName, newFactory);
            }
            catch (Exception e)
            {
                throw new PolloException("[ComponentManager] Could not instantiate the factory " + factoryClassName, e);
            }
        }

        return factories.get(factoryClassName);
    }
}
