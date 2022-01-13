package pc1.pc2.pc3;

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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class SnomedProjectPlanner
{
    private static final String usage = String.format("%s\n%s\n%s\n%s\n%s\n%s\n",
            "SnomedProjectPlanner",
            "Prepares an experiment based on the SNOMED project data",
            "Usage: ExperimentPlannerBasedOnStarModule -ont ontology -outDir ouput-directory -sig signature-file " +
                    "-percentage percentage",
            "\tontology: Ontology file.",
            "\toutput-directory: Output directory where files will be written, The directory should be a valid " +
                    "existing directory.",
            "\tsignature-file: Signature ontology file." +
                    "\tpercentage: Percentage of the signature");

    private static File ontFile;
    private static File outDir;
    private static File sigFile;
    private static int sigPercentage;

    public static void main(String[] args) throws IOException, ExperimentException, OWLOntologyCreationException
    {
        Bootstrap.initializeApplication();
        parseArguements(args);

        Planner planner = new Planner();
        OntologyData ontologyData = planner.planOntology(ontFile);
        serializeOntology(ontologyData);

        OntologyLoader loader = new OntologyLoader();
        OWLOntologyManager owlMgr = OWLManager.createOWLOntologyManager();
        OWLOntology owlOnt = loader.loadOntology(ontologyData.getIRI(), null, owlMgr);
        OWLOntology signatureOnt = loader.loadOntology(IRI.create(sigFile), null, owlMgr);

        Map<String, OWLEntity> ontologySig = new HashMap<>();
        owlOnt.signature()
                .filter(o -> o.getEntityType() == EntityType.CLASS)
                .forEach(o -> ontologySig.put(o.getIRI().getIRIString(), o));

        signatureOnt.signature().forEach(o -> ontologySig.remove(o.getIRI().getIRIString()));
        Set<OWLEntity> forgettingSig = new HashSet<>(ontologySig.values());

        loader.parseOntologyAsAxioms(owlOnt, true, false);

        List<SymbolData> sds = new LinkedList<>();
        ISymbolFactory sFact = FactoryMgr.getSymbolFactory();
        for (OWLEntity symbol : forgettingSig) {
            IRI iri = symbol.getIRI();
            ILiteral literal = sFact.createConceptLiteral(iri.getRemainder().orElse(iri.getIRIString()));
            if (literal != null) {
                SymbolData data = new SymbolData(literal.getSymbol());
                data.setIRI(ontologyData.getName(), iri.getIRIString());
                sds.add(data);
            }
        }
        double percent = (double) sigPercentage / 100.0;
        sds.sort(Comparator.comparing(SymbolData::getLabel));
        List<SymbolData> sigPart = sds.subList(0, (int) (sds.size() * percent + 0.5));
        serializeSignature(sigPart);
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
            else if ("-sigFile".equalsIgnoreCase(arg0)) {
                sigFile = ArgsHelper.parseFileArguement(args[++i]);
            }
            else if ("-percentage".equalsIgnoreCase(arg0)) {
                sigPercentage = Integer.parseInt(args[++i]);
            }
        }
        if (outDir == null) {
            throw new ExperimentException("Output Directory is invalid " + String.join(", ", args));
        }
    }
}
