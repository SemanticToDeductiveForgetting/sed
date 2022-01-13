package pc1.pc2.pc3.om;

public interface IAtomicClause extends IClause
{
    IConceptLiteral getLiteral();
    IAtomicClause negate();
}
