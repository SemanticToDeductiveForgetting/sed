package pc1.pc2.pc3.logic.om;

import org.jetbrains.annotations.Nullable;
import pc1.pc2.pc3.om.IConceptLiteral;
import pc1.pc2.pc3.om.ILiteral;

import java.util.*;

public class SignatureMgr
{
    private List<ILiteral> signature;
    private Map<ILiteral, Integer> indexMap;

    public SignatureMgr(List<ILiteral> signature)
    {
        this.signature = new LinkedList<>();
        indexMap = new HashMap<>();
        for (ILiteral literal : signature) {
            add((IConceptLiteral) literal);
        }
        // this.signature = new LinkedList<>(signature);

    }

    public boolean contains(ILiteral resolutionSymbol)
    {
        return indexMap.containsKey(resolutionSymbol);
    }

    public int indexOf(ILiteral symbol)
    {
        return indexMap.getOrDefault(symbol, -1);
    }

    public List<ILiteral> getSignatureLiterals()
    {
        return Collections.unmodifiableList(signature);
    }

    public void add(IConceptLiteral literal)
    {
        indexMap.put(literal, signature.size());
        signature.add(literal);
    }

    @Nullable public ILiteral get(int index)
    {
        if(index < signature.size()) {
            return signature.get(index);
        }
        return null;
    }

    public int getSize()
    {
        return signature.size();
    }
}
