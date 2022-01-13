package pc1.pc2.pc3.logic.helper;

import pc1.pc2.pc3.om.*;

import java.util.function.Predicate;

public class ClausePredicate implements IClauseVisitor
{
    private Predicate<IAtomicClause> predicate;
    private boolean succeed = false;

    public ClausePredicate(Predicate<IAtomicClause> predicate)
    {
        this.predicate = predicate;
    }

    @Override
    public void visitAtomic(IAtomicClause clause)
    {
        succeed |= predicate.test(clause);
    }

    @Override
    public void visitComplex(IComplexClause clause)
    {
        for (IClause child : clause.getChildren()) {
            child.accept(this);
            if(succeed) {
                return;
            }
        }
    }

    @Override
    public void visitQuantified(IQuantifiedClause clause)
    {
        clause.getSuccessor().accept(this);
    }

    public boolean isSuccessful()
    {
        return succeed;
    }
}
