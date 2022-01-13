package pc1.pc2.pc3.logic.resolver;

import pc1.pc2.pc3.om.*;

public class AtomicSubsumptionChecker implements IClauseVisitor
{
    private final IAtomicClause subsumer;
    private boolean isSubsumed = false;

    public AtomicSubsumptionChecker(IAtomicClause subsumer)
    {
        this.subsumer = subsumer;
    }

    @Override public void visitAtomic(IAtomicClause clause)
    {
        if(clause.getLiteral() == subsumer.getLiteral() && clause.isNegated() == subsumer.isNegated()) {
            isSubsumed = true;
        }
    }

    @Override public void visitComplex(IComplexClause clause)
    {
        if(clause.getOperator() == GroupOperator.DISJUNCTION) {
            for (IClause child : clause.getChildren()) {
                if(!isSubsumed) {
                    child.accept(this);
                }
            }
        }
    }

    @Override public void visitQuantified(IQuantifiedClause clause)
    {

    }

    public boolean subsumes()
    {
        return isSubsumed;
    }
}
