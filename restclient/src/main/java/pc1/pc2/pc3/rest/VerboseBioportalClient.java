package pc1.pc2.pc3.rest;

import pc1.pc2.pc3.input.owl5.LoadingException;

import javax.ws.rs.client.WebTarget;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class VerboseBioportalClient extends BioportalClient
{
    private final Writer out;

    public VerboseBioportalClient(int forgettingSignatureSize, Writer out)
    {
        super(forgettingSignatureSize);
        this.out = out;
    }

    @Override protected List<SymbolData> getRandomConcepts(File classes) throws LoadingException
    {
        writeLine(String.format("Loading concepts from %s", classes));
        List<SymbolData> randomConcepts = super.getRandomConcepts(classes);
        String concepts = randomConcepts.stream()
                .map(SymbolData::getLabel)
                .collect(Collectors.joining(", "));
        writeLine(String.format("Forgetting signature: {%s}", concepts));
        return randomConcepts;
    }

    @Override protected List<OntologyData> getRelatedOntologies(List<SymbolData> concepts,
                                                                Set<String> ontologyBlackList,
                                                                Collection<String> ontologySet)
    {
        List<OntologyData> relatedOntologies = super.getRelatedOntologies(concepts, ontologyBlackList, ontologySet);
        String ontologies = relatedOntologies.stream().map(OntologyData::getName).collect(Collectors.joining(", "));
        writeLine(String.format("Ontologies: %s", ontologies));
        writeLine("Signature is found across the previous ontologies with IRI:");
        for (SymbolData concept : concepts) {
            writeLine(String.format("-%s", concept.getLabel()));
            relatedOntologies.stream()
                    .filter(o -> concept.getIRI(o.getName()) != null)
                    .forEach(o -> writeLine(String.format("--%s : %s", o.getName(), concept.getIRI(o.getName()))));
        }
        return relatedOntologies;
    }

    @Override protected String executeRequest(WebTarget webTarget)
    {
        writeLine(String.format("Executing online request %s", webTarget.toString()));
        return super.executeRequest(webTarget);
    }

    @Override protected void findInRepository(List<OntologyData> ontologies, String repository) throws LoadingException
    {
        super.findInRepository(ontologies, repository);
        String local = ontologies.stream()
                .filter(o -> o.getLocalIRI() != null)
                .map(OntologyData::getName)
                .collect(Collectors.joining(", "));
        writeLine(String.format("Found the following ontologies locally: {%s}", local));
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
