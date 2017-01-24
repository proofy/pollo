package org.outerj.pollo.config;

import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.outerj.pollo.xmleditor.exception.PolloConfigurationException;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import javax.swing.*;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class PolloConfiguration
{
    protected ArrayList viewTypes = new ArrayList();
    protected ArrayList templates = new ArrayList();
    /**
     * List of example/template xpath queries the user can choose
     * from
     */
    protected ArrayList xpathQueries = new ArrayList();
    /**
     * List of recenlty opened files. The entries are String containing the
     * absoute file names. Most recent entries are last in the list (at the
     * highest index, that is)
     */
    protected ArrayList recentlyOpenedFiles = new ArrayList(10);
    protected ArrayList recentlyUsedSchemas = new ArrayList(10);
    protected ArrayList recentlyUsedXPaths = new ArrayList(10);
    protected RecentlyUsedModel usedSchemasModel = new RecentlyUsedModel(recentlyUsedSchemas);
    protected RecentlyUsedModel usedXPathsModel = new RecentlyUsedModel(recentlyUsedXPaths);

    protected String fileOpenDialogPath = null;
    protected String schemaOpenDialogPath = null;

    protected int splitPane1Pos;
    protected int splitPane2Pos;
    protected int splitPane3Pos;
    protected int windowWidth;
    protected int windowHeight;

    protected int elementNameFontSize;
    protected int elementNameFontStyle;
    protected int attributeNameFontSize;
    protected int attributeNameFontStyle;
    protected int attributeValueFontSize;
    protected int attributeValueFontStyle;
    protected int textFontSize;
    protected boolean textAntialiasing;

    protected int undoLevels;

    public static final String USER_CONF_FILE_NAME = ".pollorc";

    // element tag names
    private static final String EL_POLLO = "pollo";
    private static final String EL_FILE_OPEN_DIALOG_PATH = "file-open-dialog-path";
    private static final String EL_SCHEMA_OPEN_DIALOG_PATH = "schema-open-dialog-path";
    private static final String EL_RECENT_FILES = "recent-files";
    private static final String EL_RECENT_FILE = "recent-file";
    private static final String EL_RECENT_XPATHS = "recent-xpaths";
    private static final String EL_RECENT_XPATH = "recent-xpath";
    private static final String EL_RECENT_SCHEMAS = "recent-schemas";
    private static final String EL_RECENT_SCHEMA = "recent-schema";
    private static final String EL_SPLIT1_POS = "splitpane1-pos";
    private static final String EL_SPLIT2_POS = "splitpane2-pos";
    private static final String EL_SPLIT3_POS = "splitpane3-pos";
    private static final String EL_WINDOW_WIDTH = "window-width";
    private static final String EL_WINDOW_HEIGHT = "window-height";
    private static final String ELEMENT_NAME_FONT = "element-name-font";
    private static final String ATTRIBUTE_NAME_FONT = "attribute-name-font";
    private static final String ATTRIBUTE_VALUE_FONT = "attribute-value-font";
    private static final String TEXT_ANTIALIASING = "text-antialiasing";
    private static final String TEXT_FONT = "text-font";
    private static final String UNDO_LEVELS = "undo-levels";


    public PolloConfiguration()
    {
    }

    public void addViewType(ViewTypeConf viewType)
    {
        viewTypes.add(viewType);
    }

    public Collection getViewTypes()
    {
        return viewTypes;
    }

    public void addTemplate(TemplateConfItem template)
    {
        templates.add(template);
    }

    public void addXPathQuery(XPathQuery query)
    {
        xpathQueries.add(query);
    }

    public Collection getTemplates()
    {
        return templates;
    }

    public Collection getXPathQueries()
    {
        return xpathQueries;
    }

    public String getFileOpenDialogPath()
    {
        return fileOpenDialogPath;
    }

    public void setFileOpenDialogPath(String path)
    {
        fileOpenDialogPath = path;
    }

    public String getSchemaOpenDialogPath()
    {
        return schemaOpenDialogPath;
    }

    public void setSchemaOpenDialogPath(String path)
    {
        schemaOpenDialogPath = path;
    }

    public void addRecenltyOpenedFile(String fullpath)
    {
        addRecently(fullpath, recentlyOpenedFiles, null);
    }

    public void addRecentlyUsedSchema(String path)
    {
        addRecently(path, recentlyUsedSchemas, usedSchemasModel);
    }

    public void addRecentlyUsedXPath(String xpath)
    {
        addRecently(xpath, recentlyUsedXPaths, usedXPathsModel);
    }

    protected void addRecently(Object object, List list, RecentlyUsedModel usedModel)
    {
        if (list.contains(object))
        {
            // if the object was already in the list, move it to the front
            // (the front is the last element in the list)
            list.remove(list.indexOf(object));
            list.add(object);
        }
        else
        {
            if (list.size() >= 10)
                list.remove(0);

            list.add(object);
        }

        if (usedModel != null)
            usedModel.fireChanged();
    }

    /**
     * This method is used while reading the configuration file, for normal
     * use, see the method addRecentlyOpenedFile(String)
     */
    public void putRecentlyOpenedFile(String fullpath)
    {
        recentlyOpenedFiles.add(fullpath);
    }

    public void putRecentlyUsedSchema(String path)
    {
        recentlyUsedSchemas.add(path);
    }

    public void putRecentlyUsedXPath(String xpath)
    {
        recentlyUsedXPaths.add(xpath);
    }

    public List getRecentlyOpenedFiles()
    {
        return recentlyOpenedFiles;
    }

    public List getRecentlyUsedSchemas()
    {
        return recentlyUsedSchemas;
    }

    public List getRecentlyUsedXPahts()
    {
        return recentlyUsedXPaths;
    }

    public RecentlyUsedModel getRecentlyUsedXPathsModel()
    {
        return usedXPathsModel;
    }

    public RecentlyUsedModel getRecentlyUsedSchemasModel()
    {
        return usedSchemasModel;
    }

    public int getSplitPane1Pos()
    {
        return splitPane1Pos;
    }

    public void setSplitPane1Pos(int pos)
    {
        splitPane1Pos = pos;
    }

    public void setSplitPane1Pos(String pos)
    {
        splitPane1Pos = Integer.parseInt(pos);
    }

    public int getSplitPane2Pos()
    {
        return splitPane2Pos;
    }

    public void setSplitPane2Pos(int pos)
    {
        splitPane2Pos = pos;
    }

    public void setSplitPane2Pos(String pos)
    {
        splitPane2Pos = Integer.parseInt(pos);
    }

    public int getSplitPane3Pos()
    {
        return splitPane3Pos;
    }

    public void setSplitPane3Pos(String pos)
    {
        splitPane3Pos = Integer.parseInt(pos);
    }

    public void setSplitPane3Pos(int splitPane3Pos)
    {
        this.splitPane3Pos = splitPane3Pos;
    }

    public int getWindowHeight()
    {
        return windowHeight;
    }

    public int getWindowWidth()
    {
        return windowWidth;
    }

    public void setWindowHeight(int height)
    {
        this.windowHeight = height;
    }

    public void setWindowWidth(int width)
    {
        this.windowWidth = width;
    }

    public void setWindowHeight(String height)
    {
        this.windowHeight = Integer.parseInt(height);
    }

    public void setWindowWidth(String width)
    {
        this.windowWidth = Integer.parseInt(width);
    }

    public int getElementNameFontStyle()
    {
        return elementNameFontStyle;
    }

    public void setElementNameFontStyle(int elementNameFontStyle)
    {
        this.elementNameFontStyle = elementNameFontStyle;
    }

    public int getElementNameFontSize()
    {
        return elementNameFontSize;
    }

    public void setElementNameFontSize(int elementNameFontSize)
    {
        this.elementNameFontSize = elementNameFontSize;
    }

    public int getAttributeNameFontSize()
    {
        return attributeNameFontSize;
    }

    public void setAttributeNameFontSize(int attributeNameFontSize)
    {
        this.attributeNameFontSize = attributeNameFontSize;
    }

    public int getAttributeNameFontStyle()
    {
        return attributeNameFontStyle;
    }

    public void setAttributeNameFontStyle(int attributeNameFontStyle)
    {
        this.attributeNameFontStyle = attributeNameFontStyle;
    }

    public int getAttributeValueFontSize()
    {
        return attributeValueFontSize;
    }

    public void setAttributeValueFontSize(int attributeValueFontSize)
    {
        this.attributeValueFontSize = attributeValueFontSize;
    }

    public int getAttributeValueFontStyle()
    {
        return attributeValueFontStyle;
    }

    public void setAttributeValueFontStyle(int attributeValueFontStyle)
    {
        this.attributeValueFontStyle = attributeValueFontStyle;
    }

    public int getTextFontSize()
    {
        return textFontSize;
    }

    public void setTextFontSize(int textFontSize)
    {
        this.textFontSize = textFontSize;
    }

    public boolean isTextAntialiasing()
    {
        return textAntialiasing;
    }

    public void setTextAntialiasing(boolean textAntialiasing)
    {
        this.textAntialiasing = textAntialiasing;
    }

    public int getUndoLevels()
    {
        return undoLevels;
    }

    public void setUndoLevels(int undoLevels)
    {
        this.undoLevels = undoLevels;
    }

    public void store()
            throws PolloConfigurationException
    {
        File file = new File(System.getProperty("user.home"), USER_CONF_FILE_NAME);
        FileOutputStream fos = null;

        try
        {
            fos = new FileOutputStream(file);
            OutputFormat outputFormat = new OutputFormat();
            outputFormat.setIndenting(true);
            outputFormat.setIndent(2);

            XMLSerializer serializer = new XMLSerializer(fos, outputFormat);

            serializer.startDocument();
            serializer.startElement(null, EL_POLLO, EL_POLLO, new AttributesImpl());

            store(serializer);

            serializer.endElement(null, EL_POLLO, EL_POLLO);
            serializer.endDocument();
        }
        catch (Exception e)
        {
            String message = "Could not store user configuration.";
            throw new PolloConfigurationException(message, e);
        }
        finally
        {
            if (fos != null) try
            {
                fos.close();
            }
            catch (Exception e)
            {
            }
            ;
        }
    }

    public void store(ContentHandler handler)
            throws SAXException
    {
        if (fileOpenDialogPath != null)
        {
            handler.startElement("", EL_FILE_OPEN_DIALOG_PATH, EL_FILE_OPEN_DIALOG_PATH, new AttributesImpl());
            handler.characters(fileOpenDialogPath.toCharArray(), 0, fileOpenDialogPath.length());
            handler.endElement("", EL_FILE_OPEN_DIALOG_PATH, EL_FILE_OPEN_DIALOG_PATH);
        }

        if (schemaOpenDialogPath != null)
        {
            handler.startElement("", EL_SCHEMA_OPEN_DIALOG_PATH, EL_SCHEMA_OPEN_DIALOG_PATH, new AttributesImpl());
            handler.characters(fileOpenDialogPath.toCharArray(), 0, fileOpenDialogPath.length());
            handler.endElement("", EL_SCHEMA_OPEN_DIALOG_PATH, EL_SCHEMA_OPEN_DIALOG_PATH);
        }

        handler.startElement("", EL_SPLIT1_POS, EL_SPLIT1_POS, new AttributesImpl());
        String splitPane1PosString = String.valueOf(splitPane1Pos);
        handler.characters(splitPane1PosString.toCharArray(), 0, splitPane1PosString.length());
        handler.endElement("", EL_SPLIT1_POS, EL_SPLIT1_POS);

        handler.startElement("", EL_SPLIT2_POS, EL_SPLIT2_POS, new AttributesImpl());
        String splitPane2PosString = String.valueOf(splitPane2Pos);
        handler.characters(splitPane2PosString.toCharArray(), 0, splitPane2PosString.length());
        handler.endElement("", EL_SPLIT2_POS, EL_SPLIT2_POS);

        handler.startElement("", EL_SPLIT3_POS, EL_SPLIT3_POS, new AttributesImpl());
        String splitPane3PosString = String.valueOf(splitPane3Pos);
        handler.characters(splitPane3PosString.toCharArray(), 0, splitPane3PosString.length());
        handler.endElement("", EL_SPLIT3_POS, EL_SPLIT3_POS);

        handler.startElement("", EL_WINDOW_HEIGHT, EL_WINDOW_HEIGHT, new AttributesImpl());
        String windowHeightString = String.valueOf(windowHeight);
        handler.characters(windowHeightString.toCharArray(), 0, windowHeightString.length());
        handler.endElement("", EL_WINDOW_HEIGHT, EL_WINDOW_HEIGHT);

        handler.startElement("", EL_WINDOW_WIDTH, EL_WINDOW_WIDTH, new AttributesImpl());
        String windowWidthString = String.valueOf(windowWidth);
        handler.characters(windowWidthString.toCharArray(), 0, windowWidthString.length());
        handler.endElement("", EL_WINDOW_WIDTH, EL_WINDOW_WIDTH);

        // store recent files
        storeList(EL_RECENT_FILES, EL_RECENT_FILE, recentlyOpenedFiles, handler);
        storeList(EL_RECENT_SCHEMAS, EL_RECENT_SCHEMA, recentlyUsedSchemas, handler);
        storeList(EL_RECENT_XPATHS, EL_RECENT_XPATH, recentlyUsedXPaths, handler);

        AttributesImpl elementNameFontAttrs = new AttributesImpl();
        elementNameFontAttrs.addAttribute("", "size", "size", "CDATA", String.valueOf(getElementNameFontSize()));
        elementNameFontAttrs.addAttribute("", "style", "style", "CDATA", String.valueOf(getElementNameFontStyle()));
        handler.startElement("", ELEMENT_NAME_FONT, ELEMENT_NAME_FONT, elementNameFontAttrs);
        handler.endElement("", ELEMENT_NAME_FONT, ELEMENT_NAME_FONT);

        AttributesImpl attributeNameFontAttrs = new AttributesImpl();
        attributeNameFontAttrs.addAttribute("", "size", "size", "CDATA", String.valueOf(getAttributeNameFontSize()));
        attributeNameFontAttrs.addAttribute("", "style", "style", "CDATA", String.valueOf(getAttributeNameFontStyle()));
        handler.startElement("", ATTRIBUTE_NAME_FONT, ATTRIBUTE_NAME_FONT, attributeNameFontAttrs);
        handler.endElement("", ATTRIBUTE_NAME_FONT, ATTRIBUTE_NAME_FONT);

        AttributesImpl attributeValueFontAttrs = new AttributesImpl();
        attributeValueFontAttrs.addAttribute("", "size", "size", "CDATA", String.valueOf(getAttributeValueFontSize()));
        attributeValueFontAttrs.addAttribute("", "style", "style", "CDATA", String.valueOf(getAttributeValueFontStyle()));
        handler.startElement("", ATTRIBUTE_VALUE_FONT, ATTRIBUTE_VALUE_FONT, attributeValueFontAttrs);
        handler.endElement("", ATTRIBUTE_VALUE_FONT, ATTRIBUTE_VALUE_FONT);

        AttributesImpl textFontAttrs = new AttributesImpl();
        textFontAttrs.addAttribute("", "size", "size", "CDATA", String.valueOf(getTextFontSize()));
        handler.startElement("", TEXT_FONT, TEXT_FONT, textFontAttrs);
        handler.endElement("", TEXT_FONT, TEXT_FONT);

        handler.startElement("", TEXT_ANTIALIASING, TEXT_ANTIALIASING, new AttributesImpl());
        String textAntialiasingString = String.valueOf(textAntialiasing);
        handler.characters(textAntialiasingString.toCharArray(), 0, textAntialiasingString.length());
        handler.endElement("",TEXT_ANTIALIASING, TEXT_ANTIALIASING);

        handler.startElement("", UNDO_LEVELS, UNDO_LEVELS, new AttributesImpl());
        String undoLevelsString = String.valueOf(undoLevels);
        handler.characters(undoLevelsString.toCharArray(), 0, undoLevelsString.length());
        handler.endElement("",UNDO_LEVELS, UNDO_LEVELS);
    }

    protected void storeList(final String listelement, final String itemelement, final List list, ContentHandler handler)
            throws SAXException
    {
        handler.startElement("", listelement, listelement, new AttributesImpl());
        Iterator iterator = list.iterator();
        while (iterator.hasNext())
        {
            String item = (String) iterator.next();
            handler.startElement("", itemelement, itemelement, new AttributesImpl());
            handler.characters(item.toCharArray(), 0, item.length());
            handler.endElement("", itemelement, itemelement);
        }
        handler.endElement("", EL_RECENT_FILES, EL_RECENT_FILES);
    }

    public class RecentlyUsedModel extends AbstractListModel implements ComboBoxModel
    {
        ArrayList list;
        Object selectedObject;

        public RecentlyUsedModel(ArrayList list)
        {
            this.list = list;
        }

        public void setSelectedItem(Object object)
        {
            if ((this.selectedObject != null && !selectedObject.equals(object)) ||
                    selectedObject == null && object != null)
            {
                selectedObject = object;
                fireContentsChanged(this, -1, -1);
            }
        }

        public int getSize()
        {
            return list.size();
        }

        public Object getSelectedItem()
        {
            return selectedObject;
        }

        public Object getElementAt(int index)
        {
            return list.get(getSize() - index - 1);
        }

        public void fireChanged()
        {
            fireContentsChanged(this, 0, 10);
        }

    }
}
