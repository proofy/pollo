package org.outerj.pollo.gui;

import org.outerj.pollo.config.PolloConfiguration;
import org.outerj.pollo.util.ResourceManager;

import javax.swing.*;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * User preferences dialog.
 */
public class UserPreferencesDialog extends JPanel implements ActionListener
{
    protected static UserPreferencesDialog instance;
    protected boolean ok;
    protected ArrayList configurationPanels = new ArrayList();
    protected static final ResourceManager resourceManager = ResourceManager.getManager(UserPreferencesDialog.class);

    public static UserPreferencesDialog getInstance()
    {
        if (instance == null)
        {
            instance = new UserPreferencesDialog();
        }

        return instance;
    }

    public UserPreferencesDialog()
    {
        setBorder(new EmptyBorder(12, 12, 12, 12));
        setLayout(new BorderLayout(12, 12));

        JTabbedPane tabbedPane = new JTabbedPane();
        this.add(tabbedPane, BorderLayout.CENTER);

        ViewSettingsPanel viewSettingsPanel = new ViewSettingsPanel();
        configurationPanels.add(viewSettingsPanel);
        tabbedPane.add(resourceManager.getString("fonts-and-colors-tab"), viewSettingsPanel);

        VariousSettingsPanel variousSettingsPanel = new VariousSettingsPanel();
        configurationPanels.add(variousSettingsPanel);
        tabbedPane.add(resourceManager.getString("various-tab"), variousSettingsPanel);

        JButton okButton = new JButton(resourceManager.getString("ok"));
        okButton.setActionCommand("ok");
        okButton.addActionListener(this);

        JButton cancelButton = new JButton(resourceManager.getString("cancel"));
        cancelButton.setActionCommand("cancel");
        cancelButton.addActionListener(this);

        Box buttons = new Box(BoxLayout.X_AXIS);
        buttons.add(Box.createGlue());
        buttons.add(okButton);
        buttons.add(Box.createHorizontalStrut(6));
        buttons.add(cancelButton);
        add(buttons, BorderLayout.SOUTH);

    }

    public boolean showDialog(Frame parent)
    {
        JDialog dialog = new JDialog(parent, resourceManager.getString("dialog-title"));
        dialog.addWindowListener(new WindowAdapter() { public void windowClosing(WindowEvent evt) { ok = false; }});
        dialog.setModal(true);
        dialog.setContentPane(this);
        dialog.pack();
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);
        dialog.getLayeredPane().remove(this);
        return ok;
    }

    public abstract class ConfigurationPanel extends JPanel
    {
        public abstract void readConfiguration(PolloConfiguration configuration);

        public abstract void storeConfiguration(PolloConfiguration configuration);
    }

    public class ViewSettingsPanel extends ConfigurationPanel
    {
        protected FontSizeComboBox elementNameFontSizeCombo = new FontSizeComboBox();
        protected FontStyleComboBox elementNameFontStyleCombo = new FontStyleComboBox();
        protected FontSizeComboBox attributeNameFontSizeCombo = new FontSizeComboBox();
        protected FontStyleComboBox attributeNameFontStyleCombo = new FontStyleComboBox();
        protected FontSizeComboBox attributeValueFontSizeCombo = new FontSizeComboBox();
        protected FontStyleComboBox attributeValueFontStyleCombo = new FontStyleComboBox();
        protected JCheckBox textAntialiasingCheckBox = new JCheckBox(resourceManager.getString("antialias-text"));
        protected FontSizeComboBox textFontSizeCombo = new FontSizeComboBox();

        public ViewSettingsPanel()
        {
            setLayout(new GridBagLayout());

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(0, 6, 0, 6);
            gbc.anchor = gbc.WEST;
            gbc.gridx = 1;
            gbc.gridy = 0;
            add(new JLabel(resourceManager.getString("size")), gbc);

            gbc.gridx = 2;
            gbc.gridy = 0;
            add(new JLabel(resourceManager.getString("style")), gbc);

            gbc.gridx = 0;
            gbc.gridy = 1;
            add(new JLabel(resourceManager.getString("element-name-font")), gbc);

            gbc.gridx = 1;
            gbc.gridy = 1;
            add(elementNameFontSizeCombo, gbc);

            gbc.gridx = 2;
            gbc.gridy = 1;
            add(elementNameFontStyleCombo, gbc);

            gbc.gridx = 0;
            gbc.gridy = 2;
            add(new JLabel(resourceManager.getString("attribute-name-font")), gbc);

            gbc.gridx = 1;
            gbc.gridy = 2;
            add(attributeNameFontSizeCombo, gbc);

            gbc.gridx = 2;
            gbc.gridy = 2;
            add(attributeNameFontStyleCombo, gbc);

            gbc.gridx = 0;
            gbc.gridy = 3;
            add(new JLabel(resourceManager.getString("attribute-value-font")), gbc);

            gbc.gridx = 1;
            gbc.gridy = 3;
            add(attributeValueFontSizeCombo, gbc);

            gbc.gridx = 2;
            gbc.gridy = 3;
            add(attributeValueFontStyleCombo, gbc);

            gbc.gridx = 0;
            gbc.gridy = 4;
            add(new JLabel(resourceManager.getString("text-font")), gbc);

            gbc.gridx = 1;
            gbc.gridy = 4;
            add(textFontSizeCombo, gbc);

            gbc.gridx = 0;
            gbc.gridy = 5;
            add(textAntialiasingCheckBox, gbc);
        }


        public void readConfiguration(PolloConfiguration configuration)
        {
            elementNameFontSizeCombo.setFontSize(configuration.getElementNameFontSize());
            elementNameFontStyleCombo.setStyle(configuration.getElementNameFontStyle());
            attributeNameFontSizeCombo.setFontSize(configuration.getAttributeNameFontSize());
            attributeNameFontStyleCombo.setStyle(configuration.getAttributeNameFontStyle());
            attributeValueFontSizeCombo.setFontSize(configuration.getAttributeValueFontSize());
            attributeValueFontStyleCombo.setStyle(configuration.getAttributeValueFontStyle());
            textFontSizeCombo.setFontSize(configuration.getTextFontSize());
            textAntialiasingCheckBox.setSelected(configuration.isTextAntialiasing());
        }

        public void storeConfiguration(PolloConfiguration configuration)
        {
            configuration.setElementNameFontSize(elementNameFontSizeCombo.getFontSize());
            configuration.setElementNameFontStyle(elementNameFontStyleCombo.getStyle());
            configuration.setAttributeNameFontSize(attributeNameFontSizeCombo.getFontSize());
            configuration.setAttributeNameFontStyle(attributeNameFontStyleCombo.getStyle());
            configuration.setAttributeValueFontSize(attributeValueFontSizeCombo.getFontSize());
            configuration.setAttributeValueFontStyle(attributeValueFontStyleCombo.getStyle());
            configuration.setTextFontSize(textFontSizeCombo.getFontSize());
            configuration.setTextAntialiasing(textAntialiasingCheckBox.isSelected());
        }
    }

    public class VariousSettingsPanel extends ConfigurationPanel
    {
        public JTextField undoLevels;

        public VariousSettingsPanel()
        {
            setLayout(new GridBagLayout());

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(0, 6, 0, 6);
            gbc.anchor = gbc.WEST;
            gbc.gridx = 0;
            gbc.gridy = 0;
            add(new JLabel(resourceManager.getString("undolevels")), gbc);

            undoLevels = new NumericTextField();
            undoLevels.setColumns(5);
            gbc.gridx = 1;
            gbc.gridy = 0;
            add(undoLevels, gbc);

            gbc.gridx = 1;
            gbc.gridy = 1;
            add(new JLabel("(will only impact newly opened files)"), gbc);
        }

        public void readConfiguration(PolloConfiguration configuration)
        {
            undoLevels.setText(String.valueOf(configuration.getUndoLevels()));
        }

        public void storeConfiguration(PolloConfiguration configuration)
        {
            configuration.setUndoLevels(Integer.parseInt(undoLevels.getText()));
        }
    }

    public void actionPerformed(ActionEvent event)
    {
        if (event.getActionCommand().equals("ok"))
        {
            ok = true;
            getTopLevelAncestor().setVisible(false);
        }
        else if (event.getActionCommand().equals("cancel"))
        {
            ok = false;
            getTopLevelAncestor().setVisible(false);
        }
    }

    public void readConfiguration(PolloConfiguration configuration)
    {
        Iterator configurationPanelIt = configurationPanels.iterator();
        while (configurationPanelIt.hasNext())
        {
            ConfigurationPanel configurationPanel = (ConfigurationPanel)configurationPanelIt.next();
            configurationPanel.readConfiguration(configuration);
        }
    }

    public void storeConfiguration(PolloConfiguration configuration)
    {
        Iterator configurationPanelIt = configurationPanels.iterator();
        while (configurationPanelIt.hasNext())
        {
            ConfigurationPanel configurationPanel = (ConfigurationPanel)configurationPanelIt.next();
            configurationPanel.storeConfiguration(configuration);
        }
    }

}

class FontSizeComboBox extends JComboBox
{
    protected static String[] fontSizes = new String[15];
    protected static final int MIN_SIZE = 6;
    protected static final int MAX_SIZE_COUNT = 14;
    protected static final int DEFAULT_SIZE = 12;
    static
    {
        for (int i = 0; i <= MAX_SIZE_COUNT; i++)
            fontSizes[i] = String.valueOf(i + MIN_SIZE);
    }

    public FontSizeComboBox()
    {
        super(fontSizes);
    }

    public int getFontSize()
    {
        return getSelectedIndex() != -1 ? getSelectedIndex() + MIN_SIZE : DEFAULT_SIZE;
    }

    public void setFontSize(int size)
    {
        int index = size - MIN_SIZE;
        if (index < 0 || index > MAX_SIZE_COUNT)
            index = DEFAULT_SIZE - MIN_SIZE;
        setSelectedIndex(index);
    }
}

class FontStyleComboBox extends JComboBox
{
    protected static final ResourceManager resourceManager = ResourceManager.getManager(FontStyleComboBox.class);
    protected static final String[] styles = {resourceManager.getString("font-normal"),
                                              resourceManager.getString("font-bold"),
                                              resourceManager.getString("font-italic"),
                                              resourceManager.getString("font-bold-italic")};
    protected static final int NORMAL_INDEX = 0;
    protected static final int BOLD_INDEX = 1;
    protected static final int ITALIC_INDEX = 2;
    protected static final int BOLD_ITALIC_INDEX = 3;

    public FontStyleComboBox()
    {
        super(styles);
    }

    public int getStyle()
    {
        switch (getSelectedIndex())
        {
            case NORMAL_INDEX:
                return 0;
            case BOLD_INDEX:
                return Font.BOLD;
            case ITALIC_INDEX:
                return Font.ITALIC;
            case BOLD_ITALIC_INDEX:
                return Font.BOLD + Font.ITALIC;
            default:
                return 0;
        }
    }

    public void setStyle(int style)
    {
        switch (style)
        {
            case Font.BOLD:
                setSelectedIndex(BOLD_INDEX);
                break;
            case Font.ITALIC:
                setSelectedIndex(ITALIC_INDEX);
                break;
            case Font.BOLD + Font.ITALIC:
                setSelectedIndex(BOLD_ITALIC_INDEX);
                break;
            default:
                setSelectedIndex(NORMAL_INDEX);
        }
    }
}

class NumericTextField extends JTextField
{
    protected Document createDefaultModel()
    {
        return new WholeNumberDocument();
    }

    protected class WholeNumberDocument extends PlainDocument
    {

        public void insertString(int offs, String str, AttributeSet a)
                        throws BadLocationException
        {

            char[] source = str.toCharArray();
            char[] result = new char[source.length];
            int j = 0;

            for (int i = 0; i < result.length; i++)
            {
                if (Character.isDigit(source[i]))
                    result[j++] = source[i];
                else
                {
                    Toolkit.getDefaultToolkit().beep();
                }
            }
            super.insertString(offs, new String(result, 0, j), a);
        }
    }
}
