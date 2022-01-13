package pc1.pc2.pc3.logic.helper;

import pc1.pc2.pc3.logic.factory.DefinerFactory;
import pc1.pc2.pc3.om.*;
import pc1.pc2.pc3.om.privileged.IPrivilegedQuantifiedClause;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class ConjunctionExtractor implements IClauseVisitor
{
    private final DefinerFactory definerFactory;
    private Collection<IConceptLiteral> definers;

    public ConjunctionExtractor(DefinerFactory definerFactory)
    {
        this.definerFactory = definerFactory;
    }

    @Override public void visitAtomic(IAtomicClause clause)
    {

    }

    @Override public void visitComplex(IComplexClause clause)
    {
        clause.getChildren().forEach(c -> c.accept(this));
    }

    @Override public void visitQuantified(IQuantifiedClause clause)
    {
        IClause successor = clause.getSuccessor();
        if (successor instanceof IComplexClause &&
                ((IComplexClause) successor).getOperator() == GroupOperator.CONJUNCTION) {
            IConceptLiteral newDefiner = definerFactory.createDefinerForClause(successor, clause.getRole(),
                    clause.getQuantifier() == Quantifier.Existential);
            definers.add(newDefiner);
            if (clause instanceof IPrivilegedQuantifiedClause) {
                ((IPrivilegedQuantifiedClause) clause)
                        .setSuccessor(FactoryMgr.getCommonFactory().createAtomicClause(newDefiner));
            }
        }
    }

    public Collection<IClause> extract(IClause clause)
    {
        definers = new LinkedList<>();
        clause.accept(this);
        List<IClause> expansion = new LinkedList<>();
        expansion.add(clause);
        definers.stream()
                .map(definerFactory::getDefinerDefinitionAsClause)
                .flatMap(d -> new CnfConverter().convert(d).stream())
                .forEach(expansion::add);

        return expansion;
    }
}
