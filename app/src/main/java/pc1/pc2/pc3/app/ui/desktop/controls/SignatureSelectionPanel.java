package pc1.pc2.pc3.app.ui.desktop.controls;

import pc1.pc2.pc3.app.ui.desktop.Presenter;

import javax.swing.*;
import java.awt.*;

public class SignatureSelectionPanel extends JPanel
{
    private final Presenter presenter;
    private final AvailableSignatureListModel availableDataModel;
    private final SelectedSignatureListModel selectedDataModel;
    private final SignatureSelectionModel availableSelectionModel;
    private final SignatureSelectionModel selectedSelectionModel;

    public SignatureSelectionPanel(Presenter presenter, AvailableSignatureListModel availableDataModel,
                                   SelectedSignatureListModel selectedDataModel)
    {
        this.presenter = presenter;
        this.availableDataModel = availableDataModel;
        this.selectedDataModel = selectedDataModel;
        availableSelectionModel = createSelectionModel();
        selectedSelectionModel = createSelectionModel();
        BoxLayout layout = new BoxLayout(this, BoxLayout.X_AXIS);
        setLayout(layout);
        build();
    }

    private SignatureSelectionModel createSelectionModel()
    {
        SignatureSelectionModel model = new SignatureSelectionModel();
        model.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        return model;
    }

    private void build()
    {
        add(new SignaturePanel(availableDataModel, availableSelectionModel));
        add(Box.createRigidArea(new Dimension(15, 0)));
        add(createControlPanel());
        add(Box.createRigidArea(new Dimension(15, 0)));
        add(new SignaturePanel(selectedDataModel, selectedSelectionModel));
    }

    private Component createControlPanel()
    {
        JPanel panel = new JPanel();
        BoxLayout layout = new BoxLayout(panel, BoxLayout.Y_AXIS);
        panel.setLayout(layout);
        panel.add(Box.createVerticalGlue());
        panel.add(new ControlHelper(presenter)
                .createButton("/images/right_arrow.png",
                        p -> p.selectLiterals(availableDataModel, availableSelectionModel),
                        new Dimension(24, 24)));
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(new ControlHelper(presenter)
                .createButton("/images/left_arrow.png",
                        p -> p.deselectLiterals(selectedDataModel, selectedSelectionModel),
                        new Dimension(24, 24)));
        panel.add(Box.createVerticalGlue());
        return panel;
    }
}
