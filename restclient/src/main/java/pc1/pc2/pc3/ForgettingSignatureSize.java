package pc1.pc2.pc3;

import pc1.pc2.pc3.rest.SymbolData;

import java.io.IOException;
import java.util.*;

public class ForgettingSignatureSize
{
    private static String usage = String.format("%s\n%s\n%s\n",
            "ForgettingSignatureSize:",
            "Reads the size of the forgetting signature of an experiment",
            "Usage: ForgettingSignatureSize -sig signature -name ExperimentName -outFile OutputFile");

    private static List<SymbolData> signature = null;
    private static String name = null;
    private static String outputFile = null;


    public static void main(String[] args) throws IOException
    {
        System.out.println(Runtime.getRuntime().totalMemory());
//        Bootstrap.initializeApplication();
//        parseArguements(args);
//
//        signature.size();
//
//        try (OutputStreamWriter writer = new FileWriter(outputFile, true)) {
//            writer.append(name).append(',').append(String.valueOf(signature.size())).append('\n');
//        }
    }

    private static void parseArguements(String[] args)
    {
        if (args.length < 6) {
            System.out.println(String.format("Incorrect number of arguements. Args: %s", String.join(", ", args)));
            System.out.println(usage);
            System.exit(1);
        }

        for (int i = 0; i < args.length; i++) {
            String arg0 = args[i];
            if ("-sig".equalsIgnoreCase(arg0)) {
                signature = ArgsHelper.parseSignatureFileArg(args[++i], usage);
            }
            else if ("-name".equalsIgnoreCase(arg0)) {
                name = args[++i];
            }
            else if ("-outFile".equalsIgnoreCase(arg0)) {
                outputFile = args[++i];
            }
        }
        if (signature == null || name == null || outputFile == null) {
            System.out.println(usage);
            System.exit(1);
        }
    }
}
