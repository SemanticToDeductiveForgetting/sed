package pc1.pc2.pc3;

import org.junit.BeforeClass;
import pc1.pc2.pc3.app.Bootstrap;

public class Test {

    public String getTestDataRoot()
    {
        return "./src/test/resources/" + getClass().getName().replace('.', '/') ;
    }

    @BeforeClass
    public static void init()
    {
        Bootstrap.initializeApplication();
    }
}
