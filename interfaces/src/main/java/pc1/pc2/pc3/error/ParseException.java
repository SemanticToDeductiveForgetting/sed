package pc1.pc2.pc3.error;

public class ParseException extends Exception
{
    public ParseException(String message)
    {
        super(message);
    }

    public ParseException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
