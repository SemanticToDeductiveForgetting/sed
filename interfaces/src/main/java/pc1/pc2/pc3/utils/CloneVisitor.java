package pc1.pc2.pc3.utils;

import org.jetbrains.annotations.Nullable;
import pc1.pc2.pc3.om.*;
import pc1.pc2.pc3.om.*;
import pc1.pc2.pc3.om.*;
import pc1.pc2.pc3.om.*;

public class CloneVisitor implements IClauseVisitor
{
    private IClause clonedClause = null;

    @Override public void visitAtomic(IAtomicClause clause)
    {
        IAtomicClause clone = FactoryMgr.getCommonFactory().createAtomicClause(clause.getLiteral());
        clonedClause = clone;
        if(clause.isNegated()) {
            clonedClause = FactoryMgr.getCommonFactory().createShallowNegationStrategy().negate(clonedClause);
        }
    }

    @Override public void visitComplex(IComplexClause clause)
    {
        IComplexClause clonedComplex = FactoryMgr.getCommonFactory().createComplexClause(clause.getOperator());
        for (IClause child : clause.getChildren()) {
            CloneVisitor visitor = new CloneVisitor();
            child.accept(visitor);
            clonedComplex.addChild(visitor.getClonedClause());
        }
        clonedClause = clonedComplex;
        if(clause.isNegated()) {
            clonedClause = FactoryMgr.getCommonFactory().createShallowNegationStrategy().negate(clonedClause);
        }
    }

    @Override public void visitQuantified(IQuantifiedClause clause)
    {
        CloneVisitor visitor = new CloneVisitor();
        clause.getSuccessor().accept(visitor);
        IClause successor = visitor.getClonedClause();
        clonedClause = FactoryMgr.getCommonFactory()
                .createQuantifiedClause(clause.getQuantifier(), clause.getRole(), successor);
        if(clause.isNegated()) {
            clonedClause = FactoryMgr.getCommonFactory().createShallowNegationStrategy().negate(clonedClause);
        }
    }

    @Nullable IClause getClonedClause()
    {
        return clonedClause;
    }
}
