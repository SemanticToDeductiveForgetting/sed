package pc1.pc2.pc3.logic.helper;

import org.jetbrains.annotations.NotNull;
import pc1.pc2.pc3.error.TimeException;
import pc1.pc2.pc3.logic.resolver.DeltaAxiomPredicate;
import pc1.pc2.pc3.norm.InPlaceShallowNegationStrategy;
import pc1.pc2.pc3.norm.Normalizer;
import pc1.pc2.pc3.om.*;

import java.util.*;
import java.util.function.Function;

public class DefinerAxiomEliminator
{

    private Collection<IAxiom> ontology;
    private final Collection<IConceptLiteral> definers;
    private final DeltaAxiomPredicate deltaAxiomPredicate;

    public DefinerAxiomEliminator(Collection<IAxiom> ontology, Collection<IConceptLiteral> definers,
                                  DeltaAxiomPredicate deltaAxiomPredicate)
    {
        this.ontology = ontology;
        this.definers = definers;
        this.deltaAxiomPredicate = deltaAxiomPredicate;
    }

    public Collection<IAxiom> eliminate() throws TimeException
    {
        IOntology ontology = FactoryMgr.getCommonFactory().createOntology();
        ontology.setContent(this.ontology);
        this.ontology = ontology.getAllActiveAxioms();
//        Collection<IConceptLiteral> excludedDefiners = getExcludedDefiners();
        for (IConceptLiteral definer : definers) {
//            if (!excludedDefiners.contains(definer)) {
            FactoryMgr.getTimeMgr().checkTime();
            Collection<IAxiom> leftDefinitionClauses = getLeftDefinitionClauses(definer);
            Collection<IAxiom> rightDefinitionClauses = getRightDefinitionClauses(definer);
            boolean isCyclic = isCyclicDefiner(definer, leftDefinitionClauses)
                    || isCyclicDefiner(definer, rightDefinitionClauses);
            if (!isCyclic) {
                ICommonFactory cFact = FactoryMgr.getCommonFactory();
                if (!leftDefinitionClauses.isEmpty()) {
                    this.ontology.removeAll(leftDefinitionClauses);
                    IAxiom axiom = prepareLeftDefinition(definer, cFact, leftDefinitionClauses);
                    replaceWithDefinition(definer, axiom.getRight());
                }
                else if (!rightDefinitionClauses.isEmpty()) {
                    this.ontology.removeAll(rightDefinitionClauses);
                    IAxiom axiom = prepareRightDefinition(definer, cFact, rightDefinitionClauses);
                    replaceWithDefinition(definer, axiom.getLeft());
                }
            }
//            }
        }

        return this.ontology;
    }

    private boolean isCyclicDefiner(IConceptLiteral definer, Collection<IAxiom> axioms)
    {
        for (IAxiom axiom : axioms) {
            Set<IConceptLiteral> zeroPolarity = new HashSet<>();
            PolarityCalculator.getPolarityOfConceptNames(axiom, new HashSet<>(), new HashSet<>(), zeroPolarity);
            if (zeroPolarity.contains(definer)) {
                return true;
            }
//            Set<ILiteral> leftSig = sigCollector.getSignature(axiom.getLeft());
//            Set<ILiteral> rightSig = sigCollector.getSignature(axiom.getRight());
//            if (leftSig.contains(definer) && rightSig.contains(definer)) {
//                boolean isCyclic = ClauseHelper.containsUnQuantifiedLiteral(definer, axiom.getLeft()) &&
//                        ClauseHelper.containsQuantifiedLiteral(definer, axiom.getRight());
//                return isCyclic || ClauseHelper.containsUnQuantifiedLiteral(definer, axiom.getRight()) &&
//                        ClauseHelper.containsQuantifiedLiteral(definer, axiom.getLeft());
//            }
        }
        return false;
    }

    @NotNull
    private Collection<IAxiom> getRightDefinitionClauses(IConceptLiteral definer)
    {
        return getAxiomsWithUnquantifiedLiteral(definer, IAxiom::getRight);
    }

    @NotNull
    private Collection<IAxiom> getLeftDefinitionClauses(IConceptLiteral definer)
    {
        return getAxiomsWithUnquantifiedLiteral(definer, IAxiom::getLeft);
    }

    private IAxiom prepareRightDefinition(IConceptLiteral definer, ICommonFactory fact,
                                          Collection<IAxiom> rightDefinitions)
    {
        if (rightDefinitions.size() > 1) {
            IComplexClause def = fact.createDisjunctiveClause();
            rightDefinitions.stream()
                    .map(axiom -> normalizeRightDefinition(definer, axiom))
                    .map(IAxiom::getLeft)
                    .forEach(c -> Normalizer.addSubClause(def, c));
            ontology.removeAll(rightDefinitions);

            return fact
                    .createSubsetAxiom(def, fact.createAtomicClause(definer));
        }
        ontology.removeAll(rightDefinitions);
        return rightDefinitions.iterator().next();
    }

    private IAxiom prepareLeftDefinition(IConceptLiteral definer, ICommonFactory fact,
                                         Collection<IAxiom> leftDefinitions)
    {
        if (leftDefinitions.size() > 1) {
            IComplexClause def = fact.createConjunctiveClause();
            leftDefinitions.stream()
                    .map(axiom -> normalizeLeftDefinition(definer, axiom))
                    .map(IAxiom::getRight)
                    .forEach(c -> Normalizer.addSubClause(def, c));
            ontology.removeAll(leftDefinitions);

            return fact
                    .createSubsetAxiom(fact.createAtomicClause(definer), def);
        }
        IAxiom def = leftDefinitions.iterator().next();
        ontology.removeAll(leftDefinitions);
        return def;
    }

    private IAxiom normalizeLeftDefinition(IConceptLiteral definer, IAxiom axiom)
    {
        IClause clause = axiom.getLeft();
        if (clause instanceof IComplexClause) {
            IClause negatedRemainder = getNegatedRemainder(definer, clause);
            ICommonFactory cfact = FactoryMgr.getCommonFactory();
            IComplexClause right = cfact.createDisjunctiveClause();
            Normalizer.addSubClause(right, negatedRemainder);
            Normalizer.addSubClause(right, axiom.getRight());
            return cfact.createSubsetAxiom(cfact.createAtomicClause(definer), right);
        }
        return axiom;
    }

    private IAxiom normalizeRightDefinition(IConceptLiteral definer, IAxiom axiom)
    {
        IClause clause = axiom.getRight();
        if (clause instanceof IComplexClause) {
            ICommonFactory cfact = FactoryMgr.getCommonFactory();
            IComplexClause left = cfact.createConjunctiveClause();
            IClause negatedRemainder = getNegatedRemainder(definer, clause);
            Normalizer.addSubClause(left, negatedRemainder);
            Normalizer.addSubClause(left, axiom.getLeft());
            return cfact.createSubsetAxiom(left, cfact.createAtomicClause(definer));
        }
        return axiom;
    }

    private IClause getNegatedRemainder(IConceptLiteral definer, IClause clause)
    {
        IClause cloned = pc1.pc2.pc3.utils.ClauseHelper.clone(clause);
        IComplexClause complex = (IComplexClause) cloned;
        complex.removeChild(c -> c instanceof IAtomicClause && ((IAtomicClause) c).getLiteral() == definer);
        IClause newClause = complex;
        if (complex.getChildren().size() == 1) {
            newClause = complex.getChildren().iterator().next();
        }
        InPlaceShallowNegationStrategy strategy = new InPlaceShallowNegationStrategy();
        return strategy.negate(newClause);
    }

    private Collection<IAxiom> getAxiomsWithUnquantifiedLiteral(IConceptLiteral definer, Function<IAxiom, IClause> part)
    {
        Collection<IAxiom> leftDefinitions = new LinkedList<>();
        for (IAxiom axiom : ontology) {
            if (ClauseHelper.containsUnQuantifiedLiteral(definer, part.apply(axiom))) {
                leftDefinitions.add(axiom);
            }
        }
        return leftDefinitions;
    }

    /**
     * Replaces the given definer in the ontology by its definition
     *
     * @param definer    definer symbol
     * @param definition definition axiom
     */
    private void replaceWithDefinition(IConceptLiteral definer, IAxiom definition)
    {
        IClause clause = ClauseHelper.containsUnQuantifiedLiteral(definer, definition.getLeft()) ?
                definition.getRight() : definition.getLeft();
        for (IAxiom axiom : ontology) {
            DefinerEliminator eliminator = new DefinerEliminator(Collections.singletonList(definer));
            eliminator.eliminate(Collections.singletonList(axiom.getLeft()), Collections.singletonMap(definer, clause));
            eliminator
                    .eliminate(Collections.singletonList(axiom.getRight()), Collections.singletonMap(definer, clause));
        }
    }

    /**
     * Replaces the given definer in the ontology by its definition
     *
     * @param definer    definer symbol
     * @param definition definition clause
     */
    private void replaceWithDefinition(IConceptLiteral definer, IClause definition) throws TimeException
    {
        for (IAxiom axiom : ontology) {
            FactoryMgr.getTimeMgr().checkTime();
            DefinerEliminator eliminator = new DefinerEliminator(Collections.singletonList(definer));
            eliminator.eliminate(Collections.singletonList(axiom.getLeft()),
                    Collections.singletonMap(definer, definition));
            eliminator.eliminate(Collections.singletonList(axiom.getRight()),
                    Collections.singletonMap(definer, definition));
        }
    }

    /**
     * Returns the excluded definers.
     * These are definers which occur with the same polarity in an axiom.
     *
     * @return A collection of definers that should not be eliminated.
     */
    private Collection<IConceptLiteral> getExcludedDefiners()
    {
        Collection<IConceptLiteral> excluded = new LinkedList<>();
        for (IAxiom axiom : ontology) {
            deltaAxiomPredicate.test(axiom);
            excluded.addAll(deltaAxiomPredicate.getDeltaDefiners());
        }
        excluded.addAll(extractCyclicDefiners(ontology));
        return excluded;
    }

    private Collection<IConceptLiteral> extractCyclicDefiners(Collection<IAxiom> axioms)
    {
        List<IConceptLiteral> cyclic = new LinkedList<>();
        for (IAxiom axiom : axioms) {
            cyclic.addAll(extractCyclicDefiners(axiom));
        }
        return cyclic;
    }

    private Collection<? extends IConceptLiteral> extractCyclicDefiners(IAxiom axiom)
    {
        Collection<IConceptLiteral> leftDefiners = getDefiners(axiom.getLeft());
        Collection<IConceptLiteral> rightDefiners = getDefiners(axiom.getRight());
        leftDefiners.retainAll(rightDefiners);
        return leftDefiners;
    }

    private Collection<IConceptLiteral> extractSeveralDefinersAppearingTogether(IAxiom axiom)
    {
        List<IConceptLiteral> excluded = new LinkedList<>();
        excluded.addAll(getExcludedDefiners(axiom.getLeft()));
        excluded.addAll(getExcludedDefiners(axiom.getRight()));
        return excluded;
    }

    private Collection<IConceptLiteral> getExcludedDefiners(IClause left)
    {
        Collection<IConceptLiteral> axiomDefiners = getDefiners(left);
        if (axiomDefiners.size() >= 2) {
            return axiomDefiners;
        }
        return Collections.emptyList();
    }

    private Collection<IConceptLiteral> getDefiners(IClause left)
    {
        Collection<IConceptLiteral> definersInClause = new LinkedList<>();
        for (IConceptLiteral definer : definers) {
            if (ClauseHelper.containsUnQuantifiedLiteral(definer, left)) {
                definersInClause.add(definer);
            }
        }
        return definersInClause;
    }
}
