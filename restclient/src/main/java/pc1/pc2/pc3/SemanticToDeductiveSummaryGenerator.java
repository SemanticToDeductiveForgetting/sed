package pc1.pc2.pc3;

import org.semanticweb.owlapi.apibinding.OWLManager;
import pc1.pc2.pc3.utils.FileUtils;

import java.io.*;
import java.util.LinkedList;
import java.util.List;

public class SemanticToDeductiveSummaryGenerator
{
    private static final String usage = String.format("%s\n%s\n%s\n%s\n%s",
            "SemanticToDeductiveSummaryGenerator:",
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
        File[] experiments = SemanticToDeductiveSummaryGenerator.experiments
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
        String SEP = File.separator;
        ExperimentSummary summary = new ExperimentSummary(experiment.getName().substring("experiment".length()));
        try {
            File newRun = new File(experiment.getPath() + SEP + "newRun");
            summary.sizeOfOntology = getSizeOf(experiment, "Ontology.clausal");
            summary.sizeOfForgettingSignature = getSignatureSize(experiment);
            int sizeOfView = -1;
            try {
                sizeOfView = getSizeOf(newRun, "DeductiveView.clausal");
            } catch (Exception ignore) {
                File owlFile = new File(newRun + SEP + "DeductiveView.owl");
                if (owlFile.exists()) {
                    sizeOfView = (int) OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(owlFile)
                            .logicalAxioms().count();
                }
            }

            summary.sizeOfForgettingView = sizeOfView;
            summary.fameTime = getNanoTime(experiment, "Fame.time");
            summary.letheTime = getNanoTime(experiment, "Lethe.time");
            summary.letheSize = getSizeOf(experiment, "lethe.dl");
            summary.forgetFinalTime = getNanoTime(newRun, "time");
            summary.forgetSemanticTime = getNanoTime(newRun, "Semantic.time");
            summary.forgetReductionTime = getNanoTime(newRun, "ReductionToALC.time");
            summary.semanticDefinerElimTime = getNanoTime(newRun, "DefinerEliminationInSemanticRound.time");
            summary.deductiveDefinerElimTime = getNanoTime(newRun, "DefinerEliminationInDeductiveRound.time");
            summary.semanticSimplificationTime = getNanoTime(newRun, "SemanticViewSimplification.time");
            summary.deductiveSimplificationTime = getNanoTime(newRun, "DeductiveViewSimplification.time");
            summary.numberOfIntroducedDefiners = getValueOf(newRun, "IntroducedDefiners");
            summary.numberOfCyclicDefiners = getValueOf(newRun, "cyclicDefiners");
            summary.numberOfDeltaDefiners = getValueOf(newRun, "DeltaDefiners");
            summary.numberOfDeltaClauses = getSizeOf(newRun, "delta.clausal");
            if (summary.numberOfDeltaClauses < 0) {
                summary.numberOfDeltaClauses = getSizeOf(newRun, "deltaInAxiomForm.clausal");
            }

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

    private static int getValueOf(File experiment, String fileName)
    {
        try {
            List<String> lines = FileUtils.loadFile(new File(experiment.getPath() + File.separator + fileName));
            if (!lines.isEmpty()) {
                String line = lines.get(0).trim();
                return Integer.parseInt(line);
            }
        } catch (IOException ignored) {

        }
        return -1;

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
        public long letheTime;
        public long forgetFinalTime;
        public long forgetSemanticTime;
        public long forgetReductionTime;
        public long semanticDefinerElimTime;
        public long deductiveDefinerElimTime;
        public long semanticSimplificationTime;
        public long deductiveSimplificationTime;
        public int numberOfCyclicDefiners;
        public int numberOfDeltaDefiners;
        public int numberOfDeltaClauses;
        public int letheSize;
        private int sizeOfOntology;
        private int sizeOfForgettingSignature;
        private int sizeOfForgettingView;
        private int numberOfIntroducedDefiners;


        private ExperimentSummary(String nameOfExperiment)
        {
            this.nameOfExperiment = nameOfExperiment;
        }

        @Override
        public String toString()
        {
            return String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s",
                    nameOfExperiment,
                    sizeOfOntology,
                    sizeOfForgettingSignature,
                    sizeOfForgettingView,
                    letheSize,
                    fameTime,
                    letheTime,
                    forgetFinalTime,
                    forgetSemanticTime,
                    semanticDefinerElimTime,
                    semanticSimplificationTime,
                    forgetReductionTime,
                    deductiveDefinerElimTime,
                    deductiveSimplificationTime,
                    numberOfIntroducedDefiners,
                    numberOfCyclicDefiners,
                    numberOfDeltaDefiners,
                    numberOfDeltaClauses);
        }
    }
}
