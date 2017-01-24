package org.outerj.pollo.template;

import org.outerj.pollo.xmleditor.exception.PolloException;
import org.outerj.pollo.xmleditor.model.XmlModel;
import org.outerj.pollo.util.URLFactory;

import java.util.HashMap;
import java.io.InputStream;
import org.xml.sax.InputSource;

public class FileTemplate implements ITemplate
{
    protected String source;
    
    public void init(HashMap initParams)
        throws PolloException
    {
        source = (String)initParams.get("source");
        if (source == null)
        {
            throw new PolloException("[FileTemplate] No source init-param given!");
        }
    }

    public XmlModel createNewDocument(int undoLevels)
        throws PolloException
    {
        XmlModel model = null;
        InputStream is = null;
        try
        {
            is = URLFactory.createUrl(source).openStream();
            model = new XmlModel(undoLevels);
            model.readFromResource(new InputSource(is), null);
        }
        catch (Exception e)
        {
            throw new PolloException("[FileTemplate] Could not create file based on template " + source + ".", e);
        }
        finally
        {
            try { if (is != null) is.close(); } catch (Exception e) {}
        }
        return model;
    }
}
