package pc1.pc2.pc3.rest;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class SymbolData
{

    private final String id;
    private final String label;
    private HashMap<String, String> iriMap;

    public SymbolData(String symbol)
    {
        this(symbol, symbol);
    }

    public SymbolData(String id, String label)
    {
        this.id = id;
        this.label = label;
        iriMap = new HashMap<>();
    }

    public String getLabel()
    {
        return label;
    }

    public String getId()
    {
        return id;
    }

    @Override public String toString()
    {
        return String.format("%s \t %s", id, label);
    }

    public void setIRI(String ontologyName, String iri)
    {
        iriMap.put(ontologyName, iri);
    }

    public String getIRI(String ontology)
    {
        return iriMap.get(ontology);
    }

    public Map<String, String> getIRIMappings()
    {
        return iriMap;
    }

    public static SymbolData deserialize(JSONObject jconcept)
    {
        String id = jconcept.getString("id");
        String label = jconcept.getString("label");
        JSONArray jmappings = jconcept.getJSONArray("mappings");
        SymbolData symbolData = new SymbolData(id, label);
        for (int j = 0; j < jmappings.length(); j++) {
            JSONObject mapping = jmappings.getJSONObject(j);
            String ontology = mapping.getString("ontology");
            String iri = mapping.getString("iri");
            symbolData.setIRI(ontology, iri);
        }
        return symbolData;

    }

    @NotNull public static JSONObject serialize(SymbolData concept)
    {
        JSONObject jconcept = new JSONObject();
        jconcept.put("id", concept.getId());
        jconcept.put("label", concept.getLabel());
        JSONArray jmappings = new JSONArray();
        for (Map.Entry<String, String> entry : concept.getIRIMappings().entrySet()) {
            JSONObject mapping = new JSONObject();
            mapping.put("ontology", entry.getKey());
            mapping.put("iri", entry.getValue());
            jmappings.put(mapping);
        }
        jconcept.put("mappings", jmappings);
        return jconcept;
    }
}
