package org.outerj.pollo.xmleditor.schema;

import com.sun.msv.grammar.*;
import com.sun.msv.grammar.util.ExpressionWalker;
import com.sun.msv.reader.dtd.DTDReader;
import com.sun.msv.reader.util.GrammarLoader;
import com.sun.msv.reader.util.IgnoreController;
import com.sun.msv.verifier.DocumentDeclaration;
import com.sun.msv.verifier.ErrorInfo;
import com.sun.msv.verifier.ValidityViolation;
import com.sun.msv.verifier.regexp.ExpressionAcceptor;
import com.sun.msv.verifier.regexp.REDocumentDeclaration;
import com.sun.msv.datatype.xsd.EnumerationFacet;
import com.sun.msv.datatype.xsd.StringType;
import org.jaxen.SimpleNamespaceContext;
import org.outerj.pollo.util.URLFactory;
import org.outerj.pollo.xmleditor.exception.PolloException;
import org.outerj.pollo.xmleditor.schema.msv.PolloMsvVerifier;
import org.outerj.pollo.xmleditor.schema.msv.SAXEventGenerator;
import org.outerj.pollo.xmleditor.schema.msv.StopValidationException;
import org.outerj.pollo.xmleditor.util.NodeMap;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.*;

import javax.xml.parsers.SAXParserFactory;
import java.util.*;

/**
 * An ISchema implementation using MSV. The schema is read and then flattend
 * out to the same level as the BasicSchema implementation, meaning information
 * about content model, required attributes, etc. is ignored. The MSV grammar
 * is kept however for doing validation (FIXME note to myself: these need to be
 * cached and shared between open files).
 *
 * @author Bruno Dumon, inspired by work by Al Byers.
 */
public class MsvSchema implements ISchema
{
    protected SimpleNamespaceContext namespaceContext = new SimpleNamespaceContext();
    protected NodeMap elementSchemas;
    protected String source;
    protected Grammar grammar;
    protected PolloMsvVerifier verifier;
    protected MsvSchemaErrorHandler errorHandler;


    /**
     * Builds elementSchemas NodeMap (as with Basic Schema).
     */
    protected void init(HashMap initParams)
            throws Exception
    {
        try
        {
            source = (String) initParams.get("source");

            if (source == null || source.trim().equals(""))
            {
                throw new PolloException("[MsvSchema] The source init-param is not specified!");
            }

            // load grammar
            SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
            saxParserFactory.setNamespaceAware(true);

            boolean trackOptionalAttributes = false;
            InputSource inputSource = new InputSource(URLFactory.createUrl(source).toExternalForm());
            String type = (String)initParams.get("type");
            if (type != null && type.equals("dtd"))
            {
                grammar =  DTDReader.parse(inputSource, new PolloMsvController());
                trackOptionalAttributes = true;
            }
            else
            {
                // let MSV autodetect
                grammar = GrammarLoader.loadSchema(inputSource, new PolloMsvController(), saxParserFactory);
            }

            if (grammar == null)
                throw new PolloException("[MsvSchema] Could not load the schema with MSV (parse or loadSchema returned null)");

            elementSchemas = new NodeMap();

            MsvGrammarWalker walker = new MsvGrammarWalker(trackOptionalAttributes);
            grammar.getTopLevel().visit(walker);

        }
        catch (Exception e)
        {
            throw new PolloException("[MsvSchema] Error initialising MSV schema", e);
        }
    }

    /**
     * The goals of MsvGrammarWalker is to convert an MSV Grammar object
     * to Pollo's own simple schema-structure.
     */
    public class MsvGrammarWalker extends ExpressionWalker
    {
        final Set visitedNodes = new HashSet();
        final Set visitedSubNodes = new HashSet();
        ElementSchema elementSchema; // the current ElementSchema being created
        final SubNodeWalker subNodeWalker = new SubNodeWalker();
        final boolean trackOptionalAttributes;

        /**
         * @param trackOptionalAttributes in general, should only be used for DTD's
         * because for other schema languages the content model for attributes can be more complex,
         * or the same element name may be reused with different required attributes on it.
         */
        public MsvGrammarWalker(boolean trackOptionalAttributes)
        {
            this.trackOptionalAttributes = trackOptionalAttributes;
        }

        public void onElement(ElementExp exp)
        {
            // if we havn't visited this expression before (can be cyclic!)
            if (!visitedNodes.contains(exp))
            {
                visitedNodes.add(exp);
                String[] names = getNameParts(exp.getNameClass());
                if (names != null)
                {
                    elementSchema = (ElementSchema) elementSchemas.get(names[0], names[1]);
                    if (elementSchema == null)
                    {
                        elementSchema = new ElementSchema();
                        elementSchema.namespaceURI = names[0];
                        elementSchema.localName = names[1];
                        elementSchemas.put(names[0], names[1], elementSchema);
                    }

                    // find out what the subelements of this element are:
                    visitedSubNodes.clear();
                    exp.contentModel.visit(subNodeWalker);
                }
                else
                {
                    // other name classes not yet supported
                }

                // now recursively visit the childexpressions
                super.onElement(exp);
            }
        }

        /**
         * This walker records all elements that can appear as subelement, and
         * all attributes that the element can have.
         */
        public class SubNodeWalker extends ExpressionWalker
        {
            protected AttributeSchema attrSchema;
            protected final HashSet attrValues = new HashSet();
            protected final AttributeWalker attrWalker = new AttributeWalker();
            protected boolean nextAttributeOptional = false;

            public void onElement(ElementExp subExp)
            {
                if (!visitedSubNodes.contains(subExp))
                {
                    visitedSubNodes.add(subExp);
                    String[] names = getNameParts(subExp.getNameClass());
                    if (names != null)
                    {
                        // if a subelement with this name does not yet exist
                        if (!elementSchema.containsSubElement(names[0], names[1]))
                        {
                            ElementSchema.SubElement subElement = elementSchema.createSubElement(names[0], names[1]);
                            elementSchema.subelements.put(names[0], names[1], subElement);
                        }
                    }
                    else
                    {
                        // other name classes not yet supported
                    }
                    // sub expressions are not further visited
                }
            }

            public void onAttribute(AttributeExp attrExp)
            {
                String[] names = getNameParts(attrExp.getNameClass());
                if (names != null)
                {
                    // if an attribute with this name does not yet exist
                    if (elementSchema.getAttributeSchema(names[0], names[1]) == null)
                    {
                        attrSchema = new AttributeSchema(names[0], names[1], null, null);

                        if (trackOptionalAttributes)
                        {
                            attrSchema.required = !nextAttributeOptional;
                            nextAttributeOptional = false;
                        }

                        // search possible values for this attribute by using the AttributeWalker
                        attrValues.clear();
                        attrExp.exp.visit(attrWalker);
                        if (attrValues.size() > 0)
                            attrSchema.values = (String [])attrValues.toArray(new String [] {});
                        elementSchema.attributes.add(attrSchema);
                    }
                }
            }

            public void onChoice(ChoiceExp choiceExp)
            {
                if (trackOptionalAttributes)
                {
                    if (choiceExp.exp1 == Expression.epsilon)
                    {
                        if (choiceExp.exp2 instanceof AttributeExp)
                        {
                            nextAttributeOptional = true;
                            choiceExp.exp2.visit(this);
                            return;
                        }
                    }
                    else if (choiceExp.exp2 == Expression.epsilon)
                    {
                        if (choiceExp.exp1 instanceof AttributeExp)
                        {
                            nextAttributeOptional = true;
                            choiceExp.exp1.visit(this);
                            return;
                        }
                    }
                }
                super.onChoice(choiceExp);
            }

            public void onValue(ValueExp exp)
            {
                elementSchema.subtexts.add(exp.value);
            }

            public void onData(DataExp exp)
            {
                if (exp.getType() instanceof EnumerationFacet)
                {
                    Iterator it = ((EnumerationFacet)exp.getType()).values.iterator();
                    while (it.hasNext())
                    {
                        String value = it.next().toString();
                        elementSchema.subtexts.add(value);
                    }
                }
                else if (exp.getType() instanceof StringType)
                {
                    elementSchema.subtexts.add("");
                }
            }

            public void onAnyString()
            {
                elementSchema.subtexts.add("");
            }

            /**
             * AttributeWalker finds values an attribute may have. Note that this does
             * not work for e.g. DTD's because there the possible attribute values are
             * part of the datatype. Works fine for RELAX-NG though.
             */
            public class AttributeWalker extends ExpressionWalker
            {
                public void onElement(ElementExp exp)
                {
                    // avoid recursing through elements (should not occur inside an attribute anyhow).
                }

                public void onValue(ValueExp exp)
                {
                    attrValues.add(exp.value.toString());
                }

                public void onData(DataExp exp)
                {
                    if (exp.getType() instanceof EnumerationFacet)
                    {
                        Iterator it = ((EnumerationFacet)exp.getType()).values.iterator();
                        while (it.hasNext())
                        {
                            attrValues.add(it.next().toString());
                        }
                    }
                }
            }
        }
    }


    private final String[] getNameParts(NameClass nameClass)
    {
        if (nameClass instanceof SimpleNameClass)
        {
            SimpleNameClass simple = (SimpleNameClass) nameClass;

            // pollo expects that no namespace == null string
            String namespaceURI = simple.namespaceURI;
            if (namespaceURI.equals(""))
                namespaceURI = null;

            return new String[]{namespaceURI, simple.localName};
        }
        else
            return null;
    }

    public class PolloMsvController extends IgnoreController
    {
        public void warning(Locator[] locs, String errorMessage)
        {
            System.out.println("MSV warning: " + errorMessage);
        }

        public void error(Locator[] locs, String errorMessage, Exception nestedException)
        {
            System.out.println("MSV error: " + errorMessage);
        }
    }


    //
    // The rest of the methods is copied from BasicSchema.
    //

    /**
     * Returns the list of attributes an element can have.
     */
    public Collection getAttributesFor(Element element)
    {
        ElementSchema elementSchema = getElementSchema(element.getNamespaceURI(), element.getLocalName());

        if (elementSchema == null)
            return new LinkedList();
        else
            return elementSchema.attributes;
    }

    /**
     * Returns true if the element <i>child</i> is allowed as child
     * of the element <i>parent</i>.
     */
    public boolean isChildAllowed(Element parent, Element child)
    {
        ElementSchema elementSchema = getElementSchema(parent.getNamespaceURI(), parent.getLocalName());
        if (elementSchema != null)
        {
            return elementSchema.isAllowedAsSubElement(child);
        }
        else
        {
            return false;
        }
    }

    /**
     * Returns an array containing a list of possible values an attribute can have,
     * or null if such a list is not available.
     */
    public String[] getPossibleAttributeValues(Element element, String namespaceURI, String localName)
    {
        AttributeSchema attrSchema = getAttributeSchema(element, namespaceURI, localName);
        if (attrSchema != null)
        {
            return attrSchema.getPossibleValues(element);
        }
        return null;
    }


    public Collection getAllowedSubElements(Element element)
    {
        ElementSchema elementSchema = getElementSchema(element.getNamespaceURI(), element.getLocalName());
        if (elementSchema != null)
        {
            return elementSchema.subelements.values();
        }
        else
        {
            return Collections.EMPTY_LIST;
        }
    }

    public Collection getAllowedSubTexts(Element element)
    {
        ElementSchema elementSchema = getElementSchema(element.getNamespaceURI(), element.getLocalName());
        if (elementSchema != null)
        {
            return elementSchema.subtexts;
        }
        else
        {
            return Collections.EMPTY_LIST;
        }
    }

    protected ElementSchema getElementSchema(String namespaceURI, String localName)
    {
        return (ElementSchema) elementSchemas.get(namespaceURI, localName);
    }

    protected AttributeSchema getAttributeSchema(Element element, String namespaceURI, String localName)
    {
        ElementSchema elementSchema = getElementSchema(element.getNamespaceURI(), element.getLocalName());

        if (elementSchema == null)
            return null;
        else
            return elementSchema.getAttributeSchema(namespaceURI, localName);
    }

    public Collection validate(Document document)
            throws ValidationNotSupportedException, Exception
    {
        initializeVerifier();

        SAXEventGenerator generator = new SAXEventGenerator(document);
        errorHandler.errorCollection = new ArrayList();
        errorHandler.generator = generator;
        verifier.setStopNow(false);

        generator.makeEvent(verifier);

        return errorHandler.errorCollection;
    }


    /**
     * Verifies the document up to the given element, and then returns the
     * Expression describing the content model that the verifiers expects
     * for that element.
     *
     * <p>
     * Implementation note: this depends on the classes PolloMsvVerifier and
     * SAXEventGenerator to stop the validation at a certain node.
     */
    public Expression getExpression(Document document, Element element)
            throws Exception
    {
        initializeVerifier();

        SAXEventGenerator generator = new SAXEventGenerator(document);
        generator.stopAtNode(element);
        errorHandler.errorCollection = new ArrayList();
        errorHandler.generator = generator;
        verifier.setStopNow(false);

        // validate up to the wanted node
        try
        {
            generator.makeEvent(verifier);
        }
        catch (StopValidationException e)
        {
            ExpressionAcceptor acceptor = (ExpressionAcceptor) e.getAcceptor();
            Expression expression = acceptor.getExpression();

            verifier.setStopNow(false);
            return expression;
        }
        catch (Exception e)
        {
            verifier.setStopNow(false);
            throw e;
        }

        verifier.setStopNow(false);
        throw new PolloException("[MsvSchema] Could not find expression for the wanted node.");
    }

    protected void initializeVerifier()
    {
        // normally verifiers should be reusable but i noticed some problems that dissappeard
        // with not reusing them
        //if (verifier == null)
        //{
            DocumentDeclaration documentDeclaration = new REDocumentDeclaration(grammar);
            this.errorHandler = new MsvSchemaErrorHandler();
            this.verifier = new PolloMsvVerifier(documentDeclaration, errorHandler);
        //}
    }

    public class MsvSchemaErrorHandler implements ErrorHandler
    {
        public Collection errorCollection;
        public SAXEventGenerator generator;

        public void error(SAXParseException e) throws SAXException
        {
            addError(e);
        }

        public void fatalError(SAXParseException e) throws SAXException
        {
            addError(e);
        }

        public void warning(SAXParseException e) throws SAXException
        {
            addError(e);
        }

        protected void addError(SAXParseException e)
        {
            Node location = null;
            String attrNamespaceURI = null;
            String attrLocalName = null;

            if (e instanceof ValidityViolation)
            {
                ErrorInfo errorInfo = ((ValidityViolation) e).getErrorInfo();
                if (errorInfo instanceof ErrorInfo.BadText)
                {
                    location = generator.getLastCharacterData();
                }
                else
                {
                    location = generator.getLastElement();
                    if (errorInfo instanceof ErrorInfo.BadAttribute)
                    {
                        ErrorInfo.BadAttribute badAttr = (ErrorInfo.BadAttribute) errorInfo;
                        attrNamespaceURI = badAttr.attNamespaceURI;
                        attrLocalName = badAttr.attLocalName;
                    }
                }
            }
            errorCollection.add(new ValidationErrorInfo(location, e.getMessage(), attrNamespaceURI, attrLocalName));
        }
    }
}
