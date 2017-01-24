package org.outerj.pollo.xmleditor.model;

import java.awt.*;

/**
 * This is View interface that the XmlModel class uses to communicate with the
 * views on this model.
 *
 * This should not be confused with the View interface in the package
 * org.outerj.pollo.xmleditor.view. While the latter are views on individual
 * nodes of a DOM tree, this View here is rather a window containing a
 * view on the document as a whole.
 *
 * If you create a class that implements this interface (thus a view on an
 * XmlModel), you should register your view with the XmlModel so that it knows
 * about it.
 *
 * @author Bruno Dumon
 */
public interface View
{
    /**
     * Called when this view should close itself. This is usually called when
     * the document itself is closed, which is normally when the application
     * ends.
     */
    public void stop();

    /**
     * Should return the component to use as parent for displaying modal dialog boxes.
     */
    public Component getParentForDialogs();
}
