package pc1.pc2.pc3;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import pc1.pc2.pc3.app.Bootstrap;
import pc1.pc2.pc3.experiment.RedundancyChecker;
import pc1.pc2.pc3.input.owl5.AxiomVisitor;
import pc1.pc2.pc3.input.owl5.LoadingException;
import pc1.pc2.pc3.input.owl5.OWLHelper;
import pc1.pc2.pc3.input.owl5.format.Formatter;
import pc1.pc2.pc3.om.IAxiom;
import pc1.pc2.pc3.output.Output;
import pc1.pc2.pc3.utils.AxiomSimplifier;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

public class ForgettingViewCompare
{
    private static final String usage = String.format("%s\n%s\n%s\n%s\n%s\n%s\n%s",
            "ForgettingViewCompare:",
            "Compares two forgetting views. Unentailed clauses are written to Untailed",
            "Usage: ForgettingViewCompare -ref reference-ontology -ont ontology-file -outDir output-directory " +
                    "-unentailed unentailed-file",
            "\treference-ontology: path to the reference ontology .owl.",
            "\tontology-file: path to the main ontology file. The ontology should be in .owl format",
            "\toutput-directory: a path to the directory where the uniform interpolant any other debug files will be " +
                    "written.",
            "\tunentailed-file: name of output unentailed clauses file.");

    private static File reference;
    private static File ontFile;
    private static File outDir;
    private static String unentailedFile;

    public static void main(String[] args) throws LoadingException, OWLOntologyStorageException, OWLOntologyCreationException, IOException
    {
        Bootstrap.initializeApplication();

        parseArguements(args);

        OWLOntologyManager owlmgr = OWLManager.createOWLOntologyManager();
        OWLOntology ont = OWLHelper.loadOntology(IRI.create(ontFile), null, owlmgr);
        OWLOntology ref = OWLHelper.loadOntology(IRI.create(reference), null, owlmgr);

        ont = simplify(ont);
        ref = simplify(ref);

        RedundancyChecker checker = new RedundancyChecker(ont, ref);
        OWLOntology redundancies = checker.findRedundantClauses();

        redundancies.axioms().forEach(ont::remove);

        Collection<IAxiom> result = new AxiomVisitor().parse(ont, Formatter.Default);
        result = new AxiomSimplifier().simplify(result);
        //Todo: Subsumption Deletion new ClauseSubsumptionChecker().eliminate(ontology);
        ExperimentHelper.save(result, outDir.getPath(), unentailedFile,
                ExperimentHelper.SaveFormat.CLAUSAL, ExperimentHelper.SaveFormat.OWL);
    }

    private static OWLOntology simplify(OWLOntology ont) throws OWLOntologyCreationException
    {
        List<IAxiom> ontology = new AxiomVisitor().parse(ont, Formatter.Default);
        Collection<IAxiom> simplifiedOntology = new AxiomSimplifier().simplify(ontology);
        ont = Output.convertAxioms(simplifiedOntology, IRI.create(ontFile));
        return ont;
    }

    private static void parseArguements(String[] args)
    {
        if (args.length < 8) {
            System.out.println(usage);
            System.exit(1);
        }

        for (int i = 0; i < args.length; i++) {
            String arg0 = args[i];
            if ("-ref".equalsIgnoreCase(arg0)) {
                reference = ArgsHelper.parseFileArguement(args[++i]);
            }
            else if ("-ont".equalsIgnoreCase(arg0)) {
                ontFile = ArgsHelper.parseFileArguement(args[++i]);
            }
            else if ("-outDir".equalsIgnoreCase(arg0)) {
                outDir = ArgsHelper.parseOutputDirArg(arg0, args[++i], usage);
            }
            else if("-unentailed".equalsIgnoreCase(arg0)) {
                unentailedFile = args[++i];
            }
        }
    }
}
