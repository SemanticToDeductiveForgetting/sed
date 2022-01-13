package pc1.pc2.pc3.utils;

import java.nio.charset.Charset;

public class StringUtils
{
    public static byte[] toByteArray(String text)
    {
        return text.getBytes(Charset.forName("UTF-8"));
    }
}
