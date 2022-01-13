package pc1.pc2.pc3;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import pc1.pc2.pc3.app.Bootstrap;
import pc1.pc2.pc3.experiment.OntologyLoader;
import pc1.pc2.pc3.rest.OntologyData;
import pc1.pc2.pc3.rest.SymbolData;

import java.io.*;
import java.util.List;

public class OfflineDataLoader
{
    private static final String usage = String.format("%s\n%s\n%s\n%s\n%s\n%s\n%s\n%s\n%s",
            "DataLoader:",
            "Uses an ontology descriptor file and a signature file to load the ontology",
            "and extract a test ontology using the given signature.",
            "If the ontology was downloaded from the internet, it will be written to the repository.",
            "This program is designed as a pipeline step after ExperimentPlanner program.",
            "Usage: DataLoader -outDir output-directory -sig signature -ont ontology [options]",
            "\toutput-directory: a path to the directory where the descriptor files will be written.",
            "\tsignature: signature descriptor file.",
            "\tontology: ontology descriptor file.");

    private static File outDir;
    private static List<SymbolData> signature;
    private static OntologyData ontologyDesc;
    private static boolean asAxioms = false;
    private static boolean label = false;

    public static void main(String[] args) throws IOException, OWLOntologyCreationException, OWLOntologyStorageException
    {
        Bootstrap.initializeApplication();
        parseArguements(args);
        OntologyLoader loader = new OntologyLoader();

        if (ontologyDesc.getLocalIRI() == null) {
            System.out.println("Invalid ontology descriptor. Descriptor does not have local IRI");
        }
        File outFile = new File(outDir + File.separator + "Ontology.owl");

        OWLOntology owlOntology;
        try (FileOutputStream out = new FileOutputStream(outFile)) {
            owlOntology = loader.loadOntology(ontologyDesc.getIRI(), out, OWLManager.createOWLOntologyManager());
        }

        try (Writer out = new FileWriter(new File(outDir.getPath() + File.separator + "concepts"))) {
            out.write(String.valueOf(owlOntology.signature().filter(e -> e.isOWLClass()).count()));
        }

//        long before = System.nanoTime();
//        long after = before;
//        if (owlOntology != null && owlOntology.getLogicalAxiomCount() > 0) {
//            if (asAxioms) {
//                List<IAxiom> axioms = loader.parseOntologyAsAxioms(owlOntology, true, true);
//                after = System.nanoTime();
//                if (label) {
//                    List<IAxiom> labelledAxioms = new LinkedList<>();
//                    for (int i = 0; i < axioms.size(); i++) {
//                        IAxiom axiom = axioms.get(i);
//                        if (axiom.getType() == AxiomType.INCLUSION) {
//                            IComplexClause left = FactoryMgr.getCommonFactory().createConjunctiveClause();
//                            Normalizer.addSubClause(left, FactoryMgr.getCommonFactory().createAtomicClause("_A" + i));
//                            Normalizer.addSubClause(left, axiom.getLeft());
//                            IAxiom ax = FactoryMgr.getCommonFactory().createSubsetAxiom(left, axiom.getRight());
//                            labelledAxioms.add(ax);
//                        }
//                        else {
//                            labelledAxioms.add(axiom);
//                        }
//                    }
//                    axioms = labelledAxioms;
//                }
//                if (!axioms.isEmpty()) {
//                    ExperimentHelper.saveAxioms(axioms,
//                            outDir.getCanonicalPath() + File.separator +
//                                    String.format("%s.clausal", "Ontology"));
//                }
//            }
//            else {
//                List<IAxiom> axioms = loader.parseOntologyAsClauses(owlOntology);
//                after = System.nanoTime();
//                if (!axioms.isEmpty()) {
//                    ExperimentHelper.saveAxioms(axioms,
//                            outDir.getCanonicalPath() + File.separator +
//                                    String.format("%s.clausal", "Ontology"));
//                }
//            }
//        }
//        else {
//            System.out.println("Ontology is empty");
//        }
//
//        try (Writer out = new FileWriter(new File(outDir.getPath() + File.separator + "dataloading" + ".time"))) {
//            ExperimentHelper.logElapsedTime(after, before, out);
//        }
    }

    private static void parseArguements(String[] args)
    {
        if (args.length < 6) {
            System.out.println(usage);
            System.exit(1);
        }

        for (int i = 0; i < args.length; i++) {
            String arg0 = args[i];
            if ("-outDir".equalsIgnoreCase(arg0)) {
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
            else if ("-label".equalsIgnoreCase(arg0)) {
                label = true;
            }
        }
        if (outDir == null || signature == null || ontologyDesc == null) {
            System.out.println(usage);
            System.exit(1);
        }
    }
}
