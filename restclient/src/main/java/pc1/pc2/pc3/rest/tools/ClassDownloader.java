package pc1.pc2.pc3.rest.tools;

import org.json.JSONArray;
import org.json.JSONObject;
import pc1.pc2.pc3.rest.SymbolData;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.*;
import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.List;

public class ClassDownloader
{
    private static final String BIOPORTAL = "http://data.bioontology.org";
    public static final String AUTHORIZATION_KEY = "3b81d259-dde9-4ce4-b786-628d92c2999c";

    private final String ontology;
    private final int searchPages;

    public ClassDownloader(String ontology, int searchPages)
    {
        this.ontology = ontology;
        this.searchPages = searchPages;
    }

    public void downloadAll(File outputFile) throws IOException
    {
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target(BIOPORTAL).path(String.format("/ontologies/%s/classes", ontology));
        Writer writer = new FileWriter(outputFile, Charset.forName("UTF-8"));
        for (int page = 1; page <= searchPages; page++) {
            WebTarget pageTarget = target.queryParam("page", page);
            String pageData = executeRequest(pageTarget);
            List<SymbolData> symbols = parseClasses(pageData);
            saveSymbols(symbols, writer);
        }
        writer.close();
    }

    private void saveSymbols(List<SymbolData> symbols, Writer writer) throws IOException
    {
        for (SymbolData symbol : symbols) {
            writer.append(String.format("%s\n", symbol.getLabel()));
        }
    }

    private List<SymbolData> parseClasses(String pageData)
    {
        List<SymbolData> symbols = new LinkedList<>();
        JSONObject json = new JSONObject(pageData);
        JSONArray classes = json.getJSONArray("collection");
        for (int i = 0; i < classes.length(); i++) {
            SymbolData symbolData = parseClass(classes.getJSONObject(i));
            if (symbolData != null) {
                symbols.add(symbolData);
            }
        }
        return symbols;
    }

    private SymbolData parseClass(JSONObject clazz)
    {
        SymbolData concept;
        boolean obsolete = clazz.getBoolean("obsolete");
        if (!obsolete) {
            String preferredLabel = clazz.getString("prefLabel");
            String id = clazz.getString("@id");
            concept = new SymbolData(id, preferredLabel);
            return concept;
        }
        return null;
    }

    private String executeRequest(WebTarget webTarget)
    {
        Invocation.Builder invocationBuilder = webTarget.request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, String.format("apikey token=%s", AUTHORIZATION_KEY));
        Response response = invocationBuilder.get();
        return response.readEntity(String.class);
    }

    public static void main(String[] args) throws IOException
    {
        ClassDownloader downloader = new ClassDownloader("IOBC", 2537);
        File folder = new File("./restclient/ontologies");
        File canonicalFile = folder.getCanonicalFile();
        File file = new File(canonicalFile.getPath() + File.separator + "classes.txt");
        downloader.downloadAll(file);
    }
}
