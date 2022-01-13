package pc1.pc2.pc3.logic.resolver;

import org.jetbrains.annotations.NotNull;
import pc1.pc2.pc3.logic.helper.ClauseHelper;
import pc1.pc2.pc3.om.*;

import java.util.*;

public class ClauseSubsumptionChecker
{
    public List<IClause> eliminate(List<IClause> result)
    {
        List<IClause> reducedClauses = new LinkedList<>(result);
        List<Pair<IClause, List<ILiteral>>> clauseToSignature = getSignature(result);
        clauseToSignature.sort(Comparator.comparing(p -> p.getSecond().size()));

        for (int i = 0; i < clauseToSignature.size(); i++) {
            Pair<IClause, List<ILiteral>> clause1 = clauseToSignature.get(i);
            for (int j = i + 1; j < clauseToSignature.size(); j++) {
                Pair<IClause, List<ILiteral>> clause2 = clauseToSignature.get(j);
                if (clause2.getSecond().containsAll(clause1.getSecond()) &&
                        subsumes(clause1.getFirst(), clause2.getFirst())) {
                    reducedClauses.remove(clause2.getFirst());
                }
            }
        }
        return reducedClauses;
    }

    public boolean subsumes(@NotNull IClause clause1, @NotNull IClause clause2)
    {
        if(clause1 instanceof IAtomicClause) {
            AtomicSubsumptionChecker checker = new AtomicSubsumptionChecker((IAtomicClause) clause1);
            clause2.accept(checker);
            return checker.subsumes();
        }
        if(clause1 instanceof IComplexClause) {
            IComplexClause subsumer = (IComplexClause) clause1;
            if(subsumer.getOperator() == GroupOperator.DISJUNCTION) {
                boolean subsumes = true;
                for (IClause child : subsumer.getChildren()) {
                    subsumes &= subsumes(child, clause2);
                }
                return subsumes;
            }
            else {
                // it is a conjunction
                ConjunctiveSubsumptionChecker checker = new ConjunctiveSubsumptionChecker(subsumer);
                clause2.accept(checker);
                return checker.subsumes();
            }
        }
        if(clause1 instanceof IQuantifiedClause) {
            IQuantifiedClause subsumer = (IQuantifiedClause) clause1;
            QuantifierSubsumptionChecker checker = new QuantifierSubsumptionChecker(subsumer);
            clause2.accept(checker);
            return checker.subsumes();
        }

        return false;
    }

    @NotNull private List<Pair<IClause, List<ILiteral>>> getSignature(List<IClause> result)
    {
        List<Pair<IClause, List<ILiteral>>> clauseToSignature = new ArrayList<>();
        for (IClause clause : result) {
            clauseToSignature.add(new Pair<>(clause, ClauseHelper
                    .calculateSignature(Collections.singleton(clause), Comparator.comparing(ILiteral::getSymbol))));
        }
        return clauseToSignature;
    }
}
