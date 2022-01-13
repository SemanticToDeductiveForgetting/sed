package pc1.pc2.pc3.utils;

import java.io.*;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class FileUtils
{

    public static List<String> loadFile(File file) throws IOException
    {
        try (FileReader reader = new FileReader(file)) {
            return new BufferedReader(reader).lines().collect(Collectors.toList());
        }
    }

    public static void writeFile(String path, Collection<String> lines) throws IOException
    {
        try (FileWriter writer = new FileWriter(new File(path), false)) {
            writer.write(String.join("\n", lines));
        }
    }
}
