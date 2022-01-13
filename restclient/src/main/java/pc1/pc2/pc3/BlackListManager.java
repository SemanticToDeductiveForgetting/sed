package pc1.pc2.pc3;

import pc1.pc2.pc3.rest.BioportalClient;
import pc1.pc2.pc3.utils.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class BlackListManager
{
    public static void main(String[] args) throws IOException
    {
        File bioportalLocalRepository = new File("restclient/ontologies/bioportal");
        File[] blacklist = bioportalLocalRepository.listFiles((f, n) -> n.equalsIgnoreCase("blacklist"));
        String[] ontologies = bioportalLocalRepository.list((f, n) -> !n.equalsIgnoreCase("blacklist"));
        Set<String> blacklistedOntologies = new HashSet<>(FileUtils.loadFile(blacklist[0]));
        BioportalClient client = new BioportalClient(20);
        for (String ontology : ontologies) {
            System.out.println("Processing " + ontology);
            if (!blacklistedOntologies.contains(ontology)) {
                OntologyMetric metrics = client.getMetrics(ontology);
                if (metrics == null || metrics.getConcepts() == 0) {
                    blacklistedOntologies.add(ontology);
                    System.out.println("Added " + ontology);
                }
            }
        }
        FileUtils.writeFile("restclient/ontologies/bioportal/blacklist", blacklistedOntologies);
    }
}
