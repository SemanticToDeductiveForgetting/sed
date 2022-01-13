package pc1.pc2.pc3.app;

import pc1.pc2.pc3.error.ParseException;
import pc1.pc2.pc3.input.AxiomBuilder;
import pc1.pc2.pc3.om.IAxiom;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class OntologyModel
{
    private final boolean readOnly;
    private List<IAxiom> axioms = new ArrayList<>();
    private List<IOntologyModelListener> listeners;

    OntologyModel(boolean readOnly)
    {
        this.readOnly = readOnly;
        listeners = new LinkedList<>();
    }

    public int getSize()
    {
        return axioms.size();
    }

    public IAxiom getAxiomAt(int index)
    {
        return axioms.get(index);
    }

    public boolean addAxiom(String axiom)
    {
        if (!readOnly) {
            try {
                IAxiom clause = AxiomBuilder.fromString(axiom);
                axioms.add(clause);
                listeners.forEach(l -> l.axiomAdded(clause));
                return true;
            } catch (ParseException e) {
                return false;
            }
        }
        return false;
    }

    List<IAxiom> getAxioms()
    {
        return axioms;
    }

    public void removeAt(int index)
    {
        if (index < axioms.size() && index >= 0) {
            IAxiom axiom = axioms.remove(index);
            listeners.forEach(l -> l.axiomRemoved(axiom));
        }
    }

    public void addListener(IOntologyModelListener listener)
    {
        listeners.add(listener);
    }

    void clear()
    {
        ArrayList<IAxiom> temp = new ArrayList<>(axioms);
        axioms.clear();
        listeners.forEach(listener -> listener.ontologyCleared(temp));
    }

    void setAxioms(List<IAxiom> axioms)
    {
        this.axioms = axioms;
        listeners.forEach(listener -> listener.axiomsAdded(axioms));
    }



    public boolean isReadOnly()
    {
        return readOnly;
    }
}
