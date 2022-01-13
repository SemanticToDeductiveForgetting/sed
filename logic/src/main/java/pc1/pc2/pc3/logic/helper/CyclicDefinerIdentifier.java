package pc1.pc2.pc3.logic.helper;

import pc1.pc2.pc3.om.IAxiom;
import pc1.pc2.pc3.om.IConceptLiteral;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class CyclicDefinerIdentifier
{
    public CyclicDefinerIdentifier()
    {
    }

    public Collection<IConceptLiteral> getCyclicDefiners(Collection<IAxiom> ontology)
    {
        Set<IConceptLiteral> cyclicDefiners = new HashSet<IConceptLiteral>();
        for (IAxiom axiom : ontology) {
            Set<IConceptLiteral> clauseDefiners = ClauseHelper.getConceptNamesInStatement(axiom).stream()
                    .filter(IConceptLiteral::isDefiner)
                    .collect(Collectors.toSet());

            PolarityCalculator polarity = new PolarityCalculator(-1);
            axiom.getLeft().accept(polarity);
            Set<IConceptLiteral> positive = new HashSet<IConceptLiteral>(polarity.getPositive());
            Set<IConceptLiteral> negative = new HashSet<IConceptLiteral>(polarity.getNegative());
            polarity = new PolarityCalculator(1);
            axiom.getRight().accept(polarity);
            positive.addAll(polarity.getPositive());
            negative.addAll(polarity.getNegative());
            positive.retainAll(negative);
            positive.retainAll(clauseDefiners);
            cyclicDefiners.addAll(positive);
        }
        return cyclicDefiners;
    }
}