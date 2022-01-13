package pc1.pc2.pc3.input.owl5;

import org.junit.Assert;
import org.junit.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import pc1.pc2.pc3.input.owl5.format.Formatter;
import pc1.pc2.pc3.om.IAxiom;

import java.util.List;

public class OwlParsingTest extends pc1.pc2.pc3.Test
{

    @Test
    public void testParsingEmptyOntology() throws OWLOntologyCreationException
    {
        OWLOntology ontology = createOntology();
        AxiomVisitor axiomVisitor = new AxiomVisitor();
        List<IAxiom> clauses = axiomVisitor.parse(ontology, Formatter.Default);
        Assert.assertTrue("Ontology should be empty", clauses.isEmpty());
    }

    @Test
    public void testParsingSubClassAxiom() throws OWLOntologyCreationException
    {
        OWLOntology ontology = createOntology();
        OWLDataFactory factory = ontology.getOWLOntologyManager().getOWLDataFactory();
        OWLClass person = factory.getOWLClass(IRI.create("person"));
        OWLClass human = factory.getOWLClass(IRI.create("human"));
        OWLSubClassOfAxiom axiom = factory.getOWLSubClassOfAxiom(person, human);
        ontology.addAxiom(axiom);

        AxiomVisitor visitor = new AxiomVisitor();
        List<IAxiom> clauses = visitor.parse(ontology, Formatter.Default);

        Assert.assertEquals("Ontology should contain one axiom", 1, clauses.size());
        Assert.assertEquals("Incorrect axiom in ontology", "person -> human", clauses.get(0).toString());
    }

    @Test
    public void testParseDisjointClassAxiom() throws OWLOntologyCreationException
    {
        OWLOntology ontology = createOntology();
        OWLDataFactory factory = ontology.getOWLOntologyManager().getOWLDataFactory();
        OWLObjectProperty runsBy = factory.getOWLObjectProperty(IRI.create("runsBy"));
        OWLClass electricity = factory.getOWLClass(IRI.create("electricity"));
        OWLObjectSomeValuesFrom robot = factory.getOWLObjectSomeValuesFrom(runsBy, electricity);
        OWLClass animal = factory.getOWLClass(IRI.create("animal"));
        OWLClass plant = factory.getOWLClass(IRI.create("plant"));
        ontology.add(factory.getOWLDisjointClassesAxiom(robot, animal, plant));

        AxiomVisitor visitor = new AxiomVisitor();
        List<IAxiom> axioms = visitor.parse(ontology, Formatter.Default);
        Assert.assertEquals("Ontology should contain three axioms", 3, axioms.size());
    }

    @Test
    public void testParsingOnlineOntology() throws LoadingException, OWLOntologyStorageException
    {
        IRI iri = IRI.create("http://protege.stanford.edu/ontologies/pizza/pizza.owl");
        AxiomVisitor visitor = new AxiomVisitor();
        List<IAxiom> clauses = visitor.parse(iri, null, Formatter.Default);
        Assert.assertEquals("Ontology should contain axioms", 683, clauses.size());
    }

    private OWLOntology createOntology() throws OWLOntologyCreationException
    {
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        return manager.createOntology();
    }
}
