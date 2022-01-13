package pc1.pc2.pc3.input.owl5;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.semanticweb.owlapi.formats.OWLXMLDocumentFormat;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.model.parameters.Imports;

import java.io.OutputStream;
import java.util.Set;
import java.util.stream.Collectors;

public class OWLHelper
{
    @NotNull public static OWLOntology loadOntology(@NotNull IRI iri, @Nullable OutputStream outStream,
                                                    @NotNull OWLOntologyManager manager)
            throws OWLOntologyStorageException, LoadingException
    {
        try {
            OWLOntology ontology = manager.loadOntology(iri);
            saveOntology(outStream, ontology);
            return ontology;
        } catch (OWLOntologyCreationException e) {
            throw new LoadingException(e);
        }
    }

    public static void saveOntology(@Nullable OutputStream outStream,
                                    @NotNull OWLOntology ontology) throws OWLOntologyStorageException
    {
        if (outStream != null) {
            OWLDocumentFormat format = new OWLXMLDocumentFormat();
            ontology.saveOntology(format, outStream);
        }
    }

    public static OWLOntology extractModule(OWLOntology owlOntology, @NotNull IRI moduleIri,
                                            @NotNull Set<IRI> signature) throws OWLOntologyCreationException
    {
        OWLOntology module = owlOntology.getOWLOntologyManager().createOntology(moduleIri);
        owlOntology.axioms(Imports.INCLUDED)
                .filter(a -> a.signature().anyMatch(s -> signature.contains(s.getIRI())))
                .forEach(module::addAxiom);
        Set<IRI> moduleSig = module.signature().map(HasIRI::getIRI).collect(Collectors.toSet());
        owlOntology.axioms(AxiomType.ANNOTATION_ASSERTION, Imports.INCLUDED)
                .filter(a -> a.getProperty().getIRI().getRemainder().filter("label"::equalsIgnoreCase).isPresent() ||
                        a.getProperty().getIRI().getRemainder().filter("prefLabel"::equalsIgnoreCase).isPresent())
                .filter(a -> a.getSubject().isIRI())
                .filter(a -> a.getSubject().asIRI().isPresent() && moduleSig.contains(a.getSubject().asIRI().get()))
                .forEach(module::addAxiom);
        return module;
    }

    public static void removeFromMgr(OWLOntology owlOntology)
    {
        owlOntology.getOWLOntologyManager().removeOntology(owlOntology);
    }
}
