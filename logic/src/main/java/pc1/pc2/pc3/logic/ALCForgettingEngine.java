package pc1.pc2.pc3.logic;

import org.jetbrains.annotations.NotNull;
import pc1.pc2.pc3.error.TimeException;
import pc1.pc2.pc3.logic.factory.DefinerFactory;
import pc1.pc2.pc3.logic.helper.CyclicDefinerIdentifier;
import pc1.pc2.pc3.logic.helper.DefinerAxiomEliminator;
import pc1.pc2.pc3.logic.helper.OntologySimplifier;
import pc1.pc2.pc3.logic.reduction.ALCSemanticToDeductive;
import pc1.pc2.pc3.logic.resolver.ALCResolver;
import pc1.pc2.pc3.logic.resolver.DeferingALCResolver;
import pc1.pc2.pc3.logic.resolver.DeltaAxiomPredicate;
import pc1.pc2.pc3.om.*;
import pc1.pc2.pc3.utils.ConceptReplacer;
import pc1.pc2.pc3.logic.helper.ClauseHelper;
import pc1.pc2.pc3.om.*;
import pc1.pc2.pc3.om.*;
import pc1.pc2.pc3.om.*;

import java.util.*;
import java.util.stream.Collectors;

public class ALCForgettingEngine
{
    // 1gb
    public static final int DEFER_SIZE = 1000000000;
    private final DefinerFactory factory;
    private final ALCSemanticToDeductive reductionStrategy;
    private Set<IConceptLiteral> definersEliminatedInSemanticRound;

    public ALCForgettingEngine(DefinerFactory factory)
    {
        this(factory, null);
    }

    public ALCForgettingEngine(DefinerFactory factory, ALCSemanticToDeductive reductionStrategy)
    {
        this.factory = factory;
        this.reductionStrategy = reductionStrategy;
    }

    public IOntology forget(IOntology ontology, Collection<IConceptLiteral> signature)
            throws TimeException
    {
        forgetSemantic(ontology, signature);
        reduceToDeductive(ontology, signature);
        return ontology;
    }

    protected void reduceToDeductive(IOntology ontology, Collection<IConceptLiteral> signature) throws TimeException
    {
        if (reductionStrategy != null) {
            _reduceToDeductive(ontology, signature);
            try {
                eliminateDefiners(ontology, factory);
                simplifyOntology(ontology);
            } catch (Exception ex) {
                System.out.println(ex);
            }
        }
    }

    protected void _reduceToDeductive(IOntology ontology, Collection<IConceptLiteral> signature)
    {
        reductionStrategy.reduceToALC(ontology, signature, factory);
    }

    protected void forgetSemantic(IOntology ontology, Collection<IConceptLiteral> signature) throws TimeException
    {
        Set<IConceptLiteral> sig = new HashSet<>(signature);
        forgetBySubstitutingDefinitions(ontology, sig);
        Set<IConceptLiteral> purified = new HashSet<>();
        new OntologySimplifier(ontology).purify(sig, purified).eliminateUnitClausesAndTautologies();
        sig.removeAll(purified);
        forgetSignature(ontology, sig);
        try {
//            simplifyOntology(ontology);
//            definersEliminatedInSemanticRound = eliminateDefiners(ontology, factory);
        } catch (Exception | StackOverflowError ex) {
            System.out.println(ex.getMessage());
        }
    }

    protected void simplifyOntology(IOntology ontology)
    {
        new OntologySimplifier(ontology).simplifyAxioms();
    }

    protected void forgetSignature(IOntology ontology, Collection<IConceptLiteral> signature) throws TimeException
    {
        Set<IConceptLiteral> sig = new HashSet<>(signature);
        List<IConceptLiteral> deferredSymbols = new LinkedList<>();
        for (IConceptLiteral symbol : sig) {
            try {
                if (forget(ontology.extractAxiomsOf(Collections.singleton(symbol)), symbol, ontology, true) == -1) {
                    deferredSymbols.add(symbol);
                }
            } catch (TimeException e) {
                deferredSymbols.add(symbol);
            }
        }

        for (IConceptLiteral symbol : deferredSymbols) {
            forget(ontology.extractAxiomsOf(Collections.singleton(symbol)), symbol, ontology, false);
        }
    }

    protected Set<IConceptLiteral> eliminateDefiners(IOntology ontology, DefinerFactory factory) throws TimeException
    {
        Set<IConceptLiteral> definers = new HashSet<>(factory.getDefiners());
        excludeCyclicDefiners(ontology, definers);
        Set<IAxiom> deltaAxioms = excludeDeltaDefinersAndAxioms(ontology, definers);
        excludeDefinersEliminatedInSemanticRound(definers);
        purifyDefiners(ontology, definers);
        Set<IConceptLiteral> eliminatedDefiners = eliminateDefiners(ontology, definers);
        ontology.addAxioms(deltaAxioms);
        return eliminatedDefiners;
    }

    protected void purifyDefiners(IOntology ontology, Set<IConceptLiteral> definers)
    {
        Set<IConceptLiteral> purifiedDefiners = new HashSet<>();
        new OntologySimplifier(ontology).purify(definers, purifiedDefiners);
        definers.removeAll(purifiedDefiners);
    }

    protected void excludeDefinersEliminatedInSemanticRound(Set<IConceptLiteral> definers)
    {
        if (definersEliminatedInSemanticRound != null) {
            definers.removeAll(definersEliminatedInSemanticRound);
        }
    }

    @NotNull
    protected Set<IAxiom> excludeDeltaDefinersAndAxioms(IOntology ontology, Set<IConceptLiteral> definers)
    {
        DeltaAxiomPredicate predicate = new DeltaAxiomPredicate(factory);
        Collection<IAxiom> axioms = ontology.getAllActiveAxioms();
        Set<IConceptLiteral> deltaDefiners = new HashSet<>();
        Set<IAxiom> deltaAxioms = extractDeltaAxiomsAndDefiners(predicate, axioms, deltaDefiners);
        axioms.removeAll(deltaAxioms);
        ontology.setContent(axioms);
        definers.removeAll(deltaDefiners);
        return deltaAxioms;
    }

    protected void excludeCyclicDefiners(IOntology ontology, Set<IConceptLiteral> definers)
    {
        Collection<IConceptLiteral> cyclicDefiners =
                new CyclicDefinerIdentifier().getCyclicDefiners(ontology.getAllActiveAxioms());
        definers.removeAll(cyclicDefiners);
    }

    @NotNull
    protected Set<IAxiom> extractDeltaAxiomsAndDefiners(DeltaAxiomPredicate predicate, Collection<IAxiom> axioms,
                                                        Collection<IConceptLiteral> excludedDefiners)
    {
        Set<IAxiom> deltaAxioms = new HashSet<>();
        for (IAxiom axiom : axioms) {
            if (predicate.test(axiom)) {
                deltaAxioms.add(axiom);
                excludedDefiners.addAll(predicate.getDeltaDefiners());
            }
        }
        return deltaAxioms;
    }

    protected Set<IConceptLiteral> eliminateDefiners(IOntology ontology, Collection<IConceptLiteral> definers)
            throws TimeException
    {
        Set<IConceptLiteral> eliminatedDefiners = new HashSet<>();
        for (IConceptLiteral definer : definers) {
            FactoryMgr.getTimeMgr().checkTime();
            eliminateDefiner(ontology, definer);
            eliminatedDefiners.add(definer);
        }
        return eliminatedDefiners;
    }

    protected void eliminateDefiner(IOntology ontology, IConceptLiteral definer) throws TimeException
    {
        Set<IConceptLiteral> singleton = Collections.singleton(definer);
        DefinerAxiomEliminator definerEliminator =
                new DefinerAxiomEliminator(ontology.extractAxiomsOf(singleton), singleton,
                        new DeltaAxiomPredicate(factory));
        ontology.addAxioms(definerEliminator.eliminate());
    }

    private void forgetBySubstitutingDefinitions(IOntology ontology, Collection<IConceptLiteral> signature)
    {
        Collection<IAxiom> allAxioms = ontology.getAllActiveAxioms();
        Collection<IAxiom> axiomsWithSignature = extractAxiomsWithSignature(allAxioms, signature);
        Map<IConceptLiteral, IClause> definitions = extractDefinitions(axiomsWithSignature, signature);
        Set<IConceptLiteral> invalidForExpansion = new HashSet<>();
        for (Map.Entry<IConceptLiteral, IClause> entry : definitions.entrySet()) {
            Collection<ILiteral> defSig = entry.getValue().getSignature();
            defSig.retainAll(signature);
            Set<IConceptLiteral> defCSig = defSig.stream().map(l -> (IConceptLiteral) l).collect(Collectors.toSet());
            if (!defCSig.isEmpty()) {
                if (defCSig.stream().anyMatch(l -> ClauseHelper.containsQuantifiedLiteral(l, entry.getValue()))) {
                    invalidForExpansion.add(entry.getKey());
                }
            }
        }
        ICommonFactory fact = FactoryMgr.getCommonFactory();
        for (IConceptLiteral concept : invalidForExpansion) {
            allAxioms.add(fact.createSubsetAxiom(fact.createAtomicClause(concept), definitions.get(concept)));
            allAxioms.add(fact.createSubsetAxiom(definitions.get(concept), fact.createAtomicClause(concept)));
        }
        invalidForExpansion.forEach(definitions::remove);
        axiomsWithSignature = replaceByDefinition(axiomsWithSignature, definitions);
        Collection<IAxiom> subsetAxioms = toSubsetAxioms(axiomsWithSignature);
        allAxioms.addAll(subsetAxioms);
        ontology.setContent(allAxioms);
        signature.removeAll(definitions.keySet());
    }

    private Collection<IAxiom> toSubsetAxioms(Collection<IAxiom> axiomsWithSignature)
    {
        Set<IAxiom> subsetAxioms = new LinkedHashSet<>();
        for (IAxiom axiom : axiomsWithSignature) {
            if (axiom.getType() == AxiomType.INCLUSION) {
                subsetAxioms.add(axiom);
            }
            else {
                subsetAxioms.add(FactoryMgr.getCommonFactory().createSubsetAxiom(axiom.getLeft(), axiom.getRight()));
                subsetAxioms.add(FactoryMgr.getCommonFactory().createSubsetAxiom(axiom.getRight(), axiom.getLeft()));
            }
        }
        return subsetAxioms;
    }

    protected Collection<IAxiom> replaceByDefinition(Collection<IAxiom> ontology,
                                                     Map<IConceptLiteral, IClause> definitions)
    {
        for (IAxiom axiom : ontology) {
            Collection<ILiteral> signature = axiom.getSignature();
            for (Map.Entry<IConceptLiteral, IClause> entry : definitions.entrySet()) {
                if (signature.contains(entry.getKey())) {
                    ConceptReplacer.replaceConcept(axiom, entry.getKey(), entry::getValue);
                }
            }
        }
        return ontology;
    }

    private Collection<IAxiom> extractAxiomsWithSignature(Collection<IAxiom> ontology,
                                                          Collection<IConceptLiteral> signature)
    {
        Set<IAxiom> axiomsWithSignature = new LinkedHashSet<>();
        for (IAxiom axiom : ontology) {
            Collection<ILiteral> sig = axiom.getSignature();
            sig.retainAll(signature);
            if (!sig.isEmpty()) {
                axiomsWithSignature.add(axiom);
            }
        }
        ontology.removeAll(axiomsWithSignature);
        return axiomsWithSignature;
    }

    private Collection<IAxiom> getDefinitorialAxioms(Collection<IAxiom> axiomsWithSignature)
    {
        return axiomsWithSignature.stream()
                .filter(a -> a.getType() == AxiomType.EQUIVALENCE)
                .collect(Collectors.toSet());
    }

    private Map<IConceptLiteral, IClause> extractDefinitions(Collection<IAxiom> ontology,
                                                             Collection<IConceptLiteral> signature)
    {
        Map<IConceptLiteral, IClause> definitions = new HashMap<>();
        Set<IAxiom> definitionAxioms = new HashSet<>();
        for (IAxiom axiom : ontology) {
            if (axiom.getType() == AxiomType.EQUIVALENCE) {
                if (axiom.getLeft() instanceof IAtomicClause) {
                    IConceptLiteral symbol = ((IAtomicClause) axiom.getLeft()).getLiteral();
                    if (signature.contains(symbol)) {
                        definitions.put(symbol, axiom.getRight());
                        definitionAxioms.add(axiom);
                    }
                }
                else if (axiom.getRight() instanceof IAtomicClause) {
                    IConceptLiteral symbol = ((IAtomicClause) axiom.getRight()).getLiteral();
                    if (signature.contains(symbol)) {
                        definitions.put(symbol, axiom.getLeft());
                        definitionAxioms.add(axiom);
                    }
                }
            }
        }
        ontology.removeAll(definitionAxioms);
        return definitions;
    }

    protected long forget(Collection<IAxiom> ontology, IConceptLiteral symbol,
                          IOntology outOntology, boolean withDeferral) throws TimeException
    {
        if (withDeferral) {
            return new DeferingALCResolver(factory, DEFER_SIZE).resolve(ontology, symbol, outOntology);
        }
        else {
            return new ALCResolver(factory).resolve(ontology, symbol, outOntology);
        }
    }

    public List<IConceptLiteral> getDefiners()
    {
        return factory.getDefiners();
    }

    public Collection<? extends ILiteral> getRemainingDefiners(List<IAxiom> ontology)
    {
        Set<ILiteral> sig = new HashSet<>();
        for (IAxiom axiom : ontology) {
            sig.addAll(axiom.getSignature());
        }
        sig.retainAll(factory.getDefiners());
        return sig;
    }
}
