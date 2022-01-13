package pc1.pc2.pc3.rest;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
import org.semanticweb.owlapi.model.IRI;
import pc1.pc2.pc3.om.IAxiom;

import java.util.List;

public class OntologyData
{
    private final String name;
    private final IRI onlineIRI;
    private IRI localIRI;
    private List<IAxiom> axioms;

    public OntologyData(String name, IRI onlineIRI)
    {
        this.name = name;
        this.onlineIRI = onlineIRI;
    }

    public String getName()
    {
        return name;
    }

    public IRI getOnlineIRI()
    {
        return onlineIRI;
    }

    public void setLocalIRI(IRI localIRI)
    {
        this.localIRI = localIRI;
    }

    public IRI getLocalIRI()
    {
        return localIRI;
    }

    public IRI getIRI()
    {
        if(localIRI != null) {
            return localIRI;
        }
        return onlineIRI;
    }

    public List<IAxiom> getAxioms()
    {
        return axioms;
    }

    public void setAxioms(List<IAxiom> axioms)
    {
        this.axioms = axioms;
    }


    @NotNull public static JSONObject serialize(OntologyData ontology)
    {
        JSONObject jontology = new JSONObject();
        jontology.put("name", ontology.getName());
        if (ontology.getLocalIRI() != null) {
            jontology.put("local_iri", ontology.getLocalIRI().getIRIString());
        }
        jontology.put("online_iri", ontology.getOnlineIRI().getIRIString());
        return jontology;
    }

    @NotNull public static OntologyData deserialize(String string)
    {
        JSONObject jontology = new JSONObject(string);
        String name = jontology.getString("name");
        String online_iri = jontology.getString("online_iri");
        OntologyData ontology = new OntologyData(name, IRI.create(online_iri));
        if(jontology.has("local_iri")) {
            String local_iri = jontology.getString("local_iri");
            ontology.setLocalIRI(IRI.create(local_iri));
        }
        return ontology;
    }
}
