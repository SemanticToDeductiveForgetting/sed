package pc1.pc2.pc3;

import org.jetbrains.annotations.NotNull;
import pc1.pc2.pc3.error.ParseException;
import pc1.pc2.pc3.input.AxiomBuilder;
import pc1.pc2.pc3.logic.factory.DefinerFactory;
import pc1.pc2.pc3.om.*;
import pc1.pc2.pc3.utils.CollectionUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class FileOntology implements IOntology
{
    public static final OpenOption[] setContentFileOptions = new OpenOption[]{
            StandardOpenOption.CREATE,
            StandardOpenOption.WRITE,
            StandardOpenOption.TRUNCATE_EXISTING};
    public static final OpenOption[] APPEND_FILE_OPTIONS = new OpenOption[]{
            StandardOpenOption.CREATE,
            StandardOpenOption.WRITE,
            StandardOpenOption.APPEND};
    private final String activeOntologyFile;
    private final String finalOntologyFile;
    private final List<IConceptLiteral> signature;
    protected final DefinerFactory definerFactory;
    private final IAxiomExtractor axiomExtractor;

    public FileOntology(String ontologyFile, List<IConceptLiteral> forgettingSignature,
                        DefinerFactory definerFactory, IAxiomExtractor axiomExtractor)
    {
        this.activeOntologyFile = ontologyFile + ".active";
        this.finalOntologyFile = ontologyFile + ".clausal";
        signature = forgettingSignature;
        this.definerFactory = definerFactory;
        this.axiomExtractor = axiomExtractor;
    }

    @Override
    public Collection<IAxiom> getAllActiveAxioms()
    {
        return getAxioms(activeOntologyFile);
    }

    @Override
    public Collection<IAxiom> getAllFinalAxioms()
    {
        return getAxioms(finalOntologyFile);
    }

    private Collection<IAxiom> getAxioms(String file)
    {
        File input = new File(file);
        List<IAxiom> axioms = extractAxioms(input);
        return axioms;
    }

    @NotNull
    private List<IAxiom> extractAxioms(File input)
    {
        List<IAxiom> axioms = new LinkedList<>();
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(input))) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                if (!line.isBlank()) {
                    IAxiom axiom;
                    try {
                        axiom = AxiomBuilder.fromString(line);
                        axioms.add(axiom);
                    } catch (ParseException ignored) {
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return axioms;
    }

    private Collection<IAxiom> getAxioms(Predicate<String> symbolFilter, String file)
    {
        File input = new File(file);
        Path temp = null;
        try {
            temp = Files.createTempFile("ForgettingView", null);
        } catch (IOException e) {
            e.printStackTrace();
        }
        List<IAxiom> axioms = new LinkedList<>();
        if (temp != null) {
            axioms = axiomExtractor.extract(input, temp, symbolFilter);
            try {
                Files.move(temp, Path.of(input.toURI()), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        return axioms;
    }

    @Override
    public void addAxiom(IAxiom axiom)
    {
        addAxioms(Collections.singleton(axiom));
    }

    @Override
    public void addAxioms(Collection<IAxiom> axioms)
    {
        writeAxioms(axioms, APPEND_FILE_OPTIONS);
    }

    @Override
    public void setContent(Collection<IAxiom> axioms)
    {
        writeAxioms(axioms, setContentFileOptions);
    }

    protected void writeAxioms(Collection<IAxiom> axioms, OpenOption[] fileOptions)
    {
        Set<IAxiom> activeAxioms = new HashSet<>(axioms);
        Set<IAxiom> finalAxioms = getFinalAxioms(axioms);
        activeAxioms.removeAll(finalAxioms);

        if (!finalAxioms.isEmpty()) {
            addToFile(finalAxioms, finalOntologyFile, fileOptions);
        }

        addToFile(activeAxioms, activeOntologyFile, fileOptions);
    }

    private Set<IAxiom> getFinalAxioms(Collection<IAxiom> axioms)
    {
        Set<IAxiom> finalAxioms = new HashSet<>();
        for (IAxiom axiom : axioms) {
            Collection<ILiteral> axiomSig = axiom.getSignature();
            axiomSig.retainAll(CollectionUtils.merge(signature, definerFactory.getDefiners()));
            if (axiomSig.isEmpty()) {
                finalAxioms.add(axiom);
            }
        }
        return finalAxioms;
    }

    protected void addToFile(Collection<?> axioms, String file, OpenOption[] openOptions)
    {
        try {
            ExperimentHelper.saveInHumanReadableForm(axioms, file, openOptions);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Collection<IAxiom> extractAxiomsOf(Collection<IConceptLiteral> symbols)
    {
        Collection<IAxiom> allAxioms = getAxioms(new SymbolFilter(symbols), activeOntologyFile);
        IOntology ontology = FactoryMgr.getCommonFactory().createOntology();
        ontology.setContent(allAxioms);
        Collection<IAxiom> extractedAxioms = ontology.extractAxiomsOf(symbols);
        allAxioms.removeAll(extractedAxioms);
        if (!allAxioms.isEmpty()) {
            addToFile(allAxioms, activeOntologyFile, APPEND_FILE_OPTIONS);
        }
        return extractedAxioms;
    }

    @Override
    public void begin()
    {

    }

    @Override
    public void end()
    {

    }

    private static class SymbolFilter implements Predicate<String>
    {
        private final Collection<String> symbols;

        public SymbolFilter(Collection<IConceptLiteral> symbols)
        {
            this.symbols = symbols.stream().map(ILiteral::getSymbol).collect(Collectors.toList());
        }

        @Override
        public boolean test(String line)
        {
            return symbols.stream().anyMatch(line::contains);
        }
    }

}
