package org.outerj.pollo.gui;

import org.outerj.pollo.Pollo;
import org.outerj.pollo.xmleditor.displayspec.IDisplaySpecification;
import org.outerj.pollo.config.*;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.io.File;

/**
 * A dialog from which the user can select a viewtype.
 *
 * @author Bruno Dumon
 */
public class ViewTypesDialog extends JPanel implements ActionListener
{
    protected static ViewTypesDialog instance;

    protected boolean ok = false;
    protected JList viewTypesList;

    protected JRadioButton predefinedViewTypeButton;
    protected JRadioButton customViewTypeButton;

    protected JPanel schemaPanel;
    protected JComboBox schemaFileCombo;
    protected JButton browseForFileButton;
    protected JRadioButton autodetectSchemaButton;
    protected JRadioButton dtdButton;
    protected JRadioButton polloButton;

    protected JPanel displaySpecPanel;
    protected JRadioButton genericRandomColors;
    protected JRadioButton genericFixedColor;
    protected JButton colorSelectButton;
    protected JRadioButton basicDisplaySpec;
    protected JTextField basicDisplaySpecFileField;
    protected JButton basicDisplaySpecBrowseButton;
    protected JRadioButton polloTree;
    protected JRadioButton classicTree;

    protected JRadioButton useConfiguredTreetype;
    protected JRadioButton forcePolloTreetype;
    protected JRadioButton forceClassicTreetype;

    protected JFileChooser schemaChooser;
    protected JFileChooser displaySpecChooser;

    public static ViewTypesDialog getInstance()
    {
        if (instance == null)
        {
            instance = new ViewTypesDialog();
        }

        return instance;
    }

    public ViewTypesDialog()
    {
        // as an experiment, I did the layout of this dialog with a lot of Box'es
        // instead of with a GridBagLayout. Seems to work faster.

        setBorder(new EmptyBorder(12, 12, 12, 12));

        setLayout(new BorderLayout(12, 12));

        Box verticalBox = new Box(BoxLayout.Y_AXIS);

        ButtonGroup predefinedOrCustomButtonGroup = new ButtonGroup();

        // first radio button: select a predefined configuration
        predefinedViewTypeButton = new JRadioButton("Select a predefined schema & display configuration:");
        predefinedOrCustomButtonGroup.add(predefinedViewTypeButton);
        predefinedViewTypeButton.setActionCommand("view-type-changed");
        predefinedViewTypeButton.addActionListener(this);
        predefinedViewTypeButton.setSelected(true);
        Box box1 = new Box(BoxLayout.X_AXIS);
        box1.add(predefinedViewTypeButton);
        box1.add(Box.createHorizontalGlue());
        verticalBox.add(box1);

        // make a box containing the options for forcing the tree type
        Box treetypeBox = new Box(BoxLayout.Y_AXIS);
        useConfiguredTreetype = new JRadioButton("Specified in selected configuration.");
        forcePolloTreetype = new JRadioButton("Pollo (colored bars)");
        forceClassicTreetype = new JRadioButton("Classic");
        ButtonGroup treetypeGroup = new ButtonGroup();
        treetypeGroup.add(useConfiguredTreetype);
        treetypeGroup.add(forcePolloTreetype);
        treetypeGroup.add(forceClassicTreetype);
        useConfiguredTreetype.setSelected(true);
        treetypeBox.add(Box.createVerticalGlue());
        treetypeBox.add(new JLabel("Use tree type:"));
        treetypeBox.add(useConfiguredTreetype);
        treetypeBox.add(forcePolloTreetype);
        treetypeBox.add(forceClassicTreetype);
        treetypeBox.add(Box.createVerticalStrut(12));

        // make the list with predefined configuration
        Pollo pollo = Pollo.getInstance();
        PolloConfiguration config = pollo.getConfiguration();

        viewTypesList = new JList(config.getViewTypes().toArray());
        viewTypesList.setSelectedIndex(0);
        // make the list react on double clicks
        MouseListener mouseListener = new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int index = viewTypesList.locationToIndex(e.getPoint());
                    if (index != -1)
                    {
                        viewTypesList.setSelectedIndex(index);
                        ok = true;
                        getTopLevelAncestor().setVisible(false);
                    }
                }
            }
        };
        viewTypesList.addMouseListener(mouseListener);
        JScrollPane viewTypesListScrollPane = new JScrollPane(viewTypesList);
        Dimension spDimension = viewTypesListScrollPane.getPreferredSize();
        spDimension.height = Integer.MAX_VALUE;
        viewTypesListScrollPane.setMaximumSize(spDimension);
        Box box2 = new Box(BoxLayout.X_AXIS);
        box2.add(Box.createHorizontalStrut(18));
        box2.add(viewTypesListScrollPane);
        box2.add(Box.createHorizontalStrut(12));
        box2.add(treetypeBox);
        box2.add(Box.createHorizontalGlue());
        verticalBox.add(box2);

        // second option: choose a schema file
        customViewTypeButton = new JRadioButton("Choose an other schema and display specification:");
        predefinedOrCustomButtonGroup.add(customViewTypeButton);
        customViewTypeButton.setActionCommand("view-type-changed");
        customViewTypeButton.addActionListener(this);
        Box box3 = new Box(BoxLayout.X_AXIS);
        box3.add(customViewTypeButton);
        box3.add(Box.createHorizontalGlue());
        verticalBox.add(box3);

        // create panel for schema options
        schemaPanel = new JPanel();
        Border border = BorderFactory.createEtchedBorder(BevelBorder.LOWERED);
        Border border2 = BorderFactory.createTitledBorder(border, "Schema:");
        Border border3 = BorderFactory.createCompoundBorder(border2, schemaPanel.getBorder());
        schemaPanel.setBorder(border3);
        Box schemaBox = new Box(BoxLayout.Y_AXIS);
        schemaPanel.setLayout(new BorderLayout());
        schemaPanel.add(schemaBox, BorderLayout.CENTER);
        Box spacerBox1 = new Box(BoxLayout.X_AXIS);
        spacerBox1.add(Box.createHorizontalStrut(18));
        spacerBox1.add(schemaPanel);
        verticalBox.add(spacerBox1);

        // field + button to choose schema file name
        schemaFileCombo = new JComboBox(pollo.getConfiguration().getRecentlyUsedSchemasModel());
        schemaFileCombo.setEditable(true);
        schemaFileCombo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                JComboBox source = (JComboBox)e.getSource();
                source.getEditor().setItem(source.getSelectedItem());
            }
        });
        Dimension fnfDimension = schemaFileCombo.getPreferredSize();
        fnfDimension.width = Integer.MAX_VALUE;
        schemaFileCombo.setMaximumSize(fnfDimension);
        browseForFileButton = new JButton("Browse...");
        browseForFileButton.setActionCommand("browse-for-schema");
        browseForFileButton.addActionListener(this);
        Box box4 = new Box(BoxLayout.X_AXIS);
        box4.add(schemaFileCombo);
        box4.add(browseForFileButton);
        schemaBox.add(box4);

        // schema type selection
        ButtonGroup schemaTypeGroup = new ButtonGroup();
        Box box5 = new Box(BoxLayout.X_AXIS);

        autodetectSchemaButton = new JRadioButton("W3C XML Schema, Relax NG, Relax Core, Relax ns, Trex");
        autodetectSchemaButton.setSelected(true);
        schemaTypeGroup.add(autodetectSchemaButton);
        box5.add(autodetectSchemaButton);

        dtdButton = new JRadioButton("DTD");
        box5.add(dtdButton);
        schemaTypeGroup.add(dtdButton);

        polloButton = new JRadioButton("Pollo");
        box5.add(polloButton);
        schemaTypeGroup.add(polloButton);

        box5.add(Box.createHorizontalGlue());
        schemaBox.add(box5);

        // make schema panel height fixed
        Dimension schemaPanelDimension = schemaPanel.getPreferredSize();
        schemaPanelDimension.width = Integer.MAX_VALUE;
        schemaPanel.setMaximumSize(schemaPanelDimension);

        // selection of custom display specification
        displaySpecPanel = new JPanel();
        Border border4 = BorderFactory.createEtchedBorder(BevelBorder.LOWERED);
        Border border5 = BorderFactory.createTitledBorder(border4, "Display specification :");
        Border border6 = BorderFactory.createCompoundBorder(border5, displaySpecPanel.getBorder());
        displaySpecPanel.setBorder(border6);
        displaySpecPanel.setLayout(new BorderLayout());
        Box displaySpecVertBox = new Box(BoxLayout.Y_AXIS);
        displaySpecPanel.add(displaySpecVertBox, BorderLayout.CENTER);
        Box spacerBox2 = new Box(BoxLayout.X_AXIS);
        spacerBox2.add(Box.createHorizontalStrut(18));
        spacerBox2.add(displaySpecPanel);
        verticalBox.add(spacerBox2);

        ButtonGroup displaySpecGroup = new ButtonGroup();

        Box box6 = new Box(BoxLayout.X_AXIS);
        genericRandomColors = new JRadioButton("Generic with random assignment of element colors");
        genericRandomColors.setSelected(true);
        genericRandomColors.setActionCommand("display-spec-option-changed");
        genericRandomColors.addActionListener(this);
        displaySpecGroup.add(genericRandomColors);
        box6.add(genericRandomColors);
        box6.add(Box.createHorizontalGlue());
        displaySpecVertBox.add(box6);

        Box box7 = new Box(BoxLayout.X_AXIS);
        genericFixedColor = new JRadioButton("Generic with fixed element color: ");
        genericFixedColor.setActionCommand("display-spec-option-changed");
        genericFixedColor.addActionListener(this);
        displaySpecGroup.add(genericFixedColor);
        box7.add(genericFixedColor);
        colorSelectButton = new JButton("Select color...");
        colorSelectButton.setActionCommand("select-color");
        colorSelectButton.addActionListener(this);
        colorSelectButton.setBackground(Color.white);
        box7.add(colorSelectButton);
        box7.add(Box.createHorizontalGlue());
        displaySpecVertBox.add(box7);

        Box box8 = new Box(BoxLayout.X_AXIS);
        basicDisplaySpec = new JRadioButton("Read from file: ");
        basicDisplaySpec.setActionCommand("display-spec-option-changed");
        basicDisplaySpec.addActionListener(this);
        displaySpecGroup.add(basicDisplaySpec);
        box8.add(basicDisplaySpec);
        basicDisplaySpecFileField = new JTextField("");
        box8.add(basicDisplaySpecFileField);
        basicDisplaySpecBrowseButton = new JButton("Browse...");
        basicDisplaySpecBrowseButton.setActionCommand("browse-for-displayspec");
        basicDisplaySpecBrowseButton.addActionListener(this);
        box8.add(basicDisplaySpecBrowseButton);
        displaySpecVertBox.add(box8);

        Box box9 = new Box(BoxLayout.X_AXIS);
        ButtonGroup treeTypeGroup = new ButtonGroup();
        polloTree = new JRadioButton("Pollo tree");
        treeTypeGroup.add(polloTree);
        box9.add(polloTree);
        polloTree.setSelected(true);
        classicTree = new JRadioButton("Classic tree");
        treeTypeGroup.add(classicTree);
        box9.add(classicTree);
        box9.add(Box.createHorizontalGlue());
        displaySpecVertBox.add(box9);

        colorSelectButton.setEnabled(false);
        basicDisplaySpecFileField.setEnabled(false);
        basicDisplaySpecBrowseButton.setEnabled(false);

        // make displaySpecPanel height fixed
        Dimension displaySpecPanelDimension = displaySpecPanel.getPreferredSize();
        displaySpecPanelDimension.width = Integer.MAX_VALUE;
        displaySpecPanel.setMaximumSize(displaySpecPanelDimension);


        add(verticalBox, BorderLayout.CENTER);


        JButton okButton = new JButton("Okay");
        okButton.setActionCommand("ok");
        okButton.addActionListener(this);

        JButton cancelButton = new JButton("Cancel");
        cancelButton.setActionCommand("cancel");
        cancelButton.addActionListener(this);

        Box buttons = new Box(BoxLayout.X_AXIS);
        buttons.add(Box.createGlue());
        buttons.add(okButton);
        buttons.add(Box.createHorizontalStrut(6));
        buttons.add(cancelButton);
        add(buttons, BorderLayout.SOUTH);

        enableSelectCustom(false);
        enableSelectPredefined(true);

    }


    public void actionPerformed(ActionEvent event)
    {
        if (event.getActionCommand().equals("ok"))
        {
            if (customViewTypeButton.isSelected() && ((String)schemaFileCombo.getEditor().getItem()).trim().equals(""))
            {
                JOptionPane.showMessageDialog(getTopLevelAncestor(), "Please enter the schema file name to use.");
                return;
            }
            ok = true;
            getTopLevelAncestor().setVisible(false);
        }
        else if (event.getActionCommand().equals("cancel"))
        {
            ok = false;
            getTopLevelAncestor().setVisible(false);
        }
        else if (event.getActionCommand().equals("view-type-changed"))
        {
            if (predefinedViewTypeButton.isSelected())
            {
                enableSelectCustom(false);
                enableSelectPredefined(true);
            }
            else
            {
                enableSelectPredefined(false);
                enableSelectCustom(true);
            }
        }
        else if (event.getActionCommand().equals("browse-for-schema"))
        {
            PolloConfiguration polloConfiguration = Pollo.getInstance().getConfiguration();
            if (schemaChooser == null)
            {
                schemaChooser = new JFileChooser();
                schemaChooser.setDialogTitle("Choose a schema");
                String schemaCurrentDir = polloConfiguration.getSchemaOpenDialogPath();
                if (schemaCurrentDir != null)
                    schemaChooser.setCurrentDirectory(new File(schemaCurrentDir));
                schemaChooser.setDialogType(JFileChooser.OPEN_DIALOG);
            }

            int returnVal = schemaChooser.showOpenDialog(getTopLevelAncestor());
            if (returnVal == JFileChooser.APPROVE_OPTION)
            {
                String path = schemaChooser.getSelectedFile().getAbsolutePath();
                schemaFileCombo.getEditor().setItem(path);
                polloConfiguration.setSchemaOpenDialogPath(path);
            }
        }
        else if (event.getActionCommand().equals("browse-for-displayspec"))
        {
            if (displaySpecChooser == null)
            {
                displaySpecChooser = new JFileChooser();
                displaySpecChooser.setDialogTitle("Choose a display specification");
                displaySpecChooser.setDialogType(JFileChooser.OPEN_DIALOG);
            }

            int returnVal = displaySpecChooser.showOpenDialog(getTopLevelAncestor());
            if (returnVal == JFileChooser.APPROVE_OPTION)
            {
                String path = displaySpecChooser.getSelectedFile().getAbsolutePath();
                basicDisplaySpecFileField.setText(path);
            }
        }
        else if (event.getActionCommand().equals("select-color"))
        {
            Color newColor = JColorChooser.showDialog(
                                 getTopLevelAncestor(),
                                 "Choose Element Background Color",
                                 colorSelectButton.getBackground());
            if (newColor != null)
                colorSelectButton.setBackground(newColor);
        }
        else if (event.getActionCommand().equals("display-spec-option-changed"))
        {
            if (genericRandomColors.isSelected())
            {
                colorSelectButton.setEnabled(false);
                basicDisplaySpecFileField.setEnabled(false);
                basicDisplaySpecBrowseButton.setEnabled(false);
            }
            else if (genericFixedColor.isSelected())
            {
                colorSelectButton.setEnabled(true);
                basicDisplaySpecFileField.setEnabled(false);
                basicDisplaySpecBrowseButton.setEnabled(false);
            }
            else if (basicDisplaySpec.isSelected())
            {
                colorSelectButton.setEnabled(false);
                basicDisplaySpecFileField.setEnabled(true);
                basicDisplaySpecBrowseButton.setEnabled(true);
            }
        }

    }

    protected void enableSelectPredefined(boolean enabled)
    {
        viewTypesList.setEnabled(enabled);
        useConfiguredTreetype.setEnabled(enabled);
        forcePolloTreetype.setEnabled(enabled);
        forceClassicTreetype.setEnabled(enabled);
    }

    protected void enableSelectCustom(boolean enabled)
    {
        schemaFileCombo.setEnabled(enabled);
        browseForFileButton.setEnabled(enabled);
        autodetectSchemaButton.setEnabled(enabled);
        dtdButton.setEnabled(enabled);
        schemaPanel.setEnabled(enabled);
        displaySpecPanel.setEnabled(enabled);
        genericRandomColors.setEnabled(enabled);
        genericFixedColor.setEnabled(enabled);
        colorSelectButton.setEnabled(enabled);
        polloButton.setEnabled(enabled);
        basicDisplaySpec.setEnabled(enabled);
        basicDisplaySpecFileField.setEnabled(enabled);
        basicDisplaySpecBrowseButton.setEnabled(enabled);
        polloTree.setEnabled(enabled);
        classicTree.setEnabled(enabled);
    }

    /**
     * @return true if the user selected ok, otherwise false
     */
    public boolean showDialog(Frame parent)
    {
        JDialog dialog = new JDialog(parent, "Select View Type");
        dialog.addWindowListener(new WindowAdapter() { public void windowClosing(WindowEvent evt) { ok = false; }});
        dialog.setModal(true);
        dialog.setContentPane(this);
        dialog.pack();
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);
        //System.out.println("na de show");
        dialog.getLayeredPane().remove(this);
        return ok;
    }

    class WindowHandler extends WindowAdapter
    {
        public void windowClosing(WindowEvent evt)
        {
            ok = false;
        }
    }

    /**
     * Get the ViewTypeConf for the selected view type.
     */
    public ViewTypeConf getSelectedViewTypeConf()
    {
        if (predefinedViewTypeButton.isSelected())
        {
            return (ViewTypeConf)viewTypesList.getSelectedValue();
        }
        else
        {
            // create the ViewTypeConf dynamically

            ViewTypeConf viewTypeConf = new ViewTypeConf();

            // create schema configuration
            SchemaConfItem schemaConf = new SchemaConfItem();
            String schemaFileName = (String)schemaFileCombo.getEditor().getItem();
            schemaConf.addInitParam("source", schemaFileName);
            Pollo.getInstance().getConfiguration().addRecentlyUsedSchema(schemaFileName);

            if (polloButton.isSelected())
            {
                schemaConf.setFactoryClass("org.outerj.pollo.xmleditor.schema.BasicSchemaFactory");
            }
            else
            {
                schemaConf.setFactoryClass("org.outerj.pollo.xmleditor.schema.MsvSchemaFactory");

                if (dtdButton.isSelected())
                    schemaConf.addInitParam("type", "dtd");
                // other types are auto-detected by MSV
            }

            viewTypeConf.addSchema(schemaConf);

            // create display spec configuration
            DisplaySpecConfItem displaySpecConf = new DisplaySpecConfItem();
            // select the tree type
            if (genericRandomColors.isSelected() || genericFixedColor.isSelected())
            {
                displaySpecConf.setFactoryClass("org.outerj.pollo.xmleditor.displayspec.GenericDisplaySpecFactory");

                if (classicTree.isSelected())
                    displaySpecConf.addInitParam("treetype", "classic");
                else
                    displaySpecConf.addInitParam("treetype", "pollo");

                if (genericRandomColors.isSelected())
                {
                    displaySpecConf.addInitParam("use-random-colors", "true");
                }
                else
                {
                    Color color = colorSelectButton.getBackground();
                    displaySpecConf.addInitParam("fixed-color", color.getRed() + "," + color.getGreen() + "," + color.getBlue());
                }
                viewTypeConf.addDisplaySpec(displaySpecConf);
            }
            else if (basicDisplaySpec.isSelected())
            {
                displaySpecConf.setFactoryClass("org.outerj.pollo.xmleditor.displayspec.BasicDisplaySpecFactory");
                displaySpecConf.addInitParam("source", basicDisplaySpecFileField.getText());
                viewTypeConf.addDisplaySpec(displaySpecConf);

                // the display spec chain should always end with a generic implementation (its methods should never return null)
                DisplaySpecConfItem displaySpec2 = new DisplaySpecConfItem();
                displaySpec2.setFactoryClass("org.outerj.pollo.xmleditor.displayspec.GenericDisplaySpecFactory");
                viewTypeConf.addDisplaySpec(displaySpec2);
            }


            AttrEditorPluginConfItem attrEditorPluginConf = new AttrEditorPluginConfItem();
            attrEditorPluginConf.setFactoryClass("org.outerj.pollo.xmleditor.plugin.DefaultAttributeEditorPluginFactory");
            viewTypeConf.addAttrEditorPlugin(attrEditorPluginConf);

            return viewTypeConf;
        }
    }

    public int getTreeType()
    {
        if (forcePolloTreetype.isSelected())
            return IDisplaySpecification.POLLO_TREE;
        else if (forceClassicTreetype.isSelected())
            return IDisplaySpecification.CLASSIC_TREE;
        else
            return -1;
    }
}
