package pc1.pc2.pc3.utils;

import pc1.pc2.pc3.norm.Normalizer;
import pc1.pc2.pc3.om.*;
import pc1.pc2.pc3.om.*;
import pc1.pc2.pc3.om.*;
import pc1.pc2.pc3.om.*;
import pc1.pc2.pc3.om.privileged.IPrivilegedQuantifiedClause;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

public class FormulaeSimplifier implements IClauseVisitor
{
    @Override
    public void visitAtomic(IAtomicClause clause)
    {

    }

    @Override
    public void visitComplex(IComplexClause clause)
    {
        Set<IClause> children = new HashSet<>(clause.getChildren());
        clause.removeAll();
        for (IClause child : children) {
            Normalizer.addSubClause(clause, simplify(child));
        }

        ICommonFactory fact = FactoryMgr.getCommonFactory();
        if (clause.getOperator() == GroupOperator.DISJUNCTION) {
            if (clause.isTop()) {
                new LinkedList<>(clause.getChildren()).forEach(clause::removeChild);
                Normalizer.addSubClause(clause,
                        fact.createAtomicClause(IConceptLiteral.TOP));
            }
            clause.removeChild(IClause::isBottom);
            if (clause.getChildren().isEmpty()) {
                Normalizer.addSubClause(clause,
                        fact.createAtomicClause(IConceptLiteral.TOP).negate());
            }
        }
        if (clause.getOperator() == GroupOperator.CONJUNCTION) {
            if (clause.isBottom()) {
                new LinkedList<>(clause.getChildren()).forEach(clause::removeChild);
                clause.addChild(fact.createAtomicClause(IConceptLiteral.TOP).negate());
            }
            clause.removeChild(IClause::isTop);
            if (clause.getChildren().isEmpty()) {
                Normalizer.addSubClause(clause, fact.createAtomicClause(IConceptLiteral.TOP));
            }
        }
    }

    @Override
    public void visitQuantified(IQuantifiedClause clause)
    {
        if (clause instanceof IPrivilegedQuantifiedClause) {
            ((IPrivilegedQuantifiedClause) clause).setSuccessor(simplify(clause.getSuccessor()));
        }
    }

    public IClause simplify(IClause clause)
    {
        clause.accept(this);
        if (clause.isTop()) {
            return FactoryMgr.getCommonFactory().createAtomicClause(IConceptLiteral.TOP);
        }
        if (clause.isBottom()) {
            return FactoryMgr.getCommonFactory().createAtomicClause(IConceptLiteral.TOP).negate();
        }
        if (clause instanceof IComplexClause && ((IComplexClause) clause).getChildren().size() == 1) {
            return ((IComplexClause) clause).getChildren().iterator().next();
        }
        return clause;
    }
}
