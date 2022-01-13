package pc1.pc2.pc3.utils;

import pc1.pc2.pc3.om.*;
import pc1.pc2.pc3.om.*;
import pc1.pc2.pc3.om.*;
import pc1.pc2.pc3.om.*;

import java.util.Collection;
import java.util.Set;

public class UnquantifiedRestrictedSignatureCollector extends SignatureCollector implements IClauseVisitor
{
    private final Collection<IConceptLiteral> retainSig;

    public UnquantifiedRestrictedSignatureCollector(Collection<IConceptLiteral> retainSig)
    {
        this.retainSig = retainSig;
    }

    @Override
    public void visitQuantified(IQuantifiedClause clause)
    {
    }

    @Override
    public Set<ILiteral> getSignature(IClause clause)
    {
        Set<ILiteral> signature = super.getSignature(clause);
        signature.retainAll(retainSig);
        return signature;
    }
}
