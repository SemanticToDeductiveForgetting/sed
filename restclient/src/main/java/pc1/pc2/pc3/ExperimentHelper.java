package pc1.pc2.pc3;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import pc1.pc2.pc3.om.IAxiom;
import pc1.pc2.pc3.om.IClause;
import pc1.pc2.pc3.output.Output;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class ExperimentHelper
{
    public enum SaveFormat
    {
        CLAUSAL,
        OWL,
    }

    public static void saveAxioms(Collection<IAxiom> axioms, String path)
            throws IOException, OWLOntologyCreationException, OWLOntologyStorageException
    {
        saveInHumanReadableForm(axioms, path);
        saveAxiomsInOWLForm(axioms, path + ".owl");
    }

    public static void save(Collection<IAxiom> axioms, String path, String filename, SaveFormat... formats)
            throws OWLOntologyCreationException, OWLOntologyStorageException, IOException
    {
        for (SaveFormat format : formats) {
            if (format == SaveFormat.CLAUSAL) {
                saveInHumanReadableForm(axioms, path + File.separator + filename + ".clausal");
            }
            else if (format == SaveFormat.OWL) {
                saveAxiomsInOWLForm(axioms, path + File.separator + filename + ".owl");
            }
        }
    }

    public static void saveInHumanReadableForm(Collection<?> clauses, String path, OpenOption... openOptions)
            throws IOException
    {
        List<String> lines = clauses.stream()
                .map(Object::toString)
                .collect(Collectors.toList());
        Path filePath = Path.of(path);
        // Files.write(filePath, StringUtils.toByteArray(file), openOptions);
        Files.write(filePath, lines, StandardCharsets.UTF_8, openOptions);
    }

    public static void saveClausesInOWLForm(Collection<IClause> clauses,
                                            String path)
            throws OWLOntologyCreationException, OWLOntologyStorageException
    {
        IRI outputIri = IRI.create(new File(path));
        OWLOntology owlOntology = Output.convertClauses(clauses, outputIri);
        owlOntology.saveOntology(outputIri);
    }

    public static OWLOntology saveAxiomsInOWLForm(Collection<IAxiom> clauses,
                                                  String path)
            throws OWLOntologyCreationException, OWLOntologyStorageException
    {
        IRI outputIri = IRI.create(new File(path));
        OWLOntology owlOntology = Output.convertAxioms(clauses, outputIri);
        owlOntology.saveOntology(outputIri);
        return owlOntology;
    }

    public static void logElapsedTime(long end, long start, Writer out)
    {
        long elapsedTime = end - start;
        writeLine(String.format("Time consumed (Nano seconds) = %d", elapsedTime), out);
        long seconds = TimeUnit.SECONDS.convert(elapsedTime, TimeUnit.NANOSECONDS);
        elapsedTime -= TimeUnit.NANOSECONDS.convert(seconds, TimeUnit.SECONDS);
        long milliSeconds = TimeUnit.MILLISECONDS.convert(elapsedTime, TimeUnit.NANOSECONDS);
        writeLine(String.format("Time consumed (Seconds.Milliseconds) = %d.%d", seconds, milliSeconds), out);
    }

    public static void writeLine(String message, Writer out)
    {
        try {
            out.append(message).append('\n');
            out.flush();
        } catch (IOException ignored) {

        }
    }
}
