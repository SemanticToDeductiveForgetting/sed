package pc1.pc2.pc3;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import uk.ac.man.cs.lethe.internal.dl.datatypes.*;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class MergeOntology
{
    public static final String ONT_CLAUSE_PREFIX = "__O";
    public static final String BG_CLAUSE_PREFIX = "__BG";
    private static String usage = String.format("%s\n%s\n%s\n%s\n%s\n%s",
            "MergeOntology:",
            "Merges the given ontologies",
            "Usage: MergeOntology -ont ontology-file -theory theory-file -out merged-file",
            "\tontology-file: path to the first ontology. The ontology should be in owl format.",
            "\ttheory-file: a path to the second ontology. The ontology should be in owl format.",
            "\tmerged-file: path of the merged ontology.");

    private static File ontFile;
    private static File bgFile;
    private static String outFile;

    public static void main(
            String[] args) throws OWLOntologyStorageException, OWLOntologyCreationException, IOException
    {
        parseArguements(args);

        OWLOntologyManager owlmgr = OWLManager.createOWLOntologyManager();
        OWLOntology ontology = owlmgr.loadOntology(IRI.create(ontFile));
        OWLOntology bg = owlmgr.loadOntology(IRI.create(bgFile));

        Ontology letheOnt = LetheHelper.toLetheOntology(ontology);
        Ontology letheBg = LetheHelper.toLetheOntology(bg);

        OWLOntology merged = LetheHelper.toOWLOntology(letheOnt, letheBg);
        Set<OWLAxiom> logicalAxioms = merged.getLogicalAxioms().stream().map(a -> (OWLAxiom) a).collect(Collectors.toSet());
        merged = owlmgr.createOntology(logicalAxioms, IRI.create(new File(outFile)));
        try (FileOutputStream out = new FileOutputStream(outFile)) {
            owlmgr.saveOntology(merged, out);
        }
    }

    private static void parseArguements(String[] args)
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
            else if ("-theory".equalsIgnoreCase(arg0)) {
                bgFile = ArgsHelper.parseFileArguement(args[++i]);
            }
            else if ("-out".equalsIgnoreCase(arg0)) {
                outFile = args[++i];
            }
        }
    }

    private static class NameGenerator
    {
        private int idx = 1;
        private String label;


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
