package org.outerj.pollo.config;

import java.util.HashMap;

public class ConfItem
{
    protected String factoryClass;
    protected HashMap initParams = new HashMap();

    public void setFactoryClass(String factoryClass)
    {
        this.factoryClass = factoryClass;
    }

    public String getFactoryClass()
    {
        return factoryClass;
    }

    public void addInitParam(String name, String value)
    {
        initParams.put(name, value);
    }

    public HashMap getInitParams()
    {
        return initParams;
    }
}
