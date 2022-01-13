package pc1.pc2.pc3.output;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import pc1.pc2.pc3.om.AxiomType;
import pc1.pc2.pc3.om.IAxiom;
import pc1.pc2.pc3.om.IClause;
import pc1.pc2.pc3.output.visitor.ClauseToOWL;

import java.util.Collection;

public class Output
{
    public static OWLOntology convertClauses(Collection<IClause> ontology, IRI outputIri) throws OWLOntologyCreationException
    {
        OWLOntologyManager mgr = OWLManager.createOWLOntologyManager();
        OWLOntology owlOntology = mgr.createOntology(outputIri);
        OWLDataFactory factory = OWLManager.getOWLDataFactory();
        for (IClause clause : ontology) {
            OWLClassExpression expression = parse(factory, clause);
            owlOntology.addAxiom(toAxiom(factory, factory.getOWLThing(), expression, AxiomType.INCLUSION));
        }

        return owlOntology;
    }

    public static OWLOntology convertAxioms(Collection<IAxiom> ontology, IRI outputIri) throws OWLOntologyCreationException
    {
        OWLOntologyManager mgr = OWLManager.createOWLOntologyManager();
        OWLOntology owlOntology = mgr.createOntology(outputIri);
        OWLDataFactory factory = OWLManager.getOWLDataFactory();
        for (IAxiom axiom : ontology) {
            OWLClassExpression left = parse(factory, axiom.getLeft());
            OWLClassExpression right = parse(factory, axiom.getRight());
            owlOntology.addAxiom(toAxiom(factory, left, right, axiom.getType()));
        }

        return owlOntology;
    }

    private static OWLClassExpression parse(OWLDataFactory factory, IClause clause)
    {
        ClauseToOWL parser = new ClauseToOWL(factory);
        clause.accept(parser);
        return parser.getExpression();
    }

    private static OWLAxiom toAxiom(OWLDataFactory factory, OWLClassExpression left, OWLClassExpression right,
                                    AxiomType type)
    {
        if (type == AxiomType.INCLUSION) {
            return factory.getOWLSubClassOfAxiom(left, right);
        }
        else if (type == AxiomType.EQUIVALENCE) {
            return factory.getOWLEquivalentClassesAxiom(left, right);
        }

        assert false : "Cannot parse axiom not of type inclusion or equivalence";
        return null;
    }
}
