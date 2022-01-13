package pc1.pc2.pc3.logic.resolver;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pc1.pc2.pc3.om.IConceptLiteral;
import pc1.pc2.pc3.om.Quantifier;
import pc1.pc2.pc3.logic.factory.DefinerFactory;
import pc1.pc2.pc3.logic.helper.ClauseHelper;
import pc1.pc2.pc3.logic.om.ResolvableClause;
import pc1.pc2.pc3.logic.om.SignatureMgr;

import java.util.*;
import java.util.stream.Stream;

public class RolePropagator
{
    private final List<ResolvableClause> ontology;
    private final List<ResolvableClause> theory;
    private final IConceptLiteral forgettingSymbol;
    private final List<IConceptLiteral> definers;
    private DefinerFactory factory;
    private SignatureMgr signatureMgr;

    public RolePropagator(List<ResolvableClause> ontology, List<ResolvableClause> theory,
                          IConceptLiteral forgettingSymbol, List<IConceptLiteral> definers,
                          DefinerFactory factory, SignatureMgr signatureMgr)
    {

        this.ontology = ontology;
        this.theory = theory;
        this.forgettingSymbol = forgettingSymbol;
        this.definers = definers;
        this.factory = factory;
        this.signatureMgr = signatureMgr;
    }

    @NotNull
    public List<ResolvableClause> perform()
    {
        Map<IConceptLiteral, List<ResolvableClause>> positiveOntologyClausesWithNegativeDefiner = new HashMap<>();
        Map<IConceptLiteral, List<ResolvableClause>> negativeOntologyClausesWithNegativeDefiner = new HashMap<>();
        Map<IConceptLiteral, List<ResolvableClause>> positiveTheoryClausesWithNegativeDefiner = new HashMap<>();
        Map<IConceptLiteral, List<ResolvableClause>> negativeTheoryClausesWithNegativeDefiner = new HashMap<>();

        // find clauses with positive forgetting symbol and negative definer
        sortCandidateClauses(ontology, positiveOntologyClausesWithNegativeDefiner,
                negativeOntologyClausesWithNegativeDefiner);
        sortCandidateClauses(theory, positiveTheoryClausesWithNegativeDefiner,
                negativeTheoryClausesWithNegativeDefiner);

        List<ResolvableClause> result = new LinkedList<>();
        result.addAll(performRolePropagations(ontology, positiveOntologyClausesWithNegativeDefiner,
                ontology, negativeOntologyClausesWithNegativeDefiner));
        result.addAll(performRolePropagations(ontology, positiveOntologyClausesWithNegativeDefiner,
                theory, negativeTheoryClausesWithNegativeDefiner));
        result.addAll(performRolePropagations(ontology, negativeOntologyClausesWithNegativeDefiner,
                theory, positiveTheoryClausesWithNegativeDefiner));

        return result;
    }

    private Collection<ResolvableClause> performRolePropagations(
            List<ResolvableClause> ontology,
            Map<IConceptLiteral, List<ResolvableClause>> positive,
            List<ResolvableClause> theory,
            Map<IConceptLiteral, List<ResolvableClause>> negative)
    {
        List<ResolvableClause> newClauses = new LinkedList<>();
        for (Map.Entry<IConceptLiteral, List<ResolvableClause>> positiveEntry : positive.entrySet()) {
            for (Map.Entry<IConceptLiteral, List<ResolvableClause>> negativeEntry : negative.entrySet()) {

                IConceptLiteral definer1 = positiveEntry.getKey();
                List<ResolvableClause> ontologyClauses =
                        ClauseHelper.getResolvablesWithQuantifiedLiteral(ontology, definer1);
                IConceptLiteral definer2 = negativeEntry.getKey();
                List<ResolvableClause> theoryClauses =
                        ClauseHelper.getResolvablesWithQuantifiedLiteral(theory, definer2);

                List<Pair<ResolvableClause, ResolvableClause>> resolvables =
                        getPropagatableClauses(positiveEntry, negativeEntry, ontologyClauses, theoryClauses);

                if (!resolvables.isEmpty()) {
                    IConceptLiteral newDefiner = factory.getDefinerFor(definer1, definer2);
                    if (newDefiner == null &&
                            !factory.isParentDefiner(definer1, definer2) &&
                            !factory.isParentDefiner(definer2, definer1)) {
                        newDefiner = factory.createDefinerFrom(definer1, definer2);
                        signatureMgr.add(newDefiner);
                        definers.add(newDefiner);
                        // create new clauses

                        for (Pair<ResolvableClause, ResolvableClause> pair : resolvables) {
                            ResolvableClause propagate =
                                    propagate(pair.getFirst(), pair.getSecond(), definer1, definer2, newDefiner);
                            if (propagate != null) {
                                newClauses.add(propagate);
                            }
                        }
                    }
                    newClauses.addAll(propagateNewDefiner(definer1, positiveEntry.getValue(), newDefiner));
                    newClauses.addAll(propagateNewDefiner(definer2, negativeEntry.getValue(), newDefiner));
                }
            }
        }
        return newClauses;
    }

    @NotNull
    private List<Pair<ResolvableClause, ResolvableClause>> getPropagatableClauses(
            Map.Entry<IConceptLiteral, List<ResolvableClause>> positiveEntry,
            Map.Entry<IConceptLiteral, List<ResolvableClause>> negativeEntry, List<ResolvableClause> ontologyClauses,
            List<ResolvableClause> theoryClauses)
    {
        List<Pair<ResolvableClause, ResolvableClause>> resolvables = new LinkedList<>();
        for (ResolvableClause ontologyClause : ontologyClauses) {
            for (ResolvableClause theoryClause : theoryClauses) {
                if (canPropagate(ontologyClause, theoryClause, positiveEntry.getKey(),
                        negativeEntry.getKey())) {
                    resolvables.add(new Pair<>(ontologyClause, theoryClause));
                }
            }
        }
        return resolvables;
    }

    private boolean canPropagate(ResolvableClause clause1, ResolvableClause clause2,
                                 IConceptLiteral definer1, IConceptLiteral definer2)
    {
        long negativeDefiners = Stream.concat(ClauseHelper.findNegativeDefiners(clause1, factory.getDefiners()),
                ClauseHelper.findNegativeDefiners(clause2, factory.getDefiners())).distinct().count();
        return negativeDefiners <= 1 && (definer1.isExistentialDefiner() != definer2.isExistentialDefiner() ||
                !definer1.isExistentialDefiner());
//        LinkedList<ResolvableClause> pathToDefiner1 = getPathToDefiner(clause1, definer1);
//        LinkedList<ResolvableClause> pathToDefiner2 = getPathToDefiner(clause2, definer2);
//        if (definer1.isExistentialDefiner() != definer2.isExistentialDefiner() || !definer1.isExistentialDefiner()) {
//            while (!pathToDefiner1.isEmpty() && !pathToDefiner2.isEmpty()) {
//                ResolvableClause last1 = pathToDefiner1.removeLast();
//                ResolvableClause last2 = pathToDefiner2.removeLast();
//                if (last1.getQuantifier() != null && last2.getQuantifier() != null && !canPropagate(last1, last2)) {
//                    return false;
//                }
//            }
//        }
//        return true;
    }

    @Nullable
    protected ResolvableClause propagate(ResolvableClause clause1, ResolvableClause clause2,
                                         IConceptLiteral definer1, IConceptLiteral definer2, IConceptLiteral newDefiner)
    {
        LinkedList<ResolvableClause> pathToDefiner1 = getPathToDefiner(clause1, definer1);
        ResolvableClause last1 = pathToDefiner1.removeLast();

        LinkedList<ResolvableClause> pathToDefiner2 = getPathToDefiner(clause2, definer2);
        ResolvableClause last2 = pathToDefiner2.removeLast();

        if (canPropagate(last1, last2)) {
            ResolvableClause propagated = createPropagatedClause(newDefiner, last1, last2);

            while (!pathToDefiner1.isEmpty() && !pathToDefiner2.isEmpty()) {
                ResolvableClause upperLevel1 = pathToDefiner1.removeLast();
                ResolvableClause upperLevel2 = pathToDefiner2.removeLast();

                int[] polarity = new int[signatureMgr.getSignatureLiterals().size()];
                boolean tautology = mergePolarity(upperLevel1, upperLevel2, polarity);
                if (tautology) {
                    int[] polarityArray = new int[signatureMgr.getSize()];
                    Arrays.fill(polarityArray, 0);
                    polarityArray[signatureMgr.indexOf(IConceptLiteral.TOP)] = 1;
                    propagated = new ResolvableClause(signatureMgr, polarityArray);
                }
                else if (upperLevel1.getQuantifier() == null || upperLevel2.getQuantifier() == null ||
                        canPropagate(upperLevel1, upperLevel2)) {
                    ResolvableClause resolvable =
                            new ResolvableClause(signatureMgr, polarity);
                    List<ResolvableClause> quantifiedParts1 = new LinkedList<>(upperLevel1.getQuantifiedParts());
                    List<ResolvableClause> quantifiedParts2 = new LinkedList<>(upperLevel2.getQuantifiedParts());
                    quantifiedParts1.remove(last1);
                    quantifiedParts2.remove(last2);
                    quantifiedParts1.forEach(resolvable::addQuantifiedClause);
                    quantifiedParts2.forEach(resolvable::addQuantifiedClause);
                    resolvable.addQuantifiedClause(propagated);
                    if (upperLevel1.getQuantifier() == null && upperLevel2.getQuantifier() != null) {
                        resolvable.setQuantifier(upperLevel2.getQuantifier());
                        resolvable.setRole(upperLevel2.getRole());
                    }
                    else if (upperLevel1.getQuantifier() != null && upperLevel2.getQuantifier() == null) {
                        resolvable.setQuantifier(upperLevel1.getQuantifier());
                        resolvable.setRole(upperLevel1.getRole());
                    }
                    else if (upperLevel1.getQuantifier() != null && upperLevel2.getQuantifier() != null) {
                        resolvable.setQuantifier(Quantifier.Existential);
                        resolvable.setRole(upperLevel1.getRole());
                    }
                    propagated = resolvable;
                }
                last1 = upperLevel1;
                last2 = upperLevel2;
            }

            // take remaining
            propagated = propagateUpperLevels(pathToDefiner1, last1, propagated);
            propagated = propagateUpperLevels(pathToDefiner2, last2, propagated);

            return propagated;
        }
        return null;
    }

    private ResolvableClause propagateUpperLevels(LinkedList<ResolvableClause> pathToDefiner, ResolvableClause last,
                                                  ResolvableClause propagated)
    {
        while (!pathToDefiner.isEmpty()) {
            ResolvableClause upperLevel = pathToDefiner.removeLast();
            ResolvableClause clause = new ResolvableClause(signatureMgr,
                    upperLevel.getPolarity());
            List<ResolvableClause> quantifiedParts = new LinkedList<>(upperLevel.getQuantifiedParts());
            quantifiedParts.remove(last);
            quantifiedParts.forEach(clause::addQuantifiedClause);
            clause.setQuantifier(upperLevel.getQuantifier());
            clause.setRole(upperLevel.getRole());
            clause.addQuantifiedClause(propagated);
            propagated = clause;
            last = upperLevel;
        }
        return propagated;
    }

    private boolean mergePolarity(ResolvableClause clause1, ResolvableClause clause2, int[] polarity)
    {
        for (int i = 0; i < clause1.getPolarity().length; i++) {
            polarity[i] = clause1.getPolarity()[i];
        }
        int[] polarityOfUpperLevel2 = clause2.getPolarity();
        for (int i1 = 0; i1 < polarityOfUpperLevel2.length; i1++) {
            if (polarityOfUpperLevel2[i1] == -polarity[i1] && polarity[i1] != 0) {
                return true;
            }
            else if (polarityOfUpperLevel2[i1] != polarity[i1]) {
                polarity[i1] += polarityOfUpperLevel2[i1];
            }
        }
        return false;
    }

    @NotNull
    private ResolvableClause createPropagatedClause(IConceptLiteral newDefiner, ResolvableClause clause1,
                                                    ResolvableClause clause2)
    {
        int[] polarityOfPropagated = new int[signatureMgr.getSignatureLiterals().size()];
        polarityOfPropagated[signatureMgr.indexOf(newDefiner)] = 1;
        ResolvableClause propagated = new ResolvableClause(signatureMgr,
                polarityOfPropagated);
        propagated.setRole(clause1.getRole());
        propagated.setQuantifier(
                clause1.getQuantifier() != clause2.getQuantifier() ? Quantifier.Existential : Quantifier.Universal);
        return propagated;
    }

    private LinkedList<ResolvableClause> getPathToDefiner(ResolvableClause clause, IConceptLiteral definer)
    {
        LinkedList<ResolvableClause> path = new LinkedList<>();
        if (clause.getPolarityOfLiteral(definer) != 0) {
            path.addFirst(clause);
            return path;
        }
        else {
            for (ResolvableClause quantifiedPart : clause.getQuantifiedParts()) {
                List<ResolvableClause> subPath = getPathToDefiner(quantifiedPart, definer);
                if (!subPath.isEmpty()) {
                    path.addAll(subPath);
                    path.addFirst(clause);
                    return path;
                }
            }
        }
        return path;
    }

    private boolean canPropagate(ResolvableClause c1, ResolvableClause c2)
    {
        boolean goodQuantifiers =
                c1.getQuantifier() != c2.getQuantifier() || c1.getQuantifier() == Quantifier.Universal;
        boolean goodRoles = c1.getRole() == c2.getRole();
        return goodQuantifiers && goodRoles;
    }

    protected List<ResolvableClause> propagateNewDefiner(IConceptLiteral oldDefiner,
                                                         List<ResolvableClause> oldDefinerClauses,
                                                         IConceptLiteral newDefiner)
    {
        List<ResolvableClause> newClauses = new LinkedList<>();
        for (ResolvableClause definerClause : oldDefinerClauses) {
            ResolvableClause newDefinerClause = ClauseHelper.clone(definerClause);
            newDefinerClause.setPolarityOf(oldDefiner, 0);
            newDefinerClause.setPolarityOf(newDefiner, -1);
            newClauses.add(newDefinerClause);
        }
        return newClauses;
    }

    private void sortCandidateClauses(
            List<ResolvableClause> clauses,
            Map<IConceptLiteral, List<ResolvableClause>> positiveOntologyClausesWithNegativeDefiner,
            Map<IConceptLiteral, List<ResolvableClause>> negativeOntologyClausesWithNegativeDefiner)
    {
        List<ResolvableClause> positive = new LinkedList<>();
        List<ResolvableClause> negative = new LinkedList<>();
        for (ResolvableClause clause : clauses) {
            int polarityOfForgettingSymbol = clause.getPolarityOfLiteral(forgettingSymbol);
            if (polarityOfForgettingSymbol > 0) {
                positive.add(clause);
            }
            else if (polarityOfForgettingSymbol < 0) {
                negative.add(clause);
            }
        }

        positiveOntologyClausesWithNegativeDefiner.putAll(groupByNegativeDefiner(definers, positive));
        negativeOntologyClausesWithNegativeDefiner.putAll(groupByNegativeDefiner(definers, negative));
    }

    private Map<IConceptLiteral, List<ResolvableClause>> groupByNegativeDefiner(List<IConceptLiteral> definers,
                                                                                List<ResolvableClause> clauses)
    {
        Map<IConceptLiteral, List<ResolvableClause>> clausesWithDefiner = new HashMap<>();
        for (ResolvableClause clause : clauses) {
            IConceptLiteral definer = ClauseHelper.findFirstNegativeDefiner(clause, definers);
            if (definer != null) {
                List<ResolvableClause> resolvableClauses =
                        clausesWithDefiner.computeIfAbsent(definer, k -> new LinkedList<>());
                resolvableClauses.add(clause);
            }
        }
        return clausesWithDefiner;
    }

}
