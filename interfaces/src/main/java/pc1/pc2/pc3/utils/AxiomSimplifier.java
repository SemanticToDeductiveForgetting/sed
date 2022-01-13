package pc1.pc2.pc3.utils;

import pc1.pc2.pc3.om.FactoryMgr;
import pc1.pc2.pc3.om.IAxiom;
import pc1.pc2.pc3.om.IClause;

import java.util.Collection;
import java.util.stream.Collectors;

public class AxiomSimplifier
{
    public IAxiom simplify(IAxiom axiom)
    {
        FormulaeSimplifier simplifier = new FormulaeSimplifier();
        IClause simplifiedLeft = simplifier.simplify(axiom.getLeft());
        IClause simplifiedRight = simplifier.simplify(axiom.getRight());
        return FactoryMgr.getCommonFactory().createSubsetAxiom(simplifiedLeft, simplifiedRight);
    }

    public Collection<IAxiom> simplify(Collection<IAxiom> ontology)
    {
        return ontology.stream()
                .map(new AxiomSimplifier()::simplify)
                .filter(a -> !a.getLeft().isBottom())
                .filter(a -> !a.getRight().isTop())
                .collect(Collectors.toSet());
    }
}
