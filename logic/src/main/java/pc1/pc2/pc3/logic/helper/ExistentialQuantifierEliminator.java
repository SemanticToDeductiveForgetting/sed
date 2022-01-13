package pc1.pc2.pc3.logic.helper;

import pc1.pc2.pc3.om.*;
import pc1.pc2.pc3.om.privileged.IPrivilegedQuantifiedClause;

import java.util.List;

public class ExistentialQuantifierEliminator implements IClauseVisitor
{
    private IClause parent = null;
    private boolean emptyQuantifierFound = false;

    @Override public void visitAtomic(IAtomicClause clause)
    {

    }

    @Override public void visitComplex(IComplexClause clause)
    {
        IClause oldParent = parent;
        parent = clause;
        clause.getChildren().forEach(child -> child.accept(this));
        parent = oldParent;

        if (clause.getChildren().isEmpty() || clause.isBottom()) {
            replace(clause);
        }
    }

    @Override public void visitQuantified(IQuantifiedClause clause)
    {
        IClause oldParent = parent;
        parent = clause;
        clause.getSuccessor().accept(this);
        parent = oldParent;

        if (clause.getSuccessor().isBottom() && clause.getQuantifier() == Quantifier.Existential) {
            replace(clause);
        }
    }

    private void replace(IClause clause)
    {
        if (parent instanceof IComplexClause) {
            ((IComplexClause) parent).removeChild(clause);
        }
        else if (parent instanceof IPrivilegedQuantifiedClause) {
            ((IPrivilegedQuantifiedClause) parent)
                    .setSuccessor(FactoryMgr.getCommonFactory().createAtomicClause(IConceptLiteral.TOP).negate());
        }
    }

    public void eliminate(List<IClause> ontology)
    {
        for (IClause clause : ontology) {
            clause.accept(this);
        }
        //ontology.forEach(clause -> clause.accept(this));
        boolean emptyClauseFound = ontology.removeIf(
                clause -> clause instanceof IComplexClause && ((IComplexClause) clause).getChildren().isEmpty());
        emptyClauseFound |= ontology.removeIf(clause -> clause instanceof IQuantifiedClause &&
                ((IQuantifiedClause) clause).getQuantifier() == Quantifier.Existential &&
                ((IQuantifiedClause) clause).getSuccessor().isBottom());
        if (emptyClauseFound) {
            ontology.add(FactoryMgr.getCommonFactory().createAtomicClause(IConceptLiteral.TOP).negate());
        }

        for (int i = 0; i < ontology.size(); i++) {
            IClause clause = ontology.get(i);
            if (clause instanceof IComplexClause && ((IComplexClause) clause).getChildren().size() == 1) {
                ontology.set(i, ((IComplexClause) clause).getChildren().iterator().next());
            }
        }
    }
}
