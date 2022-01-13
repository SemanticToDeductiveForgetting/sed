package pc1.pc2.pc3.app.actions;

import pc1.pc2.pc3.app.ui.desktop.Presenter;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.function.Consumer;

public class BaseAction
{
    private final Presenter presenter;

    public BaseAction(Presenter presenter)
    {
        this.presenter = presenter;
    }

    public Action makeAction(Consumer<Presenter> actionCode)
    {
        return new AbstractAction()
        {
            @Override public void actionPerformed(ActionEvent e)
            {
                actionCode.accept(presenter);
            }
        };
    }
}
