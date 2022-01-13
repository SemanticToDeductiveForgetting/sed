package pc1.pc2.pc3;

import org.json.JSONArray;
import org.json.JSONObject;
import pc1.pc2.pc3.experiment.ExperimentException;
import pc1.pc2.pc3.rest.BioportalClient;
import pc1.pc2.pc3.rest.OntologyData;
import pc1.pc2.pc3.rest.SymbolData;
import pc1.pc2.pc3.rest.VerboseBioportalClient;
import pc1.pc2.pc3.input.owl5.LoadingException;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class ExperimentPlanner
{
    private static final String usage = String.format("%s\n%s\n%s\n%s\n%s\n%s\n%s\n%s",
            "ExperimentPlanner:",
            "Select a random signature from a list of classes repository/classes.txt.",
            "The program connects to the Bioportal repository. and writes descriptor files \n" +
                    "for the ontologies that will be required for the experiment",
            "Usage: ExperimentPlanner -repo repository -outDir output-directory -sigSize signature-size -classes " +
                    "classes-file",
            "\trepository: a path to the repository that contains local copies of ontologies and the list of classes.",
            "\toutput-directory: a path to the directory where the descriptor files will be written",
            "\tsignature-size: size of seed signature",
            "\tclasses-file: file of forgetting concept names"
    );

    private static File repository = null;
    private static File outDir = null;
    private static File classes = null;
    private static boolean verbose;
    private static int signatureSize;

    public static void main(String[] args) throws IOException, LoadingException, ExperimentException
    {
        parseArguements(args);

        try (OutputStreamWriter writer = new OutputStreamWriter(System.out)) {
            BioportalClient client = verbose ?
                    new VerboseBioportalClient(signatureSize, writer)
                    : new BioportalClient(signatureSize);
            client.loadData(repository.getCanonicalPath(), classes);
            List<OntologyData> ontologies = client.getOntologies();
            for (OntologyData ontology : ontologies) {
                serializeOntology(ontology);
            }
            serializeSignature(client.getConcepts());
        }
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
            throw new ExperimentException(String.format("Incorrect number of arguements. Args: %s\n%s", String.join(
                    ", ", args), usage));
        }

        for (int i = 0; i < args.length; i++) {
            String arg0 = args[i];
            if ("-repo".equalsIgnoreCase(arg0)) {
                repository = ArgsHelper.parseRepositoryArg(arg0, args[++i], usage);
            }
            else if ("-outDir".equalsIgnoreCase(arg0)) {
                outDir = ArgsHelper.parseOutputDirArg(arg0, args[++i], usage);
            }
            else if ("-sigSize".equalsIgnoreCase(arg0)) {
                signatureSize = Integer.parseInt(args[++i]);
            }
            else if ("-classes".equalsIgnoreCase(arg0)) {
                classes = ArgsHelper.parseFileArguement(args[++i]);
            }
            else if ("-verbose".equalsIgnoreCase(arg0)) {
                verbose = true;
            }
        }
        if (repository == null || outDir == null || signatureSize <= 0) {
            System.out.println(usage);
            System.exit(1);
        }
    }
}
