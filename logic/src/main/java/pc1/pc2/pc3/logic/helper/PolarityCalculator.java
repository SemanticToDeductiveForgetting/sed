package pc1.pc2.pc3.logic.helper;

import pc1.pc2.pc3.om.*;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class PolarityCalculator implements IClauseVisitor
{
    private final Set<IConceptLiteral> positive;
    private final Set<IConceptLiteral> negative;
    private int polarity;

    public PolarityCalculator(int polarity)
    {
        this.polarity = polarity;
        positive = new HashSet<>();
        negative = new HashSet<>();
    }

    public PolarityCalculator(int polarity, Set<IConceptLiteral> pos, Set<IConceptLiteral> neg)
    {
        this.polarity = polarity;
        positive = pos;
        negative = neg;
    }

    public static void getPolarityOfConceptNames(IAxiom axiom,
                                                 Set<IConceptLiteral> outPositive,
                                                 Set<IConceptLiteral> outNegative,
                                                 Set<IConceptLiteral> outZero)
    {
        if (axiom.getType() == AxiomType.INCLUSION) {
            axiom.getLeft().accept(new PolarityCalculator(-1, outPositive, outNegative));
            axiom.getRight().accept(new PolarityCalculator(1, outPositive, outNegative));
            Set<IConceptLiteral> zero = new HashSet<>(outPositive);
            zero.retainAll(outNegative);
            outZero.addAll(zero);
            outPositive.removeAll(outZero);
            outNegative.removeAll(outZero);
        }
        else {
            outZero.addAll(ClauseHelper.getConceptNamesInStatement(axiom));
        }
    }

    public static void getPolarityOfConceptNames(Collection<IAxiom> axioms,
                                                 Set<IConceptLiteral> outPositive,
                                                 Set<IConceptLiteral> outNegative,
                                                 Set<IConceptLiteral> outZero)
    {
        for (IAxiom axiom : axioms) {
            getPolarityOfConceptNames(axiom, outPositive, outNegative, outZero);
        }
    }

    @Override
    public void visitAtomic(IAtomicClause clause)
    {
        if (clause.isNegated()) {
            polarity *= -1;
        }

        if (clause.getLiteral() != IConceptLiteral.TOP) {
            if (polarity > 0) {
                positive.add(clause.getLiteral());
            }
            else {
                negative.add(clause.getLiteral());
            }
        }
    }

    public Set<IConceptLiteral> getPositive()
    {
        return positive;
    }

    public Set<IConceptLiteral> getNegative()
    {
        return negative;
    }

    @Override
    public void visitComplex(IComplexClause clause)
    {
        if (clause.isNegated()) {
            polarity *= -1;
        }

        for (IClause child : clause.getChildren()) {
            PolarityCalculator calc = new PolarityCalculator(polarity, positive, negative);
            child.accept(calc);
//            positive.addAll(calc.getPositive());
//            negative.addAll(calc.getNegative());
        }
    }

    @Override
    public void visitQuantified(IQuantifiedClause clause)
    {
        if (clause.isNegated()) {
            polarity *= -1;
        }
        PolarityCalculator calc = new PolarityCalculator(polarity, positive, negative);
        clause.getSuccessor().accept(calc);
//        positive.addAll(calc.getPositive());
//        negative.addAll(calc.getNegative());
    }
}
