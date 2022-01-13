package pc1.pc2.pc3.rest;

import org.junit.BeforeClass;
import org.junit.Test;
import pc1.pc2.pc3.app.Bootstrap;

public class BioportalClientTest
{

    @BeforeClass public static void init()
    {
        Bootstrap.initializeApplication();
    }

    @Test public void testConnection()
    {
        //new BioportalClient().loadData();
    }
}
