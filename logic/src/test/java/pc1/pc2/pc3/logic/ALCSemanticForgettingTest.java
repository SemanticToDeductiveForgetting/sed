package pc1.pc2.pc3.logic;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;
import pc1.pc2.pc3.app.Bootstrap;
import pc1.pc2.pc3.error.ParseException;
import pc1.pc2.pc3.error.TimeException;
import pc1.pc2.pc3.om.*;
import pc1.pc2.pc3.logic.factory.DefinerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class ALCSemanticForgettingTest extends pc1.pc2.pc3.logic.Test
{
    @Test
    public void testUnitPropositionalCase() throws ParseException, TimeException
    {
        verify(Arrays.asList("A -> B", "B -> C"),
                Collections.singletonList("B"),
                Collections.singletonList("A -> C"),
                Collections.emptyList());
    }

    @Test
    public void testResolutionUnderRoleRestriction() throws ParseException, TimeException
    {
        verify(Arrays.asList("A -> *EXISTS* r.B", "B -> C"),
                Collections.singletonList("B"),
                Collections.singletonList("A -> *EXISTS* r.C"),
                Collections.emptyList());
    }

    @Test
    public void testNormalizingDefiners() throws TimeException, ParseException
    {
        verify(Arrays.asList("A -> *EXISTS* r.(C2 & B)", "C1 -> C3 | !B"),
                Collections.singletonList("B"),
                Collections.singletonList("A -> *EXISTS* r.(C2 & (!C1 | C3))"),
                Collections.emptyList());
    }

    /**
     * Example 1
     *
     * @throws ParseException exception when input is not in correct syntax
     */
    @Test
    public void testPropositionalTwoSymbolsForgettingCase() throws ParseException, TimeException
    {
        verify(Arrays.asList("A -> B", "C -> I", "*TOP* -> B | C | D", "B -> H"),
                Arrays.asList("B", "C"),
                Arrays.asList("A -> H", "*TOP* -> H | D | I"),
                Collections.emptyList());
    }

    @Test
    public void testSimpleRolePropagation() throws ParseException, TimeException
    {
        verify(Arrays.asList("A -> *EXISTS* r.B", "*EXISTS* r.B -> C"),
                Collections.singletonList("B"),
                Arrays.asList("A -> *EXISTS* r.D1", "*EXISTS* r.D1 -> C"),
                Collections.singletonList("D1"));
    }

    @Test
    public void testSimpleRolePropagationWithForall() throws ParseException, TimeException
    {
        verify(Arrays.asList("A -> *EXISTS* r.B", "*FORALL* r.B -> C"),
                Collections.singletonList("B"),
                Arrays.asList("A -> *EXISTS* r.D1", "*FORALL* r.D1 -> C"),
                Collections.singletonList("D1"));
    }

    @Test
    public void testNestedRoleRestriction() throws ParseException, TimeException
    {
        verify(Arrays.asList("A -> *EXISTS* r.(B | *EXISTS* r.C)", "A -> *EXISTS* r.!C"),
                Collections.singletonList("C"),
                Arrays.asList("A -> *EXISTS* r.(B | *EXISTS* r.D1)", "A -> *EXISTS* r.D2", "D2 & D1 -> *BOTTOM*"),
                Arrays.asList("D1", "D2"));
    }

    @Test
    public void test16() throws ParseException, TimeException
    {
        verify(Arrays.asList("A -> *EXISTS* r.(B | C)", "G -> *EXISTS* r.(!B | E)"),
                Collections.singletonList("B"),
                Arrays.asList("A -> *EXISTS* r.D1", "G -> *EXISTS* r.D2", "D1 & D2 -> C | E"),
                Arrays.asList("D1", "D2"));
    }

    @Test
    public void testNestedResolution() throws ParseException, TimeException
    {
        Bootstrap.initializeApplication();
        verify(Arrays.asList("A1 -> *EXISTS* r.(!B | C)", "A2 -> *EXISTS* r.B", "A3 -> *EXISTS* r.(!C | F)"),
                Arrays.asList("B", "C"),
                Arrays.asList("A1 -> *EXISTS* r.D1", "A2 -> *EXISTS* r.D2", "A3 -> *EXISTS* r.D3", "D1 & D2 & D3 -> F"),
                Arrays.asList("D1", "D2", "D3"));
    }

    @Test
    public void testPurification() throws ParseException, TimeException
    {
        verify(Arrays.asList("A -> EXISTS r.B", "A -> EXISTS r.C"),
                Arrays.asList("B", "C"),
                Arrays.asList("A -> EXISTS r.TOP", "A -> EXISTS r.TOP"),
                Collections.emptyList());
    }

    @Test
    public void test7() throws ParseException, TimeException
    {
        verify(Arrays.asList("TOP -> A | B", "C -> H", "TOP -> C | EXISTS r.(B | E)", "B -> F"),
                Arrays.asList("B", "C"),
                Arrays.asList("TOP -> A | F", "TOP -> H | EXISTS r.(E | F)"),
                Collections.emptyList());
    }

    @Test
    public void testEquivalenceReplacementInPropositionalCase() throws TimeException, ParseException
    {
        verify(Arrays.asList("A -> B", "B = C"),
                Collections.singletonList("B"),
                Collections.singletonList("A -> C"),
                Collections.emptyList());
    }

    @Test
    public void testEquivalenceReplacementInPropositionalCaseWithNegation() throws TimeException, ParseException
    {
        verify(Arrays.asList("A -> !B", "B = C"),
                Collections.singletonList("B"),
                Collections.singletonList("A -> !C"),
                Collections.emptyList());
    }

    @Test
    public void testEquivalenceReplacementInCompositeCase() throws TimeException, ParseException
    {
        verify(Arrays.asList("A -> B | E", "B = C"),
                Collections.singletonList("B"),
                Collections.singletonList("A -> C | E"),
                Collections.emptyList());
    }

    @Test
    public void testEquivalenceReplacementInCompositeCaseWithNegation() throws TimeException, ParseException
    {
        verify(Arrays.asList("A -> !B & E", "B = C"),
                Collections.singletonList("B"),
                Collections.singletonList("A -> !C & E"),
                Collections.emptyList());
    }

    @Test
    public void testEquivalenceReplacementInQuantifiedCase() throws TimeException, ParseException
    {
        verify(Arrays.asList("A -> *EXISTS* r. (B | E)", "B = C"),
                Collections.singletonList("B"),
                Collections.singletonList("A -> *EXISTS* r. (C | E)"),
                Collections.emptyList());
    }

    @Test
    public void testEquivalenceReplacementInQuantifiedCaseWithNegation() throws TimeException, ParseException
    {
        verify(Arrays.asList("A -> *FORALL* r.(!B & E)", "B = C"),
                Collections.singletonList("B"),
                Collections.singletonList("A -> *FORALL* r.(!C & E)"),
                Collections.emptyList());
    }

    @Test
    public void testEquivalenceReplacementInNestedQuantifiedCase() throws TimeException, ParseException
    {
        verify(Arrays.asList("A -> *EXISTS* r. (*FORALL* s.(B) | E)", "B = C"),
                Collections.singletonList("B"),
                Collections.singletonList("A -> *EXISTS* r. (*FORALL* s.(C) | E)"),
                Collections.emptyList());
    }

    @Test
    public void testEquivalenceReplacementInNestedQuantifiedCaseWithNegation() throws TimeException, ParseException
    {
        verify(Arrays.asList("A -> *FORALL* r.(*EXISTS* s.(!B) & E)", "B = C"),
                Collections.singletonList("B"),
                Collections.singletonList("A -> *FORALL* r.(*EXISTS* s.(!C) & E)"),
                Collections.emptyList());
    }

    @Test
    public void testEquivalenceReplacementSimplifiedWhenApplicable() throws TimeException, ParseException
    {
        verify(Arrays.asList("A -> *FORALL* r.(*EXISTS* s.(B | E) & E)", "B = C | E"),
                Collections.singletonList("B"),
                Collections.singletonList("A -> *FORALL* r.(*EXISTS* s.(C | E) & E)"),
                Collections.emptyList());
    }

    protected void verify(List<String> axioms, List<String> forgettingSymbols, List<String> goldenRef,
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
        return new ALCForgettingEngine(new DefinerFactory());
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