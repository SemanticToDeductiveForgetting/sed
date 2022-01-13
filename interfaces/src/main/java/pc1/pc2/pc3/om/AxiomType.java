package pc1.pc2.pc3.om;

public enum AxiomType
{
    INCLUSION("->"),
    EQUIVALENCE("=");

    private String value = "";

    AxiomType(String value)
    {
        this.value = value;
    }


    @Override
    public String toString()
    {
        return value;
    }
}
