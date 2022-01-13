package pc1.pc2.pc3.om;

public interface IConceptLiteral extends ILiteral
{
    IConceptLiteral TOP = new IConceptLiteral()
    {
        @Override public boolean isExistentialDefiner()
        {
            return false;
        }

        @Override public boolean isDefiner()
        {
            return false;
        }

        @Override public String getSymbol()
        {
            return "*TOP*";
        }
    };

    boolean isDefiner();
    boolean isExistentialDefiner();
}
