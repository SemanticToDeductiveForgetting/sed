package pc1.pc2.pc3;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import pc1.pc2.pc3.app.Bootstrap;
import pc1.pc2.pc3.input.owl5.LoadingException;
import pc1.pc2.pc3.input.owl5.OWLHelper;
import pc1.pc2.pc3.rest.SymbolData;
import pc1.pc2.pc3.om.FactoryMgr;
import pc1.pc2.pc3.utils.FileUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.*;
import java.util.stream.Collectors;

public class ExistingCoverageCalculator
{
    private static String usage = String.format("%s\n%s\n%s\n",
            "CoverageCalculator:",
            "Generates a csv containing the coverage of concept symbols",
            "Usage: CoverageCalculator -repo repository -outDir output-Directory -ontology ontologyName");

    private static final String conceptCoverageFiles = "C:/ccviews/forget/restclient/ontologies/conceptCoverage";
    private static final String ontologies = "C:/ccviews/forget/restclient/ontologies";
    private static final String experiments = "C:/ccviews/forget/experiment";
    private static File summaryFile;

    private Map<OWLEntity, Integer> occurance = new HashMap<>();
    private List<Integer> axiomSize = new LinkedList<>();

    public ExistingCoverageCalculator()
    {

    }

    public static void main(String[] args) throws IOException, LoadingException, OWLOntologyStorageException
    {
        Bootstrap.initializeApplication();

        File exps = new File(experiments);
        File[] experimentFolders = exps.listFiles((f, n) -> n.startsWith("experiment") && f.isDirectory());
        Map<String, Integer> coverageInExperiment = new LinkedHashMap<>();
        Map<String, int[]> overlaps = new LinkedHashMap<>();
        for (File experimentFolder : experimentFolders) {
            List<SymbolData> signature = ArgsHelper
                    .parseSignatureFileArg(experimentFolder.getPath() + File.separator + "data/signature",
                            "Invalid signature file " + experimentFolder.getName());

            File coverageFile = new File(
                    conceptCoverageFiles + File.separator + experimentFolder.getName() + "_conceptCoverage.csv");
            Map<Symbol, Integer> coverage = parseCoverageFile(coverageFile);
            File ontologyFile = new File(ontologies + File.separator + experimentFolder.getName());

            ExistingCoverageCalculator calculator = new ExistingCoverageCalculator();
            int totalCoverage = calculator.totalCoverage(signature, coverage);
            if(ontologyFile.exists()) {
                OWLOntology ontology = loadOntology(ontologyFile);
                overlaps.put(experimentFolder.getName(), calculator.overlap(signature, coverage, ontology));
            }

            coverageInExperiment.put(experimentFolder.getName(), totalCoverage);

        }

        summaryFile = new File(experiments + File.separator + "experimentCoverage.csv");
        int[] empty = new int[0];
        try (OutputStreamWriter writer = new FileWriter(summaryFile)) {
            writer.append("Ontology").append(',').append("Coverage").append('\n');
            for (Map.Entry<String, Integer> entry : coverageInExperiment.entrySet()) {
                writer.append(String.format("%s,%s", entry.getKey(), entry.getValue()));
                int[] overlap = overlaps.getOrDefault(entry.getKey(), empty);
                for (int i : overlap) {
                    writer.append(',').append(String.valueOf(i));
                }
                writer.append('\n');
            }
        }
    }

    private int[] overlap(List<SymbolData> signature, Map<Symbol, Integer> coverage, OWLOntology ontology)
    {
        for (SymbolData symbolData : signature) {
            String label = symbolData.getLabel();
            String normalisedLabel = FactoryMgr.getSymbolFactory().normaliseSymbol(label);
            for (Map.Entry<Symbol, Integer> entry : coverage.entrySet()) {
                Symbol key = entry.getKey();
                if (key.label.equals(normalisedLabel)) {
                    symbolData.setIRI("this", key.iri);
                    break;
                }
            }
        }

        Set<IRI> signatureIRI = signature.stream()
                .filter(s -> s.getIRI("this") != null)
                .map(s -> s.getIRI("this"))
                .map(IRI::create)
                .collect(Collectors.toSet());

        List<Long> overlaps = ontology.logicalAxioms()
                .map(axiom -> axiom.signature()
                        .filter(entity -> signatureIRI.contains(entity.getIRI()))
                        .count())
                .collect(Collectors.toList());

        int[] overlapArr = new int[signature.size()];
        overlaps.forEach(i -> overlapArr[Math.toIntExact(i)]++);
        return overlapArr;
    }

    private static @org.jetbrains.annotations.NotNull OWLOntology loadOntology(File ontologyFile) throws OWLOntologyStorageException, LoadingException
    {
        OWLOntologyManager ontologyMgr = OWLManager.createOWLOntologyManager();
        return OWLHelper.loadOntology(IRI.create(ontologyFile), null, ontologyMgr);
    }

    private int totalCoverage(List<SymbolData> signature, Map<Symbol, Integer> coverage)
    {
        int totalCoverage = 0;
        for (SymbolData symbolData : signature) {
            String label = symbolData.getLabel();
            String normalisedLabel = FactoryMgr.getSymbolFactory().normaliseSymbol(label);
            for (Map.Entry<Symbol, Integer> entry : coverage.entrySet()) {
                Symbol key = entry.getKey();
                if (key.label.equals(normalisedLabel)) {
                    totalCoverage += entry.getValue();
                    break;
                }
            }
        }
        return totalCoverage;
    }

    public static Map<Symbol, Integer> parseCoverageFile(File coverage)
    {
        Map<Symbol, Integer> coverageMap = new HashMap<>();
        try {
            if (coverage.isFile() && coverage.exists()) {
                List<String> lines = FileUtils.loadFile(coverage);
                if(!lines.isEmpty()) {
                    lines.remove(0);
                    for (String line : lines) {
                        String[] parts = line.split(",");
                        String iri = parts[0];
                        int i = iri.lastIndexOf('/');
                        String label = iri.substring(i + 1);
                        int coveredAxioms = Integer.parseInt(parts[1].trim());
                        double coveredAxiomsPercentage = Double.parseDouble(parts[2].trim());
                        Symbol key = new Symbol(iri, label, coveredAxioms, coveredAxiomsPercentage);
                        coverageMap.put(key, coveredAxioms);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error parsing coverage file " + coverage);
        }
        return coverageMap;
    }

    public static class Symbol
    {
        private String iri;
        private String label;
        private final int coveredAxioms;
        private final double coveredAxiomsPercentage;

        private Symbol(String iri, String label, int coveredAxioms, double coveredAxiomsPercentage)
        {
            this.iri = iri;
            this.label = label;
            this.coveredAxioms = coveredAxioms;
            this.coveredAxiomsPercentage = coveredAxiomsPercentage;
        }

        public int getCoveredAxioms()
        {
            return coveredAxioms;
        }

        public double getCoveredAxiomsPercentage()
        {
            return coveredAxiomsPercentage;
        }

        public String getLabel()
        {
            return label;
        }

        public String getIri()
        {
            return iri;
        }
    }
}