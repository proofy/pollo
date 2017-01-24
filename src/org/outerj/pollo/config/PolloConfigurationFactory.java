package org.outerj.pollo.config;

import org.apache.avalon.framework.configuration.*;
import org.outerj.pollo.xmleditor.exception.PolloConfigurationException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.mxp1.MXParser;

import java.io.File;
import java.io.InputStream;
import java.awt.*;

/**
 * Reads the pollo configuration file and builds the configuration object
 * model.
 *
 * @author Bruno Dumon.
 */
public class PolloConfigurationFactory
{
    public static org.apache.log4j.Category logcat = org.apache.log4j.Category.getInstance(
            org.outerj.pollo.xmleditor.AppenderDefinitions.CONFIG);

    public static PolloConfiguration loadConfiguration()
        throws PolloConfigurationException
    {
        final PolloConfiguration polloConfiguration = new PolloConfiguration();
        Parser parser = new Parser(polloConfiguration);

        InputStream is = null;
        try
        {
            is = PolloConfigurationFactory.class.getClassLoader()
                    .getResourceAsStream("pollo_conf.xml");
            parser.parse(is);
        }
        catch (Exception e)
        {
            throw new PolloConfigurationException("Exception parsing the pollo_conf.xml configuration file.", e);
        }
        finally
        {
            if (is != null)
                try { is.close(); } catch (Exception e) { e.printStackTrace(); }
        }

        //
        // load user configuration
        //

        File file = new File(System.getProperty("user.home"), PolloConfiguration.USER_CONF_FILE_NAME);
        Configuration config = null;
        try
        {
            if (file.exists())
                config = new DefaultConfigurationBuilder().buildFromFile(file);
        }
        catch (Exception e)
        {
            logcat.error("Exception parsing the user configuration file " + file.getAbsolutePath() + ", but will start up anyway", e);
        }

        if (config == null)
            config = new DefaultConfiguration("dummy", "-");

        String fileOpenDialogPath = config.getChild("file-open-dialog-path").getValue(null);
        if (fileOpenDialogPath != null)
            polloConfiguration.setFileOpenDialogPath(fileOpenDialogPath);

        String schemaOpenDialogPath = config.getChild("schema-open-dialog-path").getValue(null);
        if (schemaOpenDialogPath != null)
            polloConfiguration.setSchemaOpenDialogPath(schemaOpenDialogPath);

        polloConfiguration.setSplitPane1Pos(config.getChild("splitpane1-pos").getValueAsInteger(620));
        polloConfiguration.setSplitPane2Pos(config.getChild("splitpane2-pos").getValueAsInteger(370));
        polloConfiguration.setSplitPane3Pos(config.getChild("splitpane3-pos").getValueAsInteger(polloConfiguration.getSplitPane1Pos()));
        polloConfiguration.setWindowHeight(config.getChild("window-height").getValueAsInteger(600));
        polloConfiguration.setWindowWidth(config.getChild("window-width").getValueAsInteger(800));

        Configuration[] recentFileConfs = config.getChild("recent-files").getChildren("recent-file");
        for (int i = 0; i < recentFileConfs.length; i++)
            polloConfiguration.putRecentlyOpenedFile(recentFileConfs[i].getValue(""));

        Configuration[] recentXPathConfs = config.getChild("recent-xpaths").getChildren("recent-xpath");
        for (int i = 0; i < recentXPathConfs.length; i++)
            polloConfiguration.putRecentlyUsedXPath(recentXPathConfs[i].getValue(""));

        Configuration[] recentSchemaConfs = config.getChild("recent-schemas").getChildren("recent-schema");
        for (int i = 0; i < recentSchemaConfs.length; i++)
            polloConfiguration.putRecentlyUsedSchema(recentSchemaConfs[i].getValue(""));

        Configuration elementNameFontConf = config.getChild("element-name-font");
        polloConfiguration.setElementNameFontSize(elementNameFontConf.getAttributeAsInteger("size", 13));
        polloConfiguration.setElementNameFontStyle(elementNameFontConf.getAttributeAsInteger("style", Font.BOLD));

        Configuration attributeNameFontConf = config.getChild("attribute-name-font");
        polloConfiguration.setAttributeNameFontSize(attributeNameFontConf.getAttributeAsInteger("size", 12));
        polloConfiguration.setAttributeNameFontStyle(attributeNameFontConf.getAttributeAsInteger("style", Font.BOLD));

        Configuration attributeValueFontConf = config.getChild("attribute-value-font");
        polloConfiguration.setAttributeValueFontSize(attributeValueFontConf.getAttributeAsInteger("size", 12));
        polloConfiguration.setAttributeValueFontStyle(attributeValueFontConf.getAttributeAsInteger("style", 0));

        Configuration textFontConf = config.getChild("text-font");
        polloConfiguration.setTextFontSize(textFontConf.getAttributeAsInteger("size", 12));

        polloConfiguration.setTextAntialiasing(config.getChild("text-antialiasing").getValueAsBoolean(false));
        polloConfiguration.setUndoLevels(config.getChild("undo-levels").getValueAsInteger(50));

        return polloConfiguration;
    }

    public static class Parser
    {
        private XmlPullParser parser;
        private final PolloConfiguration polloConf;

        public Parser(PolloConfiguration polloConf)
        {
            this.polloConf = polloConf;
        }

        public void parse(InputStream is) throws Exception
        {
            parser = new MXParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true);
            parser.setInput(is, null);
            int eventType = parser.getEventType();

            while (eventType != XmlPullParser.END_DOCUMENT)
            {
                eventType = parser.next();
                if (eventType == XmlPullParser.START_TAG)
                {
                    if (!parser.getName().equals("pollo"))
                        throw new Exception("Root element should be called \"pollo\"");

                    // run over the children of the root element
                    eventType = parser.next();
                    while (eventType != XmlPullParser.END_TAG)
                    {
                        if (eventType == XmlPullParser.START_TAG)
                        {
                            if (parser.getName().equals("viewtypes"))
                                readViewTypes();
                            else if (parser.getName().equals("templates"))
                            {
                                readConfItems("template", new ConfItemParserAssistant()
                                {
                                    public ConfItem newConfItem()
                                    {
                                        return new TemplateConfItem();
                                    }

                                    public void addConfItem(ConfItem confItem)
                                    {
                                        polloConf.addTemplate((TemplateConfItem)confItem);
                                    }

                                    public void readExtraAttributes(ConfItem confItem, XmlPullParser parser) throws Exception
                                    {
                                        String description = getAttribute("description");
                                        ((TemplateConfItem)confItem).setDescription(description);
                                    }
                                });
                            }
                            else if (parser.getName().equals("xpath-queries"))
                                readXPathQueries();
                            else
                                throw new Exception("Unexpected element \"" + parser.getName() + "\" on line " + parser.getLineNumber());
                        }
                        eventType = parser.next();
                    }

                    // we're not interested in the rest of the file
                    break;
                }
            }
        }

        private void readViewTypes() throws Exception
        {
            int eventType = parser.next();
            while (eventType != XmlPullParser.END_TAG)
            {
                if (eventType == XmlPullParser.START_TAG)
                {
                    if (!parser.getName().equals("viewtype"))
                        throw new Exception("Unexpected element \"" + parser.getName() + "\" on line " + parser.getLineNumber());

                    final ViewTypeConf viewTypeConf = new ViewTypeConf();
                    viewTypeConf.setName(getAttribute("name"));
                    viewTypeConf.setDescription(getAttribute("description"));

                    eventType = parser.next();
                    while (eventType != XmlPullParser.END_TAG)
                    {
                        if (eventType == XmlPullParser.START_TAG)
                        {
                            if (parser.getName().equals("schemas"))
                            {
                                readConfItems("schema", new ConfItemParserAssistant()
                                {
                                    public ConfItem newConfItem()
                                    {
                                        return new SchemaConfItem();
                                    }

                                    public void addConfItem(ConfItem confItem)
                                    {
                                        viewTypeConf.addSchema((SchemaConfItem)confItem);
                                    }

                                    public void readExtraAttributes(ConfItem confItem, XmlPullParser parser) {}
                                });
                            }
                            else if (parser.getName().equals("display-specifications"))
                            {
                                readConfItems("display-specification", new ConfItemParserAssistant()
                                {
                                    public ConfItem newConfItem()
                                    {
                                        return new DisplaySpecConfItem();
                                    }

                                    public void addConfItem(ConfItem confItem)
                                    {
                                        viewTypeConf.addDisplaySpec((DisplaySpecConfItem)confItem);
                                    }

                                    public void readExtraAttributes(ConfItem confItem, XmlPullParser parser) {}
                                });
                            }
                            else if (parser.getName().equals("attribute-editor-plugins"))
                            {
                                readConfItems("attribute-editor-plugin", new ConfItemParserAssistant()
                                {
                                    public ConfItem newConfItem()
                                    {
                                        return new AttrEditorPluginConfItem();
                                    }

                                    public void addConfItem(ConfItem confItem)
                                    {
                                        viewTypeConf.addAttrEditorPlugin((AttrEditorPluginConfItem)confItem);
                                    }

                                    public void readExtraAttributes(ConfItem confItem, XmlPullParser parser) {}
                                });
                            }
                            else if (parser.getName().equals("action-plugins"))
                            {
                                readConfItems("action-plugin", new ConfItemParserAssistant()
                                {
                                    public ConfItem newConfItem()
                                    {
                                        return new ActionPluginConfItem();
                                    }

                                    public void addConfItem(ConfItem confItem)
                                    {
                                        viewTypeConf.addActionPlugin((ActionPluginConfItem)confItem);
                                    }

                                    public void readExtraAttributes(ConfItem confItem, XmlPullParser parser) {}
                                });
                            }
                            else
                                throw new Exception("Unexpected element \"" + parser.getName() + "\" on line " + parser.getLineNumber());
                        }
                        eventType = parser.next();
                    }

                    polloConf.addViewType(viewTypeConf);
                }
                eventType = parser.next();
            }
        }

        private void readConfItems(String elementName, ConfItemParserAssistant assistant) throws Exception
        {
            int eventType = parser.next();
            while (eventType != XmlPullParser.END_TAG)
            {
                if (eventType == XmlPullParser.START_TAG)
                {
                    if (parser.getName().equals(elementName))
                    {
                        ConfItem confItem = assistant.newConfItem();
                        String factory = getAttribute("factory");
                        confItem.setFactoryClass(factory);
                        assistant.readExtraAttributes(confItem, parser);

                        // read init parameters
                        eventType = parser.next();
                        while (eventType != XmlPullParser.END_TAG)
                        {
                            if (eventType == XmlPullParser.START_TAG)
                            {
                                if (!parser.getName().equals("parameter"))
                                    throw new Exception("Unexpected element \"" + parser.getName() + "\" on line " + parser.getLineNumber());

                                String name = getAttribute("name");
                                String value = getAttribute("value");

                                confItem.addInitParam(name, value);
                                goToEndElement();
                            }
                            eventType = parser.next();
                        }
                        assistant.addConfItem(confItem);
                    }
                    else
                        throw new Exception("Unexpected element \"" + parser.getName() + "\" on line " + parser.getLineNumber());
                }
                eventType = parser.next();
            }
        }

        public void readXPathQueries() throws Exception
        {
            int eventType = parser.next();
            while (eventType != XmlPullParser.END_TAG)
            {
                if (eventType == XmlPullParser.START_TAG)
                {
                    if (!parser.getName().equals("query"))
                        throw new Exception("Unexpected element \"" + parser.getName() + "\" on line " + parser.getLineNumber());
                    String description = getAttribute("description");
                    String expression = getAttribute("expression");

                    XPathQuery xpathQuery = new XPathQuery();
                    xpathQuery.setDescription(description);
                    xpathQuery.setExpression(expression);
                    polloConf.addXPathQuery(xpathQuery);
                    goToEndElement();
                }
                eventType = parser.next();
            }
        }

        private void goToEndElement() throws Exception
        {
            int eventType = parser.next();
            while (eventType != XmlPullParser.END_TAG)
            {
                if (eventType == XmlPullParser.START_TAG)
                    goToEndElement();
                eventType = parser.next();
            }
        }

        private String getAttribute(String name) throws Exception
        {
            String value = parser.getAttributeValue(XmlPullParser.NO_NAMESPACE, name);
            if (value == null)
                throw new Exception("Missing attribute " + name + " on element " + parser.getName() + " on line " + parser.getLineNumber());
            return value;
        }
    }

    public interface ConfItemParserAssistant
    {
        public ConfItem newConfItem();

        public void addConfItem(ConfItem confItem);

        public void readExtraAttributes(ConfItem confItem, XmlPullParser parser) throws Exception;
    }

}
