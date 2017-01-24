package org.outerj.pollo.xmleditor.displayspec;

import org.outerj.pollo.xmleditor.exception.PolloException;

import java.util.HashMap;

/**
 * Interface for factories of displayspecifications. The main reason that
 * factories are used is so that a factory can cache instances of display
 * specifications, if they can be shared between multiple views.
 *
 * @author Bruno Dumon
 */
public interface IDisplaySpecificationFactory
{
    public IDisplaySpecification getDisplaySpecification(HashMap initParams)
        throws PolloException;
}
