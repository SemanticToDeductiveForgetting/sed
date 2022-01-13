package pc1.pc2.pc3.om;

import pc1.pc2.pc3.norm.INegationStrategy;

public interface ICommonFactory
{
    IComplexClause createDisjunctiveClause();

    IAtomicClause createAtomicClause(String text);

    IComplexClause createConjunctiveClause();

    IQuantifiedClause createExistentialClause(IRoleLiteral role, IClause expression);

    IQuantifiedClause createUniversalClause(IRoleLiteral role, IClause expression);

    IQuantifiedClause createQuantifiedClause(Quantifier quantifier, IRoleLiteral role, IClause newSuccessor);

    IAtomicClause createAtomicClause(IConceptLiteral literal);

    IComplexClause createComplexClause(GroupOperator operator);

    IAxiom createSubsetAxiom(IClause subsumee, IClause subsumer);

    IAxiom createEquivalenceAxiom(IClause left, IClause right);

    INegationStrategy createShallowNegationStrategy();

    ITimeMgr createTimeMgr(long value);

    IOntology createOntology();
}
