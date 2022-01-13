package pc1.pc2.pc3.logic.factory;

import org.jetbrains.annotations.NotNull;
import pc1.pc2.pc3.om.*;
import pc1.pc2.pc3.logic.om.ResolvableClause;
import pc1.pc2.pc3.logic.om.SignatureMgr;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class LogicFactory
{
    private SignatureMgr signatureMgr;

    public LogicFactory(SignatureMgr signatureMgr)
    {
        this.signatureMgr = signatureMgr;
    }

    @NotNull public ResolvableClause createResolvable(IClause clause)
    {
        List<IAtomicClause> atomics = new LinkedList<>();
        List<IQuantifiedClause> quantified = new LinkedList<>();
        LinkedList<IClause> parts = new LinkedList<>();
        parts.add(clause);
        while (!parts.isEmpty()) {
            IClause first = parts.removeFirst();
            if (first instanceof IAtomicClause) {
                atomics.add((IAtomicClause) first);
            }
            else if (first instanceof IComplexClause) {
                parts.addAll(((IComplexClause) first).getChildren());
            }
            else {
                quantified.add((IQuantifiedClause) first);
            }
        }

        if (atomics.isEmpty() && quantified.size() == 1) {
            return createQuantifiedResolvable(quantified.get(0));
        }

        int[] polarity = calculatePolarity(atomics);
        ResolvableClause topResolvable = new ResolvableClause(signatureMgr, polarity);
        quantified.stream().map(this::createQuantifiedResolvable).forEach(topResolvable::addQuantifiedClause);

        return topResolvable;
    }

    @NotNull private ResolvableClause createQuantifiedResolvable(IQuantifiedClause quantifiedClause)
    {
        ResolvableClause subResolvable = createResolvable(quantifiedClause.getSuccessor());
        subResolvable.setQuantifier(quantifiedClause.getQuantifier());
        subResolvable.setRole(quantifiedClause.getRole());
        return subResolvable;
    }

    /**
     * Given a list of atomics, this method will calculate an array of polarities
     * ordered by the signature member variable. This corresponds to building a row in a matrix
     * where each cell is either -1 (negative), 0 (does not exist in clause), 1 (positive).
     *
     * @param atomics a list of atomics
     * @return an array of -1, 0, 1 representing the polarity of literals in a clause
     */
    @NotNull private int[] calculatePolarity(List<IAtomicClause> atomics)
    {
        int[] polarity = new int[signatureMgr.getSignatureLiterals().size()];
        Arrays.fill(polarity, 0);
        atomics.forEach(a -> polarity[signatureMgr.indexOf(a.getLiteral())] = a.isNegated() ? -1 : 1);
        return polarity;
    }

    @NotNull public IClause createClause(ResolvableClause resolvable)
    {
        if (resolvable.getQuantifier() == null) {
            return createDisjunctive(resolvable);
        }
        else {
            IClause clause = createDisjunctive(resolvable);
            return FactoryMgr.getCommonFactory()
                    .createQuantifiedClause(resolvable.getQuantifier(), resolvable.getRole(), clause);
        }
    }

    @NotNull private IClause createDisjunctive(ResolvableClause resolvable)
    {
        IComplexClause clause = FactoryMgr.getCommonFactory().createDisjunctiveClause();
        int[] polarity = resolvable.getPolarity();
        for (int i = 0; i < resolvable.getPolarity().length; i++) {
            if (polarity[i] != 0) {
                IAtomicClause atomic = FactoryMgr.getCommonFactory().createAtomicClause(
                        (IConceptLiteral) signatureMgr.get(i));
                if (polarity[i] == -1) {
                    atomic.negate();
                }
                clause.addChild(atomic);
            }
        }
        resolvable.getQuantifiedParts().stream()
                .map(this::createClause)
                .forEach(clause::addChild);

        if (clause.getChildren().size() == 1) {
            return clause.getChildren().iterator().next();
        }

        return clause;
    }
}
