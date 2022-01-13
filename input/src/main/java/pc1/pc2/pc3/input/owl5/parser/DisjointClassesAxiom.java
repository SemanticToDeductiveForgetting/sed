package pc1.pc2.pc3.input.owl5.parser;

import org.semanticweb.owlapi.model.OWLDisjointClassesAxiom;
import pc1.pc2.pc3.norm.Normalizer;
import pc1.pc2.pc3.om.*;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class DisjointClassesAxiom extends BaseParser
{
    private final OWLDisjointClassesAxiom axiom;

    public DisjointClassesAxiom(OWLDisjointClassesAxiom axiom)
    {
        this.axiom = axiom;
    }

    public Collection<IAxiom> parse() {
        List<IAxiom> axioms = new LinkedList<>();
        ICommonFactory factory = FactoryMgr.getCommonFactory();
        Collection<OWLDisjointClassesAxiom> disjointPairs = axiom.asPairwiseAxioms();
        for (OWLDisjointClassesAxiom pair : disjointPairs) {
            IComplexClause conjunction = factory.createConjunctiveClause();
            pair.classExpressions()
                    .map(this::parseExpression)
                    .forEach(c -> Normalizer.addSubClause(conjunction, c));
            IClause right = conjunction;
            if(conjunction.getChildren().size() == 1) {
                right = conjunction.getChildren().iterator().next();
            }
            axioms.add(factory.createSubsetAxiom(right, factory.createAtomicClause(IConceptLiteral.TOP).negate()));
        }
        return axioms;
    }
}
