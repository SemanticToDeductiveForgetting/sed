package pc1.pc2.pc3.app.ui.desktop.controls;

import pc1.pc2.pc3.app.IOntologyModelListener;
import pc1.pc2.pc3.app.OntologyModel;
import pc1.pc2.pc3.om.IAxiom;

import javax.swing.*;
import java.util.Arrays;
import java.util.List;

public class OntologyDisplayListModel extends AbstractListModel<IAxiom> implements IOntologyModelListener
{
    private final OntologyModel ontologyModel;

    OntologyDisplayListModel(OntologyModel model)
    {
        ontologyModel = model;
    }

    @Override public int getSize()
    {
        return ontologyModel.getSize();
    }

    @Override public IAxiom getElementAt(int index)
    {
        return ontologyModel.getAxiomAt(index);
    }

    boolean addAxiom(String axiom)
    {
        int idx = getSize();
        if (ontologyModel.addAxiom(axiom.trim())) {
            fireIntervalAdded(this, idx, idx);
            return true;
        }

        return false;
    }

    public void removeAxioms(int[] selectedIndices)
    {
        if (selectedIndices.length > 0) {
            Arrays.sort(selectedIndices);
            for (int i = selectedIndices.length - 1; i >= 0; i--) {
                ontologyModel.removeAt(selectedIndices[i]);
            }
            fireIntervalRemoved(this, selectedIndices[0], selectedIndices[selectedIndices.length - 1]);
        }
    }

    public boolean addAxioms(List<String> axioms)
    {
        int start = getSize();
        int end = start - 1;
        for (String axiom : axioms) {
            if (ontologyModel.addAxiom(axiom.trim())) {
                end++;
            }
        }
        if (end >= start) {
            fireIntervalAdded(this, start, end);
            return true;
        }
        return false;
    }

    boolean isReadOnly()
    {
        return ontologyModel.isReadOnly();
    }


    @Override public void axiomAdded(IAxiom axiom)
    {

    }

    @Override public void axiomRemoved(IAxiom axiom)
    {

    }

    @Override public void ontologyCleared(List<IAxiom> axioms)
    {
        if (!axioms.isEmpty()) {
            fireIntervalRemoved(this, 0, axioms.size() - 1);
        }
    }

    @Override public void axiomsAdded(List<IAxiom> axioms)
    {
        if (!axioms.isEmpty()) {
            fireIntervalAdded(this, 0, axioms.size() - 1);
        }
    }
}
