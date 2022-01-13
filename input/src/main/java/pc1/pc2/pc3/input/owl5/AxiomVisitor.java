package pc1.pc2.pc3.input.owl5;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import pc1.pc2.pc3.error.ParseException;
import pc1.pc2.pc3.input.owl5.format.Formatter;
import pc1.pc2.pc3.input.owl5.parser.DisjointClassesAxiom;
import pc1.pc2.pc3.input.owl5.parser.EquivalentClassesAxiom;
import pc1.pc2.pc3.input.owl5.parser.OWLParsingException;
import pc1.pc2.pc3.input.owl5.parser.SubClassOfAxiom;
import pc1.pc2.pc3.om.*;

import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AxiomVisitor implements OWLAxiomVisitor
{

    private final boolean parseAnnotations;
    private final boolean parseLogicalAxioms;
    private final List<IAxiom> axioms = new LinkedList<>();
    private IriMapper mapper;

    public AxiomVisitor()
    {
        this(true, true);
    }

    public AxiomVisitor(boolean parseAnnotations, boolean parseLogicalAxioms)
    {
        this.parseAnnotations = parseAnnotations;
        this.parseLogicalAxioms = parseLogicalAxioms;
    }

    public AxiomVisitor(IriMapper mapper)
    {
        this();
        this.mapper = mapper;
    }

    public List<IAxiom> parse(@NotNull IRI ontologyIRI, @Nullable OutputStream outStream, @NotNull Formatter formatter)
            throws LoadingException, OWLOntologyStorageException
    {
        OWLOntology ontology = OWLHelper.loadOntology(ontologyIRI, outStream, OWLManager.createOWLOntologyManager());
        return parse(ontology, formatter);
    }

    @NotNull
    public List<IAxiom> parse(OWLOntology ontology, @NotNull Formatter formatter)
    {
        if (parseAnnotations) {
            parseAnnotations(ontology, a -> a instanceof OWLAnnotationAxiom);
        }
        if (parseLogicalAxioms) {
            return parseLogicalAxioms(ontology, formatter);
        }
        return new LinkedList<>();
    }

    @NotNull
    private List<IAxiom> parseLogicalAxioms(OWLOntology ontology, @NotNull Formatter formatter)
    {
        return ontology.axioms()
                .filter(a -> !(a instanceof OWLAnnotationAxiom))
                .map(formatter::format)
                .flatMap(axiom -> parseAxiom(axiom, mapper))
                .collect(Collectors.toList());
    }

    private void parseAnnotations(@NotNull OWLOntology ontology, Predicate<OWLAxiom> isAnnotation)
    {
        IriMapper mapper = new IriMapper(ontology);
        ontology.axioms()
                .filter(isAnnotation)
                .forEach(axiom -> parseAxiom(axiom, mapper));
    }

    private Stream<IAxiom> parseAxiom(@NotNull OWLAxiom axiom, IriMapper mapper)
    {
        try {
            AxiomVisitor visitor = new AxiomVisitor(mapper);
            axiom.accept(visitor);
            return visitor.getAxioms().stream();
        } catch (OWLParsingException exception) {
            ParserLogger.writeLine(String.format("Cannot parse axiom %s. Axiom will be ignored", axiom));
            ParserLogger.writeLine(String.format("Reason of failure: %s", exception.getMessage()));
            return Stream.empty();
        }
    }

    @Override
    public void visit(@NotNull OWLSubClassOfAxiom axiom)
    {
        IAxiom parsedAxiom = null;
        try {
            parsedAxiom = new SubClassOfAxiom(axiom).parse();
        } catch (Exception ex) {
            ParserLogger.writeLine(String.format("Error when parsing axiom %s\n%s", axiom, ex.getMessage()));
        }
        if (parsedAxiom != null) {
            axioms.add(parsedAxiom);
        }
    }

    @Override
    public void visit(@NotNull OWLDisjointClassesAxiom axiom)
    {
        axioms.addAll(new DisjointClassesAxiom(axiom).parse());
    }

    /**
     * Shortcut for  EXISTS property.TOP -> domain
     *
     * @param axiom property domain axiom
     */
    @Override
    public void visit(@NotNull OWLObjectPropertyDomainAxiom axiom)
    {
        OWLClassExpression domain = axiom.getDomain();
        OWLDataFactory fact = OWLManager.getOWLDataFactory();
        OWLSubClassOfAxiom subClassAxiom = fact.getOWLSubClassOfAxiom(
                fact.getOWLObjectSomeValuesFrom(axiom.getProperty(), fact.getOWLThing()), domain);
        visit(subClassAxiom);
    }

    /**
     * Shortcut for the axiom TOP -> FORALL r.B
     *
     * @param axiom property range axiom
     */
    @Override
    public void visit(@NotNull OWLObjectPropertyRangeAxiom axiom)
    {
        OWLDataFactory fact = OWLManager.getOWLDataFactory();
        OWLObjectAllValuesFrom expression =
                fact.getOWLObjectAllValuesFrom(axiom.getProperty(), axiom.getRange());
        ICommonFactory factory = FactoryMgr.getCommonFactory();
        axioms.add(factory.createSubsetAxiom(factory.createAtomicClause(IConceptLiteral.TOP),
                parseExpression(expression)));
    }

    @Override
    public void visit(@NotNull OWLEquivalentClassesAxiom axiom)
    {
        axioms.addAll(new EquivalentClassesAxiom(axiom).parse());
    }

    @Override
    public void visit(@NotNull OWLAnnotationAssertionAxiom axiom)
    {
        OWLAnnotationProperty property = axiom.getProperty();

        if (property.getIRI().getRemainder().filter("label"::equalsIgnoreCase).isPresent()) {
            registerName(axiom, FactoryMgr.getSymbolFactory()::addMapping);
        }
        else if (property.getIRI().getRemainder().filter("altLabel"::equalsIgnoreCase).isPresent()) {
            registerName(axiom, FactoryMgr.getSymbolFactory()::addMapping);
        }
        else if (property.getIRI().getRemainder().filter("prefLabel"::equals).isPresent()) {
            registerPrefName(axiom);
        }
    }

    private void registerPrefName(@NotNull OWLAnnotationAssertionAxiom axiom)
    {
        OWLAnnotationSubject subject = axiom.getSubject();
        Optional<IRI> iri = subject.asIRI();
        OWLAnnotationValue value = axiom.annotationValue();
        Optional<String> literal = value.asLiteral()
                .filter(l -> !l.hasLang() || l.getLang().equalsIgnoreCase("en"))
                .map(OWLLiteral::getLiteral);
        Optional<String> id = iri.map(IRI::getRemainder).map(s -> s.orElse(""));
        if (!id.isPresent() || id.get().isEmpty()) {
            id = iri.map(IRI::getIRIString);
        }
        ISymbolFactory sFactory = FactoryMgr.getSymbolFactory();
        if (id.isPresent() && literal.isPresent()) {
            try {
                sFactory.setPreferredMapping(id.get(), literal.map(sFactory::normaliseSymbol).get());
            } catch (ParseException e) {
                try {
                    registerDuplicateLiteral(subject, literal.get(), id.get());
                } catch (ParseException e1) {
                    ParserLogger.writeLine(e1.getMessage());
                    ParserLogger.writeLine("Preferred name will not be registered for " + subject);
                    e.printStackTrace();
                }
            }
        }
    }

    private void registerDuplicateLiteral(OWLObject subject, String literal, String id) throws ParseException
    {
        ISymbolFactory sFactory = FactoryMgr.getSymbolFactory();
        if (subject instanceof OWLClass) {
            sFactory.setPreferredMapping(id, sFactory.normaliseSymbol(literal) + "-Concept");
        }
        else if (subject instanceof OWLObjectProperty) {
            sFactory.setPreferredMapping(id, sFactory.normaliseSymbol(literal) + "-Property");
        }
        else if (subject instanceof IRI) {
            OWLEntity entity = mapper.getEntity((IRI) subject);
            registerDuplicateLiteral(entity, literal, id);
        }
    }

    private void registerName(@NotNull OWLAnnotationAssertionAxiom axiom,
                              BiConsumer<String, String> mapper)
    {
        Optional<IRI> iri = axiom.getSubject().asIRI();
        OWLAnnotationValue value = axiom.annotationValue();
        Optional<String> literal = value.asLiteral()
                .filter(l -> !l.hasLang() || l.getLang().equalsIgnoreCase("en"))
                .map(OWLLiteral::getLiteral);
        Optional<String> id = iri.map(IRI::getRemainder).map(s -> s.orElse(""));
        if (!id.isPresent() || id.get().isEmpty()) {
            id = iri.map(IRI::getIRIString);
        }
        if (id.isPresent() && literal.isPresent()) {
            mapper.accept(id.get(), literal.map(FactoryMgr.getSymbolFactory()::normaliseSymbol).get());
        }
    }

    @Override
    public void doDefault(@NotNull Object object)
    {
        throw new OWLParsingException(String.format("Cannot parse axiom %s. " +
                "Axioms of type %s are not yet handled. ", object, ((OWLAxiom) object).getAxiomType()));
    }

    @Override
    public void visit(@NotNull OWLObjectPropertyAssertionAxiom axiom)
    {
        // do nothing, not yet required
    }

    @Override
    public void visit(@NotNull OWLDeclarationAxiom axiom)
    {
        // do nothing, not required
    }

    public List<IAxiom> getAxioms()
    {
        return axioms;
    }

    private IClause parseExpression(@NotNull OWLClassExpression expression)
    {
        ExpressionVisitor visitor = new ExpressionVisitor();
        expression.accept(visitor);
        return visitor.getExpression();
    }
}
