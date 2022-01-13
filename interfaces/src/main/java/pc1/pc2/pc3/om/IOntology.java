package pc1.pc2.pc3.om;

import java.util.Collection;

public interface IOntology
{
    void addAxiom(IAxiom axiom);

    void addAxioms(Collection<IAxiom> axioms);

    Collection<IAxiom> getAllActiveAxioms();

    Collection<IAxiom> getAllFinalAxioms();

    void setContent(Collection<IAxiom> axioms);

    Collection<IAxiom> extractAxiomsOf(Collection<IConceptLiteral> symbols);

    void begin();

    void end();
}
