package pc1.pc2.pc3.om;

import pc1.pc2.pc3.om.privileged.IPrivilegedClause;
import pc1.pc2.pc3.utils.ClauseStringifier;
import pc1.pc2.pc3.utils.SignatureCollector;

import java.util.Collection;

public abstract class Clause implements IClause, IPrivilegedClause
{
    private boolean negative;
    Collection<ILiteral> signature;

    @Override
    public boolean isNegated()
    {
        return negative;
    }

    @Override
    public void setNegated(boolean negated)
    {
        negative = negated;
    }

    @Override
    public String toString()
    {
        return new ClauseStringifier().asString(this);
    }

    @Override
    public Collection<ILiteral> getSignature()
    {
        // changes in child clauses does not reflect in this clause because there is no "parent" link between child
        // and parent clauses.
//        if (signature == null) {
        SignatureCollector sigCollector = new SignatureCollector();
        signature = sigCollector.getSignature(this);
//        }
        signature.remove(IConceptLiteral.TOP);
        return signature;
    }

    protected void clauseUpdated()
    {
        signature = null;
    }
}
