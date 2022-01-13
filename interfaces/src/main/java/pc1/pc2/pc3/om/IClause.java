package pc1.pc2.pc3.om;

public interface IClause extends IStatement
{
    void accept(IClauseVisitor visitor);

    boolean isBottom();

    boolean isNegated();

    boolean isTop();
}
