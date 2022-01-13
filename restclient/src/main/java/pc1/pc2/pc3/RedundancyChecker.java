package pc1.pc2.pc3;

import com.google.common.io.Files;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import pc1.pc2.pc3.app.Bootstrap;
import pc1.pc2.pc3.experiment.VerboseRedundancyChecker;
import pc1.pc2.pc3.input.owl5.AxiomVisitor;
import pc1.pc2.pc3.input.owl5.LoadingException;
import pc1.pc2.pc3.input.owl5.OWLHelper;
import pc1.pc2.pc3.input.owl5.format.Formatter;
import pc1.pc2.pc3.om.IAxiom;

import java.io.*;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class RedundancyChecker
{
    private static final String usage = String.format("%s\n%s\n%s\n%s\n%s\n%s\n%s\n%s\n",
            "RedundancyChecker:",
            "Filters out clauses from the first ontology that are a consequence of the second ",
            "Usage: RedundancyChecker -ont consequences-ontology -bg reference-background -outDir output-directory " +
                    "[options]",
            "\tconsequences: path to the consequences ontology file with extension .owl.",
            "\treference-background: path to the reference ontology in owl format.",
            "\toutput-directory: a path to the directory where the uniform interpolant any other debug files will be " +
                    "written.",
            "Options:",
            "\t-verbose: verbose output.");
    private static File background;
    private static File ontFile;
    private static File outDir;
    private static boolean verbose;
    private static String redundanciesFileName;
    private static String timeLogFileName;

    public static void main(
            String[] args) throws IOException, OWLOntologyCreationException, OWLOntologyStorageException, LoadingException
    {
        Bootstrap.initializeApplication();
        parseArguements(args);

        OWLOntologyManager owlmgr = OWLManager.createOWLOntologyManager();
        OWLOntology ontology = OWLHelper.loadOntology(IRI.create(ontFile), null, owlmgr);
        OWLOntology bg = OWLHelper.loadOntology(IRI.create(background), null, owlmgr);

        System.out.println("Checking redundancies...");

        pc1.pc2.pc3.experiment.RedundancyChecker checker;
        OutputStreamWriter out = new OutputStreamWriter(System.out);
        if (verbose) {
            checker = new VerboseRedundancyChecker(ontology, bg, outDir, out);
        }
        else {
            checker = new pc1.pc2.pc3.experiment.RedundancyChecker(ontology, bg);
        }

        long before = System.nanoTime();
        OWLOntology redundancies = checker.findRedundantClauses();
        long after = System.nanoTime();

        List<IAxiom> clauses = new AxiomVisitor().parse(redundancies, Formatter.Nnf);

        String outPath = outDir.getCanonicalPath();
        ExperimentHelper.save(clauses, outPath, redundanciesFileName, ExperimentHelper.SaveFormat.CLAUSAL, ExperimentHelper.SaveFormat.OWL);

        System.out.println(String.format("Found %d redundant clauses.", clauses.size()));
        logElapsedTime(after, before, out);

        try (FileWriter writer = new FileWriter(outPath + File.separator + timeLogFileName + ".time")) {
            logElapsedTime(after, before, writer);
        }

        redundancies.axioms().forEach(ontology::remove);

        List<IAxiom> unRedundant = new AxiomVisitor().parse(ontology, Formatter.Default);

        String nameWithoutExtension = Files.getNameWithoutExtension(ontFile.getPath());
        ExperimentHelper.save(unRedundant, outPath, nameWithoutExtension + "NoRedundancy",
                ExperimentHelper.SaveFormat.CLAUSAL, ExperimentHelper.SaveFormat.OWL);
    }

    private static void logElapsedTime(long end, long start, Writer out)
    {
        long elapsedTime = end - start;
        try {
            out.append(String.format("Time consumed (Nano seconds) = %d", elapsedTime)).append('\n');
        } catch (IOException e) {
            e.printStackTrace();
        }
        long seconds = TimeUnit.SECONDS.convert(elapsedTime, TimeUnit.NANOSECONDS);
        elapsedTime -= TimeUnit.NANOSECONDS.convert(seconds, TimeUnit.SECONDS);
        long milliSeconds = TimeUnit.MILLISECONDS.convert(elapsedTime, TimeUnit.NANOSECONDS);
        try {
            out.append(String.format("Time consumed (Seconds.Milliseconds) = %d.%d", seconds, milliSeconds))
                    .append('\n');
        } catch (IOException e) {
            e.printStackTrace();
        }
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
            else if ("-outDir".equalsIgnoreCase(arg0)) {
                outDir = ArgsHelper.parseOutputDirArg(arg0, args[++i], usage);
            }
            else if ("-redundanciesFileName".equalsIgnoreCase(arg0)) {
                redundanciesFileName = args[++i];
            }
            else if ("-timeLogFileName".equalsIgnoreCase(arg0)) {
                timeLogFileName = args[++i];
            }
            else if ("-verbose".equalsIgnoreCase(arg0)) {
                verbose = true;
            }
        }
    }
}
