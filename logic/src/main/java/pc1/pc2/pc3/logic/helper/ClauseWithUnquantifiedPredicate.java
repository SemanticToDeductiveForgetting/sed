package pc1.pc2.pc3.logic.helper;

import pc1.pc2.pc3.om.IAtomicClause;
import pc1.pc2.pc3.om.IClauseVisitor;
import pc1.pc2.pc3.om.IComplexClause;
import pc1.pc2.pc3.om.IQuantifiedClause;

import java.util.function.Predicate;

public class ClauseWithUnquantifiedPredicate implements IClauseVisitor
{
    private Predicate<IAtomicClause> predicate;
    private boolean successful = false;

    ClauseWithUnquantifiedPredicate(Predicate<IAtomicClause> predicate)
    {
        this.predicate = predicate;
    }

    @Override
    public void visitAtomic(IAtomicClause clause)
    {
        successful |= predicate.test(clause);
    }

    @Override
    public void visitComplex(IComplexClause clause)
    {
        clause.getChildren().forEach(child -> child.accept(this));
    }

    @Override
    public void visitQuantified(IQuantifiedClause clause)
    {

    }

    boolean isSuccessful()
    {
        return successful;
    }
}
