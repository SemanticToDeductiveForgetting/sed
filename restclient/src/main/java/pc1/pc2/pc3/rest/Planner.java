package pc1.pc2.pc3.rest;

import org.semanticweb.owlapi.model.IRI;
import pc1.pc2.pc3.ExistingCoverageCalculator;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class Planner
{
    public Planner()
    {

    }

    public OntologyData planOntology(File ontFile)
    {
        IRI iri = IRI.create(ontFile);
        OntologyData ont = new OntologyData(ontFile.getName(), iri);
        ont.setLocalIRI(iri);

        return ont;
    }

    public List<SymbolData> planSignature(Set<ExistingCoverageCalculator.Symbol> symbols, int requiredCoverageRate,
                                          String ontologyName)
    {
        List<ExistingCoverageCalculator.Symbol> forgettingSymbols = getForgettingSymbols(symbols, requiredCoverageRate);
        return forgettingSymbols.stream()
                .map(s -> toSymbolData(s, ontologyName))
                .collect(Collectors.toList());
    }

    private SymbolData toSymbolData(ExistingCoverageCalculator.Symbol symbol, String ontologyName)
    {
        SymbolData data = new SymbolData(symbol.getLabel());
        data.setIRI(ontologyName, symbol.getIri());
        return data;
    }

    private List<ExistingCoverageCalculator.Symbol> getForgettingSymbols(Set<ExistingCoverageCalculator.Symbol> symbols,
                                                                         int requiredCoverageRate)
    {
        List<ExistingCoverageCalculator.Symbol> sortedSymbols = new ArrayList<>(symbols);
        sortedSymbols.sort(Comparator.comparing(ExistingCoverageCalculator.Symbol::getCoveredAxioms).reversed());

        List<ExistingCoverageCalculator.Symbol> extractedSymbols = new LinkedList<>();
        double accumulatedCoverage = 0;
        for (ExistingCoverageCalculator.Symbol symbol : sortedSymbols) {
            if(!symbol.getLabel().equalsIgnoreCase("owl#Thing")) {
                accumulatedCoverage += symbol.getCoveredAxiomsPercentage() * 100;
                extractedSymbols.add(symbol);
                if (accumulatedCoverage >= requiredCoverageRate) {
                    break;
                }
            }
        }

        return extractedSymbols;
    }
}
