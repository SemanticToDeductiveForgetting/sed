package pc1.pc2.pc3;

import org.jetbrains.annotations.NotNull;
import pc1.pc2.pc3.app.Bootstrap;
import pc1.pc2.pc3.utils.FileUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.*;

public class RedundantResolutions
{

    private static String usage = String.format("%s\n%s\n%s\n%s\n%s\n%s\n%s\n%s\n",
            "RedundantResolutions:",
            "Finds and reports statistics on the redundant resolutions performed during the forgetting process. ",
            "The redundant resolutions are resolutions that did not lead to any information in the forgetting view.",
            "Usage: RedundantResolutions -bg background-file -ont ontology-file -outDir output-directory",
            "\tbackground-file: path to the background ontology resolution file",
            "\tontology-file: path to the main ontology resolution file.",
            "\trestriction-file: path to the restricted resolutions file",
            "\toutput-directory: a path to the directory where the output statistics will be written.");
    private static File outDir;
    private static Set<PerformedResolution> ontPerformedResolutions;
    private static Set<PerformedResolution> bgPerformedResolutions;
    private static Set<Resolution> restrictions;
    private static Set<String> inputBGClauses;

    public static void main(String[] args) throws IOException
    {
        Bootstrap.initializeApplication();
        parseArguments(args);
        Set<String> effectiveBGClauses = findEffectiveBGClauses();
        Set<PerformedResolution> redundancies = getRedundantResolutions(effectiveBGClauses);
        report(redundancies);
    }

    private static void report(Set<PerformedResolution> redundancies)
    {
        String filePath = outDir.getPath() + File.separator + "redundantResolutions.csv";
        try (Writer out = new FileWriter(filePath)) {
            writeLine(out, "RedundantClause,Premise1,Premise2");
            redundancies.stream()
                    .map(r -> String.format("%s,%s,%s", r.outputClause, r.premise1, r.premise2))
                    .forEach(l -> writeLine(out, l));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void writeLine(Writer out, String message)
    {
        try {
            out.append(message).append('\n');
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static Set<PerformedResolution> getRedundantResolutions(Set<String> effectiveBGClauses)
    {
        Set<PerformedResolution> redundancies = new HashSet<>();
        for (Resolution restriction : restrictions) {
            addResolutionIfRedundant(effectiveBGClauses, redundancies, restriction.premise1);
            addResolutionIfRedundant(effectiveBGClauses, redundancies, restriction.premise2);
        }
        return redundancies;
    }

    private static void addResolutionIfRedundant(Set<String> effectiveBGClauses,
                                                 Set<PerformedResolution> redundantResolutions,
                                                 String clause)
    {
        if (clause != null && !clause.isBlank() && !effectiveBGClauses.contains(clause) && !inputBGClauses.contains(clause)) {
            Optional<PerformedResolution> premise = getPerformedResolution(bgPerformedResolutions, clause);
            premise.ifPresent(redundantResolutions::add);
        }
    }

    @NotNull private static Optional<PerformedResolution> getPerformedResolution(
            Set<PerformedResolution> bgPerformedResolutions, String premise1)
    {
        return bgPerformedResolutions.stream()
                .filter(r -> r.outputClause.equals(premise1))
                .findFirst();
    }

    private static Set<String> findEffectiveBGClauses()
    {
        Set<String> bgClauses = new HashSet<>();
        for (PerformedResolution performedResolution : ontPerformedResolutions) {
            String premise1 = performedResolution.premise1;
            if (premise1 != null && !inputBGClauses.contains(premise1)&& premise1.startsWith("BG")) {
                bgClauses.add(premise1);
            }
            String premise2 = performedResolution.premise2;
            if (premise2 != null && !inputBGClauses.contains(premise2)&& premise2.startsWith("BG")) {
                bgClauses.add(premise2);
            }
        }

        LinkedList<String> stack = new LinkedList<>(bgClauses);
        while (!stack.isEmpty()) {
            String clause = stack.pop();
            List<String> premises = getPremises(clause, bgPerformedResolutions);
            premises.stream().filter(bgClauses::add).forEach(stack::add);
        }

        return bgClauses;
    }

    private static List<String> getPremises(String clause, Set<PerformedResolution> bgPerformedResolutions)
    {
        Optional<PerformedResolution> resolution = getPerformedResolution(bgPerformedResolutions, clause);
        if (resolution.isEmpty()) {
            throw new RuntimeException(String.format("Cannot find the clause %s in the background theory", clause));
        }
        PerformedResolution res = resolution.get();
        if ((res.premise1 != null && !res.premise1.isEmpty()) || (res.premise2 != null && !res.premise2.isEmpty())) {
            List<String> premises = new LinkedList<>();
            if (res.premise1 != null && !res.premise1.isEmpty() && !inputBGClauses.contains(res.premise1)) {
                premises.add(res.premise1);
            }
            if (res.premise2 != null && !res.premise2.isEmpty() && !inputBGClauses.contains(res.premise2)) {
                premises.add(res.premise2);
            }
            return premises;
        }
        return Collections.emptyList();
    }

    private static void parseArguments(String[] args) throws IOException
    {
        if (args.length < 3) {
            System.out.println(usage);
            System.exit(1);
        }

        for (int i = 0; i < args.length; i++) {
            String arg0 = args[i];
            if ("-bg".equalsIgnoreCase(arg0)) {
                File backgroundFile = ArgsHelper.parseFileArguement(args[++i]);
                bgPerformedResolutions = parseValidResolutions(backgroundFile);
                inputBGClauses = parseInputClauses(backgroundFile);
            }
            else if ("-ont".equalsIgnoreCase(arg0)) {
                File ontFile = ArgsHelper.parseFileArguement(args[++i]);
                ontPerformedResolutions = parseValidResolutions(ontFile);
            }
            else if ("-restrictions".equalsIgnoreCase(arg0)) {
                restrictions = parseRestrictedResolutions(ArgsHelper.parseFileArguement(args[++i]));
            }
            else if ("-outDir".equalsIgnoreCase(arg0)) {
                outDir = ArgsHelper.parseOutputDirArg(arg0, args[++i], usage);
            }
        }
    }

    private static Set<Resolution> parseRestrictedResolutions(File restricted) throws IOException
    {
        Set<Resolution> resolutions = new HashSet<>();
        List<String> lines = FileUtils.loadFile(restricted);
        for (String line : lines) {
            String[] parts = line.split(",");
            Resolution resolution = new Resolution();
            resolution.premise1 = parts[0].trim();
            resolution.premise2 = parts[1].trim();
            resolutions.add(resolution);
        }
        return resolutions;
    }

    private static Set<PerformedResolution> parseValidResolutions(File ontFile) throws IOException
    {
        Set<PerformedResolution> performedResolutions = new HashSet<>();
        List<String> lines = FileUtils.loadFile(ontFile);
        for (String line : lines) {
            String[] parts = line.split(",");
            String out = parts[0];
            if (parts.length >= 3 && !parts[2].isBlank()) {
                PerformedResolution r = new PerformedResolution();
                r.outputClause = out.trim();
                String in = String.join(",", Arrays.copyOfRange(parts, 2, parts.length));
                in = in.substring(in.indexOf('(') + 1, in.indexOf(')'));
                if (in.contains(",")) {
                    String[] inParts = in.split(",");
                    r.premise1 = inParts[0].trim();
                    r.premise2 = inParts[1].trim();
                }
                else {
                    r.premise1 = in.trim();
                }
                performedResolutions.add(r);
            }
        }
        return performedResolutions;
    }

    private static Set<String> parseInputClauses(File ontFile) throws IOException
    {
        Set<String> input = new HashSet<>();
        List<String> lines = FileUtils.loadFile(ontFile);
        for (String line : lines) {
            if (!line.isBlank()) {
                String[] parts = line.split(",");
                if (parts.length < 3 || parts[2].isBlank()) {
                    input.add(parts[0].trim());
                }
            }
        }
        return input;
    }

    private static class Resolution
    {
        String premise1 = "";
        String premise2 = "";
    }

    private static class PerformedResolution extends Resolution
    {
        String outputClause = "";

        @Override public int hashCode()
        {
            return outputClause.hashCode();
        }

        @Override public boolean equals(Object obj)
        {
            if (obj instanceof PerformedResolution) {
                return outputClause.equals(((PerformedResolution) obj).outputClause);
            }
            if (obj instanceof String) {
                return outputClause.equals(obj);
            }
            return super.equals(obj);
        }
    }
}
