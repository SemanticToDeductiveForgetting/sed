package pc1.pc2.pc3.om;

public class FactoryMgr
{
    private static FactoryMgr instance;

    private final ISymbolFactory symbolFactory;
    private final ICommonFactory commonFactory;
    private ITimeMgr timeMgr;

    public static void setInstance(FactoryMgr factory)
    {
        instance = factory;
    }

    private static FactoryMgr getInstance()
    {
        return instance;
    }

    public FactoryMgr(ISymbolFactory symbolFactory, ICommonFactory commonFactory,
                      ITimeMgr timeMgr)
    {
        this.symbolFactory = symbolFactory;
        this.commonFactory = commonFactory;
        this.timeMgr = timeMgr;
    }

    private ISymbolFactory _getSymbolFactory()
    {
        return symbolFactory;
    }

    private ICommonFactory _getCommonFactory()
    {
        return commonFactory;
    }

    /**
     * Sets the timeout of the program.
     *
     * @param value Timeout in Seconds.
     */
    public static void setMaxTime(long value)
    {
        getInstance()._setMaxTime(value * 1000000000);
    }

    public static ITimeMgr getTimeMgr()
    {
        return getInstance()._getTimeMgr();
    }

    private ITimeMgr _getTimeMgr()
    {
        return timeMgr;
    }

    public static ISymbolFactory getSymbolFactory()
    {
        return getInstance()._getSymbolFactory();
    }

    public static ICommonFactory getCommonFactory()
    {
        return getInstance()._getCommonFactory();
    }

    private void _setMaxTime(long value)
    {
        timeMgr = _getCommonFactory().createTimeMgr(value);
    }
}
