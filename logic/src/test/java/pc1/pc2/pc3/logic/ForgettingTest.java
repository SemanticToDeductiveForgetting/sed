package pc1.pc2.pc3.logic;

import org.junit.Assert;
import org.junit.Test;
import pc1.pc2.pc3.error.ParseException;
import pc1.pc2.pc3.error.TimeException;
import pc1.pc2.pc3.om.FactoryMgr;
import pc1.pc2.pc3.om.IClause;
import pc1.pc2.pc3.om.IConceptLiteral;
import pc1.pc2.pc3.utils.CollectionUtils;
import pc1.pc2.pc3.logic.factory.DefinerFactory;
import pc1.pc2.pc3.logic.helper.ClauseHelper;
import pc1.pc2.pc3.logic.helper.OntologyPair;
import pc1.pc2.pc3.logic.reduction.Alc;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ForgettingTest extends pc1.pc2.pc3.logic.Test
{
    private static final String A = "A";

    @Test
    public void testResolveTwoUnitClausesReturnsEmptyClause() throws ParseException
    {
        List<IClause> interpolant = forget("A", "¬A");
        Assert.assertTrue("Resolver should return empty clause", interpolant.get(0).isBottom());
    }

    @Test
    public void testResolveTwoUnitClausesReturnsEmptyInterpolantWhenAnotherLiteralExistWithOppositePolarity()
            throws ParseException
    {
        List<IClause> interpolant = forget("A | B", "¬A | ¬B");
        Assert.assertTrue("Resolver should return empty interpolant", interpolant.isEmpty());
    }

    @Test
    public void testResolveTwoClauses() throws ParseException, TimeException
    {
        verify("A | B", Collections.singletonList("¬A | ¬C"), Collections.singletonList("B | ¬C"));
    }

    @Test
    public void testResolverRemovesDuplicates() throws ParseException, TimeException
    {
        verify("A | B | ¬C", Collections.singletonList("¬A | ¬C | B"), Collections.singletonList("B | ¬C"));
    }

    @Test
    public void testClauseHasQuantifier() throws ParseException, TimeException
    {
        verify("A | B | ¬C | EXISTS r.E",
                Collections.singletonList("¬A | D"),
                Collections.singletonList("B | ¬C | D | EXISTS r.E"));
    }


    @Test
    public void testResolventIsQuantifiedClause() throws ParseException, TimeException
    {
        verify("A | EXISTS r.E", Collections.singletonList("¬A"), Collections.singletonList("EXISTS r.E"));
    }

    @Test
    public void testResolventLiteralUnderQuantifier() throws ParseException, TimeException
    {
        verify("EXISTS r.(A | B)", Collections.singletonList("¬A"), Collections.singletonList("EXISTS r.B"));
    }

    @Test
    public void testExistentialQuantifierElimination() throws ParseException, TimeException
    {
        verify("C | EXISTS r.(A)", Collections.singletonList("¬A"), Collections.singletonList("C"));
    }

    @Test
    public void testEliminateNestedExistentialQuantifier() throws ParseException, TimeException
    {
        verify("C | EXISTS r.(EXISTS s. A)", Collections.singletonList("¬A"), Collections.singletonList("C"));
    }

    @Test
    public void testResolveLiteralUnderNestedQuantifier() throws ParseException, TimeException
    {
        verify("FORALL s. (C | EXISTS r.(A | B))", Collections.singletonList("¬A"),
                Collections.singletonList("FORALL s. (C | EXISTS r.(B))"));
    }

    @Test
    public void testRolePropagation_UnitSuccessor() throws ParseException, TimeException
    {
        verify("C1 | EXISTS r.(A | B)", Collections.singletonList("C2 | FORALL r.(¬A)"),
                Arrays.asList("C1 | C2 | EXISTS r.(B)", "C1 | EXISTS r. TOP"));
    }

    @Test
    public void testRolePropagation_DisjunctiveSuccessor() throws ParseException, TimeException
    {
        verify("C1 | EXISTS r.(A | B)", Collections.singletonList("C2 | FORALL r.(¬A | E)"),
                Arrays.asList("C1 | C2 | EXISTS r.(B | E)", "C1 | EXISTS r. TOP"));
    }

    @Test
    public void testRolePropagation_ConjunctiveSuccessor() throws ParseException, TimeException
    {
        verify("C1 | EXISTS r.(A)", Arrays.asList("C2 | FORALL r.(¬A | B)", "¬A | F"),
                Arrays.asList("C1 | C2 | EXISTS r.(B & F)", "C1 | EXISTS r. F"));
    }

    @Test
    public void testRolePropagationInDifferentLevels() throws ParseException, TimeException
    {
        verify("C1 | EXISTS s. (C2 | EXISTS r.(A))", Collections.singletonList("C3 | FORALL r.(¬A | B)"),
                Collections.singletonList("C1 | EXISTS s.((C2 | C3 | EXISTS r.B)&(C2 | EXISTS r.TOP))"));
    }

    @Test
    public void testRolePropagationDoesNotApplyOnExistExistClauses() throws ParseException, TimeException
    {
        verify(Collections.singletonList("C1 | EXISTS r.(A | B)"),
                Collections.singletonList("C2 | EXISTS r.(¬A | E)"),
                Collections.singletonList("A"),
                Arrays.asList("C1 | EXISTS r.D1", "C2 | EXISTS r.D2", "B | E | ¬D1 | ¬D2"),
                Arrays.asList("D1", "D2"), true);
    }

    @Test
    public void testRolePropagationAppliesOnForallForallClauses() throws ParseException, TimeException
    {
        verify("C1 | FORALL r.(A | B)", Collections.singletonList("C2 | FORALL r.(¬A | E)"),
                Collections.singletonList("C1 | C2 | FORALL r.(B | E)"));
    }

    @Test
    public void testForgettingInBackgroundTheory() throws ParseException, TimeException
    {
        verify(Arrays.asList("¬A | B", "¬C | I"),
                Arrays.asList("B | C | D", "¬B | H"),
                Arrays.asList("B", "C"),
                Arrays.asList("¬A | H", "I | D | H"));
    }

    @Test
    public void testResolveUnderQuantifier() throws ParseException, TimeException
    {
        verify(Arrays.asList("¬A | B", "¬C | I", "¬Q | EXISTS r.(¬B)"),
                Arrays.asList("B | C | D", "¬B | H"),
                Arrays.asList("B", "C"),
                Arrays.asList("¬A | H", "I | D | H", "¬Q | EXISTS r.(¬A & (D | I))"));
    }

    @Test
    public void testResolveUnderQuantifierWithRolePropagation() throws ParseException, TimeException
    {
        verify(Arrays.asList("¬A | B", "¬C | I", "¬Q | EXISTS r.(¬B)"),
                Arrays.asList("D | FORALL r.(B | C)", "¬B | H"),
                Arrays.asList("B", "C"),
                Arrays.asList("¬A | H", "¬Q | EXISTS r.(¬A)", "D | FORALL r.(I | H)",
                        "¬Q | D | EXISTS r.(¬A & I)"));
    }

    @Test
    public void testRolePropagationInsideTheoryAndWithOntology() throws ParseException, TimeException
    {
        verify(Arrays.asList("¬A | B", "¬C | I", "¬Q | EXISTS r.(¬B)"),
                Arrays.asList("D | FORALL r.(B | C)", "¬B | H", "¬M | EXISTS r.(¬B | K)"),
                Arrays.asList("B", "C"),
                Arrays.asList("¬A | H", "¬Q | EXISTS r.(¬A)", "D | FORALL r.(I | H)",
                        "¬Q | D | EXISTS r.(¬A & I))",
                        "¬M | EXISTS r. (¬A | K)", "¬M | D | EXISTS r.((I | H) & (!A | K) & (I | K))"),
                Collections.emptyList(), false, true);
    }

    @Test
    public void testQuantifierMovedFromTheoryToInterpolantByResolution() throws ParseException, TimeException
    {
        verify(Collections.singletonList("¬B | A"),
                Arrays.asList("C | EXISTS r.B", "¬B | H"),
                Collections.singletonList("B"),
                Collections.singletonList("C | EXISTS r.(A & H)"));
    }

    @Test
    public void testQuantifierInTheoryIsNotResolvedUnlessInteractingWithOntology() throws ParseException, TimeException
    {
        verify(Collections.singletonList("¬A | FORALL r.(¬B | E)"),
                Arrays.asList("C | EXISTS r.B", "¬B | H"),
                Collections.singletonList("B"),
                Collections.singletonList("¬A | C | EXISTS r.(E & H)"));
    }

    @Test
    public void testTypeGInferenceIsPerformedEvenIfDefinerNotInIOrO() throws ParseException, TimeException
    {
        verify(Arrays.asList("A | B", "¬C | H"),
                Arrays.asList("C | EXISTS r.(B | E)", "¬B | F"),
                Arrays.asList("B", "C"),
                Arrays.asList("H | EXISTS r.(E | F)", "A | F"));
    }

    @Test
    public void testSubsumptionDeletion() throws ParseException, TimeException
    {
        verify(Arrays.asList("A | B | C", "A | B"),
                Collections.singletonList("¬A"),
                Collections.singletonList("A"),
                Collections.singletonList("B"));
    }

    @Test
    public void testSubsumptionWhenSubsumeeHasQuantifier() throws ParseException, TimeException
    {
        verify(Arrays.asList("A | B | EXISTS r.C", "A | B"),
                Collections.singletonList("¬A"),
                Collections.singletonList("A"),
                Collections.singletonList("B"));
    }

    @Test
    public void testSubsumptionOverQuantifiedParts() throws ParseException, TimeException
    {
        verify(Arrays.asList("A | EXISTS r.(¬B)", "B | C", "A | D | EXISTS r.C"),
                Collections.emptyList(),
                Collections.singletonList("B"),
                Collections.singletonList("A | EXISTS r.C"));
    }

    @Test
    public void testSubsumptionFailsOverQuantifiedParts() throws ParseException, TimeException
    {
        verify(Arrays.asList("A | EXISTS r.(¬B)", "B | C", "A | D"),
                Collections.emptyList(),
                Collections.singletonList("B"),
                Arrays.asList("A | D", "A | EXISTS r.C"));
    }

    @Test
    public void testMutualExclusiveRelations() throws ParseException, TimeException
    {
        verify(Collections.singletonList("¬A1 | EXISTS r.B"),
                Collections.singletonList("¬A2 | EXISTS r.¬B"),
                Collections.singletonList("B"),
                Arrays.asList("¬A1 | EXISTS r.D_1", "¬A2 | EXISTS r.D_2", "¬D_1 | ¬D_2"),
                Arrays.asList("D_1", "D_2"), true);
    }

    @Test
    public void testSemanticCardinalityOfRelation() throws ParseException, TimeException
    {
        verify(Arrays.asList("¬A | EXISTS r.B", "¬A | EXISTS r.C", "¬B | E", "¬C | E"),
                Collections.emptyList(),
                Arrays.asList("B", "C"),
                Arrays.asList("¬A | EXISTS r.D_1", "¬A | EXISTS r.D_2", "¬D_1 | E", "¬D_2 | E"),
                Arrays.asList("D_1", "D_2"), false);
    }

    @Test
    public void testIndirectMutualExclusion() throws ParseException, TimeException
    {
        verify(Arrays.asList("!A | EXISTS r.B", "!A | EXISTS r.C", "!B | !C"),
                Collections.emptyList(),
                Arrays.asList("B", "C"),
                Arrays.asList("!A | EXISTS r.D_1", "!A | EXISTS r.D_2", "!D_1 | !D_2"),
                Arrays.asList("D_1", "D_2"), true);
    }

    @Test
    public void testUnknownCardinality() throws ParseException, TimeException
    {
        verify(Arrays.asList("!A | EXISTS r.B", "!A | EXISTS r.C"),
                Collections.emptyList(),
                Arrays.asList("B", "C"),
                Arrays.asList("!A | EXISTS r.D_1", "!A | EXISTS r.D_2"),
                Arrays.asList("D_1", "D_2"), false);
    }

    @Test
    public void testNonconflictingElimination() throws ParseException, TimeException
    {
        verify(Arrays.asList("!A | EXISTS r.B", "!A | EXISTS r.C"),
                Collections.singletonList("!B | E"),
                Arrays.asList("B", "C"),
                Arrays.asList("!A | EXISTS r.E", "!A | EXISTS r.D_2"),
                Collections.singletonList("D_2"), false);
    }

    @Test
    public void testThreeWayExclusiveness() throws ParseException, TimeException
    {
        verify(Arrays.asList("!A | EXISTS r.B", "!A | EXISTS r.C", "!A | EXISTS r.E"),
                Arrays.asList("!B | !C", "!B | !E", "!C | !E"),
                Arrays.asList("B", "C", "E"),
                Arrays.asList("!A | EXISTS r.D_1", "!A | EXISTS r.D_2", "!A | EXISTS r.D_3", "!D_1 | !D_2", "!D_1 | " +
                        "!D_3", "!D_2 | !D_3"),
                Arrays.asList("D_1", "D_2", "D_3"), true);
    }

    @Test
    public void testNestedExistentialQuantifiers() throws ParseException, TimeException
    {
        verify(Arrays.asList("!A | EXISTS r.(B | EXISTS r.C)", "!A | EXISTS r.!C"),
                Collections.emptyList(),
                Collections.singletonList("C"),
                Arrays.asList("!A | EXISTS r.D_1", "!D_1 | B | EXISTS r.D_2", "!A | EXISTS r.D_3", "!D_3 | !D_2"),
                Arrays.asList("D_1", "D_2", "D_3"), true);
    }

    @Test
    public void testMixedUniversalAndExistentialResolutions() throws ParseException, TimeException
    {
        verify(Arrays.asList("!A | EXISTS r.!B", "!A | FORALL r.(B|C)", "!A | EXISTS r.B"),
                Collections.emptyList(),
                Collections.singletonList("B"),
                Arrays.asList("!A | EXISTS r.D_1", "!A | FORALL r.D_2", "!A | EXISTS r.D_3", "!D_1 | !D_3",
                        "!D_1 | !D_2 | C"),
                Arrays.asList("D_1", "D_2", "D_3"), true);
    }

    @Test
    public void testRolePropagationInMethod7() throws ParseException, TimeException
    {
        verify(Arrays.asList("!A | FORALL r.B", "!A | FORALL s.(!B | E)", "!G | FORALL r. (!C | F)",
                "!H | EXISTS r.(!B | C)"),
                Arrays.asList("!I | EXISTS r.B", "!C | J", "!C | K"), Arrays.asList("B", "C"),
                Arrays.asList("!A | !G | !H | EXISTS r.(F & J & K)", "!A | !H | EXISTS r.(J & K)", "!H | EXISTS r.TOP"),
                Collections.emptyList(), false, true);
    }

    @Test
    public void testConjunctionUnderRoleSymbol() throws ParseException, TimeException
    {
        verify(Collections.singletonList("!A | EXISTS r.(B & C)"), Collections.singletonList("A | !E"),
                Collections.singletonList("A"), Collections.singletonList("!E | EXISTS r.(B & C)"),
                Collections.emptyList(), false, false);
    }

    @Test
    public void testComplexNormalization() throws ParseException, TimeException
    {
        verify(Collections.singletonList("!A | EXISTS r.(B & EXISTS r. (C & D & E & EXISTS s.(F & EXISTS t.(G&H))))"),
                Collections.singletonList("A | !I"), Collections.singletonList("A"),
                Collections.singletonList("!I | EXISTS r.(B & EXISTS r. (C & D & E & EXISTS s.(F & EXISTS t.(G&H))))"),
                Collections.emptyList(), false, false);
    }

    private void verify(String ontology, List<String> backgroundTheory, List<String> goldenRef)
            throws ParseException, TimeException
    {
        verify(Collections.singletonList(ontology), backgroundTheory, Collections.singletonList(A), goldenRef,
                Collections.emptyList(), false);
    }

    private void verify(List<String> ontology, List<String> backgroundTheory, List<String> forgettingSymbols,
                        List<String> goldenRef) throws ParseException, TimeException
    {
        verify(ontology, backgroundTheory, forgettingSymbols, goldenRef, Collections.emptyList(), false);
    }

    private void verify(List<String> ontology, List<String> backgroundTheory, List<String> forgettingSymbols,
                            List<String> goldenRefs, List<String> definerVariables, boolean semantic, boolean skipLethe)
            throws ParseException, TimeException
    {
        if (semantic) {
            List<IClause> myInterpolant = forget(ontology, backgroundTheory, forgettingSymbols, true, false);
            verifyResult(goldenRefs, definerVariables, myInterpolant);
        }
        else {
            if(!skipLethe) {
                List<IClause> letheInterpolant = forget(ontology, backgroundTheory, forgettingSymbols, false, false);
                verifyResult(goldenRefs, definerVariables, letheInterpolant);
            }

            List<IClause> myInterpolant = forget(ontology, backgroundTheory, forgettingSymbols, true, true);
            verifyResult(goldenRefs, definerVariables, myInterpolant);
        }
    }

    private void verify(List<String> ontology, List<String> backgroundTheory, List<String> forgettingSymbols,
                        List<String> goldenRefs, List<String> definerVariables, boolean semantic)
            throws ParseException, TimeException
    {
        verify(ontology, backgroundTheory, forgettingSymbols, goldenRefs, definerVariables, semantic, false);
    }

    private List<IClause> forget(List<String> ontology, List<String> backgroundTheory,
                                 List<String> forgettingSymbols, boolean semantic, boolean reduceSemantic)
            throws ParseException, TimeException
    {
        List<IClause> ont = parseClauses(ontology.toArray(new String[]{}));
        List<IClause> theory = parseClauses(backgroundTheory.toArray(new String[]{}));

        ForgettingEngine forgettingEngine = new ForgettingEngine();
        List<IConceptLiteral> symbols = forgettingSymbols.stream()
                .map(FactoryMgr.getSymbolFactory()::getLiteralForText)
                .map(c -> (IConceptLiteral) c)
                .collect(Collectors.toList());

        if (semantic) {
            OntologyPair pair = forgettingEngine.forgetSemantic(ont, theory, symbols);
            if (reduceSemantic) {
                DefinerFactory definerFactory = forgettingEngine.getDefinerFactory();
                Alc alcReduction = new Alc(pair.ontology, pair.background, definerFactory.getDefiners(),
                        definerFactory.getDefinerRoleMap());
                return alcReduction.reduce();
            }
            else {
                return CollectionUtils.merge(ClauseHelper.convertToClauses(pair.ontology),
                        ClauseHelper.convertToClauses(pair.background));
            }
        }
        else {
            return forgettingEngine.forgetClauses(ont, theory, symbols);
        }
    }

    private List<IClause> forget(String ontologyStatement, String... theoryStatements) throws ParseException
    {
        List<IClause> ontology = parseClauses(ontologyStatement);
        List<IClause> background = parseClauses(theoryStatements);
        ForgettingEngine forgettingEngine = new ForgettingEngine();
        IConceptLiteral forgettingSymbol = (IConceptLiteral) FactoryMgr.getSymbolFactory().getLiteralForText(A);
        return forgettingEngine.forgetClauses(ontology, background, Collections.singletonList(forgettingSymbol));
    }

}
