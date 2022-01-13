package pc1.pc2.pc3.om;

import pc1.pc2.pc3.om.privileged.IPriviledgedAxiom;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class Axiom implements IAxiom, IPriviledgedAxiom
{
    private IClause left;
    private IClause right;
    private final AxiomType type;

    public Axiom(IClause left, IClause right, AxiomType type)
    {
        this.type = type;
        this.left = left;
        this.right = right;
    }

    @Override
    public IClause getLeft()
    {
        return left;
    }

    @Override
    public IClause getRight()
    {
        return right;
    }

    @Override
    public AxiomType getType()
    {
        return type;
    }

    @Override
    public String toString()
    {
        return String.format("%s %s %s", left.toString(), type.toString(), right.toString());
    }

    @Override
    public Collection<ILiteral> getSignature()
    {
        Set<ILiteral> sig = new HashSet<>(left.getSignature());
        sig.addAll(right.getSignature());
        return sig;
    }

    @Override
    public void setLeft(IClause left)
    {
        this.left = left;
    }

    @Override
    public void setRight(IClause right)
    {
        this.right = right;
    }
}
