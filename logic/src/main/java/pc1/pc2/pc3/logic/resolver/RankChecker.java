package pc1.pc2.pc3.logic.resolver;

import pc1.pc2.pc3.om.IConceptLiteral;
import pc1.pc2.pc3.utils.CollectionUtils;
import pc1.pc2.pc3.logic.factory.DefinerFactory;
import pc1.pc2.pc3.logic.om.ResolvableClause;

import java.util.Collection;

public class RankChecker
{
    private final int rank1;
    private final int rank2;
    private final Collection<IConceptLiteral> forgettingSignature;
    private final DefinerFactory definerFactory;
    private int rankOfFirstClause;

    public RankChecker(int rank1, int rank2, Collection<IConceptLiteral> forgettingSignature, DefinerFactory definerFactory)
    {
        this.rank1 = rank1;
        this.rank2 = rank2;
        this.forgettingSignature = forgettingSignature;
        this.definerFactory = definerFactory;
    }

    public void setMainClause(ResolvableClause mainClause)
    {
        rankOfFirstClause = calculateRank(mainClause);
    }

    private int calculateRank(ResolvableClause clause)
    {
        return CollectionUtils.merge(forgettingSignature, definerFactory.getDefiners()).stream()
                .mapToInt(literal -> Math.abs(clause.getPolarityOfLiteral(literal,true)))
                .sum();
    }

    public boolean satisfiesRank(ResolvableClause secondClause)
    {
        int rankOfSecondClause = calculateRank(secondClause);
        return (rankOfFirstClause >= rank1 && rankOfSecondClause >= rank2) ||
                (rankOfFirstClause >= rank2 && rankOfSecondClause >= rank1);
    }
}
