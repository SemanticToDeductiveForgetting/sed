package pc1.pc2.pc3.om;

public enum GroupOperator
{
    CONJUNCTION {
        @Override public String toString()
        {
            return "&";
        }
    },
    DISJUNCTION {
        @Override public String toString()
        {
            return "|";
        }
    }
}
