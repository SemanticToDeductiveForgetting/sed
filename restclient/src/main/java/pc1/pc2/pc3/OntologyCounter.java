package pc1.pc2.pc3;

import com.google.common.collect.Range;
import pc1.pc2.pc3.utils.FileUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * generates a report of the ontologies used to cook our experiment ontologies.
 */
public class OntologyCounter
{
    private static final String experimentsLocation = "C:\\ccviews\\forget\\experiment";
    private static final String metricsLocation = "C:\\ccviews\\forget\\restclient\\ontologies\\metrics.csv";
    private static final String outLocation = "C:\\ccviews\\forget\\experimentNoBG\\ontologyData.csv";

    public static void main(String[] args) throws IOException
    {
        File experimentFolder = new File(experimentsLocation);
        File[] experiments = experimentFolder.listFiles((f, n) -> n.startsWith("experiment") && f.isDirectory());
        List<ExperimentData> experimentDataList = new ArrayList<>();
        for (File experiment : experiments) {
            if(experiment.isDirectory()) {
                int exNum = Integer.parseInt(experiment.getName().substring("experiment".length()));
                if (exNum <= 90) {
                    File dataDir = new File(experiment.getPath() + File.separator + "data");
                    File[] descriptorFiles = dataDir.listFiles((f, n) -> n.endsWith(".descriptor"));
                    ExperimentData data = new ExperimentData(exNum);
                    for (File descriptorFile : descriptorFiles) {
                        String name = descriptorFile.getName();
                        String ontologyName = name.substring(0, name.length() - ".descriptor".length());
                        data.addOntology(ontologyName);
                    }
                    experimentDataList.add(data);
                }
            }
        }

        Map<String, Boolean> ontologyMetrics = loadMetrics();
        Set<String> ontologies = experimentDataList.stream()
                .flatMap(e -> e.ontologies.stream())
                .collect(Collectors.toSet());
        Map<String, List<Integer>> ontExp = new HashMap<>();
        for (String ontology : ontologies) {
            List<Integer> experimentsUsingOntology = experimentDataList.stream()
                    .filter(d -> d.ontologies.contains(ontology))
                    .map(d -> d.experimentNumber)
                    .collect(Collectors.toList());
            ontExp.put(ontology, experimentsUsingOntology);
        }

        try(FileWriter writer = new FileWriter(outLocation)) {
            String expsHeader = IntStream.rangeClosed(1, 90).mapToObj(String::valueOf).collect(Collectors.joining(","));
            writer.append("Ontology,> ALC,").append(expsHeader).append('\n');
            for (Map.Entry<String, List<Integer>> entry : ontExp.entrySet()) {
                String ontology = entry.getKey();
                int overALC = ontologyMetrics.getOrDefault(ontology, true) ? 0 : 1;
                List<Integer> exps = entry.getValue();
                int[] arr = new int[90];
                exps.forEach(i -> arr[i - 1] = 1);
                String expsBody = IntStream.of(arr).mapToObj(String::valueOf).collect(Collectors.joining(","));
                writer.append(ontology).append(',').append(String.valueOf(overALC)).append(',').append(expsBody).append('\n');
            }
        }
    }

    private static Map<String, Boolean> loadMetrics() throws IOException
    {
        File metricsFile = new File(metricsLocation);
        List<String> lines = FileUtils.loadFile(metricsFile);
        Map<String, Boolean> metrics = new HashMap<>();
        lines.remove(0);
        for (String line : lines) {
            String[] split = line.split(",");
            if(split.length == 5) {
                String ontologyName = split[0];
                Boolean isALC = Integer.parseInt(split[4]) == 1;
                metrics.put(ontologyName, isALC);
            }
        }
        return metrics;
    }

    private static class ExperimentData
    {
        private int experimentNumber;
        private Set<String> ontologies;

        private ExperimentData(int experimentNumber)
        {
            this.experimentNumber = experimentNumber;
            ontologies = new HashSet<>();
        }

        public void addOntology(String ontologyName)
        {
            ontologies.add(ontologyName);
        }
    }
}
