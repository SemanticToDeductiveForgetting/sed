package pc1.pc2.pc3.logic;

import org.jetbrains.annotations.NotNull;
import pc1.pc2.pc3.error.ParseException;
import pc1.pc2.pc3.error.TimeException;
import pc1.pc2.pc3.om.*;
import pc1.pc2.pc3.logic.factory.DefinerFactory;
import pc1.pc2.pc3.logic.reduction.ALCSemanticToDeductive;

import java.util.*;
import java.util.stream.Collectors;

public class ALCDeductiveForgettingTest extends Test
{
    @org.junit.Test
    public void testCyclicDefinersAreNotEliminated() throws ParseException, TimeException
    {
        verify(Collections.singletonList("*TOP* -> !A | *EXISTS* r.A"),
                Collections.singletonList("A"),
                Collections.singletonList("*TOP* -> !D | *EXISTS* r.D"),
                Collections.singletonList("D"));
    }

    @org.junit.Test
    public void testDeltaAxiomIsExcluded() throws ParseException, TimeException
    {
        verify(Arrays.asList("*TOP* -> !A | *EXISTS* r.B", "*TOP* -> !C | *EXISTS* r.!B"),
                Collections.singletonList("B"),
                Arrays.asList("*TOP* -> !A | *EXISTS* r.*TOP*", "*TOP* -> !C | *EXISTS* r.*TOP*"),
                Collections.emptyList());
    }

    @org.junit.Test
    public void testExistForallPropagation() throws ParseException, TimeException
    {
        verify(Arrays.asList("*TOP* -> !A | *EXISTS* r.B", "*TOP* -> !C | *FORALL* r.!B"),
                Collections.singletonList("B"),
                Arrays.asList("*TOP* -> !A | *EXISTS* r.*TOP*", "*TOP* -> !A | !C"),
                Collections.emptyList());
    }

    @org.junit.Test
    public void testForallForallPropagation() throws ParseException, TimeException
    {
        verify(Arrays.asList("*TOP* -> !A | *FORALL* r.B", "*TOP* -> !C | *FORALL* r.!B"),
                Collections.singletonList("B"),
                Collections.singletonList("*TOP* -> !A | !C | *FORALL* r.!*TOP*"),
                Collections.emptyList());
    }

    @org.junit.Test
    public void testExistForallForallPropagation() throws ParseException, TimeException
    {
        verify(Arrays.asList("*TOP* -> !A | *FORALL* r.B", "*TOP* -> !E | *FORALL* r.(!B | C)", "*TOP* -> !F | " +
                        "*EXISTS* r.(!C | G)"),
                Arrays.asList("B", "C"),
                Arrays.asList("*TOP* -> !F | *EXISTS* r.*TOP*", "*TOP* -> !A | !E | !F | *EXISTS* r.G"),
                Collections.emptyList());
    }

    @org.junit.Test
    public void testResolutionWithDeltaAxioms() throws ParseException, TimeException
    {
        verify(Arrays.asList("*TOP* -> !A | *FORALL* r.B", "*TOP* -> !E | *FORALL* r.(!B | C)", "*TOP* -> !F | " +
                        "*EXISTS* r.(!C | G)", "*TOP* -> !C | H"),
                Arrays.asList("B", "C"),
                Arrays.asList("*TOP* -> !A | !E | *FORALL* r.H", "*TOP* -> !F | *EXISTS* r.*TOP*", "*TOP* -> !A | !E " +
                        "| !F | *EXISTS* r.(G & H)"),
                Collections.emptyList());
    }

    private void verify(List<String> axioms, List<String> forgettingSymbols, List<String> goldenRef,
                        List<String> definers)
            throws ParseException, TimeException
    {
        IOntology ontology = FactoryMgr.getCommonFactory().createOntology();
        ontology.addAxioms(parseAxioms(axioms.toArray(new String[]{})));
        List<IConceptLiteral> signature = forgettingSymbols.stream()
                .map(FactoryMgr.getSymbolFactory()::getLiteralForText)
                .map(l -> (IConceptLiteral) l)
                .collect(Collectors.toList());
        ALCForgettingEngine engine = createForgettingEngine();

        List<IAxiom> forgettingView = new ArrayList<>(engine.forget(ontology, signature).getAllActiveAxioms());
        forgettingView = forgettingView.stream().sorted((o1, o2) -> {
            Collection<ILiteral> signature1 = o1.getSignature();
            signature1.retainAll(engine.getDefiners());
            Collection<ILiteral> signature2 = o2.getSignature();
            signature2.retainAll(engine.getDefiners());
            int compare = Integer.compare(signature1.size(), signature2.size());
            if (compare == 0) {
                return Integer.compare(o1.toString().length(), o2.toString().length());
            }
            return compare;
        }).collect(Collectors.toList());
        verifyResult(goldenRef, definers, forgettingView);
    }

    public @NotNull ALCForgettingEngine createForgettingEngine()
    {
        return new ALCForgettingEngine(new DefinerFactory(), new ALCSemanticToDeductive());
    }

    @Override
    protected boolean verifySemantically(IClause intClause, String goldenRef)
    {
        return false;
    }

    @Override
    protected @NotNull List<String> normalizeGoldenRefs(List<String> goldenRef) throws ParseException
    {
        return parseAxioms(goldenRef.toArray(new String[]{})).stream()
                .map(IAxiom::toString)
                .collect(Collectors.toList());
    }
}
