package pc1.pc2.pc3.logic.resolver;

import org.jetbrains.annotations.NotNull;
import pc1.pc2.pc3.om.IConceptLiteral;
import pc1.pc2.pc3.om.ILiteral;
import pc1.pc2.pc3.logic.om.ResolvableClause;
import pc1.pc2.pc3.logic.om.SignatureMgr;

import java.util.*;

public class MatrixResolver
{
    private SignatureMgr signatureMgr;
    private List<IConceptLiteral> definers;

    public MatrixResolver(SignatureMgr signatureMgr, List<IConceptLiteral> definers)
    {
        this.signatureMgr = signatureMgr;
        this.definers = definers;
    }

    public List<ResolvableClause> resolve(ResolvableClause clause, List<ResolvableClause> list,
                                          ILiteral resolutionSymbol, RankChecker rankChecker,
                                          boolean restrictResolution)
    {
        List<ResolvableClause> forgettingSolution = new LinkedList<>();
        for (ResolvableClause secondClause : list) {
            boolean hasOppositeLiterals = areResolvable(clause, secondClause, resolutionSymbol, restrictResolution);
            boolean satisfyRank = rankChecker.satisfiesRank(secondClause);
            tryResolve(clause, secondClause, forgettingSolution, resolutionSymbol, hasOppositeLiterals, satisfyRank);
        }
        return forgettingSolution;
    }

    protected void tryResolve(ResolvableClause clause, ResolvableClause secondClause,
                                 List<ResolvableClause> forgettingSolution,
                                 ILiteral resolutionSymbol, boolean hasOppositeLiterals,
                                 boolean satisfyRank)
    {
        if(hasOppositeLiterals && satisfyRank) {
            ResolvableClause resolvedClause = resolve(clause, secondClause, resolutionSymbol);
            if (!resolvedClause.isTautology()) {
                forgettingSolution.add(resolvedClause);
            }
        }
    }

    private boolean areResolvable(ResolvableClause clause1, ResolvableClause clause2, ILiteral resolutionSymbol,
                                  boolean restrictResolution)
    {
        if (clause1.isBottom() || clause2.isBottom() || clause1.isTautology() || clause2.isTautology()) {
            return false;
        }
        int polarity1 = clause1.getPolarityOfLiteral((IConceptLiteral) resolutionSymbol);
        int polarity2 = clause2.getPolarityOfLiteral((IConceptLiteral) resolutionSymbol);

        Set<IConceptLiteral> negativeDefiners = new HashSet<>();
        if (restrictResolution) {
            for (IConceptLiteral definer : definers) {
                if (clause1.getPolarityOfLiteral(definer) == -1 || clause2.getPolarityOfLiteral(definer) == -1) {
                    negativeDefiners.add(definer);
                }
            }
        }
        return polarity1 == -polarity2 && polarity1 != 0 && negativeDefiners.size() <= 1;
    }

    @NotNull
    public ResolvableClause resolve(ResolvableClause first, ResolvableClause second, ILiteral resolutionSymbol)
    {
        if (!signatureMgr.contains(resolutionSymbol)) {
            throw new IllegalArgumentException(
                    String.format("Resolution symbol (%s) is not in signature manager", resolutionSymbol.getSymbol()));
        }

        int index = signatureMgr.indexOf(resolutionSymbol);
        int[] polarity1 = first.getPolarity();
        int[] polarity2 = second.getPolarity();

        for (int i = 0; i < polarity1.length && i < polarity2.length; i++) {
            if (i != index && polarity1[i] * polarity2[i] == -1) {
                int[] polarity = new int[signatureMgr.getSize()];
                Arrays.fill(polarity, 0);
                polarity[signatureMgr.indexOf(IConceptLiteral.TOP)] = 1;
                return new ResolvableClause(signatureMgr, polarity);
            }
        }


        int[] polarityOfNewClause = Arrays.copyOf(polarity1, signatureMgr.getSignatureLiterals().size());
        for (int i = 0; i < polarity2.length; i++) {
            if (polarity2[i] != polarityOfNewClause[i]) {
                polarityOfNewClause[i] += polarity2[i];
            }
        }

        ResolvableClause resolvableClause = new ResolvableClause(signatureMgr,
                polarityOfNewClause);
        first.getQuantifiedParts().forEach(resolvableClause::addQuantifiedClause);
        second.getQuantifiedParts().forEach(resolvableClause::addQuantifiedClause);

        if (Arrays.stream(polarityOfNewClause).allMatch(i -> i == 0)) {
            if (resolvableClause.getQuantifiedParts().isEmpty()) {
                int[] polarityArray = new int[signatureMgr.getSize()];
                Arrays.fill(polarityArray, 0);
                polarityArray[signatureMgr.indexOf(IConceptLiteral.TOP)] = -1;
                return new ResolvableClause(signatureMgr, polarityArray);
            }
            else if (resolvableClause.getQuantifiedParts().size() == 1) {
                return resolvableClause.getQuantifiedParts().get(0);
            }
        }

        return resolvableClause;
    }
}
