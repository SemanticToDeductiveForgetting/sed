package pc1.pc2.pc3;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.util.DLExpressivityChecker;
import org.semanticweb.owlapi.util.Languages;
import pc1.pc2.pc3.app.Bootstrap;
import pc1.pc2.pc3.input.owl5.LoadingException;
import pc1.pc2.pc3.input.owl5.OWLHelper;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.*;

public class CoverageCalculator
{
    private static String usage = String.format("%s\n%s\n%s\n",
            "CoverageCalculator:",
            "Generates a csv containing the coverage of concept symbols",
            "Usage: CoverageCalculator -repo repository -outDir output-Directory -ontology ontologyName");

    private static final int SIGNATURE_SIZE = 30;
    private static File repository = null;
    private static File outDir = null;
    private static String ontologyName = null;
    private static File summaryFile;

    private Map<OWLEntity, Integer> occurance = new HashMap<>();
    private List<Integer> axiomSize = new LinkedList<>();

    public CoverageCalculator()
    {

    }

    public static void main(String[] args) throws IOException, LoadingException, OWLOntologyStorageException
    {
        Bootstrap.initializeApplication();
        parseArguements(args);

        OWLOntologyManager owlmgr = OWLManager.createOWLOntologyManager();
        File str = new File(repository + File.separator + ontologyName);
        OWLOntology ontology = OWLHelper.loadOntology(IRI.create(str), null, owlmgr);
        boolean alc = isAlc(ontology);
        String conceptCoverageFile = outDir + File.separator + ontologyName + "_conceptCoverage.csv";
        String axiomSizeFile = outDir + File.separator + ontologyName + "_axiomSize.csv";
        Map<OWLEntity, Integer> coverage = new CoverageCalculator().calculateConceptCoverage(ontology);
        List<Integer> sizeOfAxioms = new CoverageCalculator().calculateAxiomSize(ontology);

        int logicalAxiomCount = ontology.getLogicalAxiomCount();

        try (OutputStreamWriter writer = new FileWriter(conceptCoverageFile)) {
            writer.append("Class,Axioms,Coverage").append('\n');
            for (Map.Entry<OWLEntity, Integer> entry : coverage.entrySet()) {
                OWLEntity e = entry.getKey();
                Integer d = entry.getValue();
                writer.append(String.format("%s,%d,%.4f", e.toStringID(), d, d.doubleValue() / logicalAxiomCount))
                        .append('\n');
            }
        }

        try (OutputStreamWriter writer = new FileWriter(axiomSizeFile)) {
            writer.append("Concepts in Axiom").append('\n');
            for (Integer size : sizeOfAxioms) {
                writer.append(size.toString()).append('\n');
            }
        }

        List<Integer> counts = new ArrayList<>(coverage.values());
        counts.sort(Comparator.reverseOrder());
        int fifty = top(logicalAxiomCount / 2, counts);
        int hundred = top(logicalAxiomCount, counts);
        int hundredFifty = top((int) (logicalAxiomCount * 1.5), counts);
        int twoHundred = top(logicalAxiomCount * 2, counts);
        double averageConceptsInAxiom = sizeOfAxioms.stream().mapToInt(x -> x).average().getAsDouble();
        try (OutputStreamWriter writer = new FileWriter(summaryFile, true)) {
            writer.append('\n');
            writer.append(String.format("%s,%s,%.4f,%d,%d,%d,%d,%d,%d", ontologyName, alc,
                    averageConceptsInAxiom, coverage.size(),
                    logicalAxiomCount, fifty, hundred, hundredFifty, twoHundred));
        }
    }

    private List<Integer> calculateAxiomSize(OWLOntology ontology)
    {
        ontology.logicalAxioms().forEach(this::calculateAxiomSize);
        return axiomSize;
    }

    private void calculateAxiomSize(OWLLogicalAxiom owlLogicalAxiom)
    {
        long count = owlLogicalAxiom.signature()
                .filter(owlEntity -> owlEntity instanceof OWLClass)
                .count();
        if(count > 0) {
            axiomSize.add(Long.valueOf(count).intValue());
        }
    }

    private static int top(int limit, List<Integer> counts)
    {
        int symbols = 0;
        int sum = 0;
        for (Integer count : counts) {
            if (sum < limit) {
                sum += count;
                symbols++;
            }
        }
        return symbols;
    }

    private Map<OWLEntity, Integer> calculateConceptCoverage(OWLOntology ontology)
    {
        ontology.logicalAxioms().forEach(this::getCoverageInAxiom);
//        final double count = ontology.getLogicalAxiomCount();
//        occurance.replaceAll((e, c) -> c / count);
        return occurance;
    }

    private void getCoverageInAxiom(OWLLogicalAxiom axiom)
    {
        axiom.signature()
                .filter(entity -> entity instanceof OWLClass)
                .forEach(owlEntity -> occurance.merge(owlEntity, 1, (x, y) -> x + y));
    }

    private static void parseArguements(String[] args)
    {
        if (args.length < 3) {
            System.out.println(String.format("Incorrect number of arguements. Args: %s", String.join(", ", args)));
            System.out.println(usage);
            System.exit(1);
        }

        for (int i = 0; i < args.length; i++) {
            String arg0 = args[i];
            if ("-repo".equalsIgnoreCase(arg0)) {
                repository = ArgsHelper.parseDirectoryArguement(args[++i]);
            }
            else if ("-outDir".equalsIgnoreCase(arg0)) {
                outDir = ArgsHelper.parseDirectoryArguement(args[++i]);
            }
            else if ("-ont".equalsIgnoreCase(arg0)) {
                ontologyName = args[++i];
            }
            else if ("-summary".equalsIgnoreCase(arg0)) {
                summaryFile = ArgsHelper.parseFileArguement(args[++i]);
            }
        }
        if (repository == null || outDir == null || ontologyName == null) {
            System.out.println(usage);
            System.exit(1);
        }
    }

    private static boolean isAlc(OWLOntology ont)
    {
        DLExpressivityChecker checker = new DLExpressivityChecker(Collections.singleton(ont));
        Collection<Languages> languages = checker.expressibleInLanguages();
        if (!languages.isEmpty()) {
            return languages.stream()
                    .anyMatch(Languages.ALC::isSubLanguageOf);
        }
        return false;
    }
}