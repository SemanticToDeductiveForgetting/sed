package pc1.pc2.pc3.app.ui.desktop;

import org.jetbrains.annotations.NotNull;
import pc1.pc2.pc3.app.ui.desktop.controls.*;
import pc1.pc2.pc3.app.AppEngine;
import pc1.pc2.pc3.app.SignatureModel;
import pc1.pc2.pc3.app.ui.desktop.controls.*;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;

public class MainFrame extends JFrame
{
    public static final String fontName = "FreeSans";
    private final Presenter presenter;

    public MainFrame(@NotNull String title, @NotNull Presenter presenter)
    {
        super(title);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.presenter = presenter;
        presenter.setWindow(this);
    }

    public void launch(AppEngine engine)
    {
        build(engine);
        setPreferredSize(new Dimension(1200, 700));
        pack();
        setVisible(true);
    }

    private void build(AppEngine engine)
    {
        File fontFile = new File(getClass().getResource("/fonts/FreeSans.ttf").getFile());
        try {
            Font[] fonts = Font.createFonts(fontFile);
            for (Font font : fonts) {
                GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(font);
            }
        } catch (FontFormatException | IOException e) {
            e.printStackTrace();
        }


        JPanel mainPanel = new JPanel(new BorderLayout());
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        split.setTopComponent(createOntologiesPanel(engine));
        split.setBottomComponent(createSouthPanel(engine));
        split.setDividerSize(20);

        mainPanel.add(split, BorderLayout.CENTER);
        getContentPane().add(mainPanel);
    }

    private JPanel createOntologiesPanel(AppEngine engine)
    {
        JPanel ontologiesPanel = new JPanel(new GridLayout(1, 3));
        ontologiesPanel.add(new OntologyEditorPanel("Background theory", engine.getBackgroundTheory(), presenter));
        ontologiesPanel.add(new OntologyEditorPanel("Ontology", engine.getOntology(), presenter));
        ontologiesPanel.add(new OntologyEditorPanel("Forgetting solution", engine.getInterpolant(), presenter));
        return ontologiesPanel;
    }

    private JPanel createSouthPanel(AppEngine engine)
    {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JPanel southPanel = new JPanel();
        BoxLayout layout = new BoxLayout(southPanel, BoxLayout.X_AXIS);
        southPanel.setLayout(layout);
        southPanel.add(createSignaturePanel(engine));
        southPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        southPanel.add(createExecutionPanel());
        southPanel.add(Box.createRigidArea(new Dimension(5, 0)));
        panel.add(southPanel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createSignaturePanel(AppEngine engine)
    {
        SignatureModel signature = engine.getSignature();
        AvailableSignatureListModel availableModel = new AvailableSignatureListModel(signature);
        signature.addAvailableSignatureListener(availableModel);
        SelectedSignatureListModel selectedModel = new SelectedSignatureListModel(signature);
        signature.addSelectedSignatureListener(selectedModel);
        return new SignatureSelectionPanel(presenter, availableModel, selectedModel);
    }

    private JButton createExecutionPanel()
    {
        return new ControlHelper(presenter)
                .createButton("/images/execute.png", Presenter::forget, new Dimension(70, 70));
    }
}
