package pc1.pc2.pc3.om;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class Ontology implements IOntology
{
    Collection<IAxiom> axioms = new HashSet<>();

    @Override
    public void addAxiom(IAxiom axiom)
    {
        axioms.add(axiom);
    }

    @Override
    public void addAxioms(Collection<IAxiom> axioms)
    {
        this.axioms.addAll(axioms);
    }

    @Override
    public Collection<IAxiom> getAllActiveAxioms()
    {
        return new HashSet<>(axioms);
    }

    @Override
    public Collection<IAxiom> getAllFinalAxioms()
    {
        return new HashSet<>();
    }

    @Override
    public void setContent(Collection<IAxiom> axioms)
    {
        this.axioms = new HashSet<>(axioms);
    }

    @Override
    public Collection<IAxiom> extractAxiomsOf(Collection<IConceptLiteral> symbols)
    {
        Set<IAxiom> match = new HashSet<>();
        for (IAxiom axiom : axioms) {
            Collection<ILiteral> sig = axiom.getSignature();
            sig.retainAll(symbols);
            if (!sig.isEmpty()) {
                match.add(axiom);
            }
        }
        axioms.removeAll(match);
        return match;
    }

    @Override
    public void begin()
    {

    }

    @Override
    public void end()
    {

    }
}
