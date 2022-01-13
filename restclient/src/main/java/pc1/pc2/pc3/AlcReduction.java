package pc1.pc2.pc3;

import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import pc1.pc2.pc3.app.Bootstrap;
import pc1.pc2.pc3.experiment.VerboseAlc;
import pc1.pc2.pc3.logic.factory.LogicFactory;
import pc1.pc2.pc3.logic.helper.ClauseHelper;
import pc1.pc2.pc3.logic.om.ResolvableClause;
import pc1.pc2.pc3.logic.om.SignatureMgr;
import pc1.pc2.pc3.logic.reduction.Alc;
import pc1.pc2.pc3.om.*;
import pc1.pc2.pc3.utils.CollectionUtils;
import pc1.pc2.pc3.utils.FileUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.*;
import java.util.stream.Collectors;

public class AlcReduction
{

    private static final String usage = String.format("%s\n%s\n%s\n%s\n%s\n%s\n%s\n%s\n%s\n%s\n%s",
            "AlcReduction:",
            "Reduces a semantic forgetting view to ALC logic.",
            "This program is designed as a pipleline step that should be executed after WhiteBoxForgetting or " +
                    "ForgetRTEmptyBG.",
            "Usage: AlcReduction -bg background-theory -ont ontology -outDir output-directory -definers definers-file" +
                    " -name name [options]",
            "\tontology: path to the semantic forgetting view file. The file should be in string clausal form",
            "\tbackground-theory: path to the residual of the background theory after the semantic forgetting step",
            "\toutput-directory: a path to the directory where output files will be written.",
            "\tdefiners-file: a file containing the names of the definer symbols.",
            "\tname: a unique name to prefix any output files",
            "Options:",
            "\t-verbose: verbose output.");
    private static File bgFile;
    private static File ontFile;
    private static File definersFile;
    private static File outDir;
    private static String name;
    private static boolean verbose;

    public static void main(String[] args) throws IOException, OWLOntologyCreationException, OWLOntologyStorageException
    {
        Bootstrap.initializeApplication();
        parseArguements(args);

        List<IConceptLiteral> definers = loadDefiners();
        Map<IConceptLiteral, IRoleLiteral> definerRole = loadDefinerRoles();
        List<IClause> ontology = ArgsHelper.parseOntology(ontFile).stream()
                .map(IAxiom::getRight)
                .collect(Collectors.toList());
        List<IClause> bg = ArgsHelper.parseOntology(bgFile).stream()
                .map(IAxiom::getRight)
                .collect(Collectors.toList());
        SignatureMgr mgr = new SignatureMgr(getSignature(ontology, bg));
        List<ResolvableClause> ont = convertToResolvable(ontology, mgr);
        List<ResolvableClause> theory = convertToResolvable(bg, mgr);

        long before = System.nanoTime();
        Alc toAlc = new VerboseAlc(ont, theory, definers, definerRole, name, outDir);
        List<IClause> deductiveView = toAlc.reduce();
        long after = System.nanoTime();

        try (Writer out = new FileWriter(outDir.getPath() + File.separator + name + "Reduction.time")) {
            ExperimentHelper.logElapsedTime(after, before, out);
        } catch (IOException e) {
            e.printStackTrace();
        }

        ExperimentHelper.save(ClauseHelper.asAxioms(deductiveView), outDir.getCanonicalPath(), name,
                ExperimentHelper.SaveFormat.CLAUSAL, ExperimentHelper.SaveFormat.OWL);
    }

    private static List<ILiteral> getSignature(Collection<IClause> ontology, Collection<IClause> theory)
    {
        return ClauseHelper.calculateSignature(CollectionUtils.merge(ontology, theory),
                Comparator.comparing(ILiteral::getSymbol));
    }

    private static List<ResolvableClause> convertToResolvable(Collection<IClause> normalisedOntology,
                                                              SignatureMgr signatureMgr)
    {
        LogicFactory factory = new LogicFactory(signatureMgr);
        return normalisedOntology.stream().map(factory::createResolvable).collect(Collectors.toList());
    }

    private static Map<IConceptLiteral, IRoleLiteral> loadDefinerRoles() throws IOException
    {
        List<String> lines = FileUtils.loadFile(definersFile);
        Map<IConceptLiteral, IRoleLiteral> definers = new HashMap<>();
        for (String line : lines) {
            String[] split = line.split(",");
            IConceptLiteral definer =
                    (IConceptLiteral) FactoryMgr.getSymbolFactory().getLiteralForText(split[0].trim());
            if (definer == null) {
                definer = FactoryMgr.getSymbolFactory().createConceptLiteral(split[0].trim());
            }
            IRoleLiteral role = FactoryMgr.getSymbolFactory().createRoleLiteral(split[2].trim());
            definers.put(definer, role);
        }
        return definers;
    }

    private static List<IConceptLiteral> loadDefiners() throws IOException
    {
        List<String> lines = FileUtils.loadFile(definersFile);
        List<IConceptLiteral> definers = new LinkedList<>();
        for (String line : lines) {
            String[] split = line.split(",");
            definers.add(FactoryMgr.getSymbolFactory().createConceptLiteral(split[0].trim()));
        }
        return definers;
    }

    private static void parseArguements(String[] args)
    {
        if (args.length < 10) {
            System.out.println(usage);
            System.exit(1);
        }

        for (int i = 0; i < args.length; i++) {
            String arg0 = args[i];
            if ("-bg".equalsIgnoreCase(arg0)) {
                bgFile = ArgsHelper.parseFileArguement(args[++i]);
            }
            else if ("-ont".equalsIgnoreCase(arg0)) {
                ontFile = ArgsHelper.parseFileArguement(args[++i]);
            }
            else if ("-definers".equalsIgnoreCase(arg0)) {
                definersFile = ArgsHelper.parseFileArguement(args[++i]);
            }
            else if ("-outDir".equalsIgnoreCase(arg0)) {
                outDir = ArgsHelper.parseOutputDirArg(arg0, args[++i], usage);
            }
            else if ("-name".equalsIgnoreCase(arg0)) {
                name = args[++i];
            }
            else if ("-verbose".equalsIgnoreCase(arg0)) {
                verbose = true;
            }
        }
    }
}
