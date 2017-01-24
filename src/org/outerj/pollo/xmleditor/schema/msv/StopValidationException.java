package org.outerj.pollo.xmleditor.schema.msv;

import com.sun.msv.util.StartTagInfo;
import com.sun.msv.verifier.Acceptor;
import org.xml.sax.SAXParseException;

/**
 * This class isn't really an exception (although it's a throwable).
 * It is used as a little trick to stop the validation process once
 * the document is validated up to a certain element, so that the
 * acceptor is available. See PolloMsvVerifier and SAXEventGenerator
 * for more information.
 */
public class StopValidationException extends SAXParseException
{
    protected StartTagInfo startTagInfo;
    protected Acceptor acceptor;

    public StopValidationException(StartTagInfo info, Acceptor acceptor)
    {
        super("StopValidationException", "", "", -1, -1);
        this.startTagInfo = info;
        this.acceptor = acceptor;
    }

    public Acceptor getAcceptor()
    {
        return acceptor;
    }
}
