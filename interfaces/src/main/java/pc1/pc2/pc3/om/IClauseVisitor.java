package pc1.pc2.pc3.om;

public interface IClauseVisitor
{
    void visitAtomic(IAtomicClause clause);
    void visitComplex(IComplexClause clause);
    void visitQuantified(IQuantifiedClause clause);
}
