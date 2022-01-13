package pc1.pc2.pc3.utils;

import pc1.pc2.pc3.norm.InPlaceShallowNegationStrategy;
import pc1.pc2.pc3.om.*;
import pc1.pc2.pc3.om.*;
import pc1.pc2.pc3.om.*;
import pc1.pc2.pc3.om.*;

public class NNFConverter implements IClauseVisitor
{
    public IClause negate(IClause clause)
    {
        IClause clone = clone(clause);
        return toNNF(new InPlaceShallowNegationStrategy().negate(clone));
    }

    public IClause toNNF(IClause clause)
    {
        clause.accept(this);
        return clause;
    }

    @Override
    public void visitAtomic(IAtomicClause clause)
    {
    }

    @Override
    public void visitComplex(IComplexClause clause)
    {
        if (clause.isNegated()) {
            if (clause.getOperator() == GroupOperator.DISJUNCTION) {
                clause.setOperator(GroupOperator.CONJUNCTION);
            }
            else {
                clause.setOperator(GroupOperator.DISJUNCTION);
            }
            for (IClause child : clause.getChildren()) {
                new InPlaceShallowNegationStrategy().negate(child).accept(this);
            }
        }
    }

    @Override public void visitQuantified(IQuantifiedClause clause)
    {
        if (clause.isNegated()) {
            Quantifier quantifier = clause.getQuantifier();
            if (quantifier == Quantifier.Existential) {
                clause.setQuantifier(Quantifier.Universal);
            }
            else {
                clause.setQuantifier(Quantifier.Existential);
            }
            new InPlaceShallowNegationStrategy().negate(clause.getSuccessor()).accept(this);
            clause.getSuccessor().accept(this);
        }
    }

    private IClause clone(IClause clause)
    {
        CloneVisitor visitor = new CloneVisitor();
        clause.accept(visitor);
        return visitor.getClonedClause();
    }
}
