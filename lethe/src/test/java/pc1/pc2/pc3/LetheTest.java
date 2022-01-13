package pc1.pc2.pc3;

import org.junit.Assert;
import org.junit.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.util.DefaultPrefixManager;
import scala.collection.JavaConversions;
import uk.ac.man.cs.lethe.internal.dl.datatypes.Ontology;
import uk.ac.man.cs.lethe.internal.dl.forgetting.DirectALCForgetter;

import java.util.HashSet;
import java.util.Set;

public class LetheTest
{
    @Test
    public void testLetheForget() throws OWLOntologyCreationException
    {
        OWLDataFactory factory = OWLManager.getOWLDataFactory();
        DefaultPrefixManager prefixManager = new DefaultPrefixManager();
        OWLClass A = factory.getOWLClass(IRI.create("A"));
        OWLClass B = factory.getOWLClass(IRI.create("B"));
        OWLClass C = factory.getOWLClass(IRI.create("C"));
        OWLClass E = factory.getOWLClass(IRI.create("E"));
        OWLObjectProperty r = factory.getOWLObjectProperty(IRI.create("r"));

        Set<OWLAxiom> axioms = new HashSet<>();
        axioms.add(factory.getOWLSubClassOfAxiom(
                A, factory.getOWLObjectSomeValuesFrom(r, factory.getOWLObjectIntersectionOf(B, C))));
        axioms.add(factory.getOWLSubClassOfAxiom(
                factory.getOWLObjectSomeValuesFrom(r, factory.getOWLObjectIntersectionOf(B, C)), E));

        OWLOntologyManager mgr = OWLManager.createOWLOntologyManager();
        OWLOntology ontology = mgr.createOntology(axioms);
        Set<String> sig = new HashSet<>();
        sig.add("B");
        sig.add("C");
        Ontology view = DirectALCForgetter.forget(LetheHelper.toLetheOntology(ontology),
                JavaConversions.asScalaSet(sig).toSet());

        Assert.assertEquals("", view.toString());
    }
}
