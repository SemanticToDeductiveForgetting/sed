package pc1.pc2.pc3;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import pc1.pc2.pc3.app.Bootstrap;
import pc1.pc2.pc3.experiment.OntologyLoader;
import pc1.pc2.pc3.experiment.VerboseOntologyLoader;
import pc1.pc2.pc3.rest.OntologyData;
import pc1.pc2.pc3.rest.SymbolData;
import pc1.pc2.pc3.om.IAxiom;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class DataLoader
{
    private static final String usage = String.format("%s\n%s\n%s\n%s\n%s\n%s\n%s\n%s\n%s\n%s\n%s\n%s",
            "DataLoader:",
            "Uses an ontology descriptor file and a signature file to load the ontology",
            "and extract a star module from the ontology using the given signature.",
            "If the ontology was downloaded from the internet, it will be written to the repository.",
            "This program is designed as a pipeline step after ExperimentPlanner program.",
            "Usage: DataLoader -repo repository -outDir output-directory -sig signature -ont ontology [options]",
            "\trepository: a path to the repository that contains local copies of ontologies and the list of classes.",
            "\toutput-directory: a path to the directory where the descriptor files will be written.",
            "\tsignature: signature descriptor file.",
            "\tontology: ontology descriptor file.",
            "Options:",
            "\t-verbose: verbose output.");

    private static boolean verbose;
    private static File repository;
    private static File outDir;
    private static List<SymbolData> signature;
    private static OntologyData ontologyDesc;
    private static boolean asAxioms = false;

    public static void main(String[] args) throws IOException, OWLOntologyCreationException, OWLOntologyStorageException
    {
        Bootstrap.initializeApplication();
        parseArguements(args);
        String repo = repository.getCanonicalPath();
        OutputStreamWriter out = new OutputStreamWriter(System.out);
        OutputStreamWriter err = new OutputStreamWriter(System.err);
        OntologyLoader loader = verbose ? new VerboseOntologyLoader(repo, out, err) : new OntologyLoader();

        FileOutputStream ontStream = null;
        if (ontologyDesc.getLocalIRI() == null) {
            ontStream = new FileOutputStream(repo + File.separator + ontologyDesc.getName());
        }
        OWLOntology owlOntology =
                loader.loadOntology(ontologyDesc.getIRI(), ontStream, OWLManager.createOWLOntologyManager());
        IRI moduleIRI = IRI.create(new File(
                outDir.getCanonicalPath() + File.separator + String.format("%s_module.owl", ontologyDesc.getName())));
        OWLOntology module = loader.extractModule(signatureOf(ontologyDesc.getName()), moduleIRI, owlOntology);
        if (module != null && owlOntology != null && module.getLogicalAxiomCount() > 0) {
            List<IAxiom> axioms;
            axioms = asAxioms ? loader.parseOntologyAsAxioms(module, true, true) :
                    loader.parseOntologyAsClauses(module);
            if (!axioms.isEmpty()) {
                ExperimentHelper.saveAxioms(axioms,
                        outDir.getCanonicalPath() + File.separator +
                                String.format("%s.clausal", ontologyDesc.getName()));
            }
        }
        else {
            System.out.println("Module is empty");
        }
    }

    private static Set<IRI> signatureOf(String ontology)
    {
        return signature.stream()
                .filter(s -> s.getIRI(ontology) != null)
                .map(s -> s.getIRI(ontology))
                .map(IRI::create)
                .collect(Collectors.toSet());
    }

    private static void parseArguements(String[] args)
    {
        if (args.length < 8) {
            System.out.println(usage);
            System.exit(1);
        }

        for (int i = 0; i < args.length; i++) {
            String arg0 = args[i];
            if ("-repo".equalsIgnoreCase(arg0)) {
                repository = ArgsHelper.parseRepositoryArg(arg0, args[++i], usage);
            }
            else if ("-outDir".equalsIgnoreCase(arg0)) {
                outDir = ArgsHelper.parseOutputDirArg(arg0, args[++i], usage);
            }
            else if ("-sig".equalsIgnoreCase(arg0)) {
                signature = ArgsHelper.parseSignatureFileArg(args[++i], usage);
            }
            else if ("-ont".equalsIgnoreCase(arg0)) {
                ontologyDesc = ArgsHelper.parseOntologyDescriptorFileArg(args[++i], usage);
            }
            else if ("-asAxioms".equalsIgnoreCase(arg0)) {
                asAxioms = true;
            }
            else if ("-verbose".equalsIgnoreCase(arg0)) {
                verbose = true;
            }
        }
        if (repository == null || outDir == null || signature == null || ontologyDesc == null) {
            System.out.println(usage);
            System.exit(1);
        }
    }
}
