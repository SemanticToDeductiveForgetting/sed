package pc1.pc2.pc3;

import forgetting.Fame;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;

import java.io.*;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class ForgetFame implements Callable<Integer>
{
    private static final String usage = String.format("%s\n%s\n%s\n%s\n%s\n%s\n%s\n%s\n%s",
            "ForgetFame:",
            "Performs forgetting experiment relative to empty background theory and generates a uniform ",
            "interpolant file using Fame. This program is designed as a pipeline step that should be executed after " +
                    "DataLoader.",
            "Usage: ForgetFame -bg data-directory -ont ontology-file -outDir output-directory -sig " +
                    "signature-file [options]",
            "\tontology-file: path to the owl ontology file.",
            "\toutput-directory: a path to the directory where the uniform interpolant any other debug files will be " +
                    "written.",
            "\tsignature-file: a signature file in JSON format.",
            "Options:",
            "\t-verbose: verbose output.");
    private File ontFile;
    private List<SymbolData> signatureInfo;
    private File outDir;

    private int timeOut = 0;

    public ForgetFame(String[] args)
    {
        parseArguments(args);
    }

    public static void main(String[] args)
    {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        ForgetFame task = new ForgetFame(args);
        Future<Integer> future = executor.submit(task);

        try {
            System.out.println("Started..");
            if (task.timeOut > 0) {
                System.out.println(future.get(task.timeOut, TimeUnit.SECONDS));
            }
            else {
                future.get();
            }
            System.out.println("Finished!");
        } catch (TimeoutException e) {
            future.cancel(true);
            System.out.println("Terminated!");
        } catch (InterruptedException | ExecutionException ex) {
            ex.printStackTrace();
        }
        executor.shutdownNow();
    }

    private void logElapsedTime(long end, long start)
    {
        String path = outDir.getPath();
        try (FileWriter out = new FileWriter(path + File.separator + "Fame.time")) {
            long elapsedTime = end - start;
            writeLine(String.format("Time consumed (Nano seconds) = %d", elapsedTime), out);
            long seconds = TimeUnit.SECONDS.convert(elapsedTime, TimeUnit.NANOSECONDS);
            elapsedTime -= TimeUnit.NANOSECONDS.convert(seconds, TimeUnit.SECONDS);
            long milliSeconds = TimeUnit.MILLISECONDS.convert(elapsedTime, TimeUnit.NANOSECONDS);
            writeLine(String.format("Time consumed (Seconds.Milliseconds) = %d.%d", seconds, milliSeconds), out);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeLine(String message, Writer out)
    {
        try {
            out.append(message).append('\n');
            out.flush();
        } catch (IOException ignored) {

        }
    }

    private void parseArguments(String[] args)
    {
        if (args.length < 6) {
            System.out.println(usage);
            System.exit(1);
        }

        for (int i = 0; i < args.length; i++) {
            String arg0 = args[i];
            if ("-ont".equalsIgnoreCase(arg0)) {
                ontFile = ArgsHelper.parseFileArguement(args[++i]);
            }
            else if ("-sig".equalsIgnoreCase(arg0)) {
                signatureInfo = ArgsHelper.parseSignatureFileArg(args[++i], usage);
            }
            else if ("-outDir".equalsIgnoreCase(arg0)) {
                outDir = ArgsHelper.parseOutputDirArg(arg0, args[++i], usage);
            }
            else if ("-timeout".equalsIgnoreCase(arg0)) {
                timeOut = Integer.parseInt(args[++i]);
            }
        }
    }

    @Override
    public Integer call() throws Exception
    {
        System.out.println("Executing experiment with Fame...");

        OWLOntologyManager owlMgr = OWLManager.createOWLOntologyManager();
        OWLOntology ontology = owlMgr.loadOntology(IRI.create(ontFile));

        Set<OWLClass> concepts = ontology.getSignature().stream()
                .filter(s -> s instanceof OWLClass).map(s -> (OWLClass) s)
                .collect(Collectors.toSet());
        Set<OWLClass> forgettingSig = new HashSet<>();
        for (OWLClass concept : concepts) {
            String iri = concept.getIRI().getRemainder().or(concept.getIRI().toString());
            for (SymbolData symbolData : signatureInfo) {
                if (iri.equalsIgnoreCase(symbolData.getLabel())) {
                    forgettingSig.add(concept);
                }
            }
        }
        long before = System.nanoTime();
        Fame fame = new Fame();
        OWLOntology forgettingView = fame.FameRC(Collections.emptySet(), forgettingSig, ontology);
        long after = System.nanoTime();
        String viewPath = outDir.getPath() + File.separator + "Fame" + ".owl";
        Set<OWLAxiom> logicalAxioms =
                forgettingView.getLogicalAxioms().stream().map(a -> (OWLAxiom) a).collect(Collectors.toSet());
        forgettingView = owlMgr.createOntology(logicalAxioms, IRI.create(new File(viewPath)));

        try (FileOutputStream s = new FileOutputStream(viewPath)) {
            owlMgr.saveOntology(forgettingView, s);
        }

        logElapsedTime(after, before);

        return 1;
    }
}
