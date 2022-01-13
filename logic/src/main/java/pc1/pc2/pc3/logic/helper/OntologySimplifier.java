package pc1.pc2.pc3.logic.helper;

import pc1.pc2.pc3.om.*;
import pc1.pc2.pc3.utils.AxiomSimplifier;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

public class OntologySimplifier
{
    private final IOntology ontology;

    public OntologySimplifier(IOntology ontology)
    {
        this.ontology = ontology;
    }

    public OntologySimplifier purify(Collection<IConceptLiteral> signature, Collection<IConceptLiteral> purifiedSymbols)
    {
        System.out.println("Purifying symbols");
        System.out.println("Started at " + new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date()));
        Supplier<IAtomicClause> topSupplier =
                () -> FactoryMgr.getCommonFactory().createAtomicClause(IConceptLiteral.TOP);
        Supplier<IAtomicClause> bottomSupplier =
                () -> FactoryMgr.getCommonFactory().createAtomicClause(IConceptLiteral.TOP).negate();
        Collection<IAxiom> axioms = ontology.getAllActiveAxioms();
        Set<IConceptLiteral> positive = new HashSet<>();
        Set<IConceptLiteral> negative = new HashSet<>();
        Set<IConceptLiteral> zero = new HashSet<>();
        PolarityCalculator.getPolarityOfConceptNames(axioms, positive, negative, zero);
        positive.retainAll(signature);
        negative.retainAll(signature);
//        zero.retainAll(signature);
//        for (IAxiom axiom : axioms) {
//            if (axiom.getType() == AxiomType.INCLUSION) {
//                Collection<ILiteral> sigOfAxiom = axiom.getSignature();
//                sigOfAxiom.retainAll(signature);
//                if (!sigOfAxiom.isEmpty()) {
//                    calculatePolarity(positive, negative, -1, axiom.getLeft());
//                    calculatePolarity(positive, negative, 1, axiom.getRight());
//                }
//            }
//            else {
//                zero.addAll(ClauseHelper.getConceptNamesInStatement(axiom));
//            }
//        }
//        positive.retainAll(signature);
//        negative.retainAll(signature);
//        positive.removeAll(zero);
//        negative.removeAll(zero);
//        Set<IConceptLiteral> tempPos = new HashSet<>(positive);
//        Set<IConceptLiteral> tempNeg = new HashSet<>(negative);
//        positive.removeAll(tempNeg);
//        negative.removeAll(tempPos);
        replace(axioms, positive, topSupplier);
        replace(axioms, negative, bottomSupplier);
        purifiedSymbols.addAll(positive);
        purifiedSymbols.addAll(negative);
        System.out.println("Ended at " + new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date()));
        System.out.printf("%d concepts purified%n", positive.size() + negative.size());
        ontology.setContent(axioms);
        return this;
    }

    private void calculatePolarity(Set<IConceptLiteral> positive, Set<IConceptLiteral> negative, int initial,
                                   IClause clause)
    {
        PolarityCalculator leftPolarity = new PolarityCalculator(initial);
        clause.accept(leftPolarity);
        positive.addAll(leftPolarity.getPositive());
        negative.addAll(leftPolarity.getNegative());
    }

    public OntologySimplifier eliminateUnitClausesAndTautologies()
    {
        System.out.println("Eliminating unit clauses and tautologies");
        System.out.println("Started at " + new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date()));
        Collection<IAxiom> allAxioms = ontology.getAllActiveAxioms();
        Supplier<IAtomicClause> topSupplier =
                () -> FactoryMgr.getCommonFactory().createAtomicClause(IConceptLiteral.TOP);
        Supplier<IAtomicClause> bottomSupplier =
                () -> FactoryMgr.getCommonFactory().createAtomicClause(IConceptLiteral.TOP).negate();
        Set<IConceptLiteral> replaceByTop = new HashSet<>();
        Set<IConceptLiteral> replaceByBottom = new HashSet<>();
        Set<IAxiom> removeAxioms = new HashSet<>();
        int changedAxioms = 1;
        while (changedAxioms > 0) {
            for (IAxiom axiom : allAxioms) {
                if (axiom.getType() == AxiomType.INCLUSION) {
                    if (axiom.getRight() instanceof IAtomicClause) {
                        IAtomicClause right = (IAtomicClause) axiom.getRight();
                        if (right.isTop()) {
                            removeAxioms.add(axiom);
                        }
                        else if (right.isBottom() && axiom.getLeft() instanceof IAtomicClause) {
                            replaceByBottom.add(((IAtomicClause) axiom.getLeft()).getLiteral());
                            removeAxioms.add(axiom);
                        }
                    }
                    else if (axiom.getLeft() instanceof IAtomicClause) {
                        IAtomicClause left = (IAtomicClause) axiom.getLeft();
                        if (left.isTop() && axiom.getRight() instanceof IAtomicClause) {
                            replaceByTop.add(((IAtomicClause) axiom.getRight()).getLiteral());
                            removeAxioms.add(axiom);
                        }
                        else if (left.isBottom()) {
                            removeAxioms.add(axiom);
                        }
                    }
                }
            }
            changedAxioms = removeAxioms.size() + replaceByTop.size() + replaceByBottom.size();
            System.out.println("Ended at " + new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date()));
            System.out.printf("%d tautology axioms removed.%n", removeAxioms.size());
            allAxioms.removeAll(removeAxioms);
            removeAxioms.clear();
            System.out.printf("%d concepts replaced by TOP.%n", replaceByTop.size());
            replace(allAxioms, replaceByTop, topSupplier);
            replaceByTop.clear();
            System.out.printf("%d concepts replaced by Bottom.%n", replaceByBottom.size());
            replace(allAxioms, replaceByBottom, bottomSupplier);
            replaceByBottom.clear();
            ontology.setContent(allAxioms);
        }
        return this;
    }

    public OntologySimplifier simplifyAxioms()
    {
        Collection<IAxiom> simplifiedAxioms = new AxiomSimplifier().simplify(ontology.getAllActiveAxioms());
        ontology.setContent(simplifiedAxioms);
        return this;
    }

    private void replace(Collection<IAxiom> allAxioms, Set<IConceptLiteral> replace,
                         Supplier<IAtomicClause> by)
    {
        Set<IAxiom> newAxioms = new HashSet<>();
        Set<IAxiom> removeAxioms = new HashSet<>();
        for (IConceptLiteral literal : replace) {
            for (IAxiom axiom : allAxioms) {
                if (axiom.getSignature().contains(literal)) {
                    IClause newLeft = ClauseHelper.replace(axiom.getLeft(), literal, by);
                    IClause newRight = ClauseHelper.replace(axiom.getRight(), literal, by);
                    if (axiom.getType() == AxiomType.INCLUSION) {
                        newAxioms.add(FactoryMgr.getCommonFactory().createSubsetAxiom(newLeft, newRight));
                    }
                    else if (axiom.getType() == AxiomType.EQUIVALENCE) {
                        newAxioms.add(FactoryMgr.getCommonFactory().createEquivalenceAxiom(newLeft, newRight));
                    }
                    removeAxioms.add(axiom);
                }
            }
        }
        allAxioms.removeAll(removeAxioms);
        allAxioms.addAll(newAxioms);
    }
}
