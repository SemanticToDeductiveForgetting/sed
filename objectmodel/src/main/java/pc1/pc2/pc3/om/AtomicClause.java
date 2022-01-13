package pc1.pc2.pc3.om;

public class AtomicClause extends Clause implements IAtomicClause
{

    private IConceptLiteral literal;

    AtomicClause(IConceptLiteral literal, boolean negative)
    {
        this.literal = literal;
        setNegated(negative);
    }

    @Override
    public IConceptLiteral getLiteral()
    {
        return literal;
    }

    @Override
    public IAtomicClause negate()
    {
        setNegated(!isNegated());
        return this;
    }

    @Override
    public void accept(IClauseVisitor visitor)
    {
        visitor.visitAtomic(this);
    }

    @Override public boolean isBottom()
    {
        return isNegated() && literal == IConceptLiteral.TOP;
    }

    @Override
    public boolean isTop()
    {
        return !isNegated() && literal == IConceptLiteral.TOP;
    }
}
