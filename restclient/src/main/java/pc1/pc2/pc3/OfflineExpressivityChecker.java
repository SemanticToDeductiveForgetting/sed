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
import java.util.Comparator;
import java.util.stream.Stream;

public class OfflineExpressivityChecker
{
    private static final String repository = "C:\\ccviews\\forget\\restclient\\ontologies";
    private static final String metricsLocation = "C:\\ccviews\\forget\\restclient\\ontologies\\metrics.csv";

    public static void main(String[] args) throws IOException, LoadingException, OWLOntologyStorageException
    {
        OntologyMetric metric;
        OWLOntologyManager mgr = OWLManager.createOWLOntologyManager();
        File repoDir = new File(repository);
        File[] experiments = repoDir.listFiles((f, n) -> n.startsWith("experiment"));
        for (File ontologyFile : experiments) {
            int exNum = Integer.parseInt(ontologyFile.getName().substring("experiment".length()));
            if (exNum <= 90 && exNum > 44) {
                System.out.println("Processing ontology " + ontologyFile.getName());
                OWLOntology ont = OWLHelper.loadOntology(IRI.create(ontologyFile), null, mgr);
                int maxDepth =
                        ont.logicalAxioms().flatMap(OWLObject::nestedClassExpressions).map(c -> getDepth(c, 1))
                                .max(Comparator.naturalOrder()).orElse(1);
                metric = new OntologyMetric();
                metric.setAlc(isAlc(ont));
                metric.setConcepts((int) ont.classesInSignature().count());
                metric.setMaxDepth(maxDepth);
                metric.setRoles((int) ont.objectPropertiesInSignature().count());

                writeMetric(ontologyFile.getName(), metric);
                mgr.removeOntology(ont);
            }
        }
    }

    private static int getDepth(OWLClassExpression a, int start)
    {
        Stream<OWLClassExpression> owlClassExpressionStream = a.nestedClassExpressions();
        return owlClassExpressionStream.filter(c -> c != a)
                .map(c -> getDepth(c, start + 1)).max(Comparator.comparing(i -> i))
                .orElse(start);

    }

    private static void writeMetric(String ontologyName, OntologyMetric metric) throws IOException
    {
        File outFile = new File(metricsLocation);
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
