package pc1.pc2.pc3;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import pc1.pc2.pc3.app.Bootstrap;
import pc1.pc2.pc3.experiment.ExperimentException;
import pc1.pc2.pc3.experiment.OntologyLoader;
import pc1.pc2.pc3.om.FactoryMgr;
import pc1.pc2.pc3.om.ILiteral;
import pc1.pc2.pc3.om.ISymbolFactory;
import pc1.pc2.pc3.rest.OntologyData;
import pc1.pc2.pc3.rest.Planner;
import pc1.pc2.pc3.rest.SymbolData;
import uk.ac.manchester.cs.owlapi.modularity.ModuleType;
import uk.ac.manchester.cs.owlapi.modularity.SyntacticLocalityModuleExtractor;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class ExperimentPlannerBasedOnStarModule
{
    private static final String usage = String.format("%s\n%s\n%s\n%s\n%s\n%s\n%s\n",
            "ExperimentPlannerBasedOnStarModule",
            "Computes the forgetting signature in an experiment based on star module computation",
            "Usage: ExperimentPlannerBasedOnStarModule -ont ontology -outDir ouput-directory -sigSize signature-size " +
                    "-coverageData coverage-file",
            "\tontology: Ontology file.",
            "\toutput-directory: Output directory where files will be written, The directory should be a valid " +
                    "existing directory.",
            "\tsignature-size: Number (1 to 100) of forgetting symbols as a percentage .",
            "\tcoverage-file: A coverage file which contains the coverage of the concept symbols in the ontology");

    private static File ontFile;
    private static File outDir;
    private static int sigPercentage;
    private static File conceptCoverageFile;

    public static void main(String[] args) throws IOException, ExperimentException, OWLOntologyCreationException
    {
        Bootstrap.initializeApplication();
        parseArguements(args);

        Planner planner = new Planner();
        OntologyData ontologyData = planner.planOntology(ontFile);
        serializeOntology(ontologyData);

        int sigSize = 0;
        OntologyLoader loader = new OntologyLoader();
        OWLOntologyManager owlMgr = OWLManager.createOWLOntologyManager();
        OWLOntology owlOnt = loader.loadOntology(ontologyData.getIRI(), null, owlMgr);

        List<OWLEntity> symbols = new ArrayList<>(owlOnt.signature().collect(Collectors.toList()));
        symbols = getSortedSignature(symbols);
        loader.parseOntologyAsAxioms(owlOnt, true, false);

        Set<OWLEntity> forgetSig = new HashSet<>();
        int requiredSignature = (int) (sigPercentage / 100.0 * (double) owlOnt.signature().count());
        OWLOntology owlOntology = owlMgr.createOntology(IRI.create("ExtractedOnt"));
        SyntacticLocalityModuleExtractor extractor =
                new SyntacticLocalityModuleExtractor(owlMgr, owlOnt, ModuleType.STAR);

        while (sigSize < requiredSignature) {
            List<OWLEntity> randomSymbols = getFirstSymbols(symbols, forgetSig, requiredSignature > 10 ?
                    requiredSignature / 10 : 1);
            owlOntology.addAxioms(extractor.extract(new HashSet<>(randomSymbols)));
            owlOntology.signature().filter(e -> e.isType(EntityType.CLASS)).forEach(forgetSig::add);
            sigSize = forgetSig.size();
        }

        List<OWLEntity> sigList = new LinkedList<>(forgetSig);
        while (sigList.size() > requiredSignature) {
            sigList.remove(0);
        }

        List<SymbolData> sds = new LinkedList<>();
        ISymbolFactory sFact = FactoryMgr.getSymbolFactory();
        for (OWLEntity symbol : sigList) {
            IRI iri = symbol.getIRI();
            ILiteral literal = sFact.createConceptLiteral(iri.getRemainder().orElse(iri.getIRIString()));
            if (literal != null) {
                SymbolData data = new SymbolData(literal.getSymbol());
                data.setIRI(ontologyData.getName(), iri.getIRIString());
                sds.add(data);
            }
        }
        serializeSignature(sds);
    }

    @NotNull
    private static List<OWLEntity> getSortedSignature(List<OWLEntity> signature)
    {
        return signature;
//        Map<ExistingCoverageCalculator.Symbol, Integer> coverageMap =
//                ExistingCoverageCalculator.parseCoverageFile(conceptCoverageFile);
//
//        List<ExistingCoverageCalculator.Symbol> sortedSymbols = new ArrayList<>(coverageMap.keySet());
//        sortedSymbols.sort(Comparator.comparing(o -> coverageMap.getOrDefault(o, 0)).reversed());
//        return sortedSymbols.stream()
//                .map(ExistingCoverageCalculator.Symbol::getIri)
//                .map(iriStr -> getEntityWithIRI(iriStr, signature))
//                .filter(Objects::nonNull)
//                .collect(Collectors.toList());
    }

//    @Nullable
//    private static OWLEntity getEntityWithIRI(String iriStr, List<OWLEntity> signature)
//    {
//        return signature.stream()
//                .filter(e -> e.getIRI().getIRIString().equalsIgnoreCase(iriStr))
//                .findFirst().orElse(null);
//    }

    private static List<OWLEntity> getFirstSymbols(List<OWLEntity> all, Set<OWLEntity> forgetSig, int numberOfSymbols)
    {
        List<OWLEntity> symbols = new LinkedList<>();
        for (int i = 0; i < numberOfSymbols; ) {
            OWLEntity symbol = all.remove(0);
            if (!symbolAppearsInSignature(forgetSig, symbol)) {
                symbols.add(symbol);
                i++;

            }
        }
        return symbols;
    }

    private static boolean symbolAppearsInSignature(Set<OWLEntity> signature, OWLEntity symbol)
    {
        return signature.contains(symbol);
    }

    private static void serializeSignature(List<SymbolData> concepts) throws IOException
    {
        JSONArray jconcepts = new JSONArray();
        concepts.stream().map(SymbolData::serialize).forEach(jconcepts::put);
        Path filePath = Path.of(outDir.getPath() + File.separator + "signature");
        Files.write(filePath, jconcepts.toString().getBytes());
    }

    private static void serializeOntology(OntologyData ontology) throws IOException
    {
        JSONObject jontology = OntologyData.serialize(ontology);
        Path filePath = Path.of(outDir.getPath() + File.separator + ontology.getName() + ".descriptor");
        Files.write(filePath, jontology.toString().getBytes());
    }

    private static void parseArguements(String[] args) throws ExperimentException
    {
        if (args.length < 6) {
            throw new ExperimentException(String.format("Incorrect number of arguements. Args: %s\n%s",
                    String.join(", ", args), usage));
        }

        for (int i = 0; i < args.length; i++) {
            String arg0 = args[i];
            if ("-ont".equalsIgnoreCase(arg0)) {
                ontFile = ArgsHelper.parseFileArguement(args[++i]);
            }
            else if ("-outDir".equalsIgnoreCase(arg0)) {
                outDir = ArgsHelper.parseOutputDirArg(arg0, args[++i], usage);
            }
            else if ("-sigSize".equalsIgnoreCase(arg0)) {
                sigPercentage = Integer.parseInt(args[++i]);
            }
            else if ("-coverageData".equalsIgnoreCase(arg0)) {
                conceptCoverageFile = ArgsHelper.parseFileArguement(args[++i]);
            }
        }
        if (outDir == null) {
            throw new ExperimentException("Output Directory is invalid " + String.join(", ", args));
        }
    }
}
