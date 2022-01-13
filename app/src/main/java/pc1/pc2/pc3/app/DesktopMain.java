package pc1.pc2.pc3.app;

import pc1.pc2.pc3.app.ui.desktop.MainFrame;
import pc1.pc2.pc3.app.ui.desktop.Presenter;

import javax.swing.*;

public class DesktopMain
{

    public static void main(String[] args)
    {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }

        Bootstrap.initializeApplication();
        AppEngine engine = new AppEngine();
        MainFrame mainWindow = new MainFrame("Forgetting relative to background theory", new Presenter(engine));
        mainWindow.launch(engine);
    }
}
