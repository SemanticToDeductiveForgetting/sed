package pc1.pc2.pc3.om;

import pc1.pc2.pc3.norm.INegationStrategy;
import pc1.pc2.pc3.norm.InPlaceShallowNegationStrategy;

public class CommonFactory implements ICommonFactory
{
    @Override
    public IComplexClause createDisjunctiveClause()
    {
        return new ComplexClause(GroupOperator.DISJUNCTION);
    }

    @Override
    public IAtomicClause createAtomicClause(String text)
    {
        ILiteral literal;
        ISymbolFactory symbolFactory = FactoryMgr.getSymbolFactory();
        literal = symbolFactory.getLiteralForText(text);
        if(literal == null) {
            literal = symbolFactory.createConceptLiteral(text);
            symbolFactory.register(literal);
        }

        assert literal instanceof IConceptLiteral;
        return createAtomicClause((IConceptLiteral) literal);
    }

    @Override
    public IComplexClause createConjunctiveClause()
    {
        return new ComplexClause(GroupOperator.CONJUNCTION);
    }

    @Override
    public IQuantifiedClause createExistentialClause(IRoleLiteral role, IClause expression)
    {
        return new QuantifiedClause(Quantifier.Existential, role, expression);
    }

    @Override
    public IQuantifiedClause createUniversalClause(IRoleLiteral role, IClause expression)
    {
        return new QuantifiedClause(Quantifier.Universal, role, expression);
    }

    @Override
    public IQuantifiedClause createQuantifiedClause(Quantifier quantifier, IRoleLiteral role, IClause expression)
    {
        return new QuantifiedClause(quantifier, role, expression);
    }

    @Override
    public IAtomicClause createAtomicClause(IConceptLiteral literal)
    {
        return new AtomicClause(literal, false);
    }

    @Override
    public IComplexClause createComplexClause(GroupOperator operator)
    {
        return new ComplexClause(operator);
    }

    @Override
    public IAxiom createSubsetAxiom(IClause subsumee, IClause subsumer)
    {
        return new Axiom(subsumee, subsumer, AxiomType.INCLUSION);
    }

    @Override
    public IAxiom createEquivalenceAxiom(IClause left, IClause right)
    {
        return new Axiom(left, right, AxiomType.EQUIVALENCE);
    }

    @Override
    public INegationStrategy createShallowNegationStrategy()
    {
        return new InPlaceShallowNegationStrategy();
    }

    @Override
    public ITimeMgr createTimeMgr(long value)
    {
        return new TimeMgr(value);
    }

    @Override
    public IOntology createOntology()
    {
        return new Ontology();
    }
}
