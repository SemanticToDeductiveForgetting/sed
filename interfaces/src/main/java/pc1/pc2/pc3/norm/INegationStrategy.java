package pc1.pc2.pc3.norm;

import pc1.pc2.pc3.om.IClause;

public interface INegationStrategy
{
    /**
     * Negates a clause.
     * @param clause the clause to be negated
     * @return the negated clause
     */
    IClause negate(IClause clause);
}
