package pc1.pc2.pc3.logic.resolver;

public class Pair<T, U>
{
    private final T first;
    private final U second;

    Pair(T first, U second)
    {
        this.first = first;
        this.second = second;
    }

    public T getFirst()
    {
        return first;
    }
    public U getSecond()
    {
        return second;
    }
}
