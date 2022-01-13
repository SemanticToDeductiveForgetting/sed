package pc1.pc2.pc3.om;

public class ConceptLiteral extends Literal implements IConceptLiteral
{
    private final boolean isDefiner;
    private final boolean isExistentialDefiner;

    ConceptLiteral(String symbol, boolean isDefiner, boolean isExistentialDefiner)
    {
        super(symbol);
        this.isDefiner = isDefiner;
        this.isExistentialDefiner = isExistentialDefiner;
    }

    @Override public boolean isDefiner()
    {
        return isDefiner;
    }

    @Override public boolean isExistentialDefiner()
    {
        return isExistentialDefiner;
    }
}
