package org.outerj.pollo.util;

public interface Valuable
{
    public String getValue();

    public void setValue(String value);

    /**
     * Inserts the given string at the caret position.
     */
    public void insertString(String value);
}
