package pc1.pc2.pc3;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import pc1.pc2.pc3.app.Bootstrap;
import pc1.pc2.pc3.experiment.ExperimentException;
import pc1.pc2.pc3.experiment.OntologyLoader;
import pc1.pc2.pc3.logic.helper.ClauseHelper;
import pc1.pc2.pc3.om.IAxiom;
import pc1.pc2.pc3.om.IClause;
import pc1.pc2.pc3.om.IConceptLiteral;
import pc1.pc2.pc3.om.ILiteral;
import pc1.pc2.pc3.rest.SymbolData;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class ExperimentPlannerWithSignaturePortion
{
    private static final String usage = String.format("%s\n%s\n%s\n%s\n%s\n%s\n",
            "ExperimentPlannerWithSignaturePortion",
            "Plans a forgetting experiment",
            "Usage: ExperimentPlannerWithSignaturePortion -ontology ontology -outDir ouput-directory -signature " +
                    "signature",
            "\tontology: Ontology file.",
            "\toutDir: Output directory where files will be written, The directory should be a valid existing " +
                    "directory.",
            "\tsignature: Required signature portion (percentage e.g. 10) of forgetting symbols.");

    private static File ontFile;
    private static File outDir;
    private static int signatureSize;
    private static boolean loadNNF;
    private static boolean quantifiedSignature;

    public static void main(String[] args)
            throws IOException, ExperimentException, OWLOntologyCreationException, OWLOntologyStorageException
    {
        Bootstrap.initializeApplication();
        parseArguements(args);
        OWLOntologyManager owlMgr = OWLManager.createOWLOntologyManager();
        OWLOntology owlOntology = owlMgr.loadOntologyFromOntologyDocument(ontFile);
        OntologyLoader loader = new OntologyLoader();
        if (owlOntology != null) {
            List<IAxiom> axioms;
            if (loadNNF) {
                axioms = loader.parseOntologyAsClauses(owlOntology, true, true);
            }
            else {
                axioms = loader.parseOntologyAsAxioms(owlOntology, true, true);
            }
            if (!axioms.isEmpty()) {
                String path = outDir.getCanonicalPath() + File.separator + String.format("%s", "Ontology");
                ExperimentHelper.saveInHumanReadableForm(axioms, path + ".clausal");
                ExperimentHelper.saveAxiomsInOWLForm(axioms, path + ".owl");

                List<IConceptLiteral> signature;
                if (quantifiedSignature) {
                    signature = getConceptsSortedByRoleRestrictionAndName(axioms);
                }
                else {
                    signature = getConceptsSortedByName(axioms);
                }

                List<IConceptLiteral> forgettingSig =
                        signature.subList(0, (int) (signature.size() * (double) signatureSize / 100.0));

                serializeSignature(
                        forgettingSig.stream().map(c -> new SymbolData(c.getSymbol())).collect(Collectors.toList()));
            }
        }
    }

    @NotNull
    private static List<IConceptLiteral> getConceptsSortedByName(List<IAxiom> axioms)
    {
        List<IConceptLiteral> signature = axioms.stream()
                .flatMap(a -> a.getSignature().stream())
                .filter(a -> a instanceof IConceptLiteral)
                .map(a -> (IConceptLiteral) a)
                .distinct()
                .sorted(Comparator.comparing(ILiteral::getSymbol))
                .collect(Collectors.toList());
        return signature;
    }

    @NotNull
    private static List<IConceptLiteral> getConceptsSortedByRoleRestrictionAndName(List<IAxiom> axioms)
    {
        Map<IConceptLiteral, Integer> counts = new HashMap<>();
        countQuantifiedLiterals(axioms, counts);
        List<IConceptLiteral> conceptsSortedByName = getConceptsSortedByName(axioms);
        List<IConceptLiteral> quantifiedConcepts = new ArrayList<>(counts.keySet());
        quantifiedConcepts.sort(Comparator.comparing(counts::get).thenComparing(c -> c instanceof ILiteral ?
                ((ILiteral) c).getSymbol() : ""));
        conceptsSortedByName.removeAll(quantifiedConcepts);
        quantifiedConcepts.addAll(conceptsSortedByName);
        return quantifiedConcepts;
    }

    private static void countQuantifiedLiterals(List<IAxiom> axioms, Map<IConceptLiteral, Integer> counts)
    {
        for (IAxiom axiom : axioms) {
            countQuantifiedLiterals(counts, axiom.getLeft());
            countQuantifiedLiterals(counts, axiom.getRight());
        }
    }

    private static void countQuantifiedLiterals(Map<IConceptLiteral, Integer> counts, IClause clause)
    {
        Collection<IClause> quantifiedClauses = ClauseHelper.getQuantifiedClauses(clause);
        List<IConceptLiteral> quantifiedLiterals = quantifiedClauses.stream()
                .flatMap(c -> c.getSignature().stream())
                .filter(l -> l instanceof IConceptLiteral)
                .map(l -> (IConceptLiteral) l)
                .collect(Collectors.toList());
        for (IConceptLiteral literal : quantifiedLiterals) {
            counts.compute(literal, (l, count) -> count == null ? 1 : count + 1);
        }
    }

    private static void serializeSignature(List<SymbolData> concepts) throws IOException
    {
        JSONArray jconcepts = new JSONArray();
        concepts.stream().map(SymbolData::serialize).forEach(jconcepts::put);
        Path filePath = Path.of(outDir.getCanonicalPath() + File.separator + "signature");
        Files.write(filePath, jconcepts.toString().getBytes());
    }

    private static void parseArguements(String[] args) throws ExperimentException
    {
        if (args.length < 6) {
            throw new ExperimentException(String.format("Incorrect number of arguements. Args: %s\n%s",
                    String.join(", ", args), usage));
        }

        for (int i = 0; i < args.length; i++) {
            String arg0 = args[i];
            if ("-ontology".equalsIgnoreCase(arg0)) {
                ontFile = ArgsHelper.parseFileArguement(args[++i]);
            }
            else if ("-outDir".equalsIgnoreCase(arg0)) {
                outDir = ArgsHelper.parseOutputDirArg(arg0, args[++i], usage);
            }
            else if ("-signature".equalsIgnoreCase(arg0)) {
                signatureSize = Integer.parseInt(args[++i]);
            }
            else if ("-loadNNF".equalsIgnoreCase(arg0)) {
                loadNNF = true;
            }
            else if ("-quantifiedSignature".equalsIgnoreCase(arg0)) {
                quantifiedSignature = true;
            }
        }
        if (outDir == null) {
            throw new ExperimentException("Output Directory is invalid " + String.join(", ", args));
        }
    }
}
