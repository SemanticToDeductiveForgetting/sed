package pc1.pc2.pc3;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;

public class SymbolData
{

    private final String id;
    private final String label;
    private final HashMap<String, String> iriMap;

    public SymbolData(String id, String label)
    {
        this.id = id;
        this.label = label;
        iriMap = new HashMap<>();
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

    public String getLabel()
    {
        return label;
    }

    @Override
    public String toString()
    {
        return String.format("%s \t %s", id, label);
    }

    public void setIRI(String ontologyName, String iri)
    {
        iriMap.put(ontologyName, iri);
    }
}