package pc1.pc2.pc3.logic.resolver;

import pc1.pc2.pc3.om.IAxiom;
import pc1.pc2.pc3.om.IConceptLiteral;
import pc1.pc2.pc3.logic.factory.DefinerFactory;
import pc1.pc2.pc3.logic.helper.DefinerExtractor;

import java.util.*;

public class StructuralTransformation
{

    private final IConceptLiteral forgettingSymbol;
    private final DefinerFactory definerFactory;

    public StructuralTransformation(DefinerFactory definerFactory, IConceptLiteral forgettingSymbol)
    {
        this.forgettingSymbol = forgettingSymbol;
        this.definerFactory = definerFactory;
    }

    public Collection<IAxiom> apply(IAxiom axiom)
    {
        List<IAxiom> axioms = new LinkedList<>();
        Set<IConceptLiteral> oldDefiners = new HashSet<>(definerFactory.getDefiners());
        new DefinerExtractor(definerFactory, forgettingSymbol, -1).normalise(Collections.singleton(axiom.getLeft()));
        new DefinerExtractor(definerFactory, forgettingSymbol, 1).normalise(Collections.singleton(axiom.getRight()));
        Set<IConceptLiteral> definers = new HashSet<>(definerFactory.getDefiners());
        definers.removeAll(oldDefiners);
        axioms.add(axiom);
        definers.forEach(d -> axioms.add(definerFactory.getDefinerDefinitionAsAxiom(d)));
        return axioms;
    }
}
