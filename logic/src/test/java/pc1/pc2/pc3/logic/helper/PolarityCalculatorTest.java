package pc1.pc2.pc3.logic.helper;

import org.jetbrains.annotations.NotNull;
import org.junit.Assert;
import pc1.pc2.pc3.error.ParseException;
import pc1.pc2.pc3.logic.Test;
import pc1.pc2.pc3.om.FactoryMgr;
import pc1.pc2.pc3.om.IAxiom;
import pc1.pc2.pc3.om.IConceptLiteral;

import java.util.*;
import java.util.stream.Collectors;

public class PolarityCalculatorTest extends Test
{

    @org.junit.Test
    public void testPositiveAtomic() throws ParseException
    {
        verify(Collections.singleton("*TOP* -> A"), Collections.singleton("A"), Collections.emptySet(),
                Collections.emptySet());
    }

    @org.junit.Test
    public void testNegativeAtomic() throws ParseException
    {
        verify(Collections.singleton("*TOP* -> !A"), Collections.emptySet(), Collections.singleton("A"),
                Collections.emptySet());
    }

    @org.junit.Test
    public void testZeroAtomic() throws ParseException
    {
        verify(Collections.singleton("A -> A"), Collections.emptySet(), Collections.emptySet(),
                Collections.singleton("A"));
    }

    @org.junit.Test
    public void testConceptsInEquivalencesAreZero() throws ParseException
    {
        verify(Collections.singleton("A = *TOP*"), Collections.emptySet(), Collections.emptySet(),
                Collections.singleton("A"));
    }

    @org.junit.Test
    public void testDeltaClauseHasNegativeDefiners() throws ParseException
    {
        verify(Collections.singleton("*TOP* -> !D1 | !D2 | C"),
                Collections.singleton("C"),
                new HashSet<>(Arrays.asList("D1", "D2")),
                Collections.emptySet());
    }

    @org.junit.Test
    public void testDeltaAxiomHasNegativeDefiners() throws ParseException
    {
        verify(Collections.singleton("D1 & D2 -> C"),
                Collections.singleton("C"),
                new HashSet<>(Arrays.asList("D1", "D2")),
                Collections.emptySet());
    }

    @org.junit.Test
    public void testNestedPolarity() throws ParseException
    {
        verify(Collections.singleton("*TOP* -> !*FORALL* r.(A | !*EXISTS* r.(B | !C)"),
                Collections.singleton("B"),
                new HashSet<>(Arrays.asList("A", "C")),
                Collections.emptySet());
    }

    private void verify(Set<String> axioms, Set<String> positive, Set<String> negative, Set<String> zero)
            throws ParseException
    {
        List<IAxiom> ontology = parseAxioms(axioms.toArray(new String[]{}));
        Set<IConceptLiteral> actualPositive = new HashSet<>();
        Set<IConceptLiteral> actualNegative = new HashSet<>();
        Set<IConceptLiteral> actualZero = new HashSet<>();
        PolarityCalculator.getPolarityOfConceptNames(ontology, actualPositive, actualNegative, actualZero);

        Set<IConceptLiteral> goldenPositive = parseConcepts(positive);
        Set<IConceptLiteral> goldenNegative = parseConcepts(negative);
        Set<IConceptLiteral> goldenZero = parseConcepts(zero);

        System.out.println("Comparing positive literals...");
        verifyEqual(actualPositive, goldenPositive);
        System.out.println("Comparing negative literals...");
        verifyEqual(actualNegative, goldenNegative);
        System.out.println("Comparing zero literals");
        verifyEqual(actualZero, goldenZero);
    }

    private void verifyEqual(Set<IConceptLiteral> actual, Set<IConceptLiteral> golden)
    {
        System.out.printf("Actual: %s\n", actual.stream().map(Objects::toString).collect(Collectors.joining(", ")));
        System.out.printf("Golden: %s\n", golden.stream().map(Objects::toString).collect(Collectors.joining(", ")));

        Assert.assertEquals("Golden and actual are not equal", golden.size(), actual.size());
        golden.removeAll(actual);
        Assert.assertTrue("Golden and actual are not equal", golden.isEmpty());
    }

    @NotNull
    private Set<IConceptLiteral> parseConcepts(Set<String> literals)
    {
        return literals.stream()
                .map(concept -> FactoryMgr.getSymbolFactory().getLiteralForText(concept))
                .map(literal -> (IConceptLiteral) literal)
                .collect(Collectors.toSet());
    }
}