package pc1.pc2.pc3.logic.helper;

import pc1.pc2.pc3.om.*;
import pc1.pc2.pc3.om.privileged.IPrivilegedQuantifiedClause;

import java.util.*;

public class TautologyEliminator implements IClauseVisitor

{
    boolean tautologyDetected = false;

    @Override
    public void visitAtomic(IAtomicClause clause)
    {
        tautologyDetected = clause.getLiteral() == IConceptLiteral.TOP && !clause.isNegated();
    }

    @Override
    public void visitComplex(IComplexClause clause)
    {
        if(clause.getOperator() == GroupOperator.DISJUNCTION) {
            clause.getChildren().removeIf(IClause::isBottom);
        }
        if(clause.getOperator() == GroupOperator.DISJUNCTION) {
            Map<String, Integer> polarity = new HashMap<>();
            for (Iterator<IClause> iterator = clause.getChildren().iterator(); iterator.hasNext(); ) {
                IClause child = iterator.next();
                if(child instanceof IAtomicClause) {
                    IAtomicClause atomic = (IAtomicClause) child;
                    int sign = atomic.isNegated() ? -1 : 1;
                    Integer existingPolarity = polarity.getOrDefault(atomic.getLiteral().getSymbol(), 0);
                    if(existingPolarity == -sign) {
                        tautologyDetected = true;
                        return;
                    }
                    else if(existingPolarity == sign) {
                        iterator.remove();
                    }
                    else {
                        polarity.put(atomic.getLiteral().getSymbol(), sign);
                    }
                }
            }
        }
        for (Iterator<IClause> iterator = clause.getChildren().iterator(); iterator.hasNext(); ) {
            IClause child = iterator.next();
            child.accept(this);
            if (tautologyDetected) {
                if (clause.getOperator() == GroupOperator.CONJUNCTION) {
                    iterator.remove();
                    tautologyDetected = false;
                }
                else {
                    tautologyDetected = true;
                    break;
                }
            }
        }
    }

    @Override
    public void visitQuantified(IQuantifiedClause clause)
    {
        clause.getSuccessor().accept(this);
        if (tautologyDetected && clause instanceof IPrivilegedQuantifiedClause) {
            ((IPrivilegedQuantifiedClause) clause)
                    .setSuccessor(FactoryMgr.getCommonFactory().createAtomicClause(IConceptLiteral.TOP));
            if(clause.getQuantifier() == Quantifier.Existential) {
                tautologyDetected = false;
            }
        }
    }

    public void eliminateTautologies(Collection<IClause> ontology)
    {
        for (Iterator<IClause> iterator = ontology.iterator(); iterator.hasNext(); ) {
            IClause clause = iterator.next();
            clause.accept(this);
            if(tautologyDetected) {
                iterator.remove();
            }
        }
    }
}
