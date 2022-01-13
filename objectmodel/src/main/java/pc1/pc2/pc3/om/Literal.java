package pc1.pc2.pc3.om;

public class Literal implements ILiteral
{
    private final String symbol;

    protected Literal(String symbol)
    {
        this.symbol = symbol;
    }

    @Override
    public String getSymbol()
    {
        return symbol;
    }


    @Override
    public String toString()
    {
        return getSymbol();
    }
}
