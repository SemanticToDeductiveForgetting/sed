package pc1.pc2.pc3.app;

import pc1.pc2.pc3.om.IAxiom;

import java.util.List;

public interface IOntologyModelListener
{
    void axiomAdded(IAxiom axiom);
    void axiomRemoved(IAxiom axiom);

    void ontologyCleared(List<IAxiom> axioms);
    void axiomsAdded(List<IAxiom> axioms);
}
