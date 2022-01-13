package pc1.pc2.pc3.logic.resolver;

import pc1.pc2.pc3.error.TimeException;
import pc1.pc2.pc3.logic.factory.DefinerFactory;
import pc1.pc2.pc3.logic.helper.ClauseHelper;
import pc1.pc2.pc3.norm.Normalizer;
import pc1.pc2.pc3.om.*;
import pc1.pc2.pc3.utils.AxiomSimplifier;

import java.util.Collection;
import java.util.Collections;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class ALCResolver
{

    private final DefinerFactory factory;

    public ALCResolver(DefinerFactory factory)
    {
        this.factory = factory;
    }

    public long resolve(Collection<IAxiom> axioms, IConceptLiteral forgettingSymbol,
                        IOntology outOntology) throws TimeException
    {
        try {
            Collection<IAxiom> axiomsWithSymbol = axioms;
            Collection<IAxiom> structurallyTransformed =
                    applyStructuralTransformation(axiomsWithSymbol, forgettingSymbol);
            axiomsWithSymbol = getAxiomsWithSymbol(structurallyTransformed, forgettingSymbol);
            structurallyTransformed.removeAll(axiomsWithSymbol);
            outOntology.addAxioms(structurallyTransformed);
            FactoryMgr.getTimeMgr().checkTime();

            Supplier<IAtomicClause> top = () -> FactoryMgr.getCommonFactory().createAtomicClause(IConceptLiteral.TOP);
            Supplier<IAtomicClause> bot =
                    () -> FactoryMgr.getCommonFactory().createAtomicClause(IConceptLiteral.TOP).negate();

            Collection<IAxiom> positive = simplify(replaceWith(duplicate(axiomsWithSymbol), forgettingSymbol, top));
            Collection<IAxiom> negative = simplify(replaceWith(duplicate(axiomsWithSymbol), forgettingSymbol, bot));
            if (!shouldDefer(positive, negative, forgettingSymbol)) {
                return or(positive, negative, outOntology);
            }
            else {
                outOntology.addAxioms(axiomsWithSymbol);
                return -1;
            }
        } catch (OutOfMemoryError error) {
            System.out.printf("Out of memory error: Memory size now is: %d bytes\n",
                    Runtime.getRuntime().totalMemory() - Runtime.getRuntime()
                            .freeMemory());
            throw error;
        }
    }

    protected boolean shouldDefer(Collection<IAxiom> positive, Collection<IAxiom> negative, IConceptLiteral symbol)
    {
        return false;
    }

    private Collection<IAxiom> applyStructuralTransformation(Collection<IAxiom> axioms,
                                                             IConceptLiteral forgettingSymbol)
    {
        return axioms.stream()
                .flatMap(a -> new StructuralTransformation(factory, forgettingSymbol).apply(a).stream())
                .collect(Collectors.toSet());
    }

    private Collection<IAxiom> getAxiomsWithSymbol(Collection<IAxiom> axioms, IConceptLiteral forgettingSymbol)
    {
        return axioms.stream()
                .filter(a -> a.getSignature().contains(forgettingSymbol))
                .collect(Collectors.toSet());
    }

    private Collection<IAxiom> replaceWith(Collection<IAxiom> axioms, IConceptLiteral symbol,
                                           Supplier<IAtomicClause> replaceBy)
    {
        ICommonFactory factory = FactoryMgr.getCommonFactory();
        return axioms.stream()
                .map(a -> factory.createSubsetAxiom(ClauseHelper.replace(a.getLeft(), symbol, replaceBy),
                        ClauseHelper.replace(a.getRight(), symbol, replaceBy)))
                .collect(Collectors.toSet());
    }

    private Collection<IAxiom> simplify(Collection<IAxiom> axioms)
    {
        return new AxiomSimplifier().simplify(axioms);
    }

    private long or(Collection<IAxiom> first, Collection<IAxiom> second,
                    IOntology outOntology) throws TimeException
    {
        // Collection<IAxiom> result = new LinkedHashSet<>();
        long newAxioms = 0;
        outOntology.begin();
        for (IAxiom firstAxiom : first) {
            for (IAxiom secondAxiom : second) {
                FactoryMgr.getTimeMgr().checkTime();
                IComplexClause left = FactoryMgr.getCommonFactory().createConjunctiveClause();
                IComplexClause right = FactoryMgr.getCommonFactory().createDisjunctiveClause();
                Normalizer.addSubClause(left, firstAxiom.getLeft());
                Normalizer.addSubClause(left, secondAxiom.getLeft());
                Normalizer.addSubClause(right, firstAxiom.getRight());
                Normalizer.addSubClause(right, secondAxiom.getRight());
                IAxiom newAxiom = FactoryMgr.getCommonFactory().createSubsetAxiom(left, right);
                Collection<IAxiom> simplifiedAxiom = simplify(Collections.singleton(newAxiom));
                newAxioms += simplifiedAxiom.size();
                outOntology.addAxioms(simplifiedAxiom);
            }
        }
        outOntology.end();
        return newAxioms;
    }

    private Collection<IAxiom> duplicate(Collection<IAxiom> axioms)
    {
        ICommonFactory factory = FactoryMgr.getCommonFactory();
        return axioms.stream()
                .map(a -> factory.createSubsetAxiom(pc1.pc2.pc3.utils.ClauseHelper.clone(a.getLeft()),
                        pc1.pc2.pc3.utils.ClauseHelper.clone(a.getRight())))
                .collect(Collectors.toList());
    }
}
