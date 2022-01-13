package pc1.pc2.pc3.fame;

import forgetting.Fame;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;

import java.io.File;
import java.util.Collections;

public class FameTest
{
    private OWLClass A;
    private OWLClass B;
    private OWLOntology ontology;

    private OWLOntology createOntology() throws OWLOntologyCreationException, OWLOntologyStorageException
    {
        OWLOntologyManager mgr = OWLManager.createOWLOntologyManager();
        ontology = mgr.createOntology(IRI.create("TestOntology"));
        OWLDataFactory df = OWLManager.getOWLDataFactory();

        A = df.getOWLClass(IRI.create("A"));
        B = df.getOWLClass(IRI.create("B"));
        OWLObjectProperty r = df.getOWLObjectProperty(IRI.create("r"));
        OWLObjectSomeValuesFrom conjunct1 = df.getOWLObjectSomeValuesFrom(r, B);
        OWLObjectAllValuesFrom conjunct2 = df.getOWLObjectAllValuesFrom(r, B.getObjectComplementOf());
        OWLSubClassOfAxiom axiom = df.getOWLSubClassOfAxiom(A, df.getOWLObjectIntersectionOf(conjunct1, conjunct2));
        mgr.addAxiom(ontology, axiom);
        mgr.saveOntology(ontology, IRI.create(new File("/Users/mostafa/Projects/forget/ont.owl")));
        return ontology;
    }

    @Test
    public void testFameInitialization() throws OWLOntologyCreationException, CloneNotSupportedException
    {
        Fame fame = new Fame();
        OWLOntology owlOntology = fame.FameRC(Collections.emptySet(), Collections.singleton(B), ontology);
        System.out.println(owlOntology);
    }

    @BeforeEach
    public void setup() throws OWLOntologyCreationException, OWLOntologyStorageException
    {
        createOntology();
    }
}
