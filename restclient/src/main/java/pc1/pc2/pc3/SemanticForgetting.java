package pc1.pc2.pc3;

import org.jetbrains.annotations.NotNull;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import pc1.pc2.pc3.app.Bootstrap;
import pc1.pc2.pc3.rest.SymbolData;
import pc1.pc2.pc3.error.TimeException;
import pc1.pc2.pc3.logic.ALCForgettingEngine;
import pc1.pc2.pc3.logic.ForgettingEngine;
import pc1.pc2.pc3.logic.factory.DefinerFactory;
import pc1.pc2.pc3.logic.helper.ClausalConverter;
import pc1.pc2.pc3.logic.reduction.ALCSemanticToDeductive;
import pc1.pc2.pc3.om.*;
import pc1.pc2.pc3.utils.StringUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class SemanticForgetting
{
    private static final String usage = String.format("%s\n%s\n%s\n%s\n%s\n%s\n%s\n%s\n%s",
            "SemanticForgetting:",
            "Performs semantic forgetting and generates a forgetting view file.",
            "This program is designed as a pipleline step that should be executed after DataLoader.",
            "Usage: SemanticForgetting -ont ontology-file -outDir output-directory -sig signature-file [options]",
            "\tontology-file: path to the main ontology file. The ontology should be in string clausal form",
            "\toutput-directory: a path to the directory where the forgetting view and any other debug files will be " +
                    "written.",
            "\tsignature-file: a signature file in JSON format.",
            "Options:",
            "\t-verbose: verbose output.");
    private static List<IAxiom> axioms;
    private static List<SymbolData> signature;
    private static File outDir;
    private static boolean verbose;
    private static long timeOut = -1;
    private static boolean reduceToDeductive;

    public static void main(String[] args) throws IOException, OWLOntologyCreationException, OWLOntologyStorageException
    {
        System.out.printf("Running with maximum of %d of memory\n", Runtime.getRuntime().maxMemory());
        Bootstrap.initializeApplication();
        parseArguements(args);
        String out = outDir.getCanonicalPath();

        List<IConceptLiteral> sig = getSortedForgettingSignature();

        System.out.printf("Ontology: %d axioms, Signature: %d symbols\n", axioms.size(), sig.size());
        System.out.println("Forgetting Signature:");
        System.out.println(sig.stream().map(IConceptLiteral::getSymbol).collect(Collectors.joining(", ")));

        DefinerFactory definerFactory = new DefinerFactory();
        System.out.println("Converting the ontology to clausal form:");
        System.out.println("Start at " + new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date()));
        IOntology ontology = toClausalForm(axioms, sig, definerFactory);
        System.out.println("End at " + new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date()));
        axioms.clear();
        axioms = null;

        ALCSemanticToDeductive alcSemanticToDeductive = null;
        if (reduceToDeductive) {
            alcSemanticToDeductive = new ALCSemanticToDeductive();
        }
        ALCForgettingEngine engine =
                ArgsHelper.createALCForgettingEngine(verbose, new File(out), definerFactory,
                        alcSemanticToDeductive);
        if (timeOut > 0) {
            FactoryMgr.setMaxTime(timeOut);
        }
        long before = System.nanoTime();
        try {
            engine.forget(ontology, sig);
        } catch (TimeException e) {
            System.out.println(e.getMessage());
            System.out.println("Wrapping Up");
            Path exceptions = Path.of(out + File.separator + "Exceptions");
            Files.write(exceptions, StringUtils.toByteArray(e.getMessage()));
        }

        long after = System.nanoTime();
        saveTimeStatistics(before, after);
//        Collection<IAxiom> activeAxioms = ontology.getAllActiveAxioms();
//        ExperimentHelper.saveInHumanReadableForm(activeAxioms, forgettingviewPath + ".clausal");
//        OWLOntology owlOntology = ExperimentHelper.saveAxiomsInOWLForm(activeAxioms, forgettingviewPath + ".owl");
    }

    private static IOntology toClausalForm(List<IAxiom> axioms, List<IConceptLiteral> sig,
                                           DefinerFactory definerFactory)
    {
        IOntology ontology = new Ontology();
        ClausalConverter converter = new ClausalConverter(definerFactory);
        axioms.stream()
                .filter(Objects::nonNull)
                .flatMap(axiom -> converter.toClausalForm(axiom, sig).stream())
                .map(clause -> FactoryMgr.getCommonFactory().createSubsetAxiom(
                        FactoryMgr.getCommonFactory().createAtomicClause(IConceptLiteral.TOP), clause))
                .forEach(ontology::addAxiom);

        return ontology;
    }

    @NotNull
    private static String getDefinerFileContent(Collection<? extends ILiteral> definers)
    {
        return definers.stream()
                .map(l -> String.format("%s", l.getSymbol()))
                .collect(Collectors.joining("\n"));
    }

    @NotNull
    private static File createDebugFolder(String out)
    {
        File debugFolder = new File(out + File.separator + "debug");
        //noinspection ResultOfMethodCallIgnored
        debugFolder.mkdirs();
        return debugFolder;
    }

    @NotNull
    private static List<IConceptLiteral> getSortedForgettingSignature()
    {
        List<IConceptLiteral> list = new ArrayList<>();
        ISymbolFactory factory = FactoryMgr.getSymbolFactory();
        for (SymbolData symbolData : signature) {
            String label = symbolData.getLabel();
            String s = factory.normaliseSymbol(label);
            ILiteral conceptLiteral = factory.getLiteralForText(s);
            if (conceptLiteral instanceof IConceptLiteral) {
                list.add((IConceptLiteral) conceptLiteral);
            }
        }
//        Map<ILiteral, Long> count = countAxioms(axioms, list);
//        list.sort((symbol1, symbol2) -> {
//            long count1 = count.get(symbol1);
//            long count2 = count.get(symbol2);
//            return Long.compare(count1, count2);
//        });

        return list;
    }

    private static Map<ILiteral, Long> countAxioms(List<IAxiom> axioms, List<? extends ILiteral> literals)
    {
        Map<ILiteral, Long> count = new HashMap<>();
        for (ILiteral literal : literals) {
            count.put(literal, axioms.stream().filter(a -> a.getSignature().contains(literal)).count());
        }
        return count;
    }

    private static void saveTimeStatistics(long before, long after) throws IOException
    {
        try (Writer out = new FileWriter(new File(outDir.getPath() + File.separator + "time"))) {
            ExperimentHelper.logElapsedTime(after, before, out);
        }
    }

    private static void saveOntology(Collection<IAxiom> ontology, String out)
            throws OWLOntologyCreationException, OWLOntologyStorageException, IOException
    {
        ExperimentHelper.saveAxioms(ontology, out);
    }

    private static IRoleLiteral getRole(ForgettingEngine engine, IConceptLiteral definer)
    {
        return engine.getDefinerFactory().getRole(definer);
    }

    private static void parseArguements(String[] args) throws IOException
    {
        if (args.length < 6) {
            System.out.println(usage);
            System.exit(1);
        }

        for (int i = 0; i < args.length; i++) {
            String arg0 = args[i];
            if ("-ont".equalsIgnoreCase(arg0)) {
                axioms = ArgsHelper.parseOntologyAsAxioms(ArgsHelper.parseFileArguement(args[++i]));

            }
            else if ("-sig".equalsIgnoreCase(arg0)) {
                signature = ArgsHelper.parseSignatureFileArg(args[++i], usage);
            }
            else if ("-outDir".equalsIgnoreCase(arg0)) {
                outDir = ArgsHelper.parseOutputDirArg(arg0, args[++i], usage);
            }
            else if ("-timeout".equalsIgnoreCase(arg0)) {
                timeOut = Long.parseLong(args[++i]);
            }
            else if ("-reduceToDeductive".equalsIgnoreCase(arg0)) {
                reduceToDeductive = true;
            }
            else if ("-verbose".equalsIgnoreCase(arg0)) {
                verbose = true;
            }
        }
    }
}
