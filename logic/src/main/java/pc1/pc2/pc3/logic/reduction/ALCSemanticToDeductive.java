package pc1.pc2.pc3.logic.reduction;

import pc1.pc2.pc3.logic.helper.CyclicDefinerIdentifier;
import pc1.pc2.pc3.norm.InPlaceShallowNegationStrategy;
import pc1.pc2.pc3.norm.Normalizer;
import pc1.pc2.pc3.om.*;
import pc1.pc2.pc3.logic.factory.DefinerFactory;
import pc1.pc2.pc3.logic.helper.ClausalConverter;
import pc1.pc2.pc3.logic.helper.ClauseHelper;
import pc1.pc2.pc3.logic.helper.PolarityCalculator;
import pc1.pc2.pc3.om.*;
import pc1.pc2.pc3.om.*;
import pc1.pc2.pc3.om.*;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ALCSemanticToDeductive
{

    private Collection<IComplexClause> delta = null;

    public void reduceToALC(IOntology ontology, Collection<IConceptLiteral> forgettingSig,
                            DefinerFactory factory)
    {
        Collection<IClause> clausalForm = new ClausalConverter(factory).toClausalForm(ontology, forgettingSig);
        CyclicDefinerIdentifier cyclicIdentifier = new CyclicDefinerIdentifier();
        Collection<IConceptLiteral> cyclicDefiners = cyclicIdentifier.getCyclicDefiners(ontology.getAllActiveAxioms());
        delta = extractDeltaClauses(clausalForm);
        Map<IComplexClause, Collection<IConceptLiteral>> pConcepts =
                mapToNegativeNonCyclicDefiners(cyclicDefiners, delta);
        Collection<IComplexClause> param1Clauses = filterAtMostOneExistentialDefiner(delta, pConcepts);
        final ICommonFactory commonFact = FactoryMgr.getCommonFactory();
        System.out.printf("Semantic view is %d clauses\n", clausalForm.size());
        clausalForm.removeAll(delta);
        clausalForm.addAll(param1Clauses);
        System.out.printf("Perform Role Propagatiom using %d clauses after excluding %d Delta clauses and restoring " +
                        "%d param1 clauses\n",
                clausalForm.size(), delta.size(), param1Clauses.size());
        System.out.flush();
        for (IComplexClause param1 : param1Clauses) {
            Collection<IConceptLiteral> negativeDefiners = pConcepts.get(param1);
            Collection<IRoleLiteral> roles = getRoles(negativeDefiners, factory);
            if (roles.size() == 1) {
                Collection<IClause> param2 = getClausesWhoseNegativeDefinersAreSubsetOf(negativeDefiners,
                        clausalForm, pConcepts);
                param2.remove(param1);
                Collection<IClause> param4 = getClausesWithPositiveDefiners(negativeDefiners, clausalForm);
                IComplexClause successor = commonFact.createConjunctiveClause();
                Normalizer.addSubClause(successor, extractRemainder(param1, negativeDefiners));
                param2.forEach(param -> Normalizer.addSubClause(successor, extractRemainder(param, negativeDefiners)));
                IComplexClause newClause = commonFact.createDisjunctiveClause();
                param4.forEach(
                        clause -> Normalizer.addSubClause(newClause, extractRemainder(clause, negativeDefiners)));
                if (negativeDefiners.stream().anyMatch(IConceptLiteral::isExistentialDefiner)) {
                    Normalizer.addSubClause(newClause,
                            commonFact.createExistentialClause(roles.iterator().next(), successor));
                }
                else {
                    Normalizer.addSubClause(newClause,
                            commonFact.createUniversalClause(roles.iterator().next(), successor));
                }
                clausalForm.add(newClause);
            }
        }
        clausalForm.removeAll(delta);
        ontology.setContent(toAxiomForm(clausalForm));
    }

    private Collection<IAxiom> toAxiomForm(Collection<IClause> clausalForm)
    {
        Set<IAxiom> axioms = new HashSet<>();
        ICommonFactory fact = FactoryMgr.getCommonFactory();
        for (IClause clause : clausalForm) {
            axioms.add(fact.createSubsetAxiom(fact.createAtomicClause(IConceptLiteral.TOP), clause));
        }
        return axioms;
    }

    private Collection<IClause> getClausesWithPositiveDefiners(Collection<IConceptLiteral> definers,
                                                               Collection<IClause> clausalForm)
    {
        Set<IClause> clausesWithPositiveDefiners = new HashSet<>();
        for (IClause clause : clausalForm) {
            Set<IConceptLiteral> clauseDefiners =
                    ClauseHelper.getConceptNamesInStatement(clause).stream().filter(IConceptLiteral::isDefiner)
                            .collect(Collectors.toSet());
            clauseDefiners.retainAll(definers);
            if (!clauseDefiners.isEmpty()) {
                PolarityCalculator calculator = new PolarityCalculator(1);
                clause.accept(calculator);
                Set<IConceptLiteral> positiveDefiners = calculator.getPositive();
                positiveDefiners.retainAll(definers);
                if (!positiveDefiners.isEmpty()) {
                    clausesWithPositiveDefiners.add(clause);
                }
            }

        }
        return clausesWithPositiveDefiners;
    }

    private Collection<IClause> getClausesWhoseNegativeDefinersAreSubsetOf(Collection<IConceptLiteral> negativeDefiners,
                                                                           Collection<IClause> clausalForm,
                                                                           Map<IComplexClause,
                                                                                   Collection<IConceptLiteral>> pConcepts)
    {
        Set<IClause> clauses = new HashSet<>();
        for (IClause clause : clausalForm) {
            if (clause instanceof IComplexClause && pConcepts.containsKey(clause)) {
                if (negativeDefiners.containsAll(pConcepts.get(clause))) {
                    clauses.add(clause);
                }
            }
            else {
                Collection<IConceptLiteral> negativeDefinersOfClause = getNegativeDefiners(clause);
                if (negativeDefinersOfClause != null && !negativeDefinersOfClause.isEmpty()
                        && negativeDefiners.containsAll(negativeDefinersOfClause)) {
                    clauses.add(clause);
                }
            }
        }
        return clauses;
    }

    private IClause extractRemainder(IClause clause, Collection<IConceptLiteral> negativeDefiners)
    {
        ICommonFactory fact = FactoryMgr.getCommonFactory();
        if (clause instanceof IComplexClause) {
            IComplexClause disjunction = (IComplexClause) clause;
            if (disjunction.getOperator() == GroupOperator.DISJUNCTION) {
                IComplexClause remainder = fact.createDisjunctiveClause();
                for (IClause child : disjunction.getChildren()) {
                    if (!child.getSignature().stream().anyMatch(negativeDefiners::contains)) {
                        Normalizer.addSubClause(remainder, child);
                    }
                }
                if (remainder.getChildren().isEmpty()) {
                    IClause bottom = new InPlaceShallowNegationStrategy()
                            .negate(FactoryMgr.getCommonFactory().createAtomicClause(IConceptLiteral.TOP));
                    Normalizer.addSubClause(remainder, bottom);
                }
                return remainder;
            }
        }
        else {
            // either atomic (BOTTOM | !D) or quantified (BOTTOM | EXISTS r.D)
            Collection<ILiteral> sig = clause.getSignature();
            sig.retainAll(negativeDefiners);
            if (!sig.isEmpty()) {
                IAtomicClause top = fact.createAtomicClause(IConceptLiteral.TOP);
                return new InPlaceShallowNegationStrategy().negate(top);
            }
        }
        return null;
    }

    private Map<IComplexClause, Collection<IConceptLiteral>> mapToNegativeNonCyclicDefiners(
            Collection<IConceptLiteral> excludedDefiners, Collection<IComplexClause> delta)
    {
        Map<IComplexClause, Collection<IConceptLiteral>> clauseToNegativeDefiners =
                delta.stream().collect(Collectors.toMap(Function.identity(), this::getNegativeDefiners));
        clauseToNegativeDefiners.forEach((clause, definers) -> definers.removeAll(excludedDefiners));
        return clauseToNegativeDefiners;
    }

    private Collection<IComplexClause> filterAtMostOneExistentialDefiner(Collection<IComplexClause> delta,
                                                                         Map<IComplexClause,
                                                                                 Collection<IConceptLiteral>> clauseToNegativeNonCyclicDefiners)
    {
        Set<IComplexClause> clausesWithAtMostOneExistentialDefiner = new HashSet<>();
        for (IComplexClause clause : delta) {
            Collection<IConceptLiteral> definers = clauseToNegativeNonCyclicDefiners.get(clause);
            if (definers != null && definers.stream().filter(IConceptLiteral::isExistentialDefiner).count() <= 1) {
                clausesWithAtMostOneExistentialDefiner.add(clause);
            }
        }
        return clausesWithAtMostOneExistentialDefiner;
    }

    /**
     * Gets the negative definers in a CNF clause
     *
     * @param cnfClause clause
     * @return negative definer symbols
     */
    private Collection<IConceptLiteral> getNegativeDefiners(IClause cnfClause)
    {
        if (cnfClause instanceof IComplexClause &&
                ((IComplexClause) cnfClause).getOperator() == GroupOperator.DISJUNCTION) {
            Set<IClause> children = ((IComplexClause) cnfClause).getChildren();
            return children.stream().filter(child -> child instanceof IAtomicClause)
                    .filter(IClause::isNegated)
                    .filter(child -> ((IAtomicClause) child).getLiteral().isDefiner())
                    .map(child -> ((IAtomicClause) child).getLiteral())
                    .collect(Collectors.toSet());
        }
        return null;
    }

    private Collection<IRoleLiteral> getRoles(Collection<IConceptLiteral> negativeDefiners, DefinerFactory factory)
    {
        return negativeDefiners.stream().map(factory::getRole).collect(Collectors.toSet());
    }

    protected Collection<IComplexClause> extractDeltaClauses(Collection<IClause> ontology)
    {
        return ontology.stream()
                .filter(clause -> clause instanceof IComplexClause)
                .map(clause -> (IComplexClause) clause)
                .filter(clause -> clause.getOperator() == GroupOperator.DISJUNCTION)
                .filter(clause -> getNegativeDefiners(clause).size() >= 2)
                .collect(Collectors.toSet());
    }

    public Collection<IComplexClause> getDeltaClauses()
    {
        return delta;
    }
}
