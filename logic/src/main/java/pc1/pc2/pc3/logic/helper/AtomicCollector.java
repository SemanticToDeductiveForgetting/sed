package pc1.pc2.pc3.logic.helper;

import pc1.pc2.pc3.om.IAtomicClause;
import pc1.pc2.pc3.om.IClauseVisitor;
import pc1.pc2.pc3.om.IComplexClause;
import pc1.pc2.pc3.om.IQuantifiedClause;

import java.util.LinkedList;
import java.util.List;

public class AtomicCollector implements IClauseVisitor
{
    private List<IAtomicClause> atomics = new LinkedList<>();

    @Override
    public void visitAtomic(IAtomicClause clause)
    {
        atomics.add(clause);
    }

    @Override
    public void visitComplex(IComplexClause clause)
    {
        clause.getChildren().forEach(child -> child.accept(this));
    }

    @Override
    public void visitQuantified(IQuantifiedClause clause)
    {
        clause.getSuccessor().accept(this);
    }

    public List<IAtomicClause> getAtomics()
    {
        return atomics;
    }
}
