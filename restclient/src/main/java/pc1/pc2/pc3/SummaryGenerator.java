package pc1.pc2.pc3;

import pc1.pc2.pc3.utils.FileUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.LinkedList;
import java.util.List;

public class SummaryGenerator
{
    private static String usage = String.format("%s\n%s\n%s\n%s\n%s",
            "SummaryGenerator:",
            "Generates a summary from running experiments",
            "Usage: SummaryGenerator -repo repository -outFile output-file",
            "\trepository: a path to the repository that contains local copies of ontologies and the list of classes.",
            "\toutput-file: a path to the summary file");

    private static final int SIGNATURE_SIZE = 30;
    private static File repository = null;
    private static File summaryFile = null;

    public static void main(String[] args) throws IOException
    {
        parseArguements(args);

        List<ExperimentSummary> experimentSummaries = new LinkedList<>();
        File[] experiments = repository.listFiles((f, name) -> name.startsWith("experiment") && f.isDirectory());
        if (experiments != null) {
            for (File experiment : experiments) {
                experimentSummaries.add(generateSummary(experiment));
            }
        }
        try (OutputStreamWriter writer = new FileWriter(summaryFile, true)) {
            writer.append('\n');
            for (ExperimentSummary s : experimentSummaries) {
                writer.append(s.toString()).append('\n');
            }
        }
    }

    private static ExperimentSummary generateSummary(File experiment)
    {
        File debug = new File(experiment.getPath() + File.separator + "debug");
        ExperimentSummary summary = new ExperimentSummary(experiment.getName().substring("experiment".length()));
        try {
            summary.sizeOfOntology = getSizeOf(experiment, "Ontology.clausal");
            summary.sizeOfBackground = getSizeOf(experiment, "Theory.clausal");
            summary.sizeOfGlassBoxView = getSizeOf(experiment, "GlassBox.clausal");
            summary.timeOfGlassBoxForgetting = getNanoTime(experiment, "GlassBox.time");
            summary.timeOfAlcReduction = getNanoTime(experiment, "GlassBoxReduction.time");
            summary.glassBoxRedundancies = getSizeOf(experiment, "GlassBoxRedundant.clausal");
            summary.letheRedundancies = getRedundancies(experiment, "Lethe.redundancies");
            summary.timeOfLethe = getNanoTime(experiment, "Lethe.time");
            summary.numberOfRedundantResolutions = getSizeOf(debug, "redundantResolutions.csv") - 1;
            summary.filteredByReduction = getSizeOf(experiment, "FilteredByReduction.clausal");
            summary.bgRestricted = getSizeOf(debug, "GlassBox.restricted");
            summary.totalResolutions = getRedundancies(debug, "ResolutionCount");
            summary.resolutionsFromReduced = getRedundancies(debug, "ResolutionFromReducedClausesCount");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        return summary;
    }

    private static int getRedundancies(File experiment, String fileName) throws IOException
    {
        File[] files = experiment.listFiles((f, n) -> n.endsWith(fileName));
        if (files != null && files.length == 1) {
            List<String> lines = FileUtils.loadFile(files[0]);
            return Integer.parseInt(lines.get(0));
        }
        return -1;
    }

    private static long getNanoTime(File experiment, String fileName) throws Exception
    {
        File[] files = experiment.listFiles((f, n) -> n.endsWith(fileName));
        if (files != null && files.length == 1) {
            List<String> lines = FileUtils.loadFile(files[0]);
            if (!lines.isEmpty()) {
                String line = lines.get(0);
                String time = line.split("=")[1].trim();
                return Long.parseLong(time);
            }
            else {
                throw new Exception(String.format("%s in experiment %s is empty", fileName, experiment));
            }
        }
        throw new Exception(String.format("Cannot find %s in experiment %s", fileName, experiment));
    }

    private static int getSizeOf(File experiment, String fileName) throws Exception
    {
        File[] files = experiment.listFiles((f, n) -> n.equalsIgnoreCase(fileName));
        if (files != null && files.length == 1) {
            return FileUtils.loadFile(files[0]).size();
        }
        throw new Exception(String.format("Cannot find %s in experiment %s", fileName, experiment));
    }

    private static void parseArguements(String[] args)
    {
        if (args.length < 2) {
            System.out.println(String.format("Incorrect number of arguements. Args: %s", String.join(", ", args)));
            System.out.println(usage);
            System.exit(1);
        }

        for (int i = 0; i < args.length; i++) {
            String arg0 = args[i];
            if ("-repo".equalsIgnoreCase(arg0)) {
                repository = ArgsHelper.parseDirectoryArguement(args[++i]);
            }
            else if ("-outFile".equalsIgnoreCase(arg0)) {
                summaryFile = ArgsHelper.parseFileArguement(args[++i]);
            }
            else if ("-verbose".equalsIgnoreCase(arg0)) {
            }
        }
        if (repository == null || summaryFile == null) {
            System.out.println(usage);
            System.exit(1);
        }
    }

    private static class ExperimentSummary
    {
        private int resolutionsFromReduced;
        private int totalResolutions;
        private int bgRestricted;
        private int filteredByReduction;
        private long timeOfAlcReduction;
        private int glassBoxRedundancies;
        private long timeOfLethe;
        private int letheRedundancies;
        private int numberOfRedundantResolutions;
        private String nameOfExperiment;
        private int sizeOfOntology;
        private int sizeOfBackground;
        private int sizeOfGlassBoxView;
        private long timeOfGlassBoxForgetting;


        private ExperimentSummary(String nameOfExperiment)
        {
            this.nameOfExperiment = nameOfExperiment;
        }

        @Override public String toString()
        {
            return String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s",
                    nameOfExperiment,
                    sizeOfOntology,
                    sizeOfBackground,
                    sizeOfGlassBoxView,
                    timeOfGlassBoxForgetting,
                    timeOfAlcReduction,
                    glassBoxRedundancies,
                    numberOfRedundantResolutions,
                    letheRedundancies,
                    timeOfLethe,
                    filteredByReduction,
                    bgRestricted,
                    totalResolutions,
                    resolutionsFromReduced);
        }
    }
}
