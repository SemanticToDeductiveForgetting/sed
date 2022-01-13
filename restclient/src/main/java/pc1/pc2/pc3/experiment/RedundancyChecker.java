package pc1.pc2.pc3.experiment;

import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.reasoner.BufferingMode;
import org.semanticweb.owlapi.reasoner.SimpleConfiguration;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasoner;


public class RedundancyChecker
{
    private final OWLOntology ontology;
    private final OWLOntology background;
    private final StructuralReasoner reasoner;

    public RedundancyChecker(OWLOntology ontology, OWLOntology background)
    {
        this.ontology = ontology;
        this.background = background;
        //reasoner = new Reasoner(background);
        reasoner = new StructuralReasoner(background, new SimpleConfiguration(), BufferingMode.BUFFERING);
    }

    public OWLOntology findRedundantClauses() throws OWLOntologyCreationException
    {
        OWLOntology redundancies = ontology.getOWLOntologyManager().createOntology();
        this.ontology.logicalAxioms()
                .filter(this::isRedundant)
                .forEach(redundancies::add);
        return redundancies;
    }

    private boolean isRedundant(OWLLogicalAxiom axiom)
    {
        return reasoner.isEntailed(axiom);
    }
}
