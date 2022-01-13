package pc1.pc2.pc3.app.ui.desktop.controls;

import pc1.pc2.pc3.app.ui.desktop.MainFrame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;

public class TextInputPopup extends JDialog
{
    private JTextField textField;
    private OntologyDisplayListModel model;

    public TextInputPopup()
    {
        build();
    }

    private void build()
    {
        TextInputPopup popup = this;
        popup.setLayout(new BorderLayout());
        popup.setUndecorated(true);
        popup.setMinimumSize(new Dimension(300, 50));
        textField = new JTextField();
        textField.setBackground(new Color(203, 227, 255));
        textField.setFont(new Font(MainFrame.fontName, Font.PLAIN, 32));
        textField.setHorizontalAlignment(JTextField.CENTER);
        textField.setBorder(BorderFactory.createEmptyBorder());
        textField.setPreferredSize(new Dimension(300, 50));
        Action acceptAction = new AcceptAction(popup);
        Action hideAction = new HideAction(popup);
        ActionMap actionMap = textField.getActionMap();
        InputMap inputMap = textField.getInputMap(JComponent.WHEN_FOCUSED);
        actionMap.put("hideAction", hideAction);
        actionMap.put("acceptAction", acceptAction);
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "acceptAction");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "hideAction");

        textField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                popup.setVisible(false);
                textField.setText("");
            }
        });

        popup.add(textField, BorderLayout.CENTER);
    }

    public void takeInput(MainFrame mainFrame, OntologyDisplayListModel listModel)
    {
        model = listModel;
        setLocationRelativeTo(mainFrame);
        setVisible(true);
    }

    private static class HideAction extends AbstractAction {

        final JDialog target;

        private HideAction(JDialog target) {
            this.target = target;
        }

        public void actionPerformed(ActionEvent e) {
            target.setVisible(false);
        }
    }

    private class AcceptAction extends AbstractAction {

        final JDialog target;

        private AcceptAction(JDialog target) {
            this.target = target;
        }

        public void actionPerformed(ActionEvent e) {
            target.setVisible(false);
            model.addAxiom(textField.getText());
        }
    }
}
