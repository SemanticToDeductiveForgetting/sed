package pc1.pc2.pc3.utils;

import pc1.pc2.pc3.om.IClause;

public class ClauseHelper
{
    public static IClause clone(IClause axiom)
    {
        CloneVisitor visitor = new CloneVisitor();
        axiom.accept(visitor);
        return visitor.getClonedClause();
    }
}
