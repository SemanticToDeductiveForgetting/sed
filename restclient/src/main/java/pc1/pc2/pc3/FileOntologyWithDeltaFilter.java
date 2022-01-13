package pc1.pc2.pc3;

import pc1.pc2.pc3.logic.factory.DefinerFactory;
import pc1.pc2.pc3.logic.resolver.DeltaAxiomPredicate;
import pc1.pc2.pc3.om.IAxiom;
import pc1.pc2.pc3.om.IConceptLiteral;

import java.nio.file.OpenOption;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FileOntologyWithDeltaFilter extends FileOntology
{
    private final String deltaFile;

    public FileOntologyWithDeltaFilter(String ontologyFile,
                                       List<IConceptLiteral> forgettingSignature,
                                       DefinerFactory definerFactory)
    {
        super(ontologyFile, forgettingSignature, definerFactory, new BufferedAxiomExtractor());
        this.deltaFile = ontologyFile + ".delta";
    }

    @Override
    protected void writeAxioms(Collection<IAxiom> axioms, OpenOption[] fileOptions)
    {
        Collection<IAxiom> delta = extractDeltaAxioms(axioms);
        addToFile(delta, deltaFile, APPEND_FILE_OPTIONS);
        super.writeAxioms(axioms, fileOptions);
    }

    private Collection<IAxiom> extractDeltaAxioms(Collection<IAxiom> axioms)
    {
        DeltaAxiomPredicate predicate = new DeltaAxiomPredicate(definerFactory);
        Set<IAxiom> delta = new HashSet<>();
        for (IAxiom axiom : axioms) {
            if (predicate.test(axiom)) {
                delta.add(axiom);
            }
        }

        axioms.removeAll(delta);
        return delta;
    }
}
