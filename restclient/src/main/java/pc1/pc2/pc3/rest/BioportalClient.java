package pc1.pc2.pc3.rest;


import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.semanticweb.owlapi.model.IRI;
import pc1.pc2.pc3.OntologyMetric;
import pc1.pc2.pc3.input.owl5.LoadingException;
import pc1.pc2.pc3.utils.FileUtils;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class BioportalClient
{

    private static final String url = "http://data.bioontology.org";
    private static final String DOWNLOAD_KEY = "download";
    private static final String API_KEY = "3b81d259-dde9-4ce4-b786-628d92c2999c";
    private final int numberOfClasses;
    private List<SymbolData> concepts;
    private List<OntologyData> ontologies;

    public BioportalClient(int forgettingSignatureSize)
    {
        numberOfClasses = forgettingSignatureSize;
    }

    public void loadData(String repository, File classes) throws LoadingException
    {
        File blacklist = Objects.requireNonNull(new File(repository).listFiles((f, name) -> "blacklist"
                .equalsIgnoreCase(name)))[0];
        Set<String> ontologyBlackList;
        try {
            ontologyBlackList =
                    FileUtils.loadFile(blacklist).stream().map(String::toUpperCase).collect(Collectors.toSet());
        } catch (IOException e) {
            ontologyBlackList = new HashSet<>();
        }
        Collection<String> localOntologies =
                new LinkedList<>(getLocalBioportalOntologies(repository));
        while (ontologies == null || ontologies.isEmpty()) {
            concepts = getRandomConcepts(classes);
            ontologies = getRelatedOntologies(concepts, ontologyBlackList, localOntologies);
        }

        findInRepository(ontologies, repository);
    }

    @NotNull
    private Collection<String> getLocalBioportalOntologies(@NotNull String repository)
    {
        String[] filesInRepository = new File(repository)
                .list((f, n) -> !"blacklist".equalsIgnoreCase(n)
                        && !"classes.txt".equalsIgnoreCase(n)
                        && !"metrics.csv".equalsIgnoreCase(n)
                        && !"metrics_backup.csv".equalsIgnoreCase(n)
                        && !".DS_Store".equalsIgnoreCase(n));
        if (filesInRepository != null) {
            return Arrays.asList(filesInRepository);
        }
        return Collections.emptyList();
    }

    protected List<SymbolData> getRandomConcepts(File classes) throws LoadingException
    {
        try {
            List<String> lines = FileUtils.loadFile(classes);
            Random random = new Random();
            List<SymbolData> concepts = new LinkedList<>();
            random.ints(numberOfClasses, 3, 126762).forEach(i -> concepts.add(new SymbolData(lines.get(i))));
            return concepts;
        } catch (IOException e) {
            throw new LoadingException(e);
        }
    }

    protected void findInRepository(List<OntologyData> ontologies, String repository) throws LoadingException
    {
        for (OntologyData ontologyData : ontologies) {
            try {
                File file = new File(repository);
                File canonicalFile = file.getCanonicalFile();
                File ontologyFile = new File(canonicalFile.getPath() + File.separator + ontologyData.getName());
                if (ontologyFile.exists()) {
                    IRI localIri = IRI.create(ontologyFile);
                    ontologyData.setLocalIRI(localIri);
                }
            } catch (IOException e) {
                throw new LoadingException(e);
            }
        }
    }

    protected List<OntologyData> getRelatedOntologies(List<SymbolData> concepts,
                                                      Set<String> ontologyBlackList,
                                                      Collection<String> ontologySet)
    {
        List<OntologyData> ontologyData = new LinkedList<>();
        String input = concepts.stream()
                .map(SymbolData::getLabel)
                .collect(Collectors.joining(","));
        Client client = ClientBuilder.newClient();
        WebTarget webTarget = client.target(url)
                .path("/recommender")
                .queryParam("input_type", 2)
                .queryParam("input", input)
                .queryParam("apikey", API_KEY);
        ontologySet.removeAll(ontologyBlackList);
        if (!ontologySet.isEmpty()) {
            webTarget = webTarget.queryParam("ontologies", String.join(",", ontologySet));
        }
        String document = executeRequest(webTarget);
        try {
            JSONArray topArray = new JSONArray(document);
            for (int i = 0; i < topArray.length(); i++) {
                JSONObject scoreEntry = topArray.getJSONObject(i);
                readOntologyData(ontologyData, scoreEntry, ontologyBlackList);
                readSymbolData(concepts, scoreEntry, ontologyBlackList);
            }
        } catch (JSONException e) {
            System.out.println("Error parsing JSON string");
            System.out.println(e.getMessage());
        }
        concepts.removeIf(s -> s.getIRIMappings().size() <= 1);
        ontologyData.removeIf(o -> concepts.stream().noneMatch(s -> s.getIRI(o.getName()) != null));
        ontologyData.removeIf(o -> !ontologySet.contains(o.getName()));
        return ontologyData;
    }

    public OntologyMetric getMetrics(String ontologyName)
    {
        Client client = ClientBuilder.newClient();
        WebTarget webTarget = client.target(url)
                .path(String.format("/ontologies/%s/metrics", ontologyName))
                .queryParam("apikey", API_KEY);
        String document = executeRequest(webTarget);
        try {
            JSONObject jsonObject = new JSONObject(document);
            OntologyMetric metric = new OntologyMetric();
            if (jsonObject.has("classes")) {
                metric.setConcepts(jsonObject.getInt("classes"));
            }
            if (jsonObject.has("properties")) {
                metric.setRoles(jsonObject.getInt("properties"));
            }
            if (jsonObject.has("maxDepth")) {
                metric.setMaxDepth(jsonObject.getInt("maxDepth"));
            }
            return metric;
        } catch (JSONException e) {
            System.out.println("Error parsing JSON string");
            System.out.println(e.getMessage());
        }

        return null;
    }

    private void readSymbolData(List<SymbolData> concepts, JSONObject scoreEntry,
                                Set<String> ontologyBlackList)
    {
        JSONObject coverageResult = scoreEntry.getJSONObject("coverageResult");
        JSONArray annotations = coverageResult.getJSONArray("annotations");
        for (int i = 0; i < annotations.length(); i++) {
            JSONObject annotation = annotations.getJSONObject(i);
            String matchType = annotation.getString("matchType");
            if ("PREF".equalsIgnoreCase(matchType)) {
                String symbol = annotation.getString("text");
                JSONObject annotatedClass = annotation.getJSONObject("annotatedClass");
                String iri = annotatedClass.getString("@id");
                JSONObject links = annotatedClass.getJSONObject("links");
                String ontology = links.getString("ontology");
                String ontologyName = ontology.substring(ontology.lastIndexOf("/") + 1);
                if (!ontologyBlackList.contains(ontologyName.toUpperCase())) {
                    concepts.stream().filter(s -> s.getLabel().equalsIgnoreCase(symbol))
                            .forEach(s -> s.setIRI(ontologyName, iri));
                }
            }
        }
    }

    private void readOntologyData(List<OntologyData> ontologyData, JSONObject scoreEntry,
                                  Set<String> ontologyBlackList)
    {
        JSONArray ontologies = scoreEntry.getJSONArray("ontologies");
        for (int j = 0; j < ontologies.length(); j++) {
            JSONObject ontology = ontologies.getJSONObject(j);
            String name = ontology.getString("acronym");
            if (!ontologyBlackList.contains(name.toUpperCase())) {
                JSONObject links = ontology.getJSONObject("links");
                if (links.has(DOWNLOAD_KEY)) {
                    String downloadLink = links.getString(DOWNLOAD_KEY);
                    IRI iri = IRI.create(String.format("%s?apikey=%s", downloadLink, API_KEY));
                    ontologyData.add(new OntologyData(name, iri));
                }
            }
        }
    }

    protected String executeRequest(WebTarget webTarget)
    {
        Invocation.Builder invocationBuilder = webTarget.request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, " apikey token=" + API_KEY);
        Response response = invocationBuilder.get();
        return response.readEntity(String.class);
    }

    public List<OntologyData> getOntologies()
    {
        return ontologies;
    }

    public List<SymbolData> getConcepts()
    {
        return concepts;
    }
}
