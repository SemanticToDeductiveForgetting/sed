package pc1.pc2.pc3.input.owl5;

import org.jetbrains.annotations.Nullable;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntology;

import java.util.HashMap;
import java.util.Map;

public class IriMapper
{
    private final Map<IRI, OWLEntity> iriMap;
    private final OWLOntology ontology;

    public IriMapper(OWLOntology ontology)
    {
        this.ontology = ontology;
        this.iriMap = new HashMap<>();
        initialize();
    }

    private void initialize()
    {
        ontology.signature().forEach(this::mapEntity);
    }

    private void mapEntity(OWLEntity owlEntity)
    {
        iriMap.put(owlEntity.getIRI(), owlEntity);
    }


    @Nullable
    public OWLEntity getEntity(IRI iri)
    {
        return iriMap.get(iri);
    }
}
