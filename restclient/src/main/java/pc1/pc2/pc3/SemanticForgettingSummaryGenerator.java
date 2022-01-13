package pc1.pc2.pc3;

import org.semanticweb.owlapi.apibinding.OWLManager;
import pc1.pc2.pc3.utils.FileUtils;

import java.io.*;
import java.util.LinkedList;
import java.util.List;

public class SemanticForgettingSummaryGenerator
{
    private static final String usage = String.format("%s\n%s\n%s\n%s\n%s",
            "SemanticForgettingSummaryGenerator:",
            "Generates a summary from running experiments",
            "Usage: SemanticForgettingSummaryGenerator -experiments experiments -outFile output-file",
            "\texperiments: Path to the experiments folder.",
            "\toutput-file: Path to the summary file");

    private static File experiments = null;
    private static File summaryFile = null;
    private static boolean optimized;

    public static void main(String[] args) throws IOException
    {
        parseArguements(args);

        List<ExperimentSummary> experimentSummaries = new LinkedList<>();
        File[] experiments = SemanticForgettingSummaryGenerator.experiments
                .listFiles((f, name) -> name.startsWith("experiment") && f.isDirectory());
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
        String prefix = optimized ? "optimized_" : "";
        String SEP = File.separator;
        ExperimentSummary summary = new ExperimentSummary(experiment.getName().substring("experiment".length()));
        try {
            summary.sizeOfOntology = getSizeOf(experiment, "Ontology.clausal");
            summary.concepts = getValueOf(experiment, "concepts");
            summary.sizeOfForgettingSignature = getSignatureSize(experiment);
            int sizeOfView;
            try {
                sizeOfView = getSizeOf(experiment, prefix + "ForgettingView.clausal");
            } catch (Exception ignore) {
                File owlFile = new File(experiment + SEP + "ForgettingView.owl");
                if (owlFile.exists()) {
                    sizeOfView = (int) OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(owlFile)
                            .logicalAxioms().count();
                }
                else {
                    sizeOfView =
                            getSizeOf(experiment, "ForgettingView.active") + getSizeOf(experiment, "ForgettingView" +
                                    ".clausal");
                }
            }

            summary.sizeOfForgettingView = sizeOfView;
            summary.fameTime = getNanoTime(experiment, "Fame.time");
            summary.forgetTime = getNanoTime(experiment, prefix + "debug" + SEP + "forget.time");
            if (summary.forgetTime == 0) {
                summary.forgetTime = getNanoTime(experiment, "debug" + SEP + "forget.time");
            }
            summary.definerEliminationTime = getNanoTime(experiment, prefix + "debug" + SEP + "DefinerElimination" +
                    ".time");
            summary.totalTime = summary.forgetTime + summary.definerEliminationTime;
            summary.numberOfIntroducedDefiners = getValueOf(experiment, prefix + "debug" + SEP + "Definers.intro");
            summary.numberOfRemainingDefiners = getValueOf(experiment, prefix + "debug" + SEP + "Definers.remaining");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        return summary;
    }

    private static int getSignatureSize(File experiment) throws IOException
    {
        return ArgsHelper.parseSignatureFileArg(experiment.getCanonicalPath() + File.separator + "signature",
                "Cannot load signature file!\n").size();
    }

    private static long getNanoTime(File experiment, String fileName) throws Exception
    {
        File file = new File(experiment.getPath() + File.separator + fileName);
        if (file.exists()) {
            List<String> lines = FileUtils.loadFile(file);
            if (!lines.isEmpty()) {
                String line = lines.get(0);
                String time = line.split("=")[1].trim();
                return Long.parseLong(time);
            }
            else {
                System.out.println(String.format("%s is empty", fileName));
                return 0;
            }
        }
        return 0;
    }

    private static int getSizeOf(File experiment, String fileName) throws Exception
    {
        File[] files = experiment.listFiles((f, n) -> n.equalsIgnoreCase(fileName));
        if (files != null && files.length == 1) {
            try (LineNumberReader reader = new LineNumberReader(new FileReader(files[0]))) {
                int lines = 0;
                while (reader.readLine() != null) {
                    lines = reader.getLineNumber();
                }
                return lines;
            }
        }
        return -1;
    }

    private static int getValueOf(File experiment, String fileName) throws Exception
    {
        List<String> lines = FileUtils.loadFile(new File(experiment.getPath() + File.separator + fileName));
        if (!lines.isEmpty()) {
            String line = lines.get(0).trim();
            return Integer.parseInt(line);
        }
        else {
            return -1;
        }
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
            if ("-experiments".equalsIgnoreCase(arg0)) {
                experiments = ArgsHelper.parseDirectoryArguement(args[++i]);
            }
            else if ("-outFile".equalsIgnoreCase(arg0)) {
                summaryFile = ArgsHelper.parseFileArguement(args[++i]);
            }
            else if ("-optimized".equalsIgnoreCase(arg0)) {
                optimized = true;
            }
        }
        if (experiments == null || summaryFile == null) {
            System.out.println(usage);
            System.exit(1);
        }
    }

    private static class ExperimentSummary
    {
        private final String nameOfExperiment;
        public long fameTime;
        public int concepts;
        private int numberOfRemainingDefiners;
        private long definerEliminationTime;
        private long forgetTime;
        private int sizeOfOntology;
        private int sizeOfForgettingSignature;
        private int sizeOfForgettingView;
        private long totalTime;
        private int numberOfIntroducedDefiners;


        private ExperimentSummary(String nameOfExperiment)
        {
            this.nameOfExperiment = nameOfExperiment;
        }

        @Override
        public String toString()
        {
            return String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s",
                    nameOfExperiment,
                    sizeOfOntology,
                    concepts,
                    sizeOfForgettingSignature,
                    sizeOfForgettingView,
                    fameTime,
                    totalTime,
                    forgetTime,
                    definerEliminationTime,
                    numberOfIntroducedDefiners,
                    numberOfRemainingDefiners);
        }
    }
}
