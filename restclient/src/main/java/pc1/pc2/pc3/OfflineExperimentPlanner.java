package pc1.pc2.pc3;

import org.json.JSONArray;
import org.json.JSONObject;
import pc1.pc2.pc3.experiment.ExperimentException;
import pc1.pc2.pc3.rest.OntologyData;
import pc1.pc2.pc3.rest.Planner;
import pc1.pc2.pc3.rest.SymbolData;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class OfflineExperimentPlanner
{
    private static final String usage = String.format("%s\n%s\n%s\n%s\n%s\n%s\n%s\n",
            "OfflineExperimentPlanner",
            "Plans experiment based on estimated number of forgetting symbols in the axioms",
            "Usage: OfflineExperimentPlanner -ont ontology -outDir ouput-directory -coverage coverage-rate " +
                    "-coverageData coverage-file",
            "\tont: Ontology file.",
            "\toutDir: Output directory where files will be written, The directory should be a valid existing " +
                    "directory.",
            "\tcoverage: Required coverage (percentage e.g. 150) of forgetting symbols.",
            "\tcoverageData: A coverage file which contains the coverage of the concept symbols in the ontology");

    private static File ontFile;
    private static File outDir;
    private static int requiredCoverageRate;
    private static File conceptCoverageFile;

    public static void main(String[] args) throws IOException, ExperimentException
    {
        parseArguements(args);

        Planner planner = new Planner();
        OntologyData ontologyData = planner.planOntology(ontFile);
//        serializeOntology(ontologyData);
//
//        Set<ExistingCoverageCalculator.Symbol> symbols =
//                ExistingCoverageCalculator.parseCoverageFile(conceptCoverageFile).keySet();
//        List<SymbolData> signature = planner.planSignature(symbols, requiredCoverageRate, ontFile.getName());
//        serializeSignature(signature);
    }

    private static void serializeSignature(List<SymbolData> concepts) throws IOException
    {
        JSONArray jconcepts = new JSONArray();
        concepts.stream().map(SymbolData::serialize).forEach(jconcepts::put);
        Path filePath = Path.of(outDir.getCanonicalPath() + File.separator + "signature");
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
        if (args.length < 8) {
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
            else if ("-coverage".equalsIgnoreCase(arg0)) {
                requiredCoverageRate = Integer.parseInt(args[++i]);
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
