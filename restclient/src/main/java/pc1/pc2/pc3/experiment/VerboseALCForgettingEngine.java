package pc1.pc2.pc3.experiment;

import org.jetbrains.annotations.NotNull;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import pc1.pc2.pc3.ExperimentHelper;
import pc1.pc2.pc3.error.TimeException;
import pc1.pc2.pc3.logic.ALCForgettingEngine;
import pc1.pc2.pc3.logic.factory.DefinerFactory;
import pc1.pc2.pc3.logic.reduction.ALCSemanticToDeductive;
import pc1.pc2.pc3.logic.resolver.DeltaAxiomPredicate;
import pc1.pc2.pc3.om.*;
import pc1.pc2.pc3.utils.FileUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class VerboseALCForgettingEngine extends ALCForgettingEngine
{

    private final OutputStreamWriter out;
    private final File outDir;
    private final ALCSemanticToDeductive reductionStrategy;
    private final List<ForgettingData> statistics;
    private int forgettingSigSize;
    private int symbolsForgotten = 0;
    private int definers = 0;
    private int definersEliminated = 0;
    private boolean semanticForgettingRound = false;
    private int deltaAndCyclicDefiners;
    private int cyclicDefiners;

    public VerboseALCForgettingEngine(OutputStreamWriter out, File outDir, DefinerFactory factory,
                                      ALCSemanticToDeductive alcSemanticToDeductive)
    {
        super(factory, alcSemanticToDeductive);
        this.out = out;
        this.outDir = outDir;
        this.reductionStrategy = alcSemanticToDeductive;
        this.statistics = new LinkedList<>();
    }

    @Override
    public IOntology forget(IOntology ontology, Collection<IConceptLiteral> signature)
            throws TimeException
    {
        IOntology view;
        try {
            view = super.forget(ontology, signature);
        } catch (TimeException e) {
            throw e;
        }
        return view;
    }

    @Override
    protected void reduceToDeductive(IOntology ontology, Collection<IConceptLiteral> signature) throws TimeException
    {
        super.reduceToDeductive(ontology, signature);
        try {
            ExperimentHelper.save(ontology.getAllActiveAxioms(), outDir.getPath(), "DeductiveView",
                    ExperimentHelper.SaveFormat.CLAUSAL, ExperimentHelper.SaveFormat.OWL);
        } catch (OWLOntologyCreationException | OWLOntologyStorageException | IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void _reduceToDeductive(IOntology ontology, Collection<IConceptLiteral> signature)
    {
        try {
            out.append("Reducing the Semantic View to a Deductive View\n");
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        long before = System.nanoTime();
        super._reduceToDeductive(ontology, signature);
        long after = System.nanoTime();
        try (FileWriter writer = new FileWriter(outDir.getPath() + File.separator + "ReductionToALC.time")) {
            ExperimentHelper.logElapsedTime(after, before, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Collection<IComplexClause> deltaClauses = reductionStrategy.getDeltaClauses();

        try {
            ExperimentHelper.saveInHumanReadableForm(deltaClauses, outDir.getPath() + File.separator + "delta.clausal");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void forgetSemantic(IOntology ontology, Collection<IConceptLiteral> signature) throws TimeException
    {
        semanticForgettingRound = true;
        super.forgetSemantic(ontology, signature);
        semanticForgettingRound = false;
        try {
            ExperimentHelper.save(ontology.getAllActiveAxioms(), outDir.getPath(), "SemanticView",
                    ExperimentHelper.SaveFormat.CLAUSAL, ExperimentHelper.SaveFormat.OWL);
        } catch (OWLOntologyCreationException | OWLOntologyStorageException | IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void simplifyOntology(IOntology ontology)
    {
        long before = System.nanoTime();
        super.simplifyOntology(ontology);
        long after = System.nanoTime();
        String fileName = "DeductiveViewSimplification.time";
        if (semanticForgettingRound) {
            fileName = "SemanticViewSimplification.time";
        }
        try (FileWriter writer = new FileWriter(outDir.getPath() + File.separator + fileName)) {
            ExperimentHelper.logElapsedTime(after, before, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void logTimeStatistics()
    {
        String filePath = outDir.getPath() + File.separator + "ForgettingData.csv";
        List<String> lines = statistics.stream()
                .map(Object::toString)
                .collect(Collectors.toList());
        lines.add(0, "Forgetting Symbol,Input Size,OutputSize,Forgetting Duration (nanosecond)");
        try {
            FileUtils.writeFile(filePath, lines);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void forgetSignature(IOntology ontology, Collection<IConceptLiteral> signature) throws TimeException
    {
        forgettingSigSize = signature.size();
        long before = System.nanoTime();
        super.forgetSignature(ontology, signature);
        long after = System.nanoTime();
        logTimeStatistics();
        try (FileWriter writer = new FileWriter(outDir.getPath() + File.separator + "Semantic.time")) {
            ExperimentHelper.logElapsedTime(after, before, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected long forget(Collection<IAxiom> ontology, IConceptLiteral symbol,
                          IOntology outOntology, boolean withDeferral) throws TimeException
    {
        try {
            DateFormat timeFormat = DateFormat.getTimeInstance(DateFormat.SHORT);
            Calendar calendar = Calendar.getInstance();
            out.append(String.format("Forget %s: Started at %s on %d axioms", symbol.getSymbol(),
                    timeFormat.format(calendar.getTime()), ontology.size()));
            out.flush();
            long before = System.nanoTime();
            long newAxioms = super.forget(ontology, symbol, outOntology, withDeferral);
            long after = System.nanoTime();
            out.append(
                    String.format(", Ended at: %s with %d axioms", timeFormat.format(calendar.getTime()), newAxioms));
            double complete = (double) symbolsForgotten / (double) forgettingSigSize * 100.0;

            out.append(String.format(", %.2f%% Complete. Remains %d symbols\n", complete,
                    forgettingSigSize - symbolsForgotten - 1));

            out.flush();
            if (newAxioms >= 0) {
                ForgettingData data = new ForgettingData(symbol, after - before, ontology.size(), newAxioms);
                statistics.add(data);
                symbolsForgotten++;
            }
            return newAxioms;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    protected Collection<IAxiom> replaceByDefinition(Collection<IAxiom> ontology,
                                                     Map<IConceptLiteral, IClause> definitions)
    {
        Collection<IAxiom> axioms = super.replaceByDefinition(ontology, definitions);
        try {
            out.append(String.format("Forgot %d symbols by definition replacement\n", definitions.size()));
            symbolsForgotten += definitions.size();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return axioms;
    }

    @Override
    protected Set<IConceptLiteral> eliminateDefiners(IOntology ontology, DefinerFactory factory) throws TimeException
    {
        Set<IConceptLiteral> eliminatedDefiners = Collections.emptySet();
        try {
            out.append(String.format("Eliminating %d definer symbols\n", factory.getDefiners().size()));
            out.flush();
            if (semanticForgettingRound) {
                FileUtils.writeFile(outDir.getPath() + File.separator + "IntroducedDefiners",
                        Collections.singleton(String.valueOf(factory.getDefiners().size())));
            }

            long start = System.nanoTime();
            eliminatedDefiners = super.eliminateDefiners(ontology, factory);
            long end = System.nanoTime();
            String fileName = "DefinerEliminationInSemanticRound.time";
            if (!semanticForgettingRound) {
                fileName = "DefinerEliminationInDeductiveRound.time";
            }
            try (FileWriter writer = new FileWriter(outDir.getPath() + File.separator + fileName)) {
                ExperimentHelper.logElapsedTime(end, start, writer);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return eliminatedDefiners;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return eliminatedDefiners;
    }

    @Override
    protected void purifyDefiners(IOntology ontology, Set<IConceptLiteral> definers)
    {
        int oldSize = definers.size();
        super.purifyDefiners(ontology, definers);
        int newSize = definers.size();

        try {
            out.append(String.format("%d definers were purified.\n", oldSize - newSize));
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void excludeDefinersEliminatedInSemanticRound(Set<IConceptLiteral> definers)
    {
        int oldSize = definers.size();
        super.excludeDefinersEliminatedInSemanticRound(definers);
        int newSize = definers.size();

        if (oldSize != newSize) {
            try {
                out.append(String.format("Excluded %d definers that were eliminated in the semantic round.\n",
                        oldSize - newSize));
                out.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected @NotNull Set<IAxiom> excludeDeltaDefinersAndAxioms(IOntology ontology, Set<IConceptLiteral> definers)
    {
        int oldSize = definers.size();
        Set<IAxiom> deltaAxioms = super.excludeDeltaDefinersAndAxioms(ontology, definers);
        int newSize = definers.size();
        try {
            out.append(String.format("Excluded %d Delta definers.\n", oldSize - newSize));
            out.flush();
            if (semanticForgettingRound) {
                FileUtils.writeFile(outDir.getPath() + File.separator + "DeltaDefiners",
                        Collections.singleton(String.valueOf(oldSize - newSize)));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return deltaAxioms;
    }

    @Override
    protected void excludeCyclicDefiners(IOntology ontology, Set<IConceptLiteral> definers)
    {
        int oldSize = definers.size();
        super.excludeCyclicDefiners(ontology, definers);
        int newSize = definers.size();
        try {
            out.append(String.format("Excluded %d cyclic definers.\n", oldSize - newSize));
            out.flush();
            if (semanticForgettingRound) {
                FileUtils.writeFile(outDir.getPath() + File.separator + "CyclicDefiners",
                        Collections.singleton(String.valueOf(oldSize - newSize)));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected @NotNull Set<IAxiom> extractDeltaAxiomsAndDefiners(DeltaAxiomPredicate predicate,
                                                                 Collection<IAxiom> axioms,
                                                                 Collection<IConceptLiteral> excludedDefiners)
    {
        Set<IAxiom> delta = super.extractDeltaAxiomsAndDefiners(predicate, axioms, excludedDefiners);
        try {
            ExperimentHelper.saveInHumanReadableForm(delta, outDir.getPath() + File.separator + "deltaInAxiomForm" +
                    ".clausal");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return delta;
    }

    @Override
    protected Set<IConceptLiteral> eliminateDefiners(IOntology ontology, Collection<IConceptLiteral> definers)
            throws TimeException
    {
        this.definers = definers.size();
        definersEliminated = 0;
        return super.eliminateDefiners(ontology, definers);
    }

    @Override
    protected void eliminateDefiner(IOntology ontology, IConceptLiteral definer) throws TimeException
    {
        try {
            out.append(String.format("Eliminating: %s. ", definer.getSymbol()));
            out.flush();
            super.eliminateDefiner(ontology, definer);
            definersEliminated++;
            double complete = 0;
            int remaining;
            if (semanticForgettingRound) {
                complete = (double) (definersEliminated) / (double) definers * 100.0;
                remaining = definers - definersEliminated;
            }
            else {
                complete = (double) (definersEliminated) / (double) definers * 100.0;
                remaining = definers - definersEliminated;
            }
            out.append(String.format("%.2f%% Complete. Remains %d definers.\n", complete, remaining));
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class ForgettingData
    {
        private final IConceptLiteral forgettingSymbol;
        private final long duration;
        private final long inputSize;
        private final long outputSize;

        private ForgettingData(IConceptLiteral forgettingSymbol, long duration, long inputSize, long outputSize)
        {
            this.forgettingSymbol = forgettingSymbol;
            this.duration = duration;
            this.inputSize = inputSize;
            this.outputSize = outputSize;
        }

        @Override
        public String toString()
        {
            return String.format("%s,%d,%d,%d", forgettingSymbol, inputSize, outputSize, duration);
        }
    }
}
