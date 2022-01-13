package pc1.pc2.pc3;

import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import pc1.pc2.pc3.experiment.VerboseALCForgettingEngine;
import pc1.pc2.pc3.experiment.VerboseForgettingEngine;
import pc1.pc2.pc3.rest.OntologyData;
import pc1.pc2.pc3.rest.SymbolData;
import pc1.pc2.pc3.error.ParseException;
import pc1.pc2.pc3.input.AxiomBuilder;
import pc1.pc2.pc3.logic.ALCForgettingEngine;
import pc1.pc2.pc3.logic.ForgettingEngine;
import pc1.pc2.pc3.logic.factory.DefinerFactory;
import pc1.pc2.pc3.logic.reduction.ALCSemanticToDeductive;
import pc1.pc2.pc3.om.IAxiom;
import pc1.pc2.pc3.utils.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.LinkedList;
import java.util.List;

public class ArgsHelper
{
    public static File parseOutputDirArg(String arg0, String arg1, String usage)
    {
        if (arg0.equalsIgnoreCase("-outDir")) {
            File file = new File(arg1);
            if (file.isFile() || (!file.exists() && !file.mkdirs())) {
                System.out.println("Cannot create output directory");
                System.out.println(usage);
            }
            return file;
        }
        return null;
    }

    public static File parseRepositoryArg(String arg0, String arg1, String usage)
    {
        if (arg0.equalsIgnoreCase("-repo")) {
            return parseDirectoryArguement(arg1);
        }
        return null;
    }

    @Nullable static File parseDirectoryArguement(String dir)
    {
        File file = new File(dir);
        if (!file.isDirectory()) {
            System.out.println(String.format("%s is not a valid directory", file));
            return null;
        }
        return file;
    }

    @Nullable
    public static File parseFileArguement(String filePath)
    {
        File file = new File(filePath);
        if (file.isDirectory()) {
            System.out.println(String.format("%s is not a valid file", file));
            return null;
        }
        return file;
    }

    public static List<SymbolData> parseSignatureFileArg(String filePath, String usage)
    {
        try {
            File file = new File(filePath);
            if(file.isFile() && file.exists()) {
                String string = String.join("", FileUtils.loadFile(file));
                JSONArray jconcepts = new JSONArray(string);
                List<SymbolData> signature = new LinkedList<>();
                for (int i = 0; i < jconcepts.length(); i++) {
                    signature.add(SymbolData.deserialize(jconcepts.getJSONObject(i)));
                }
                return signature;
            }
            else {
                System.out.println(usage);
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println(usage);
        }
        return null;
    }

    public static OntologyData parseOntologyDescriptorFileArg(String filePath, String usage)
    {
        try {
            File file = new File(filePath);
            if (file.isFile() && file.exists()) {
                String string = String.join("", FileUtils.loadFile(file));
                return OntologyData.deserialize(string);
            }
            else {
                System.out.println(usage);
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println(usage);
        }
        return null;
    }

    public static ForgettingEngine createForgettingEngine(boolean verbose, File outDir, String name)
    {
        ForgettingEngine engine;
        if (verbose) {
            OutputStreamWriter out = new OutputStreamWriter(System.out);
            engine = new VerboseForgettingEngine(out, outDir, name);
        }
        else {
            engine = new ForgettingEngine();
        }
        return engine;
    }

    public static ALCForgettingEngine createALCForgettingEngine(boolean verbose, File outDir, DefinerFactory factory,
                                                                ALCSemanticToDeductive alcSemanticToDeductive)
    {
        ALCForgettingEngine engine;
        if (verbose) {
            OutputStreamWriter out = new OutputStreamWriter(System.out);
            engine = new VerboseALCForgettingEngine(out, outDir, factory, alcSemanticToDeductive);
        }
        else {
            engine = new ALCForgettingEngine(factory, alcSemanticToDeductive);
        }
        return engine;
    }

    public static List<IAxiom> parseOntology(File file) throws IOException
    {
        System.out.println(String.format("Parsing %s", file.getName()));
        List<String> lines = FileUtils.loadFile(file);
        List<IAxiom> ontology = new LinkedList<>();
        for (String line : lines) {
            IAxiom axiom;
            try {
                axiom = AxiomBuilder.fromString(line);
                if (axiom != null) {
                    ontology.add(axiom);
                }
            } catch (ParseException e) {
                System.out.println(String.format("Cannot parse statement %s from file %s", line, file));
                System.out.println("Statement will be ignored");
            }
        }
        return ontology;
    }

    public static List<IAxiom> parseOntologyAsAxioms(File file) throws IOException
    {
        List<String> lines = FileUtils.loadFile(file);
        List<IAxiom> ontology = new LinkedList<>();
        for (String line : lines) {
            IAxiom axiom = null;
            try {
                axiom = AxiomBuilder.fromString(line);
                ontology.add(axiom);
            } catch (ParseException e) {
                System.out.println(String.format("Cannot parse statement %s from file %s", line, file));
                System.out.println("Statement will be ignored");
            }
        }
        return ontology;
    }

    public static List<IAxiom> parseOntologyAsClauses(File file) throws IOException
    {
        System.out.println(String.format("Parsing %s", file.getName()));
        List<String> lines = FileUtils.loadFile(file);
        List<IAxiom> ontology = new LinkedList<>();
        for (String line : lines) {
            IAxiom axiom = null;
            try {
                axiom = AxiomBuilder.fromString(String.format("TOP -> %s", line));
                ontology.add(axiom);
            } catch (ParseException e) {
                System.out.println(String.format("Cannot parse statement %s from file %s", line, file));
                System.out.println("Statement will be ignored");
            }
        }
        return ontology;
    }
}
