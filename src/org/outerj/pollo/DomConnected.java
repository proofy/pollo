package org.outerj.pollo;

public interface DomConnected
{
    /**
     * Removes event listeners and pointers to nodes of the tree.
     */
    public void disconnectFromDom();

    /**
     * Connect to a new DOM tree of the XmlModel.
     */
    public void reconnectToDom();
}
