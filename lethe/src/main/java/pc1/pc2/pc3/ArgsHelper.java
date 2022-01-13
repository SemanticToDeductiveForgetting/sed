package pc1.pc2.pc3;

import org.json.JSONArray;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class ArgsHelper
{
    static File parseOutputDirArg(String arg0, String arg1, String usage)
    {
        if (arg0.equalsIgnoreCase("-outDir")) {
            File file = new File(arg1);
            if (file.isFile() || (!file.exists() && !file.mkdirs())) {
                System.out.println("Cannot create output directory");
                System.out.println(usage);
            }
            return file;
        }
        return null;
    }

    static File parseFileArguement(String filePath)
    {
        File file = new File(filePath);
        if (file.isDirectory()) {
            System.out.println(String.format("%s is not a valid file", file));
            return null;
        }
        return file;
    }

    public static List<SymbolData> parseSignatureFileArg(String filePath, String usage)
    {
        try {
            File file = new File(filePath);
            if(file.isFile() && file.exists()) {
                String string = String.join("", loadFile(file));
                JSONArray jconcepts = new JSONArray(string);
                List<SymbolData> signature = new LinkedList<>();
                for (int i = 0; i < jconcepts.length(); i++) {
                    signature.add(SymbolData.deserialize(jconcepts.getJSONObject(i)));
                }
                return signature;
            }
            else {
                System.out.println(usage);
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println(usage);
        }
        return null;
    }

    public static List<String> loadFile(File file) throws IOException
    {
        try(FileReader reader = new FileReader(file))
        {
            return new BufferedReader(reader).lines().collect(Collectors.toList());
        }
    }
}
