package pc1.pc2.pc3.utils;

import pc1.pc2.pc3.om.FactoryMgr;
import pc1.pc2.pc3.om.IAxiom;

public class AxiomHelper
{
    public static IAxiom clone(IAxiom axiom)
    {
        return FactoryMgr.getCommonFactory().createSubsetAxiom(ClauseHelper.clone(axiom.getLeft()),
                ClauseHelper.clone(axiom.getRight()));
    }
}
