package pc1.pc2.pc3;

import org.jetbrains.annotations.NotNull;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import pc1.pc2.pc3.app.Bootstrap;
import pc1.pc2.pc3.rest.SymbolData;
import pc1.pc2.pc3.error.TimeException;
import pc1.pc2.pc3.logic.ForgettingEngine;
import pc1.pc2.pc3.logic.helper.ClauseHelper;
import pc1.pc2.pc3.logic.helper.OntologyPair;
import pc1.pc2.pc3.logic.om.ResolvableClause;
import pc1.pc2.pc3.om.*;
import pc1.pc2.pc3.utils.StringUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class WhiteBoxForgetting
{
    public static final String EXPERIMENT_NAME = "GlassBox";
    private static final String usage = String.format("%s\n%s\n%s\n%s\n%s\n%s\n%s\n%s\n%s\n%s\n%s\n%s",
            "WhiteBoxForgetting:",
            "Performs forgetting experiment and generates a uniform interpolant file.",
            "This program is designed as a pipleline step that should be executed after DataLoader.",
            "Usage: WhiteBoxForgetting -bg data-directory -ont ontology-file -outDir output-directory -sig " +
                    "signature-file [options]",
            "\tdata-directory: a directory that contains files with extension .clausal.",
            "\t\tThese files will be loaded and parsed to construct background theory.",
            "\tontology-file: path to the main ontology file. The ontology should be in string clausal form",
            "\t\tif file is present in data-directory then it will be excluded from the background theory",
            "\toutput-directory: a path to the directory where the uniform interpolant any other debug files will be " +
                    "written.",
            "\tsignature-file: a signature file in JSON format.",
            "Options:",
            "\t-verbose: verbose output.");
    private static List<IClause> theory;
    private static List<IClause> ontology;
    private static List<SymbolData> signature;
    private static File outDir;
    private static boolean verbose;

    public static void main(String[] args)
            throws IOException, OWLOntologyCreationException, OWLOntologyStorageException, TimeException
    {
        Bootstrap.initializeApplication();
        parseArguements(args);
        List<IConceptLiteral> sig = getSignature();
        String out = outDir.getCanonicalPath();
        ForgettingEngine engine = ArgsHelper.createForgettingEngine(verbose, createDebugFolder(out), EXPERIMENT_NAME);

        long before = System.nanoTime();
        OntologyPair ontologyPair = engine.forgetSemantic(ontology, theory, sig);
        long after = System.nanoTime();

        saveTimeStatistics(before, after);
        saveOntology(out, ontologyPair.ontology, "SemanticView");
        saveOntology(out, ontologyPair.background, "SemanticBG");
        Path filePath = Path.of(out + File.separator + EXPERIMENT_NAME + ".definers");
        String file = ontologyPair.definers.stream()
                .map(l -> String.format("%s,%s,%s", l.getSymbol(), l.isExistentialDefiner(), getRole(engine, l)))
                .collect(Collectors.joining("\n"));
        Files.write(filePath, StringUtils.toByteArray(file));
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
    private static List<IConceptLiteral> getSignature()
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
        return list;
    }

    private static void saveTimeStatistics(long before, long after) throws IOException
    {
        try (Writer out = new FileWriter(new File(outDir.getPath() + File.separator + EXPERIMENT_NAME + ".time"))) {
            ExperimentHelper.logElapsedTime(after, before, out);
        }
    }

    private static void saveOntology(String out, List<ResolvableClause> ontology2, String fileName)
            throws OWLOntologyCreationException, OWLOntologyStorageException, IOException
    {
        List<IAxiom> ontClauses = ClauseHelper.convertToAxioms(ontology2);
        ExperimentHelper.save(ontClauses, out, EXPERIMENT_NAME + fileName, ExperimentHelper.SaveFormat.CLAUSAL, ExperimentHelper.SaveFormat.OWL);
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

        theory = Collections.emptyList();
        for (int i = 0; i < args.length; i++) {
            String arg0 = args[i];
            if ("-bg".equalsIgnoreCase(arg0)) {
                theory = ArgsHelper.parseOntology(ArgsHelper.parseFileArguement(args[++i])).stream()
                        .map(IAxiom::getRight)
                        .collect(Collectors.toList());
            }
            else if ("-ont".equalsIgnoreCase(arg0)) {
                ontology = ArgsHelper.parseOntology(ArgsHelper.parseFileArguement(args[++i])).stream()
                        .map(IAxiom::getRight)
                        .collect(Collectors.toList());
            }
            else if ("-sig".equalsIgnoreCase(arg0)) {
                signature = ArgsHelper.parseSignatureFileArg(args[++i], usage);
            }
            else if ("-outDir".equalsIgnoreCase(arg0)) {
                outDir = ArgsHelper.parseOutputDirArg(arg0, args[++i], usage);
            }
            else if ("-verbose".equalsIgnoreCase(arg0)) {
                verbose = true;
            }
            else if ("-timeout".equalsIgnoreCase(arg0)) {
                FactoryMgr.setMaxTime(Integer.parseInt(args[++i]));
            }
        }
    }
}
