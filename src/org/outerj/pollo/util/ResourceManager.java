/*
 * ResourceManager.java - Manage I18N resources.
 * 
 * Copyright (C) 2002 Avisto S.A.
 *
 */

/* TODO 
 * 
 *  Add Configuratin PopupToolButton
 * 
 *  Adding a EMPTY16X16ICON or EMPTY20X20ICON to an action causes an
 *  exception if the action is disabled. See ImageProducer getSource() in 
 *  EmptyImageIcon returns null.
 * 
 */
package org.outerj.pollo.util;

import java.awt.Font;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.swing.*;

import org.apache.log4j.Category;
import org.outerj.pollo.gui.EmptyIcon;

/**
 * This class is used to managed access to the resources needed by an
 * application.. 
 * <p>
 * It is used to internationalize text, icons, and key strokes. Convenience
 * methods are provided for configuring actions, setting menu mnemonics, and
 * registering accelerators.
 * <p>
 * All text strings that must be translated will be taken from a property file.
 * the property file is located along CLASSPATH and must be in the same package
 * as the java file. It is named javaFile.properties.
 * <p>
 * This class is <b>not</b> internationalizable as it could cause recursion
 * in some error situations.
 */
public class ResourceManager {
    // Category has been deprecated in 1.4.1 of log4j, but it is still better
    // to use it as some (most) packages are still using pre-1.4.1.
    //private static Logger _logger = Logger.getLogger(ResourceManager.class);
    private static Category _logger =
        Category.getInstance(ResourceManager.class);

    /** Map containing all loaded ResourceManagers. */
    private static Map _resourceManagers = new HashMap();

    private Class _class;
    private ResourceBundle _resourceBundle;
    static private Locale _locale = null;

    public static final Icon EMPTY16X16ICON = EmptyIcon.get16Instance();
    public static final Icon EMPTY20X20ICON = EmptyIcon.get20Instance();

    /**
     * Protected constructor as we use a factory method {@link #getManager} to
     * create ResourceManager instances.
     * <p>
     * @param clazz A resouce is associated with a class.
     */
    protected ResourceManager(Class clazz) {
        _class = clazz;
        String bundleName = clazz.getName();
        try {
            _resourceBundle = ResourceBundle.getBundle(bundleName, _locale);
        } catch (MissingResourceException e) {
            _resourceBundle = null;
            String msg =
                "Cannot find ResourceBundle for bundle named '"
                    + bundleName
                    + "'"
                    + " for locale '"
                    + _locale
                    + "'";
            _logger.debug(msg, e);
            _logger.error(msg);
        }
    }



    /**
     * Determin if a property value is set in the property file.
     * <p>
     * R
     * @param propertyName property to check
     * @return boolean true if propertyName is set in the perperty file
     */
    public boolean isValueSet( String propertyName  )
    {
        String value = getStr( propertyName );  
        if (value == null || value.equals(""))
            return  false;
        else
            return true;
    }
    
    /**
     * Returns a Font from a property file
     * @param propertyName
     * @return Font
     */
    public Font getFont( String propertyName )
    {
        String namePropertyName = propertyName + "_Name";
        String name;
        if( isValueSet(  namePropertyName  ) )
        {
            name = getString( namePropertyName );
        }
        else
        {           
            // use Default Font
            name = null;    
        }
        
        String stylePropertyName = propertyName + "_Style";
        String styleName = getString(stylePropertyName);
        int style = Font.PLAIN;
        if( styleName.equals("BOLD") )
        {
            style = Font.BOLD;
        }
        else if( styleName.equals("ITALIC") )
        {
            style = Font.ITALIC;
        }
        else if( styleName.equals("PLAIN") )
        {
            style = Font.PLAIN;
        }

        
        String sizePropertyName = propertyName + "_Size";
        int size = 10;
        if( isValueSet(  sizePropertyName  ) )
        {
            size = getInteger(sizePropertyName).intValue();
        }
        Font retval = new Font( name, style, size );
        _logger.debug("propertyName = " +  retval );
        return retval;
    }
    
    /**
     * Returns a ImageIcon
     * <p>
     * The image file is found relative to the class's package directory. For example for 
     * the class foo.bar.Main, the image file would be in the directory foo/bar/.
     * <p>.
     * @param propertyName name of property which holds the filename of the icon.
     * @return ImageIcon the ImageIcon or null if not found.
     */
    public Icon getIcon(String propertyName) {
        Icon retval = null;

        String iconFilename = getStr(propertyName);
        if (iconFilename!=null && !iconFilename.equals("")) {

            if (iconFilename.equals("EMPTY16X16")) {
                retval = EMPTY16X16ICON;
            } else if (iconFilename.equals("EMPTY20X20")) {
                retval = EMPTY16X16ICON;
            } else {
                try {
                    retval = new ImageIcon(_class.getResource(iconFilename));
                } catch (NullPointerException e) {
                    String msg =
                        "Missing ImageIcon for: "
                            + _class.getName()
                            + "/"
                            + iconFilename
                            + " for locale '"
                            + _locale
                            + "'";
                    _logger.debug(msg, e);
                    _logger.error(msg);
                }
            }
        }

        return retval;
    }

    /**
     * Returns the KeyStroke defined by propertyName
     * <p>
     * The definition is as described in the KeyStroke.getKeyStroke( String s )
     * method.
     * @param propertyName name of property which defines the KeyStroke
     * @return KeyStroke returns the KeyStroke or null if there is an error in
     *                    the KeyStroke definition.
     */
    public KeyStroke getKeyStroke(String propertyName) {
        KeyStroke retval = null;
        String keyStrokeDescription = getString(propertyName);

        // See if the 
        if (!keyStrokeDescription.equals("")) {
            retval = KeyStroke.getKeyStroke(keyStrokeDescription);
            if (retval == null) {
                String msg =
                    "Missing or invalid keyStrokeDescription for property '"
                        + propertyName
                        + "' in bundle '"
                        + _class.getName()
                        + "'"
                        + " for locale '"
                        + _locale
                        + "'";
                _logger.error(msg);
            }
        }

        return retval;
    }


    public Integer getInteger(String propertyName) {
        String  integerString = getString(propertyName);
        
        try{
        return new Integer(integerString);
        }
        catch( NumberFormatException e )
        {
            return new Integer( 0 );
        }
    }
    
    /**
     * Returns a String defined by propertyName
     * @param propertyName name of property holding the String
     * @return String returns  the String or an empty String if there is an error.
     */
    public String getString(String propertyName) {
        String retval = getStr( propertyName );
        
        if( retval == null )
        {
            retval = propertyName;  
            String msg =
                    "Missing property '"
                        + propertyName
                        + "' in bundle '"
                        + _class.getName()
                        + "'"
                        + " for locale '"
                        + _locale
                        + "'";
            _logger.error(msg);
        }

        return retval;
    }

    /**
     * Returns a String defined by propertyName
     * @param propertyName name of property holding the String
     * @return String returns  the String or an empty String if there is an error.
     */
    private String getStr(String propertyName) {
        String retval = null;

        if (_resourceBundle != null) {
            try {
                retval = _resourceBundle.getString(propertyName);
            } catch (MissingResourceException e) {
                retval = null;
                String msg =
                    "Missing property '"
                        + propertyName
                        + "' in bundle '"
                        + _class.getName()
                        + "'"
                        + " for locale '"
                        + _locale
                        + "'";
                _logger.debug(msg, e);
            }
        }

        return retval;
    }

    /**
     * Configures an action from a property file
     * <p>
     *  @see #configureAction(String,Action)
     * <p>
     * @param a action to be configured
     */
    public void configureAction(Action a) {
        String propertyPrefix = "";
        configureAction(propertyPrefix, a);
    }

    /**
     * Configures an action from a property file
     * <p>
     * Uses the property names in this resource to configure the Action. The expected
     * property names are:
     * <ul>
     *   <li>AcceleratorKey</li>
     *   <li>LongDescription</li>
     *   <li>MnemonicKey</li>
     *   <li>Name</li>
     *   <li>ShortDescription</li>
     *   <li>SmallIcon</li>
     * </ul>
     * <p>
     * The propertyPrefix is used to prefix the the above property names. For example,
     * if propertyPrefix = "myAction", then the property name looked for is "myAction_MnemonicKey".
     * <p>
     * If propertyPrefix is an empty stream, then the above property names are used
     * without any special prefix.
     * <p>
     * @param a action to be configured
     * @param propertyPrefix String holding the prefix the property names
     */
    public void configureAction(String propertyPrefix, Action a) {
        if (!propertyPrefix.equals("")) {
            propertyPrefix = propertyPrefix + "_";
        }

        String name = getString(propertyPrefix + Action.NAME);
        a.putValue(Action.NAME, name);

        Icon smallIcon = getIcon(propertyPrefix + Action.SMALL_ICON);
        if (smallIcon != null) {
            a.putValue(Action.SMALL_ICON, smallIcon);
        }

        KeyStroke mnemonic = getKeyStroke(propertyPrefix + Action.MNEMONIC_KEY);
        if (mnemonic != null) {
            a.putValue(Action.MNEMONIC_KEY, new Integer(mnemonic.getKeyCode()));
        }

        KeyStroke accelerator =
            getKeyStroke(propertyPrefix + Action.ACCELERATOR_KEY);
        if (accelerator != null) {
            a.putValue(Action.ACCELERATOR_KEY, accelerator);
        }

        a.putValue(
            Action.SHORT_DESCRIPTION,
            getString(propertyPrefix + Action.SHORT_DESCRIPTION));
        a.putValue(
            Action.LONG_DESCRIPTION,
            getString(propertyPrefix + Action.LONG_DESCRIPTION));
    }

    /**
     * Configures a button from a property file
     * <p>
     *  @see #configureButton(String,JButton)
     * <p>
     * @param b Button to be configured
     */
    public void configureButton(JButton b) {
        String propertyPrefix = "";
        configureButton(propertyPrefix, b);
    }

    /**
     * Configures a button from a property file
     * <p>
     * Uses the property names in this resource to configure the JButton. The expected
     * property names are:
     * <ul>
     *   <li>MnemonicKey</li>
     *   <li>Text</li>
     *   <li>ShortDescription</li>
     *   <li>SmallIcon</li>
     * </ul>
     * <p>
     * The propertyPrefix is used to prefix the the above property names. For example,
     * if propertyPrefix = "myMenu", then the property name looked for is "myMenu_MnemonicKey".
     * <p>
     * If propertyPrefix is an empty stream, then the above property names are used
     * without any special prefix.
     * <p>
     * @param b menu to be configured
     * @param propertyPrefix String holding the prefix the property names
     */
    public void configureButton(String propertyPrefix, JButton b) {
        if (!propertyPrefix.equals("")) {
            propertyPrefix = propertyPrefix + "_";
        }

        String text = getString(propertyPrefix + "Text");
        b.setText(text);

        Icon smallIcon = getIcon(propertyPrefix + Action.SMALL_ICON);
        if (smallIcon != null) {
            b.setIcon(smallIcon);
        }

        KeyStroke mnemonic = getKeyStroke(propertyPrefix + Action.MNEMONIC_KEY);
        if (mnemonic != null) {
            b.setMnemonic(mnemonic.getKeyCode());
        }

        b.setToolTipText(getString(propertyPrefix + Action.SHORT_DESCRIPTION));
    }

    /**
     * Configures a menu from a property file
     * <p>
     *  @see #configureMenu(String,JMenu)
     * <p>
     * @param m menuto be configured
     */
    public void configureMenu(JMenu m) {
        String propertyPrefix = "";
        configureMenu(propertyPrefix, m);
    }

    /**
     * Configures a menu from a property file
     * <p>
     * Uses the property names in this resource to configure the JMenu. The expected
     * property names are:
     * <ul>
     *   <li>MnemonicKey</li>
     *   <li>Text</li>
     *   <li>ShortDescription</li>
     *   <li>SmallIcon</li>
     * </ul>
     * <p>
     * The propertyPrefix is used to prefix the the above property names. For example,
     * if propertyPrefix = "myMenu", then the property name looked for is "myMenu_MnemonicKey".
     * <p>
     * If propertyPrefix is an empty stream, then the above property names are used
     * without any special prefix.
     * <p>
     * @param m menu to be configured
     * @param propertyPrefix String holding the prefix the property names
     */
    public void configureMenu(String propertyPrefix, JMenu m) {
        if (!propertyPrefix.equals("")) {
            propertyPrefix = propertyPrefix + "_";
        }

        String text = getString(propertyPrefix + "Text");
        m.setText(text);

        Icon smallIcon = getIcon(propertyPrefix + Action.SMALL_ICON);
        if (smallIcon != null) {
            m.setIcon(smallIcon);
        }

        KeyStroke mnemonic = getKeyStroke(propertyPrefix + Action.MNEMONIC_KEY);
        if (mnemonic != null) {
            m.setMnemonic(mnemonic.getKeyCode());
        }

        String toolTip = getStr(propertyPrefix + Action.SHORT_DESCRIPTION);
        if( toolTip == null )
            m.setToolTipText(toolTip);
    }

    /**
     * sets the mnemonic.key for a JMenu
     * <p>
     * @see #getKeyStroke(java.lang.String) for how the property key is parsed.
     * <p>
     * @param menu JMenu to set the mnemonic key
     * @param propertyName property name of the key stroke
     */
    public void setMnemonic(JMenu menu, String propertyName) {
        KeyStroke mnemonicKeyStroke = getKeyStroke(propertyName);
        if (mnemonicKeyStroke != null) {
            menu.setMnemonic(mnemonicKeyStroke.getKeyCode());
        }
    }

    /**
     * format the provided args
     * <p>
     *  Format takes a set of objects, formats them, then inserts the 
     * formatted strings into the pattern at the appropriate places. 
     * <p>
     * <b>propertyName</b> is used to find the pattern in the resource bundle.
     * <p>
     * Formating is based on java.text.MessageFormat.
     * @param propertyName property name of the patter
     * @param args arguments for the MessageFormater
     * @return String formated message
     */
    public String format(String propertyName, Object[] args) {
        String retval = null;

        if (_resourceBundle != null) {
            try {
                String pattern = _resourceBundle.getString(propertyName);
                retval = MessageFormat.format(pattern, args);
            } catch (MissingResourceException e) {
                retval = null;
                String msg =
                    "Missing property (format)'"
                        + propertyName
                        + "' in bundle '"
                        + _class.getName()
                        + "'"
                        + " for locale '"
                        + _locale
                        + "'";
                _logger.debug(msg, e);
                _logger.error(msg);
            }
        }

        if (retval == null) {
            retval = propertyName;
            for (int ii = 0; ii < args.length; ii++) {
                retval += ", {" + ii + "}=" + args[ii];
            }
        }

        return retval;
    }

    /**
     * Registers accelerator.key on a component
     * <p>
     * Convenience method to register an actions accelerator key onto a 
     * JComponent. If the action does not have Action.ACCELERATOR_KEY defined
     * then the method does nothing.
     * <p>
     * @param component JComponent to which the accelerator will be registered
     * @param action Action with the accelerator
     */
    public static void registerAccelerator(
        JComponent component,
        Action action) {
        KeyStroke accerator =
            (KeyStroke) action.getValue(Action.ACCELERATOR_KEY);
        if (accerator != null)
            component.registerKeyboardAction(
                action,
                accerator,
                JComponent.WHEN_FOCUSED);
        //JComponent.WHEN_IN_FOCUSED_WINDOW);
        //JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    /**
     * Factory method to return a ResourceManager for clazz.
     * <p>
     * @param clazz class to get ResourceManager for
     * @return ResourceManager the ResourceManager for clazz
     */
    public static ResourceManager getManager(Class clazz) {
        // Enusre we know the current locale
        if (_locale == null) {
            _locale = Locale.getDefault();
        }

        // Clear our cache of ResourceManagers if the locale has changed.
        if (_locale != Locale.getDefault()) {
            _resourceManagers.clear();
        }

        // See if ResourceManager is in our cache, if not create it
        ResourceManager retval = (ResourceManager) _resourceManagers.get(clazz);
        if (retval == null) {
            retval = new ResourceManager(clazz);
            _resourceManagers.put(clazz, retval);
        }

        // Returns the ResourceManager 
        return retval;
    }
}