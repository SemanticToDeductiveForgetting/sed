package pc1.pc2.pc3.app.ui.desktop;

import org.jetbrains.annotations.NotNull;
import pc1.pc2.pc3.app.ui.desktop.controls.*;
import pc1.pc2.pc3.app.utils.FileLoader;
import pc1.pc2.pc3.app.AppEngine;
import pc1.pc2.pc3.app.ui.desktop.controls.*;
import pc1.pc2.pc3.om.ILiteral;
import pc1.pc2.pc3.utils.FileUtils;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class Presenter
{

    private final AppEngine engine;
    private final TextInputPopup inputPopup;
    private MainFrame mainFrame;

    public Presenter(AppEngine engine)
    {
        this.engine = engine;
        inputPopup = new TextInputPopup();
    }

    public void loadFile(OntologyDisplayListModel listModel)
    {
        File file = new FileLoader().loadFile();
        if (file != null) {
            try {
                List<String> lines = FileUtils.loadFile(file);
                listModel.addAxioms(lines);
            } catch (IOException e1) {
                showError("Error reading or closing file " + file, e1.getMessage());
            }
        }
    }

    private void showError(String errorTitle, String errorMessage)
    {
        JOptionPane.showMessageDialog(null,
                errorMessage,
                errorTitle,
                JOptionPane.ERROR_MESSAGE);
    }

    public void removeAxiom(OntologyDisplayListModel listModel,
                            OntologySelectionModel selectionModel)
    {
        int[] selectedIndices = selectionModel.getSelectedIndices();
        selectionModel.clearSelection();
        listModel.removeAxioms(Arrays.copyOf(selectedIndices, selectedIndices.length));
    }

    public void addAxiom(OntologyDisplayListModel listModel)
    {
        inputPopup.takeInput(mainFrame, listModel);
    }

    void forget()
    {
        mainFrame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        engine.forget();
        mainFrame.setCursor(Cursor.getDefaultCursor());
    }

    void setWindow(MainFrame mainFrame)
    {
        this.mainFrame = mainFrame;
    }

    public void selectLiterals(AvailableSignatureListModel dataModel,
                               SignatureSelectionModel selectionModel)
    {
        List<ILiteral> selectedLiterals = getSelectedLiterals(dataModel, selectionModel);
        engine.getSignature().selectLiterals(selectedLiterals);
    }

    public void deselectLiterals(SelectedSignatureListModel dataModel,
                                 SignatureSelectionModel selectionModel)
    {
        List<ILiteral> selectedLiterals = getSelectedLiterals(dataModel, selectionModel);
        engine.getSignature().deSelectLiterals(selectedLiterals);
    }

    @NotNull private List<ILiteral> getSelectedLiterals(ListModel<ILiteral> dataModel,
                                                        SignatureSelectionModel selectionModel)
    {
        int[] indices = selectionModel.getSelectedIndices();
        selectionModel.clearSelection();
        List<ILiteral> selectedLiterals = new LinkedList<>();
        for (int index : indices) {
            selectedLiterals.add(dataModel.getElementAt(index));
        }
        return selectedLiterals;
    }
}
