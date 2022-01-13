package pc1.pc2.pc3.app.ui.desktop.controls;

import pc1.pc2.pc3.app.ui.desktop.Presenter;
import pc1.pc2.pc3.om.IAxiom;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

class OntologyDisplayPanel extends JPanel
{
    private OntologyDisplayListModel dataModel;
    private final OntologySelectionModel selectionModel;
    private final Presenter presenter;

    OntologyDisplayPanel(OntologyDisplayListModel dataModel,
                         OntologySelectionModel selectionModel, Presenter presenter)
    {
        this.dataModel = dataModel;
        this.selectionModel = selectionModel;
        this.presenter = presenter;
        setLayout(new BorderLayout());
        build();
    }

    private void build()
    {
        JList<IAxiom> list = new JList<>(dataModel);
        list.setSelectionModel(selectionModel);
        list.addKeyListener(new DeleteKeyPressed());
        JScrollPane scollPane = new JScrollPane(list);
        add(scollPane, BorderLayout.CENTER);
    }

    private class DeleteKeyPressed implements KeyListener
    {

        @Override public void keyTyped(KeyEvent e)
        {

        }

        @Override public void keyPressed(KeyEvent e)
        {

        }

        @Override public void keyReleased(KeyEvent e)
        {
            if(e.getKeyCode() == KeyEvent.VK_DELETE)
            {
                presenter.removeAxiom(dataModel, selectionModel);
            }
        }
    }
}
