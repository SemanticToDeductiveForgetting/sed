package pc1.pc2.pc3.norm;

import pc1.pc2.pc3.om.IClause;
import pc1.pc2.pc3.om.privileged.IPrivilegedClause;

public class InPlaceShallowNegationStrategy implements INegationStrategy
{
    @Override
    public IClause negate(IClause clause)
    {
        if(clause instanceof IPrivilegedClause) {
            ((IPrivilegedClause) clause).setNegated(!clause.isNegated());
        }
        return clause;
    }
}
