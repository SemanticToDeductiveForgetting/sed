package pc1.pc2.pc3.logic.helper;

import pc1.pc2.pc3.logic.resolver.ClauseSubsumptionChecker;
import pc1.pc2.pc3.norm.Normalizer;
import pc1.pc2.pc3.om.*;
import pc1.pc2.pc3.om.privileged.IPrivilegedQuantifiedClause;
import pc1.pc2.pc3.logic.om.ResolvableClause;


import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class DefinerEliminator implements IClauseVisitor
{
    private static final boolean OPTIMIZE = false;
    private final Set<IConceptLiteral> definers;
    private Map<IConceptLiteral, IClause> definerDefinitions;
    private IAtomicClause foundDefiner;


    public DefinerEliminator(Collection<IConceptLiteral> definers)
    {
        this.definers = new HashSet<>(definers);
    }

    private Map<IConceptLiteral, IClause> extractDefinerDefinitions(Collection<ResolvableClause> ontology)
    {
        Map<IConceptLiteral, IClause> definitions = new HashMap<>();
        for (IConceptLiteral definer : definers) {
            List<ResolvableClause> negative = new LinkedList<>();
            Predicate<ResolvableClause> negativePredicate = clause -> clause.getPolarityOfLiteral(definer) == -1;
            ontology.stream().filter(negativePredicate).forEach(negative::add);
            if (!negative.isEmpty()) {
                ontology.removeAll(negative);
                negative.forEach(clause -> clause.setPolarityOf(definer, 0));
                definitions.put(definer, createConjunctiveClauseFromDefinitions(negative));
            }
            else {
                definitions.put(definer, FactoryMgr.getCommonFactory().createAtomicClause(IConceptLiteral.TOP));
            }
        }
        return definitions;
    }

    private IClause createConjunctiveClauseFromDefinitions(List<ResolvableClause> negative)
    {
        IClause definition = negative.stream()
                .map(ClauseHelper::convert)
                .reduce(FactoryMgr.getCommonFactory().createConjunctiveClause()
                        , IComplexClause::addChild, IComplexClause::addChild);
        //noinspection ConstantConditions
        if (definition instanceof IComplexClause) {
            if (((IComplexClause) definition).getChildren().size() == 1) {
                definition = ((IComplexClause) definition).getChildren().iterator().next();
            }
            else if (((IComplexClause) definition).getChildren().isEmpty()) {
                definition = FactoryMgr.getCommonFactory().createAtomicClause(IConceptLiteral.TOP).negate();
            }
        }
        return definition;
    }

    public List<IClause> eliminate(List<ResolvableClause> ontology, List<ResolvableClause> bg)
    {
        for (IConceptLiteral definer : definers) {
            moveDefinerClauseFromTheoryToOntology(ontology, bg, definer);
        }
        Set<IConceptLiteral> cyclicDefiners = new HashSet<>();
        for (IConceptLiteral definer : definers) {
            for (ResolvableClause clause : ontology) {
                boolean hasNegativeDefiner =
                        ClauseHelper.findFirstNegativeDefiner(clause, Collections.singletonList(definer)) != null;
                boolean hasPositiveDefiner =
                        !ClauseHelper.getResolvablesWithQuantifiedLiteral(Collections.singletonList(clause), definer)
                                .isEmpty();
                if (hasNegativeDefiner && hasPositiveDefiner) {
                    cyclicDefiners.add(definer);
                }
            }
        }
        definers.removeAll(cyclicDefiners);
        Map<IConceptLiteral, IClause> definitions = extractDefinerDefinitions(ontology);
        List<IClause> result = ClauseHelper.convertToClauses(ontology);
        eliminate(result, definitions);
        if (OPTIMIZE) {
            new TautologyEliminator().eliminateTautologies(result);
            new ExistentialQuantifierEliminator().eliminate(result);
            return new ClauseSubsumptionChecker().eliminate(result);
        }
        return result;
    }

    protected void moveDefinerClauseFromTheoryToOntology(List<ResolvableClause> ontology,
                                                         Collection<ResolvableClause> theory,
                                                         IConceptLiteral definer)
    {
        Predicate<ResolvableClause> definerExists =
                clause -> clause.getPolarityOfLiteral(definer, true) != 0;

        boolean definerInOntology = ontology.stream().anyMatch(definerExists);
        if (definerInOntology) {
            theory.stream().filter(definerExists).forEach(ontology::add);
        }
    }

    public void eliminate(List<IClause> ontology, Map<IConceptLiteral, IClause> definerDefinitions)
    {
        this.definerDefinitions = definerDefinitions;
        List<IClause> preList = new LinkedList<>();
        for (IClause clause : ontology) {
            if (OPTIMIZE && ontology.size() < 50) {
                IClause pre = pc1.pc2.pc3.utils.ClauseHelper.clone(clause);
                preList.add(pre);
            }
            clause.accept(this);
//            while (definerDefinitions.keySet().stream().anyMatch(
//                    definer -> !ClauseHelper.getClausesWithQuantifiedLiteral(Collections.singletonList(clause),
//                    definer)
//                            .isEmpty()))
//                clause.accept(this);
        }
        if (OPTIMIZE && ontology.size() < 50) {
            Set<IClause> irreducible = new HashSet<>();
            for (int i = 0; i < ontology.size(); i++) {
                IClause first = ontology.get(i);
                for (int j = i + 1; j < ontology.size(); j++) {
                    IClause second = ontology.get(j);
                    ClauseSubsumptionChecker areEqual = new ClauseSubsumptionChecker();
                    if (areEqual.subsumes(first, second) && areEqual.subsumes(second, first)) {
                        irreducible.add(first);
                        irreducible.add(second);
                    }
                }
            }
            if (!irreducible.isEmpty()) {
                for (IClause clause : irreducible) {
                    int index = ontology.indexOf(clause);
                    ontology.remove(clause);
                    ontology.add(preList.remove(index));
                }
                List<ILiteral> sig =
                        ClauseHelper.calculateSignature(ontology, Comparator.comparing(ILiteral::getSymbol));
                List<IConceptLiteral> irreducedDefiners = sig.stream()
                        .filter(l -> l instanceof IConceptLiteral)
                        .map(l -> (IConceptLiteral) l)
                        .filter(definerDefinitions::containsKey)
                        .collect(Collectors.toList());

                ICommonFactory fact = FactoryMgr.getCommonFactory();
                for (IConceptLiteral definer : irreducedDefiners) {
                    if (definerDefinitions.containsKey(definer)) {
                        IComplexClause disjunct = fact.createDisjunctiveClause();
                        Normalizer.addSubClause(disjunct, fact.createAtomicClause(definer).negate());
                        Normalizer.addSubClause(disjunct, definerDefinitions.get(definer));
                        ontology.add(disjunct);
                    }
                }
            }
        }
    }

    @Override
    public void visitAtomic(IAtomicClause clause)
    {
        if (definers.contains(clause.getLiteral())) {
            foundDefiner = clause;
        }
    }

    @Override
    public void visitComplex(IComplexClause clause)
    {
        List<IAtomicClause> definers = new LinkedList<>();
        for (IClause child : clause.getChildren()) {
            child.accept(this);
            if (foundDefiner != null && definerDefinitions.containsKey(foundDefiner.getLiteral())) {
                definers.add(foundDefiner);
                foundDefiner = null;
            }
        }
        for (IAtomicClause definer : definers) {
            clause.removeChild(definer);
            Normalizer.addSubClause(clause, definerDefinitions.get(definer.getLiteral()));
            foundDefiner = null;
        }
    }

    @Override
    public void visitQuantified(IQuantifiedClause clause)
    {
        clause.getSuccessor().accept(this);
        if (foundDefiner != null && definerDefinitions.containsKey(foundDefiner.getLiteral())) {
            ((IPrivilegedQuantifiedClause) clause).setSuccessor(definerDefinitions.get(foundDefiner.getLiteral()));
            foundDefiner = null;
        }
    }
}
