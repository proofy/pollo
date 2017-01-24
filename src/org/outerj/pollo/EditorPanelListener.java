package org.outerj.pollo;

public interface EditorPanelListener
{
    /**
     * Called when the menu bar has changed, which occurs when
     * switching between text mode and dom mode.
     */
    public void editorPanelMenuChanged(EditorPanel source);

    public void editorPanelToolBarChanged(EditorPanel source);

    /**
     * Called when the EditorPanel should be closed.
     */
    public void editorPanelClosing(EditorPanel source);

    /**
     * Called when the filename has changed.
     */
    public void editorPanelTitleChanged(EditorPanel source);
}
