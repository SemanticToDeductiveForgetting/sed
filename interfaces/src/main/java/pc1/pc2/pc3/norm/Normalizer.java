package pc1.pc2.pc3.norm;

import pc1.pc2.pc3.utils.SyntacticEqualityChecker;
import pc1.pc2.pc3.om.FactoryMgr;
import pc1.pc2.pc3.om.IAtomicClause;
import pc1.pc2.pc3.om.IClause;
import pc1.pc2.pc3.om.IComplexClause;

import java.util.LinkedList;
import java.util.List;

public class Normalizer
{
    public static IClause negate(IClause clause)
    {
        if(clause instanceof IAtomicClause) {
            ((IAtomicClause) clause).negate();
        }

        return clause;
    }

    public static void addSubClause(IComplexClause clause, IClause child)
    {
        if(child instanceof IComplexClause && ((IComplexClause) child).getOperator() == clause.getOperator() && !child.isNegated()) {
            for (IClause newChild : ((IComplexClause) child).getChildren()) {
                Normalizer.addSubClause(clause, newChild);
            }
        }
        else {
            if(!hasDuplicate(clause, child)) {
                clause.addChild(child);
            }
        }
    }

    private static boolean hasDuplicate(IComplexClause clause, IClause child)
    {
        SyntacticEqualityChecker checker = new SyntacticEqualityChecker();
        for (IClause oldChild : clause.getChildren()) {
            if (checker.checkEqual(oldChild, child)) {
                return true;
            }
        }
        return false;
    }

    public static List<IClause> join(List<IClause> disjunctives, List<IClause> conjunct)
    {
        List<IClause> result = new LinkedList<>();
        for (IClause clause1 : disjunctives) {
            for (IClause clause2 : conjunct) {
                IComplexClause parent = FactoryMgr.getCommonFactory().createDisjunctiveClause();
                Normalizer.addSubClause(parent, clause1);
                Normalizer.addSubClause(parent, clause2);
                result.add(parent);
            }
        }
        return result;
    }
}
