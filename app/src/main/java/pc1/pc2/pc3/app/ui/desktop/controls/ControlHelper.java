package pc1.pc2.pc3.app.ui.desktop.controls;

import org.jetbrains.annotations.NotNull;
import pc1.pc2.pc3.app.actions.BaseAction;
import pc1.pc2.pc3.app.ui.desktop.Presenter;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

public class ControlHelper
{
    private final BaseAction actionMaker;

    public ControlHelper(Presenter presenter)
    {
        if(presenter != null) {
            actionMaker = new BaseAction(presenter);
        }
        else {
            actionMaker = null;
        }
    }

    @NotNull public JButton createButton(String iconPath, Consumer<Presenter> action, Dimension size)
    {
        ImageIcon icon = new ImageIcon(getClass().getResource(iconPath));
        JButton btn = new JButton(icon);
        btn.setContentAreaFilled(false);
        btn.setPreferredSize(size);
        btn.setMaximumSize(size);
        if(actionMaker != null) {
            btn.setAction(actionMaker.makeAction(action));
        }
        Image img = icon.getImage() ;
        Image newimg = img.getScaledInstance(size.width, size.height, Image.SCALE_SMOOTH) ;
        icon = new ImageIcon( newimg );
        btn.setIcon(icon);
        return btn;
    }
}
