package pc1.pc2.pc3.utils;

import java.util.Collection;
import java.util.LinkedList;

public class CollectionUtils
{
    public static <T> LinkedList<T> merge(Collection<T> first, Collection<T> second)
    {
        LinkedList<T> all = new LinkedList<>(first);
        all.addAll(second);
        return all;
    }
}
