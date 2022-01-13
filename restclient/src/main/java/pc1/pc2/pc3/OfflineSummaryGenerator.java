package pc1.pc2.pc3;

import org.jetbrains.annotations.NotNull;
import pc1.pc2.pc3.app.Bootstrap;
import pc1.pc2.pc3.om.FactoryMgr;
import pc1.pc2.pc3.om.ILiteral;
import pc1.pc2.pc3.om.ISymbolFactory;
import pc1.pc2.pc3.om.privileged.IPrivilegedSymbolFactory;
import pc1.pc2.pc3.utils.FileUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class OfflineSummaryGenerator
{
    private static final String usage = String.format("%s\n%s\n%s\n%s\n%s",
            "OfflineSummaryGenerator:",
            "Generates a summary from running experiments",
            "Usage: SummaryGenerator -repo repository -outFile output-file",
            "\trepository: a path to the repository that contains local copies of ontologies and the list of classes.",
            "\toutput-file: a path to the summary file");

    private static final int SIGNATURE_SIZE = 30;
    private static File repository = null;
    private static File summaryFile = null;

    public static void main(String[] args) throws IOException
    {
        Bootstrap.initializeApplication();
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
        File data = new File(experiment.getPath() + File.separator + "data");
        ExperimentSummary summary = new ExperimentSummary(experiment.getName().substring("experiment".length()));
        try {
            summary.sizeOfOntology = getSizeOf(data, "Ontology.clausal");
            summary.sizeOfSignature = getSignatureSize(data, "signature");
            summary.differentResults = getSizeOf(experiment, "MissingFromGlassBox.clausal");
            summary.semanticForgettingTime = getNanoTime(experiment, "GlassBox.time");
            summary.alcReductionTime = getNanoTime(experiment, "GlassBoxReduction.time");
            summary.filteredByReduction = getSizeOf(experiment, "FilteredByReduction.clausal");
            summary.timeOfLethe = getNanoTime(experiment, "Lethe.time");
            Set<ILiteral> cyclicDefiners = getDefiners(experiment, "GlassBox.clausal");
            Set<ILiteral> allDefiners = readDefinerFile(experiment, "GlassBox.definers");
            Set<ILiteral> filteredByReduction = getDefiners(experiment, "FilteredByReduction.clausal");
            summary.introducedDefiners = allDefiners == null ? -1 : allDefiners.size();
            summary.cyclicDefiners = cyclicDefiners == null ? -1 : cyclicDefiners.size();
            summary.filteredDefinersByReduction = filteredByReduction == null ? -1 : filteredByReduction.size();

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        return summary;
    }

    private static Set<ILiteral> readDefinerFile(File experimentDir, String fileName)
    {
        File defFile = new File(experimentDir + File.separator + fileName);
        if (defFile.exists()) {
            try {
                ((IPrivilegedSymbolFactory) FactoryMgr.getSymbolFactory()).reset();
                return FileUtils.loadFile(defFile).stream()
                        .filter(line -> !line.trim().isEmpty())
                        .map(line -> line.split(",")[0])
                        .map(definer -> FactoryMgr.getSymbolFactory().createConceptLiteral(definer))
                        .collect(Collectors.toSet());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else {
            System.out.printf("File %s does not exist%n", fileName);
        }
        return null;
    }

    private static Set<ILiteral> getDefiners(File experimentDir, String fileName)
    {
        File expFile = new File(experimentDir + File.separator + fileName);
        if (expFile.exists()) {
            try {
                ((IPrivilegedSymbolFactory) FactoryMgr.getSymbolFactory()).reset();
                ArgsHelper.parseOntologyAsClauses(expFile);
                Collection<ILiteral> signature = FactoryMgr.getSymbolFactory().getSignature();
                return getDefiners(signature);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else {
            System.out.printf("File %s does not exist%n", fileName);
        }
        return null;
    }

    @NotNull
    private static Set<ILiteral> getDefiners(Collection<ILiteral> signature)
    {
        return signature.stream()
                .filter(literal -> literal.getSymbol().startsWith(ISymbolFactory.EXISTENTIAL_DEFINER_PREFIX) ||
                        literal.getSymbol().startsWith(ISymbolFactory.UNIVERSAL_DEFINER_PREFIX))
                .collect(Collectors.toSet());
    }

    private static int getSignatureSize(File data, String signature)
    {
        return ArgsHelper.parseSignatureFileArg(data.getPath() + File.separator + signature, "").size();
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
                System.out.println(String.format("%s in experiment %s is empty", fileName, experiment));
            }
        }
        System.out.println(String.format("Cannot find %s in experiment %s", fileName, experiment));
        return 0;
    }

    private static int getSizeOf(File experiment, String fileName)
    {
        File[] files = experiment.listFiles((f, n) -> n.equalsIgnoreCase(fileName));
        if (files != null && files.length == 1) {
            try {
                return FileUtils.loadFile(files[0]).size();
            } catch (IOException e) {
                System.out.println(String.format("%s in experiment %s is empty", fileName, experiment));
            }
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
        private final String nameOfExperiment;
        public int cyclicDefiners;
        public int introducedDefiners;
        private int filteredByReduction;
        private long alcReductionTime;
        private long timeOfLethe;
        public int filteredDefinersByReduction;
        private int sizeOfOntology;
        private int sizeOfSignature;
        private long semanticForgettingTime;
        private int differentResults;


        private ExperimentSummary(String nameOfExperiment)
        {
            this.nameOfExperiment = nameOfExperiment;
        }

        @Override
        public String toString()
        {
            return String.format("%s,%s,%s,%s,%s,%s,%s,%s,%d,%d,%d",
                    nameOfExperiment,
                    sizeOfOntology,
                    sizeOfSignature,
                    differentResults,
                    semanticForgettingTime,
                    alcReductionTime,
                    filteredByReduction,
                    timeOfLethe,
                    introducedDefiners,
                    cyclicDefiners,
                    filteredDefinersByReduction);
        }
    }
}
