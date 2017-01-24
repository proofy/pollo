package org.outerj.pollo.xmleditor.schema.msv;

import com.sun.msv.util.StartTagInfo;
import com.sun.msv.verifier.Acceptor;
import com.sun.msv.verifier.DocumentDeclaration;
import com.sun.msv.verifier.Verifier;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;

/**
 * Extension of the standard MSV Verifier class. It adds the posiblity
 * to stop validation at a certain moment. This is used for determing the content
 * model of the currently selected node, this works in combination with
 * the SAXEventGenerator how will call setStopNow(true) when it streams
 * the element in question. See MsvSchema.getExpression for how its used.
 *
 * <p>
 * In the end, it would probably be better to write a special DOM version
 * of the Verifier, but this was just quicker to implement.
 *
 * @author Bruno Dumon
 */
public class PolloMsvVerifier extends Verifier
{
    protected boolean stopNow = false;

    public PolloMsvVerifier(DocumentDeclaration documentDeclaration, ErrorHandler errorHandler)
    {
        super(documentDeclaration, errorHandler);
    }

    protected void onNextAcceptorReady(StartTagInfo info, Acceptor acceptor) throws SAXException
    {
        if (stopNow)
        {
            // to stop the validation here, we throw an exception, though it's not really
            // meant as an exception, its just to stop the validation process here.
            throw new StopValidationException(info, acceptor);
        }
    }

    public void setStopNow(boolean stopNow)
    {
        this.stopNow = stopNow;
    }

}
