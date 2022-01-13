package pc1.pc2.pc3.logic.resolver;

import pc1.pc2.pc3.om.*;

public class QuantifierSubsumptionChecker implements IClauseVisitor
{
    private final IQuantifiedClause subsumer;
    private boolean isSubsumed = false;

    public QuantifierSubsumptionChecker(IQuantifiedClause subsumer)
    {
        this.subsumer = subsumer;
    }

    @Override public void visitAtomic(IAtomicClause clause)
    {

    }

    @Override public void visitComplex(IComplexClause clause)
    {
        if(clause.getOperator() == GroupOperator.DISJUNCTION) {
            for (IClause child : clause.getChildren()) {
                child.accept(this);
            }
        }
    }

    @Override public void visitQuantified(IQuantifiedClause clause)
    {
        if(clause.getQuantifier() == subsumer.getQuantifier() && clause.getRole() == subsumer.getRole()) {
            ClauseSubsumptionChecker checker = new ClauseSubsumptionChecker();
            if(checker.subsumes(subsumer.getSuccessor(), clause.getSuccessor())) {
                isSubsumed = true;
            }
        }
    }

    public boolean subsumes()
    {
        return isSubsumed;
    }
}
