package pc1.pc2.pc3;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import scala.collection.Iterator;
import scala.collection.JavaConversions;
import uk.ac.man.cs.lethe.internal.dl.datatypes.*;
import uk.ac.man.cs.lethe.internal.dl.forgetting.DirectALCForgetter$;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class ForgetLethe implements Callable<Integer>
{
    public static final String ONT_CLAUSE_PREFIX = "__O";
    public static final String BG_CLAUSE_PREFIX = "__BG";
    private static final String usage = String.format("%s\n%s\n%s\n%s\n%s\n%s\n%s\n%s\n%s\n%s\n%s\n%s",
            "ForgetLethe:",
            "Performs forgetting relative to a background theory using Lethe",
            "Usage: ForgetLethe -bg data-directory -ont ontology-file -outDir output-directory -sig signature-file" +
                    "[options]",
            "\tbackground-file: path to the background ontology file with extension .clausal.",
            "\t\tThese files will be loaded and parsed to construct background theory.",
            "\tontology-file: path to the main ontology file. The ontology should be in string clausal form",
            "\t\tif file is present in data-directory then it will be excluded from the background theory",
            "\toutput-directory: a path to the directory where the uniform interpolant any other debug files will be " +
                    "written.",
            "\tsignature-file: a signature file in JSON format.",
            "Options:",
            "\t-timout [timeout]: timeout value in seconds",
            "\t-verbose: verbose output.");
    private final DirectALCForgetter$ forgetter = DirectALCForgetter$.MODULE$;
    private File ontFile;
    private List<SymbolData> signatureInfo;
    private File outDir;
    private File bgFile;
    private int timeOut = 0;

    public ForgetLethe(String[] args)
    {
        parseArguments(args);
    }

    public static void main(String[] args)
    {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        ForgetLethe task = new ForgetLethe(args);
        Future<Integer> future = executor.submit(task);

        try {
            System.out.printf("Started with timeout %s..%n", task.timeOut);
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

    private void reportRedundancies(int redundancies)
    {
        String fileLoc = outDir.getPath() + File.separator + "Lethe" + ".redundancies";
        File file = new File(fileLoc);
        writeFile(String.valueOf(redundancies), file);
    }

    private void writeFile(String string, File file)
    {
        try (PrintStream printer = new PrintStream(file)) {
            printer.println(string);
        } catch (FileNotFoundException e) {
            System.out.println("Cannot create file " + file);
            e.printStackTrace();
        }
    }

    private Ontology addResolvingClauses(Ontology view, Set<String> annotators)
    {
        Set<Axiom> newTBox = new HashSet<>(JavaConversions.setAsJavaSet(view.tbox().axioms()));
        for (String annotator : annotators) {
            newTBox.add(new Subsumption(new BaseConcept(annotator), BottomConcept$.MODULE$));
        }

        return new Ontology(new TBox(JavaConversions.asScalaSet(newTBox).toSet()), view.abox(), view.rbox());
    }

    private Set<String> getAnnotators(Ontology view)
    {
        Set<String> annotators = new HashSet<>();
        for (DLStatement clause : JavaConversions.asJavaCollection(view.statements())) {
            JavaConversions.setAsJavaSet(clause.atomicConcepts()).stream()
                    .filter(s -> s.startsWith(ONT_CLAUSE_PREFIX) || s.startsWith(BG_CLAUSE_PREFIX))
                    .forEach(annotators::add);
        }
        return annotators;
    }

    private void reportOntology(Ontology ontology, String fileName)
    {
        String fileLoc = outDir.getPath() + File.separator + fileName + ".dl";
        File dlFile = new File(fileLoc);
        writeFile(ontology.toString(), dlFile);
    }

    private Ontology annotateOntology(Ontology ontology, NameGenerator annotation)
    {
        Iterator<Axiom> iterator = ontology.tbox().axioms().iterator();
        java.util.Set<uk.ac.man.cs.lethe.internal.dl.datatypes.Axiom> newTBox = new HashSet<>();
        while (iterator.hasNext()) {
            uk.ac.man.cs.lethe.internal.dl.datatypes.Axiom axiom = iterator.next();
            if (axiom instanceof Subsumption) {
                BaseConcept annotationSymbol = new BaseConcept(annotation.generate());
                newTBox.add(annotateSubsumption(((Subsumption) axiom).subsumer(), ((Subsumption) axiom).subsumee(),
                        annotationSymbol));
            }
            else if (axiom instanceof uk.ac.man.cs.lethe.internal.dl.datatypes.ConceptEquivalence) {
                Concept left = ((uk.ac.man.cs.lethe.internal.dl.datatypes.ConceptEquivalence) axiom).leftConcept();
                Concept right = ((uk.ac.man.cs.lethe.internal.dl.datatypes.ConceptEquivalence) axiom).rightConcept();

                BaseConcept symbol = new BaseConcept(annotation.generate());
                newTBox.add(annotateSubsumption(left, right, symbol));

                symbol = new BaseConcept(annotation.generate());
                newTBox.add(annotateSubsumption(right, left, symbol));
            }
        }

        return new Ontology(
                new TBox(JavaConversions.collectionAsScalaIterable(newTBox).toSet()),
                ontology.abox(), ontology.rbox());
    }

    private Subsumption annotateSubsumption(Concept subsumer, Concept subsumee, BaseConcept annotationSymbol)
    {
        java.util.Set<Concept> concepts = new HashSet<>();
        concepts.add(subsumee);
        concepts.add(annotationSymbol);
        Concept newSubsumee =
                new uk.ac.man.cs.lethe.internal.dl.datatypes.ConceptDisjunction(
                        JavaConversions.collectionAsScalaIterable(concepts).toSet());
        return new Subsumption(subsumer, newSubsumee);
    }

    private int filterOutAxiomsNotFromOntology(Ontology ontology)
    {
        Set<DLStatement> redundancies =
                getStatementsNotFromOntology(JavaConversions.asJavaCollection(ontology.statements()));
        for (DLStatement statement : redundancies) {
            ontology.remove(statement);
        }
        return redundancies.size();
    }

    private Set<DLStatement> getStatementsNotFromOntology(Collection<DLStatement> statements)
    {
        Set<DLStatement> nonMatching = new HashSet<>();
        for (DLStatement statement : statements) {
            if (JavaConversions.setAsJavaSet(statement.atomicConcepts()).stream()
                    .noneMatch(s -> s.startsWith(ONT_CLAUSE_PREFIX))) {
                nonMatching.add(statement);
            }
        }
        return nonMatching;
    }


    public String normaliseSymbol(String symbol)
    {
        //noinspection RegExpRedundantEscape
        return symbol.toLowerCase().replaceAll("[\\s\\(\\)]", "-").replaceAll("[^a-zA-Z0-9_-]", "");
    }

    private void logElapsedTime(long end, long start)
    {
        String path = outDir.getPath();
        try (FileWriter out = new FileWriter(path + File.separator + "Lethe.time")) {
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
            else if ("-bg".equalsIgnoreCase(arg0)) {
                bgFile = ArgsHelper.parseFileArguement(args[++i]);
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
        System.out.println("Executing experiment with Lethe...");

        OWLOntologyManager owlMgr = OWLManager.createOWLOntologyManager();
        OWLOntology ontology = owlMgr.loadOntology(IRI.create(ontFile));
        OWLOntology bg = bgFile != null ? owlMgr.loadOntology(IRI.create(bgFile)) : owlMgr.createOntology();

        long before = System.nanoTime();
        Ontology letheOnt = LetheHelper.toLetheOntology(ontology);
        Ontology letheBg = LetheHelper.toLetheOntology(bg);

        Set<String> ontSig = new HashSet<>(JavaConversions.setAsJavaSet(letheOnt.signature()));
        ontSig.addAll(JavaConversions.setAsJavaSet(letheBg.signature()));
        Map<String, String> fullName = new HashMap<>();
        for (String ontSymbol : ontSig) {
            IRI iri = IRI.create(ontSymbol);
            fullName.put(iri.getRemainder().or(iri.toString()), iri.toString());
        }
        Set<String> signature = signatureInfo.stream()
                .map(SymbolData::getLabel)
                .map(this::normaliseSymbol)
                .map(s -> fullName.getOrDefault(s, s))
                .collect(Collectors.toSet());

        if (bgFile != null) {
            letheOnt = annotateOntology(letheOnt, new NameGenerator(ONT_CLAUSE_PREFIX));
            letheBg = annotateOntology(letheBg, new NameGenerator(BG_CLAUSE_PREFIX));
        }

        Ontology mergedOntology = LetheHelper.mergeOntologies(letheOnt, letheBg);
        Ontology view = forgetter.forget(mergedOntology, JavaConversions.asScalaSet(signature).toSet());
//        int redundancies = filterOutAxiomsNotFromOntology(view);
        reportOntology(view, "lethe");


        if(bgFile != null) {
            Set<String> annotators = getAnnotators(view);
            if (!annotators.isEmpty()) {
                view = addResolvingClauses(view, annotators);
                view = forgetter.forget(view, JavaConversions.asScalaSet(annotators).toSet());
            }
        }

        // remove annotator clauses using resolution after appending clauses on the form "!__O | FALSE"
        OWLOntology forgettingView = LetheHelper.toOWLOntology(view);
        long after = System.nanoTime();

//        reportOntology(mergedOntology, "letheInputOntAndBG");

        String viewPath = outDir.getPath() + File.separator + "Lethe" + ".owl";
        Set<OWLAxiom> logicalAxioms =
                forgettingView.getLogicalAxioms().stream().map(a -> (OWLAxiom) a).collect(Collectors.toSet());
        forgettingView = owlMgr.createOntology(logicalAxioms, IRI.create(new File(viewPath)));

        logElapsedTime(after, before);
//        reportRedundancies(redundancies);

        try (FileOutputStream out = new FileOutputStream(viewPath)) {
            owlMgr.saveOntology(forgettingView, out);
        }

        return 1;
    }

    private static class NameGenerator
    {
        private int idx = 1;
        private final String label;


        public NameGenerator(String label)
        {
            this.label = label;
        }

        public String generate()
        {
            return label + idx++;
        }
    }
}
