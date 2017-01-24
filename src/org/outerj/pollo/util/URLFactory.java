package org.outerj.pollo.util;

import org.outerj.pollo.xmleditor.exception.PolloException;

import java.io.File;
import java.net.URL;

/**
 * Factory for URL objects. Supports the 'classpath:/' protocol
 * for getting resources from the classpath, in addition to the
 * standard Java protocols.
 */
public class URLFactory
{
    public static URL createUrl(String spec)
        throws PolloException
    {
        try
        {
            if (spec.startsWith("classpath:/"))
            {
                return URLFactory.class.getClassLoader().getResource(spec.substring(11, spec.length()));
            }
            else if (spec.charAt(0) == '/')
            {
                // something starting with a / is always a file path, even if :/ occurs within it
                return new File(spec).toURL();
            }
            else if (spec.indexOf(":/") != -1)
            {
                return new URL(spec);
            }
            else
            {
                return new File(spec).toURL();
            }
        }
        catch (Exception e)
        {
            throw new PolloException("[URLFactory] Could not create url for " + spec, e);
        }
    }
}
