package pc1.pc2.pc3.om;

import pc1.pc2.pc3.error.TimeException;

public class TimeMgr implements ITimeMgr
{
    private final long start;
    private final long maxDuration;

    /**
     * Construct an instance of TimeMgr
     *
     * @param maxDuration nano value for the maximum duration of the process.
     */
    public TimeMgr(long maxDuration)
    {
        this.maxDuration = maxDuration;
        this.start = System.nanoTime();
    }

    @Override
    public void checkTime() throws TimeException
    {
        long now = System.nanoTime();
        if (now - start >= maxDuration) {
            throw new TimeException("Program has been running for more than the specified duration of " + maxDuration);
        }
    }
}
