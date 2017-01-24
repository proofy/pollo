package org.outerj.pollo.util;

import javax.swing.*;
import java.awt.event.KeyEvent;

/**
 * A textfield that provides public access to the processKeyBinding
 * functionality.
 */
public class PublicKeyBindingTextField extends JTextField
{
    public boolean processKeyBindingPublic(KeyStroke ks, KeyEvent e,
                                        int condition, boolean pressed)
    {
        return super.processKeyBinding(ks, e, condition, pressed);
    }
}
