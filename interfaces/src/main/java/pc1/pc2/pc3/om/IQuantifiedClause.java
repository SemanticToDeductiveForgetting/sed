package pc1.pc2.pc3.om;

public interface IQuantifiedClause extends IClause
{
    IRoleLiteral getRole();
    Quantifier getQuantifier();
    IClause getSuccessor();

    void setQuantifier(Quantifier universal);
}
