package pc1.pc2.pc3.om;

public enum Quantifier
{
    Universal("*FORALL*"),
    Existential("*EXISTS*");

    private String value = "";

    Quantifier(String value)
    {
        this.value = value;
    }


    @Override
    public String toString()
    {
        return value;
    }
}
