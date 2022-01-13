package pc1.pc2.pc3.app.utils;

import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.File;
import java.util.prefs.Preferences;

public class FileLoader
{
    private static final String SEARCH_DIRECTORY = "SearchDirectory";
    private final JFileChooser fileChooser = new JFileChooser();

    public FileLoader()
    {
        fileChooser.setDialogTitle("Select ontology");
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
    }

    @Nullable public File loadFile()
    {
        setSearchDirectory();
        int returnVal = fileChooser.showOpenDialog(null);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            saveSearchDirectory(file);
            return file;
        }
        return null;
    }

    private void saveSearchDirectory(File file)
    {
        String directory = file.getParent();
        Preferences prefs = Preferences.userNodeForPackage(getClass());
        prefs.put(SEARCH_DIRECTORY, directory);
    }

    private void setSearchDirectory()
    {
        Preferences prefs = Preferences.userNodeForPackage(getClass());
        String searchDirectory = prefs.get(SEARCH_DIRECTORY, null);
        if(searchDirectory != null) {
            File dir = new File(searchDirectory);
            if(dir.exists()) {
                fileChooser.setCurrentDirectory(dir);
            }
        }
    }
}
