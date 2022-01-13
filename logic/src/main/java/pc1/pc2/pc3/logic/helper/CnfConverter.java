package pc1.pc2.pc3.logic.helper;

import pc1.pc2.pc3.norm.Normalizer;
import pc1.pc2.pc3.om.*;
import pc1.pc2.pc3.om.privileged.IPrivilegedQuantifiedClause;

import java.util.LinkedList;
import java.util.List;

public class CnfConverter implements IClauseVisitor
{
    private List<IClause> expandedClauses;
    private List<IConceptLiteral> newDefiners;

    public CnfConverter()
    {
    }

    public List<IClause> convert(IClause clause)
    {
        newDefiners = new LinkedList<>();
        expandedClauses = new LinkedList<>();
        clause.accept(this);
        return expandedClauses;
    }

    @Override public void visitAtomic(IAtomicClause clause)
    {
        expandedClauses.add(clause);
    }

    @Override public void visitComplex(IComplexClause clause)
    {
        if (clause.getOperator() == GroupOperator.DISJUNCTION) {
            LinkedList<List<IClause>> dnf = new LinkedList<>();
            for (IClause child : clause.getChildren()) {
                dnf.add(new CnfConverter().convert(child));
            }
            expandedClauses.addAll(distribute(dnf));
        }
        else {
            for (IClause child : clause.getChildren()) {
                expandedClauses.addAll(new CnfConverter().convert(child));
            }
        }
    }

    @Override public void visitQuantified(IQuantifiedClause clause)
    {
        List<IClause> convert = new CnfConverter().convert(clause.getSuccessor());
        if (convert.size() > 1) {
            IComplexClause conjunctiveClause = FactoryMgr.getCommonFactory().createConjunctiveClause();
            convert.forEach(c -> Normalizer.addSubClause(conjunctiveClause, c));
            if (clause instanceof IPrivilegedQuantifiedClause) {
                ((IPrivilegedQuantifiedClause) clause).setSuccessor(conjunctiveClause);
            }
        }
        expandedClauses.add(clause);
    }

    private List<IClause> distribute(LinkedList<List<IClause>> cnfConjuncts)
    {
        List<IClause> cnf = new LinkedList<>();
        if (cnfConjuncts.size() > 1) {
            List<IClause> first = cnfConjuncts.pop();
            List<IClause> subCnf = distribute(cnfConjuncts);
            cnf.addAll(Normalizer.join(first, subCnf));
        }
        else if(cnfConjuncts.size() == 1) {
            return cnfConjuncts.get(0);
        }
        return cnf;
    }
}
