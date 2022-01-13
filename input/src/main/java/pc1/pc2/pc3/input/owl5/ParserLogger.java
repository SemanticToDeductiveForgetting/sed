package pc1.pc2.pc3.input.owl5;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;

public class ParserLogger
{
    private static ParserLogger instance;


    private Writer logger;

    private ParserLogger(Writer logger)
    {
        this.logger = logger;
    }

    public void _writeLine(String message)
    {
        try {
            logger.append(String.format("%s\n", message));
        } catch (IOException e) {
            System.out.println(String.format("%s\n", message));
        }
    }

    public static void setInstance(Writer logger)
    {
        instance = new ParserLogger(logger);
    }

    private static ParserLogger getInstance()
    {
        if(instance == null) {
            instance = new ParserLogger(new PrintWriter(System.out));
        }
        return instance;
    }

    public static void writeLine(String message)
    {
        getInstance()._writeLine(message);
    }
}
