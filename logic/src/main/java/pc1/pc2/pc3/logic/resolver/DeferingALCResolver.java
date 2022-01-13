package pc1.pc2.pc3.logic.resolver;

import pc1.pc2.pc3.om.IAxiom;
import pc1.pc2.pc3.om.IConceptLiteral;
import pc1.pc2.pc3.logic.factory.DefinerFactory;

import java.nio.charset.StandardCharsets;
import java.util.Collection;

public class DeferingALCResolver extends ALCResolver
{
    private final long deferSize;

    public DeferingALCResolver(DefinerFactory factory, long deferSize)
    {
        super(factory);
        this.deferSize = deferSize;
    }

    @Override
    protected boolean shouldDefer(Collection<IAxiom> positive, Collection<IAxiom> negative, IConceptLiteral symbol)
    {
        boolean defer = super.shouldDefer(positive, negative, symbol);
        long positiveSum = getSize(positive);
        long negativeSum = getSize(negative);
        long size = positiveSum * negative.size() * negativeSum * positive.size();
        defer |= size > deferSize;
        return defer;
    }

    private long getSize(Collection<IAxiom> positive)
    {
        return positive.stream()
                .map(IAxiom::toString)
                .mapToLong(s -> s.getBytes(StandardCharsets.UTF_8).length)
                .sum();
    }
}
