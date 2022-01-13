package pc1.pc2.pc3.logic.resolver;

import pc1.pc2.pc3.om.*;
import pc1.pc2.pc3.utils.NNFConverter;

public class ConjunctiveSubsumptionChecker implements IClauseVisitor
{
    private final IComplexClause subsumer;
    private boolean isSubsumed;

    public ConjunctiveSubsumptionChecker(IComplexClause subsumer)
    {
        this.subsumer = subsumer;
    }

    @Override public void visitAtomic(IAtomicClause clause)
    {
        // a conjunction subsumes an atomic if the negative of the atomic
        // subsumes the negation of the conjunction
        NNFConverter negator = new NNFConverter();
        IClause negatedAtomic = negator.negate(clause);
        IClause negatedSubsumer = negator.negate(subsumer);
        isSubsumed = subsumes(negatedAtomic, negatedSubsumer);
    }

    @Override public void visitComplex(IComplexClause clause)
    {
        if (clause.getOperator() == GroupOperator.DISJUNCTION) {
            for (IClause child : clause.getChildren()) {
                if(!isSubsumed) child.accept(this);
            }
        }
        else {
            // a first conjunction subsumes a second conjunction iff for every element in the second conjunction
            // the negation of this element subsumes the negation of the first conjunction
            NNFConverter negator = new NNFConverter();
            IClause negatedSubsumer = negator.negate(subsumer);
            boolean subs = true;
            for (IClause child : clause.getChildren()) {
                IClause negatedChild = negator.negate(child);
                subs = subsumes(negatedChild, negatedSubsumer);
                if (!subs) {
                    break;
                }
            }
            isSubsumed = subs;
        }
    }

    @Override public void visitQuantified(IQuantifiedClause clause)
    {
        if (subsumer instanceof IQuantifiedClause) {
            IQuantifiedClause quantifiedSubsumer = (IQuantifiedClause) subsumer;
            if (quantifiedSubsumer.getQuantifier() == clause.getQuantifier() &&
                    quantifiedSubsumer.getRole() == clause.getRole()) {
                isSubsumed = subsumes(quantifiedSubsumer.getSuccessor(), clause.getSuccessor());
            }
        }
    }

    public boolean subsumes()
    {
        return isSubsumed;
    }

    private boolean subsumes(IClause subsumer, IClause subsumee)
    {
        ClauseSubsumptionChecker checker = new ClauseSubsumptionChecker();
        return checker.subsumes(subsumer, subsumee);
    }
}
