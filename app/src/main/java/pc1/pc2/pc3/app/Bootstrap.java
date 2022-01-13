package pc1.pc2.pc3.app;

import pc1.pc2.pc3.om.CommonFactory;
import pc1.pc2.pc3.om.FactoryMgr;
import pc1.pc2.pc3.om.SymbolFactory;
import pc1.pc2.pc3.om.UnlimitedTimeMgr;

public class Bootstrap
{

    public static void initializeApplication()
    {
        FactoryMgr factoryMgr = new FactoryMgr(new SymbolFactory(), new CommonFactory(), new UnlimitedTimeMgr());
        FactoryMgr.setInstance(factoryMgr);
    }
}
