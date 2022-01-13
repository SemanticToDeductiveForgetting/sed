package pc1.pc2.pc3.logic;

import org.jetbrains.annotations.NotNull;
import pc1.pc2.pc3.error.TimeException;
import pc1.pc2.pc3.logic.helper.*;
import pc1.pc2.pc3.om.*;
import pc1.pc2.pc3.utils.CollectionUtils;
import pc1.pc2.pc3.logic.factory.DefinerFactory;
import pc1.pc2.pc3.logic.om.ResolvableClause;
import pc1.pc2.pc3.logic.om.SignatureMgr;
import pc1.pc2.pc3.logic.resolver.MatrixResolver;
import pc1.pc2.pc3.logic.resolver.RankChecker;
import pc1.pc2.pc3.logic.resolver.ResolvableSubsumptionChecker;
import pc1.pc2.pc3.logic.resolver.RolePropagator;


import java.util.*;
import java.util.stream.Collectors;

public class ForgettingEngine
{

    private final DefinerFactory definerFactory;
    private RankChecker defaultRankChecker;
    private RankChecker bgRankChecker;

    public ForgettingEngine()
    {
        definerFactory = new DefinerFactory();
    }

    public List<IAxiom> forgetAxioms(Collection<IAxiom> ontology, Collection<IAxiom> backgroundTheory,
                                     List<IConceptLiteral> forgettingSignature)
    {
        IOntology ont = FactoryMgr.getCommonFactory().createOntology();
        ont.setContent(ontology);
        new OntologySimplifier(ont).purify(forgettingSignature, new HashSet<>()).eliminateUnitClausesAndTautologies();
        ontology = ont.getAllActiveAxioms();
        List<IClause> result = forgetClauses(getRight(ontology), getRight(backgroundTheory), forgettingSignature);
        ICommonFactory fact = FactoryMgr.getCommonFactory();
        return result.stream()
                .map(c -> fact.createSubsetAxiom(fact.createAtomicClause(IConceptLiteral.TOP), c))
                .collect(Collectors.toList());
    }

    private Set<IClause> getRight(Collection<IAxiom> ontology)
    {
        return ontology.stream().map(IAxiom::getRight).collect(Collectors.toSet());
    }

    public List<IClause> forgetClauses(Collection<IClause> ontology, Collection<IClause> backgroundTheory,
                                          List<IConceptLiteral> forgettingSignature)
    {
        Collection<IClause> normalisedOntology = normalize(ontology, forgettingSignature);
        Collection<IClause> normalisedTheory = normalize(backgroundTheory, forgettingSignature);
        new TautologyEliminator().eliminateTautologies(normalisedOntology);
        new TautologyEliminator().eliminateTautologies(normalisedTheory);
        List<ILiteral> signature = getSignatureIncludingDefiners(normalisedOntology, normalisedTheory);
        if (!signature.contains(IConceptLiteral.TOP)) {
            signature.add(0, IConceptLiteral.TOP);
        }
        SignatureMgr signatureMgr = new SignatureMgr(signature);
        LinkedList<ResolvableClause> ont = new LinkedList<>(convertToResolvable(normalisedOntology, signatureMgr));
        LinkedList<ResolvableClause> theory = new LinkedList<>(convertToResolvable(normalisedTheory, signatureMgr));
        LinkedList<ResolvableClause> interpolant = new LinkedList<>();

        defaultRankChecker = new RankChecker(1, 1, forgettingSignature, definerFactory);
        bgRankChecker = new RankChecker(1, 2, forgettingSignature, definerFactory);

        for (IConceptLiteral literal : forgettingSignature) {
            forgetLiteral(ont, theory, interpolant, literal, signatureMgr, true, true);
        }

        return eliminateDefinersAndSimplifyResult(ont, theory, interpolant);
    }

    private List<IClause> eliminateDefinersAndSimplifyResult(LinkedList<ResolvableClause> ont,
                                                             LinkedList<ResolvableClause> theory,
                                                             LinkedList<ResolvableClause> interpolant)
    {
        DefinerEliminator definerEliminator = new DefinerEliminator(definerFactory.getDefiners());
        return definerEliminator.eliminate(CollectionUtils.merge(ont, interpolant), theory);
    }

    public OntologyPair forgetSemantic(Collection<IClause> ontology, Collection<IClause> backgroundTheory,
                                       List<IConceptLiteral> forgettingSignature) throws TimeException
    {
        Collection<IClause> normalisedOntology = normalize(ontology, forgettingSignature);
        Collection<IClause> normalisedTheory = normalize(backgroundTheory, forgettingSignature);
        new TautologyEliminator().eliminateTautologies(normalisedOntology);
        new TautologyEliminator().eliminateTautologies(normalisedTheory);
        List<ILiteral> signature = getSignatureIncludingDefiners(normalisedOntology, normalisedTheory);
        if (!signature.contains(IConceptLiteral.TOP)) {
            signature.add(0, IConceptLiteral.TOP);
        }
        SignatureMgr signatureMgr = new SignatureMgr(signature);
        LinkedList<ResolvableClause> ont = new LinkedList<>(convertToResolvable(normalisedOntology, signatureMgr));
        LinkedList<ResolvableClause> theory = new LinkedList<>(convertToResolvable(normalisedTheory, signatureMgr));
        LinkedList<ResolvableClause> interpolant = new LinkedList<>();

        defaultRankChecker = new RankChecker(1, 1, forgettingSignature, definerFactory);
        bgRankChecker = new RankChecker(1, 2, forgettingSignature, definerFactory);

        for (IConceptLiteral literal : forgettingSignature) {
            FactoryMgr.getTimeMgr().checkTime();
            forgetLiteral(ont, theory, interpolant, literal, signatureMgr, false, false);
        }
        return new OntologyPair(CollectionUtils.merge(ont, interpolant), theory, definerFactory.getDefiners());
    }

    public DefinerFactory getDefinerFactory()
    {
        return definerFactory;
    }

    protected Collection<IClause> normalize(Collection<IClause> ontology, List<IConceptLiteral> forgettingSignature)
    {
        ClausalConverter clausalConverter = new ClausalConverter(definerFactory);
        return clausalConverter.toClausalForm(ontology, forgettingSignature);
    }

    protected void forgetLiteral(List<ResolvableClause> ont,
                                 List<ResolvableClause> theory,
                                 LinkedList<ResolvableClause> interpolant, IConceptLiteral literal,
                                 SignatureMgr signatureMgr, boolean doRolePropagation, boolean restrictResolution)
    {
        executeForgettingRound(ont, theory, interpolant, literal, signatureMgr, defaultRankChecker, doRolePropagation
                , restrictResolution);

        // type g inferences on background
        LinkedList<ResolvableClause> theoryInt = new LinkedList<>();
        executeForgettingRound(theory, Collections.emptyList(), theoryInt, literal, signatureMgr, bgRankChecker,
                doRolePropagation, restrictResolution);
        theory.addAll(theoryInt);

        removeClausesWithForgettingLiteral(ont, signatureMgr.indexOf(literal));
        removeClausesWithForgettingLiteral(theory, signatureMgr.indexOf(literal));
        removeClausesWithForgettingLiteral(interpolant, signatureMgr.indexOf(literal));
    }

    private List<ILiteral> getSignatureIncludingDefiners(Collection<IClause> ontology, Collection<IClause> theory)
    {
        List<IClause> all = new LinkedList<>(ontology);
        all.addAll(theory);
        return ClauseHelper.calculateSignature(all, Comparator.comparing(ILiteral::getSymbol));
    }

    protected void executeForgettingRound(List<ResolvableClause> ontology, List<ResolvableClause> theory,
                                          LinkedList<ResolvableClause> interpolant, IConceptLiteral forgettingSymbol,
                                          SignatureMgr signatureMgr, RankChecker rankChecker, boolean doRolePropagation,
                                          boolean restrictResolution)
    {
        if (doRolePropagation) {
            RolePropagator rolePropagator = createRolePropagator(ontology, theory, forgettingSymbol, signatureMgr,
                    definerFactory.getDefiners(), definerFactory);
            List<ResolvableClause> solutions = rolePropagator.perform();

            //checker.deleteSubsumed(solutions);
            interpolant.addAll(solutions);
        }
        ResolvableSubsumptionChecker checker = new ResolvableSubsumptionChecker(ontology, theory, interpolant);
        executeResolutionLoop(ontology, theory, interpolant, forgettingSymbol, definerFactory.getDefiners(),
                signatureMgr, checker, rankChecker, restrictResolution);
    }

    @NotNull
    protected RolePropagator createRolePropagator(List<ResolvableClause> ontology,
                                                  List<ResolvableClause> theory,
                                                  IConceptLiteral forgettingSymbol, SignatureMgr signatureMgr,
                                                  List<IConceptLiteral> definers,
                                                  DefinerFactory definerFactory)
    {
        return new RolePropagator(ontology, theory, forgettingSymbol, definers, definerFactory,
                signatureMgr);
    }

    private void removeClausesWithForgettingLiteral(List<ResolvableClause> ontology, int index)
    {
        ontology.removeIf(c -> !c.isBottom() && c.getPolarityOfLiteral(index) != 0);
    }

    // todo: parallelize
    private void executeResolutionLoop(List<ResolvableClause> ontology, List<ResolvableClause> backgroundTheory,
                                       LinkedList<ResolvableClause> interpolant, IConceptLiteral forgettingSymbol,
                                       List<IConceptLiteral> definers, SignatureMgr signatureMgr,
                                       ResolvableSubsumptionChecker checker, RankChecker rankChecker,
                                       boolean restrictResolution)
    {
        LinkedList<ResolvableClause> ont = new LinkedList<>(ontology);
        LinkedList<ResolvableClause> theory = new LinkedList<>(backgroundTheory);

        resolve(ont, ont, interpolant, forgettingSymbol, definers, signatureMgr, checker, rankChecker,
                restrictResolution);
        resolve(ont, theory, interpolant, forgettingSymbol, definers, signatureMgr, checker, rankChecker,
                restrictResolution);
        resolve(ont, interpolant, interpolant, forgettingSymbol, definers, signatureMgr, checker, rankChecker,
                restrictResolution);
        resolve(interpolant, interpolant, interpolant, forgettingSymbol, definers, signatureMgr, checker, rankChecker,
                restrictResolution);
        resolve(interpolant, theory, interpolant, forgettingSymbol, definers, signatureMgr, checker, rankChecker,
                restrictResolution);
    }

    protected void resolve(LinkedList<ResolvableClause> ont, LinkedList<ResolvableClause> theory,
                           List<ResolvableClause> interpolant, IConceptLiteral forgettingSymbol,
                           List<IConceptLiteral> definers, SignatureMgr signatureMgr,
                           ResolvableSubsumptionChecker checker, RankChecker rankChecker, boolean restrictResolution)
    {
        List<ResolvableClause> newClauses = new LinkedList<>();
        MatrixResolver resolver = createResolver(definers, signatureMgr);
        int indexOfForgettingLiteral = signatureMgr.indexOf(forgettingSymbol);
        LinkedList<ResolvableClause> backup = new LinkedList<>();
        while (!ont.isEmpty()) {
            ResolvableClause clause = ont.removeFirst();
            boolean isCandidate =
                    !clause.isBottom() &&
                            clause.getQuantifier() == null &&
                            clause.getPolarityOfLiteral(indexOfForgettingLiteral) != 0;
            if (isCandidate && !theory.isEmpty()) {
                rankChecker.setMainClause(clause);
                newClauses.addAll(resolver.resolve(clause, theory, forgettingSymbol, rankChecker, restrictResolution));
            }
            backup.add(clause);
        }
        ont.addAll(backup);
        if (newClauses.size() < 20) {
            new ResolvableSubsumptionChecker(newClauses, Collections.emptyList(), Collections.emptyList())
                    .deleteSubsumed(newClauses);
            checker.deleteSubsumed(newClauses);
        }
        interpolant.addAll(newClauses);
    }

    @NotNull
    protected MatrixResolver createResolver(List<IConceptLiteral> definers, SignatureMgr signatureMgr)
    {
        return new MatrixResolver(signatureMgr, definers);
    }


    protected List<ResolvableClause> convertToResolvable(Collection<IClause> normalisedOntology,
                                                         SignatureMgr signatureMgr)
    {
        return normalisedOntology.stream()
                .map(clause -> ClauseHelper.convert(signatureMgr, clause))
                .collect(Collectors.toList());
    }
}
