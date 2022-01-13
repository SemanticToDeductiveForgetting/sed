package pc1.pc2.pc3.app.ui.desktop.controls;

import pc1.pc2.pc3.om.ILiteral;

import javax.swing.*;
import java.awt.*;

class SignaturePanel extends JPanel
{

    SignaturePanel(ListModel<ILiteral> dataModel, ListSelectionModel selectionModel)
    {
        super();
        setLayout(new BorderLayout());
        build(dataModel, selectionModel);
    }

    private void build(ListModel<ILiteral> dataModel, ListSelectionModel selectionModel)
    {
        JList<ILiteral> list = new JList<>(dataModel);
        list.setSelectionModel(selectionModel);
        add(list);
        JScrollPane scollPane = new JScrollPane(list);
        add(scollPane, BorderLayout.CENTER);
    }
}
