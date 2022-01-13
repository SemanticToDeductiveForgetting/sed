package pc1.pc2.pc3;

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
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class ForgetRTEmptyBG
{
    public static final String EXPERIMENT_NAME = "BlackBox";
    private static final String usage = String.format("%s\n%s\n%s\n%s\n%s\n%s\n%s\n%s\n%s\n%s\n%s\n%s",
            "ForgetRTEmptyBG:",
            "Performs forgetting experiment relative to empty background theory and generates a uniform ",
            "interpolant file. This program is designed as a pipleline step that should be executed after DataLoader.",
            "Usage: ForgetRTEmptyBG -bg data-directory -ont ontology-file -outDir output-directory -sig " +
                    "signature-file [options]",
            "\tbackground-file: path to the background ontology file with extension .clausal.",
            "\t\tThese files will be loaded and parsed to construct background theory.",
            "\tontology-file: path to the main ontology file. The ontology should be in string clausal form",
            "\t\tif file is present in data-directory then it will be excluded from the background theory",
            "\toutput-directory: a path to the directory where the uniform interpolant any other debug files will be " +
                    "written.",
            "\tsignature-file: a signature file in JSON format.",
            "Options:",
            "\t-verbose: verbose output.");
    private static File background = null;
    private static File ontFile;
    private static List<SymbolData> signature;
    private static File outDir;
    private static boolean verbose;
    private static boolean semanticForgetting = false;

    public static void main(String[] args)
            throws IOException, OWLOntologyCreationException, OWLOntologyStorageException, TimeException
    {
        Bootstrap.initializeApplication();
        parseArguements(args);

        System.out.println("Executing experiment with empty background...");

        List<IConceptLiteral> sig = signature.stream()
                .map(SymbolData::getLabel)
                .map(FactoryMgr.getSymbolFactory()::normaliseSymbol)
                .map(FactoryMgr.getSymbolFactory()::createConceptLiteral)
                .collect(Collectors.toList());

        List<IClause> ontology = ArgsHelper.parseOntology(ontFile).stream()
                .map(IAxiom::getRight)
                .collect(Collectors.toList());
        if (background != null) {
            ArgsHelper.parseOntology(background).stream()
                    .map(IAxiom::getRight)
                    .forEach(ontology::add);
        }

        ForgettingEngine engine = ArgsHelper.createForgettingEngine(verbose, outDir, EXPERIMENT_NAME);
        String outDirPath = outDir.getCanonicalPath();
        if (semanticForgetting) {
            ExperimentHelper.save(ClauseHelper.asAxioms(ontology),
                    outDirPath, "OntologyAndBG", ExperimentHelper.SaveFormat.CLAUSAL, ExperimentHelper.SaveFormat.OWL);
            OntologyPair ontologyPair = engine.forgetSemantic(ontology, new LinkedList<>(), sig);
            saveOntology(outDirPath, ontologyPair.ontology, "SemanticView");
            saveOntology(outDirPath, ontologyPair.background, "SemanticBG");
            Path filePath = Path.of(outDirPath + File.separator + EXPERIMENT_NAME + ".definers");
            String file = ontologyPair.definers.stream()
                    .map(l -> String.format("%s,%s,%s", l.getSymbol(), l.isExistentialDefiner(), getRole(engine, l)))
                    .collect(Collectors.joining("\n"));
            Files.write(filePath, StringUtils.toByteArray(file));

        }
        else {
            List<IClause> interpolant = engine.forgetClauses(ontology, new LinkedList<>(), sig);
            ExperimentHelper.save(ClauseHelper.asAxioms(interpolant),
                    outDirPath, EXPERIMENT_NAME, ExperimentHelper.SaveFormat.CLAUSAL, ExperimentHelper.SaveFormat.OWL);
        }
    }

    private static void saveOntology(String out, List<ResolvableClause> ontology2, String semanticView)
            throws OWLOntologyCreationException, OWLOntologyStorageException, IOException
    {
        List<IClause> ontClauses = pc1.pc2.pc3.logic.helper.ClauseHelper.convertToClauses(ontology2);
        ExperimentHelper.save(ClauseHelper.asAxioms(ontClauses),
                out, EXPERIMENT_NAME + semanticView, ExperimentHelper.SaveFormat.CLAUSAL, ExperimentHelper.SaveFormat.OWL);
    }

    private static IRoleLiteral getRole(ForgettingEngine engine, IConceptLiteral definer)
    {
        return engine.getDefinerFactory().getRole(definer);
    }

    private static void parseArguements(String[] args)
    {
        if (args.length < 6) {
            System.out.println(usage);
            System.exit(1);
        }

        for (int i = 0; i < args.length; i++) {
            String arg0 = args[i];
            if ("-bg".equalsIgnoreCase(arg0)) {
                background = ArgsHelper.parseFileArguement(args[++i]);
            }
            else if ("-ont".equalsIgnoreCase(arg0)) {
                ontFile = ArgsHelper.parseFileArguement(args[++i]);
            }
            else if ("-sig".equalsIgnoreCase(arg0)) {
                signature = ArgsHelper.parseSignatureFileArg(args[++i], usage);
            }
            else if ("-outDir".equalsIgnoreCase(arg0)) {
                outDir = ArgsHelper.parseOutputDirArg(arg0, args[++i], usage);
            }
            else if ("-semantic".equalsIgnoreCase(arg0)) {
                semanticForgetting = true;
            }
            else if ("timeout".equalsIgnoreCase(arg0)) {
                int timeout = Integer.parseInt(args[++i]);
                FactoryMgr.setMaxTime(timeout);
            }
            else if ("-verbose".equalsIgnoreCase(arg0)) {
                verbose = true;
            }
        }
    }
}

