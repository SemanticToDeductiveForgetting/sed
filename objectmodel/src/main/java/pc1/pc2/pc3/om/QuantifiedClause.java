package pc1.pc2.pc3.om;

import pc1.pc2.pc3.om.privileged.IPrivilegedQuantifiedClause;

public class QuantifiedClause extends Clause implements IQuantifiedClause, IPrivilegedQuantifiedClause
{
    private Quantifier quantifier;
    private final IRoleLiteral role;
    private IClause successor;

    QuantifiedClause(Quantifier quantifier, IRoleLiteral role, IClause successor)
    {
        this.quantifier = quantifier;
        this.role = role;
        this.successor = successor;
    }

    @Override
    public IRoleLiteral getRole()
    {
        return role;
    }

    @Override
    public Quantifier getQuantifier()
    {
        return quantifier;
    }

    @Override
    public IClause getSuccessor()
    {
        return successor;
    }

    @Override public void setQuantifier(Quantifier quantifier)
    {
        this.quantifier = quantifier;
    }

    @Override
    public void accept(IClauseVisitor visitor)
    {
        visitor.visitQuantified(this);
    }

    @Override public boolean isBottom()
    {
        return isNegated() ? getTopNoNegation() : getBottomNoNegation();
    }

    @Override
    public boolean isTop() {
        return isNegated() ? getBottomNoNegation() : getTopNoNegation();
    }

    private boolean getBottomNoNegation() {
        return getSuccessor().isBottom() && getQuantifier() == Quantifier.Existential;
    }

    private boolean getTopNoNegation() {
        return getSuccessor().isTop() && getQuantifier() == Quantifier.Universal;
    }

    @Override
    public void setSuccessor(IClause newSuccessor)
    {
        successor = newSuccessor;
        clauseUpdated();
    }
}
