package org.outerj.pollo.xmleditor.model;

/**
 * This is an interface for classes that want to know about XmlModel
 * related events. Implement this interface and register the class with
 * the XmlModel to use it.
 *
 * @author Bruno Dumon
 */
public interface XmlModelListener
{
    /**
     * Called when the last view on the XmlModel is closed. (With 'view' we
     * mean a class implementing the interface
     * {@link org.outerj.pollo.xmleditor.model.View}).
     */
    public void lastViewClosed(XmlModel sourceXmlModel);

    /**
     * Called when the filename of this xml model changes. This is when
     * the user used 'save as'.
     */
    public void fileNameChanged(XmlModel sourceXmlModel);

    /**
     * Called when the model has changed the first time after it was
     * saved.
     */
    public void fileChanged(XmlModel sourceXmlModel);

    /**
     * Called when the file is saved.
     */
    public void fileSaved(XmlModel sourceXmlModel);

    /**
     * Called when the XmlModel switches to text mode.
     */
    public void switchToTextMode(XmlModel sourceXmlModel);

    /**
     * Called when the XmlModel switches to parsed mode.
     */
    public void switchToParsedMode(XmlModel sourceXmlModel);
}
