package pc1.pc2.pc3.experiment;

import org.jetbrains.annotations.NotNull;
import org.semanticweb.owlapi.model.*;
import pc1.pc2.pc3.input.owl5.ParserLogger;
import pc1.pc2.pc3.om.IAxiom;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class VerboseOntologyLoader extends OntologyLoader
{

    private final Writer out;
    private final Writer err;

    /**
     * Creates a verbose ontology loader.
     * @param repository A path to the local repository of ontologies.
     * @param out Output writer, will be used for loggins messages.
     * @param err Error writer, will be used to log parsing errors (for example, for unsupported language constructs)
     */
    public VerboseOntologyLoader(String repository, Writer out, Writer err)
    {
        super();
        this.out = out;
        this.err = err;
    }

    @Override public OWLOntology loadOntology(IRI ontologyIRI, FileOutputStream outStream,
                                                 OWLOntologyManager owlOntologyManager)
    {
        String iriString = ontologyIRI.getIRIString();
        String ontologyName;
        int last = iriString.indexOf("/download");
        if (last > 0) {
            int start = iriString.lastIndexOf("/", last) + 1;
            if (last > start) {
                ontologyName = iriString.substring(start, last);
            }
            else {
                ontologyName = iriString;
            }
            writeLine(String.format("Downloading %s from %s", ontologyName, iriString));
        }
        else {
            ontologyName = new File(iriString).getName();
            if (ontologyName.isEmpty()) {
                ontologyName = iriString;
            }

            writeLine(String.format("Loading %s from %s", ontologyName, iriString));
        }

        ParserLogger.setInstance(err);
        OWLOntology clauses = super.loadOntology(ontologyIRI, outStream, owlOntologyManager);
        if(clauses != null) {
            writeLine(String.format("Ontology loaded (%d axioms, %d logical axioms)", clauses.getAxiomCount(),
                    clauses.getLogicalAxiomCount()));
        }
        return clauses;
    }

    @Override public OWLOntology extractModule(Set<IRI> signature, IRI moduleIRI,
                                                  OWLOntology owlOntology) throws OWLOntologyCreationException, OWLOntologyStorageException
    {
        String symbols = signature.stream().map(IRI::getIRIString).collect(Collectors.joining(", "));
        writeLine(String.format("Extracting module...(%s)", symbols));
        OWLOntology module = super.extractModule(signature, moduleIRI, owlOntology);
        if(module != null) {
            writeLine(String.format("Module extracted (%d axioms, %d logical axioms)", module.getAxiomCount(),
                    module.getLogicalAxiomCount()));
            writeLine(String.format("Module saved to %s", moduleIRI.getIRIString()));
        }
        else {
            writeLine("Module of is null");
        }
        return module;
    }

    @Override
    @NotNull
    public List<IAxiom> parseOntologyAsClauses(OWLOntology ontology)
    {
        writeLine("Parsing...");
        List<IAxiom> clauses = super.parseOntologyAsClauses(ontology);
        writeLine(String.format("%d axioms are loaded", clauses.size()));
        return clauses;
    }

    private void writeLine(String message)
    {
        try {
            out.append(message).append('\n');
            out.flush();
        } catch (IOException ignored) {

        }
    }
}
