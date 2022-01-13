package pc1.pc2.pc3;

import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import pc1.pc2.pc3.app.Bootstrap;
import pc1.pc2.pc3.om.IAxiom;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class InputGenerator
{
    private File outDir;
    private File ontFile;
    private final List<File> bgFiles = new LinkedList<>();

    private InputGenerator(String[] args)
    {
        Bootstrap.initializeApplication();
        parseArgs(args);
//        bgFiles.removeIf(f -> f.getPath().equals(ontFile.getPath()));
    }

    private void parseArgs(String[] args)
    {
        if (args.length < 4) {
            System.out.println("Invalid arguments");
            System.exit(1);
        }

        for (String arg : args) {
            System.out.println("Running with arguments " + arg);
        }

        for (int i = 0; i < args.length; i++) {
            String arg0 = args[i];
            if ("-ont".equalsIgnoreCase(arg0)) {
                ontFile = ArgsHelper.parseFileArguement(args[++i]);
            }
            else if ("-bg".equalsIgnoreCase(arg0)) {
                do {
                    String background = args[++i];
                    int quot1 = background.indexOf('\'');
                    if (quot1 >= 0) {
                        background = background.substring(quot1 + 1);
                    }
                    int quot2 = background.lastIndexOf('\'');
                    if (quot2 >= 0) {
                        background = background.substring(0, quot2);
                    }
                    int bracket1 = background.indexOf('[');
                    if (bracket1 >= 0) {
                        background = background.substring(bracket1 + 1);
                    }
                    int bracket2 = background.indexOf(']');
                    if (bracket2 >= 0) {
                        background = background.substring(0, bracket2);
                    }
                    int comma = background.indexOf(',');
                    if (comma >= 0) {
                        background = background.substring(0, comma);
                    }
                    bgFiles.add(ArgsHelper.parseFileArguement(background));
                } while (i < args.length - 1 && !args[i + 1].startsWith("-"));
            }
            else if ("-outDir".equalsIgnoreCase(arg0)) {
                outDir = ArgsHelper.parseOutputDirArg(arg0, args[++i], "Invalid output directory");
            }
        }
    }

    private void generateInput() throws OWLOntologyCreationException, IOException, OWLOntologyStorageException
    {
        Set<IAxiom> theory = new HashSet<>();
        for (File bgFile : bgFiles) {
            theory.addAll(ArgsHelper.parseOntology(bgFile));
        }
        Set<IAxiom> ontology = new HashSet<>(ArgsHelper.parseOntology(ontFile));

        String outPath = outDir.getCanonicalPath();
        ExperimentHelper.saveAxioms(ontology, outPath + File.separator + "Ontology");
        ExperimentHelper.saveAxioms(theory, outPath + File.separator + "Theory");
    }

    public static void main(String[] args) throws OWLOntologyCreationException, IOException, OWLOntologyStorageException
    {
        new InputGenerator(args).generateInput();
    }
}
