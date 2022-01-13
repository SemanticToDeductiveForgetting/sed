package pc1.pc2.pc3.app.ui.desktop.controls;

import pc1.pc2.pc3.app.ISignatureModelListener;
import pc1.pc2.pc3.app.SignatureModel;
import pc1.pc2.pc3.om.ILiteral;

import javax.swing.*;

public class AvailableSignatureListModel extends AbstractListModel<ILiteral> implements ISignatureModelListener
{
    private final SignatureModel model;

    public AvailableSignatureListModel(SignatureModel model)
    {
        this.model = model;
    }

    @Override public int getSize()
    {
        return model.getAvailableLiterals().size();
    }

    @Override public ILiteral getElementAt(int index)
    {
        return model.getAvailableLiterals().get(index);
    }

    @Override public void literalsAdded(int from, int to)
    {
        fireIntervalAdded(this, from, to);
    }

    @Override public void literalsRemoved(int from, int to)
    {
        fireIntervalRemoved(this, from, to);
    }
}
