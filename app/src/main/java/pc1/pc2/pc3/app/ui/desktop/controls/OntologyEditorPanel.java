package pc1.pc2.pc3.app.ui.desktop.controls;

import pc1.pc2.pc3.app.OntologyModel;
import pc1.pc2.pc3.app.ui.desktop.MainFrame;
import pc1.pc2.pc3.app.ui.desktop.Presenter;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

public class OntologyEditorPanel extends JPanel
{
    private String title;
    private final OntologyDisplayListModel listModel;
    private final Presenter presenter;
    private final OntologySelectionModel selectionModel;

    public OntologyEditorPanel(String title, OntologyModel model, Presenter presenter)
    {
        this.title = title;
        listModel = new OntologyDisplayListModel(model);
        model.addListener(listModel);
        this.presenter = presenter;
        selectionModel = new OntologySelectionModel();
        selectionModel.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        build();
    }

    private void build()
    {
        setLayout(new BorderLayout());
        add(createTitlePanel(), BorderLayout.NORTH);
        add(createOntologyPanel(), BorderLayout.CENTER);
        if (!listModel.isReadOnly()) {
            add(createControlsPanel(), BorderLayout.SOUTH);
        }
    }

    private JPanel createOntologyPanel()
    {
        return new OntologyDisplayPanel(listModel, selectionModel, presenter);
    }

    private JPanel createControlsPanel()
    {
        JPanel controlsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        controlsPanel.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);


        JButton add = new ControlHelper(presenter)
                .createButton("/images/add.png",
                        p -> p.addAxiom(listModel),
                        new Dimension(24, 24));
        JButton remove = new ControlHelper(presenter)
                .createButton("/images/remove.png",
                        p -> p.removeAxiom(listModel, selectionModel),
                        new Dimension(24, 24));
        JButton addFile = new ControlHelper(presenter)
                .createButton("/images/add_file.png",
                        p -> p.loadFile(listModel),
                        new Dimension(24, 24));
        controlsPanel.add(add);
        controlsPanel.add(remove);
        controlsPanel.add(addFile);

        return controlsPanel;
    }

    private JPanel createTitlePanel()
    {
        JPanel titlePanel = new JPanel(new BorderLayout());
        Border emptyBorder = BorderFactory.createEmptyBorder();
        JLabel text = new JLabel(title);
        text.setFont(new Font(MainFrame.fontName, Font.PLAIN, 12));
        text.setBorder(emptyBorder);
        titlePanel.add(text, BorderLayout.WEST);
        return titlePanel;
    }
}
