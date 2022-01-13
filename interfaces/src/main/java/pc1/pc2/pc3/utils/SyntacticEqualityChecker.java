package pc1.pc2.pc3.utils;

import pc1.pc2.pc3.om.IAtomicClause;
import pc1.pc2.pc3.om.IClause;
import pc1.pc2.pc3.om.IComplexClause;
import pc1.pc2.pc3.om.IQuantifiedClause;

public class SyntacticEqualityChecker {

    public boolean checkEqual(IClause first, IClause second) {
        if (first instanceof IAtomicClause && second instanceof IAtomicClause) {
            return compareAtomics((IAtomicClause) first, (IAtomicClause) second);
        } else if (first instanceof IComplexClause && second instanceof IComplexClause) {
            return compareComplex((IComplexClause) first, (IComplexClause) second);
        } else if (first instanceof IQuantifiedClause && second instanceof IQuantifiedClause) {
            return compareQuantified((IQuantifiedClause) first, (IQuantifiedClause) second);
        }
        return false;
    }

    private boolean compareQuantified(IQuantifiedClause c1, IQuantifiedClause c2) {
        return c1.isNegated() == c2.isNegated() &&
                c1.getQuantifier() == c2.getQuantifier() &&
                c1.getRole() == c2.getRole() &&
                checkEqual(c1.getSuccessor(), c2.getSuccessor());
    }

    private boolean compareComplex(IComplexClause c1, IComplexClause c2) {

        if (c1.getChildren().size() == c2.getChildren().size() && c1.isNegated() == c2.isNegated()) {
            for (IClause c1c : c1.getChildren()) {
                boolean equal = false;
                for (IClause c2c : c2.getChildren()) {
                    if(checkEqual(c1c, c2c)) {
                        equal = true;
                        break;
                    }
                }
                if(!equal) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    private boolean compareAtomics(IAtomicClause c1, IAtomicClause c2) {
        return c1.getLiteral() == c2.getLiteral() && c1.isNegated() == c2.isNegated();
    }
}
