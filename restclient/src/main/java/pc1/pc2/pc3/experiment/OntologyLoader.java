package pc1.pc2.pc3.experiment;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.semanticweb.owlapi.model.*;
import pc1.pc2.pc3.input.owl5.AxiomVisitor;
import pc1.pc2.pc3.input.owl5.LoadingException;
import pc1.pc2.pc3.input.owl5.OWLHelper;
import pc1.pc2.pc3.input.owl5.format.Formatter;
import pc1.pc2.pc3.om.IAxiom;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class OntologyLoader
{

    public OntologyLoader()
    {
    }

    @Nullable
    public OWLOntology loadOntology(IRI ontologyIRI, FileOutputStream outStream,
                                    OWLOntologyManager owlOntologyManager)
    {
        try {
            return OWLHelper.loadOntology(ontologyIRI, outStream, owlOntologyManager);

        } catch (LoadingException | OWLOntologyStorageException e) {
            e.printStackTrace();
        }
        return null;
    }

    @NotNull
    public List<IAxiom> parseOntologyAsClauses(OWLOntology ontology)
    {
        return new ArrayList<>(new AxiomVisitor().parse(ontology, Formatter.Nnf));
    }

    @NotNull
    public List<IAxiom> parseOntologyAsClauses(OWLOntology ontology, boolean parseAnnotations,
                                               boolean parseLogicalAxioms)
    {
        return new ArrayList<>(new AxiomVisitor(parseAnnotations, parseLogicalAxioms).parse(ontology, Formatter.Nnf));
    }

    @NotNull
    public List<IAxiom> parseOntologyAsAxioms(OWLOntology ontology, boolean parseAnnotations,
                                              boolean parseLogicalAxioms)
    {
        return new AxiomVisitor(parseAnnotations, parseLogicalAxioms).parse(ontology, Formatter.Default);
    }

    public OWLOntology extractModule(Set<IRI> signature, IRI moduleIRI, OWLOntology owlOntology)
            throws OWLOntologyCreationException, OWLOntologyStorageException
    {
        OWLOntology module = OWLHelper.extractModule(owlOntology, moduleIRI, signature);
        module.saveOntology(moduleIRI);
        return module;
    }
}