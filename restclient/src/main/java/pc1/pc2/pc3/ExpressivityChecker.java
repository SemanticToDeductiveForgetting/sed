package pc1.pc2.pc3;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.util.DLExpressivityChecker;
import org.semanticweb.owlapi.util.Languages;
import pc1.pc2.pc3.input.owl5.LoadingException;
import pc1.pc2.pc3.input.owl5.OWLHelper;
import pc1.pc2.pc3.rest.BioportalClient;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;

public class ExpressivityChecker
{
    private static File repository;

    public static void main(String[] args) throws IOException
    {
        repository = new File(args[0]);
        String ontologyName = args[1];

        OntologyMetric metric;
        OWLOntologyManager mgr = OWLManager.createOWLOntologyManager();
        File ontology = new File(repository.getPath() + File.separator + ontologyName);
        try {
            OWLOntology ont = OWLHelper.loadOntology(IRI.create(ontology), null, mgr);
            metric = retrieveMetrics(ont, ontologyName);
            if(metric == null) {
                addToBlackList(ontologyName);
            }
            else {
                writeMetric(ontologyName, metric);
            }
            mgr.removeOntology(ont);
        } catch (LoadingException | Exception e) {
            addToBlackList(ontologyName);
        }
    }

    private static void addToBlackList(String ontologyName) throws IOException
    {
        File outFile = new File(repository.getPath() + File.separator + "blacklist");
        try (FileWriter w = new FileWriter(outFile, true)) {
            w.append(ontologyName);
            w.append("\n");
        }
    }

    private static void writeMetric(String ontologyName, OntologyMetric metric) throws IOException
    {
        File outFile = new File(repository.getPath() + File.separator + "metrics.csv");
        try (FileWriter w = new FileWriter(outFile, true)) {
            w.append(String.format("%s,%d,%d,%d,%d",
                    ontologyName, metric.getConcepts(), metric.getRoles(), metric.getMaxRoleDepth(),
                    metric.isAlc() ? 1 : 0));
            w.append("\n");
        }
    }

    private static OntologyMetric retrieveMetrics(OWLOntology ont, String ontologyName)
    {
        BioportalClient client = new BioportalClient(-1);
        OntologyMetric metrics = client.getMetrics(ontologyName);
        metrics.setAlc(isAlc(ont));
        return metrics;
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
