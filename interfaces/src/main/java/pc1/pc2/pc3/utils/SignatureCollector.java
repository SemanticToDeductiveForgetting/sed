package pc1.pc2.pc3.utils;

import pc1.pc2.pc3.om.*;
import pc1.pc2.pc3.om.*;
import pc1.pc2.pc3.om.*;
import pc1.pc2.pc3.om.*;

import java.util.HashSet;
import java.util.Set;

public class SignatureCollector implements IClauseVisitor
{
    private final Set<ILiteral> signature;

    public SignatureCollector()
    {
        signature = new HashSet<>();
    }

    @Override public void visitAtomic(IAtomicClause clause)
    {
        signature.add(clause.getLiteral());
    }

    @Override public void visitComplex(IComplexClause clause)
    {
        clause.getChildren().forEach(c -> c.accept(this));
    }

    @Override public void visitQuantified(IQuantifiedClause clause)
    {
        signature.add(clause.getRole());
        clause.getSuccessor().accept(this);
    }

    public Set<ILiteral> getSignature(IClause clause)
    {
        signature.clear();
        clause.accept(this);
        return signature;
    }
}
