package pc1.pc2.pc3.input.antlr;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import pc1.pc2.pc3.error.ParseException;
import pc1.pc2.pc3.input.AxiomBuilder;
import pc1.pc2.pc3.input.text.visitor.ALCFileVisitor;
import pc1.pc2.pc3.om.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

public class DLParsingTest extends pc1.pc2.pc3.Test
{

    @Rule
    public TestName name = new TestName();


    @Test
    public void testParsingSingleLiterals() throws IOException
    {
        String testFile = getTestDataRoot() + File.separator + "singleLiteral.txt";
        init();
        try (InputStream in = new FileInputStream(testFile)) {
            List<IAxiom> axioms = loadFile(in);
            List<IClause> clauses = axioms.stream().map(IAxiom::getRight).collect(Collectors.toList());
            Assert.assertEquals(3, clauses.size());
            clauses.forEach(c -> Assert.assertTrue(c instanceof IAtomicClause));
            Assert.assertEquals("Incorrect", "A", ((IAtomicClause) clauses.get(0)).getLiteral().getSymbol());
            Assert.assertEquals("Incorrect", "B", ((IAtomicClause) clauses.get(1)).getLiteral().getSymbol());
            Assert.assertEquals("Incorrect", "C", ((IAtomicClause) clauses.get(2)).getLiteral().getSymbol());
        }
    }

    private List<IAxiom> loadFile(InputStream in)
    {
        alcParser parser = AxiomBuilder.initializeParser(in);
        ALCFileVisitor visitor = new ALCFileVisitor();
        return parser.file().accept(visitor);
    }

    @Test
    public void testSingleAtomicConcept() throws ParseException
    {
        IClause right = buildAxiom("TOP -> A").getRight();
        Assert.assertTrue(right instanceof IAtomicClause);
        IAtomicClause clause = (IAtomicClause) right;
        Assert.assertEquals("A", clause.getLiteral().getSymbol());
    }

    @Test
    public void testSimpleDisjunction() throws ParseException
    {
        IClause clause = buildAxiom("TOP -> A | B").getRight();
        Assert.assertTrue(clause instanceof IComplexClause);
        IComplexClause complex = (IComplexClause) clause;
        Assert.assertSame(complex.getOperator(), GroupOperator.DISJUNCTION);

        Set<IClause> children = complex.getChildren();
        Assert.assertEquals(2, children.size());
        List<IAtomicClause> atomics = children.stream()
                .map(a -> (IAtomicClause) a)
                .sorted(Comparator.comparing(a -> a.getLiteral().getSymbol()))
                .collect(Collectors.toList());
        Assert.assertEquals("A", atomics.get(0).getLiteral().getSymbol());
        Assert.assertEquals("B", atomics.get(1).getLiteral().getSymbol());
    }

    @Test
    public void testLongDisjunction() throws ParseException
    {
        IClause right = buildAxiom("TOP -> A | B  | C|D").getRight();
        Assert.assertTrue(right instanceof IComplexClause);
        IComplexClause clause = (IComplexClause) right;
        Assert.assertSame(clause.getOperator(), GroupOperator.DISJUNCTION);

        Set<IClause> children = clause.getChildren();
        Assert.assertEquals(4, children.size());
        List<IAtomicClause> atomics = children.stream()
                .map(a -> (IAtomicClause) a)
                .sorted(Comparator.comparing(a -> a.getLiteral().toString()))
                .collect(Collectors.toList());
        Assert.assertEquals("A", atomics.get(0).getLiteral().toString());
        Assert.assertEquals("B", atomics.get(1).getLiteral().toString());
        Assert.assertEquals("C", atomics.get(2).getLiteral().toString());
        Assert.assertEquals("D", atomics.get(3).getLiteral().toString());

    }

    @Test
    public void testNestedDisjunction() throws ParseException
    {
        IClause right = buildAxiom("TOP -> A | (B  | C) |D | E").getRight();
        Assert.assertTrue(right instanceof IComplexClause);
        IComplexClause clause = (IComplexClause) right;
        Assert.assertSame(clause.getOperator(), GroupOperator.DISJUNCTION);

        Set<IClause> children = clause.getChildren();
        Assert.assertEquals(5, children.size());
        List<IAtomicClause> atomics = children.stream()
                .map(a -> (IAtomicClause) a)
                .sorted(Comparator.comparing(a -> a.getLiteral().toString()))
                .collect(Collectors.toList());
        Assert.assertEquals("A", atomics.get(0).getLiteral().toString());
        Assert.assertEquals("B", atomics.get(1).getLiteral().toString());
        Assert.assertEquals("C", atomics.get(2).getLiteral().toString());
        Assert.assertEquals("D", atomics.get(3).getLiteral().toString());
        Assert.assertEquals("E", atomics.get(4).getLiteral().toString());
    }

    @Test
    public void testParsingDisjunctiveLiterals_propositional() throws IOException
    {
        String testFile = getTestDataRoot() + File.separator + "disjunctionPropositional.txt";
        try (InputStream in = new FileInputStream(testFile)) {
            List<IAxiom> axioms = loadFile(in);
            List<IClause> clauses = axioms.stream().map(IAxiom::getRight).collect(Collectors.toList());
            Assert.assertEquals(3, clauses.size());
            Assert.assertTrue(clauses.get(0) instanceof IAtomicClause);
            Assert.assertEquals("Incorrect", "A", ((IAtomicClause) clauses.get(0)).getLiteral().getSymbol());

            Assert.assertTrue(clauses.get(1) instanceof IComplexClause);
            Assert.assertSame(((IComplexClause) clauses.get(1)).getOperator(), GroupOperator.DISJUNCTION);
            Assert.assertEquals(2, ((IComplexClause) clauses.get(1)).getChildren().size());
        }
    }

    @Test
    public void testSimpleConjunction() throws ParseException
    {
        IClause right = buildAxiom("TOP -> A & B").getRight();
        Assert.assertTrue(right instanceof IComplexClause);
        IComplexClause clause = (IComplexClause) right;
        Assert.assertSame(clause.getOperator(), GroupOperator.CONJUNCTION);

        Set<IClause> children = clause.getChildren();
        Assert.assertEquals(2, children.size());
        List<IAtomicClause> atomics = children.stream()
                .map(a -> (IAtomicClause) a)
                .sorted(Comparator.comparing(a -> a.getLiteral().getSymbol()))
                .collect(Collectors.toList());
        Assert.assertEquals("A", atomics.get(0).getLiteral().getSymbol());
        Assert.assertEquals("B", atomics.get(1).getLiteral().getSymbol());
    }

    @Test
    public void testLongConjunction() throws ParseException
    {
        IClause right = buildAxiom("TOP -> A & B  & C&D").getRight();
        Assert.assertTrue(right instanceof IComplexClause);
        IComplexClause clause = (IComplexClause) right;
        Assert.assertSame(clause.getOperator(), GroupOperator.CONJUNCTION);

        Set<IClause> children = clause.getChildren();
        Assert.assertEquals(4, children.size());
        List<IAtomicClause> atomics = children.stream()
                .map(a -> (IAtomicClause) a)
                .sorted(Comparator.comparing(a -> a.getLiteral().toString()))
                .collect(Collectors.toList());
        Assert.assertEquals("A", atomics.get(0).getLiteral().toString());
        Assert.assertEquals("B", atomics.get(1).getLiteral().toString());
        Assert.assertEquals("C", atomics.get(2).getLiteral().toString());
        Assert.assertEquals("D", atomics.get(3).getLiteral().toString());
    }

    @Test
    public void testNestedConjunction() throws ParseException
    {
        IClause right = buildAxiom("TOP -> A & (B  & C) &D & E").getRight();
        Assert.assertTrue(right instanceof IComplexClause);
        IComplexClause clause = (IComplexClause) right;
        Assert.assertSame(clause.getOperator(), GroupOperator.CONJUNCTION);

        Set<IClause> children = clause.getChildren();
        Assert.assertEquals(5, children.size());
        List<IAtomicClause> atomics = children.stream()
                .map(a -> (IAtomicClause) a)
                .sorted(Comparator.comparing(a -> a.getLiteral().toString()))
                .collect(Collectors.toList());
        Assert.assertEquals("A", atomics.get(0).getLiteral().toString());
        Assert.assertEquals("B", atomics.get(1).getLiteral().toString());
        Assert.assertEquals("C", atomics.get(2).getLiteral().toString());
        Assert.assertEquals("D", atomics.get(3).getLiteral().toString());
        Assert.assertEquals("E", atomics.get(4).getLiteral().toString());
    }

    private IAxiom buildAxiom(String s) throws ParseException
    {
        return AxiomBuilder.fromString(s);
    }

    @Test
    public void testConjunctionInDisjunction() throws ParseException
    {
        IClause right = buildAxiom("TOP ->  A | (B  & C) |D | E").getRight();
        Assert.assertTrue(right instanceof IComplexClause);
        IComplexClause clause = (IComplexClause) right;
        Assert.assertSame(clause.getOperator(), GroupOperator.DISJUNCTION);

        Set<IClause> children = clause.getChildren();
        Assert.assertEquals(4, children.size());
        List<IAtomicClause> atomics = children.stream()
                .filter(a -> a instanceof IAtomicClause)
                .map(a -> (IAtomicClause) a)
                .sorted(Comparator.comparing(a -> a.getLiteral().getSymbol()))
                .collect(Collectors.toList());
        Assert.assertEquals("A", atomics.get(0).getLiteral().toString());
        Assert.assertEquals("D", atomics.get(1).getLiteral().toString());
        Assert.assertEquals("E", atomics.get(2).getLiteral().toString());
        IClause conjunction = children.stream()
                .filter(a -> a instanceof IComplexClause)
                .findFirst().orElse(null);
        Assert.assertTrue(conjunction instanceof IComplexClause);
        Assert.assertEquals(GroupOperator.CONJUNCTION, ((IComplexClause) conjunction).getOperator());
        Assert.assertEquals(2, ((IComplexClause) conjunction).getChildren().size());
        List<IAtomicClause> conAtomics = ((IComplexClause) conjunction).getChildren().stream()
                .map(a -> (IAtomicClause) a)
                .sorted(Comparator.comparing(a -> a.getLiteral().getSymbol()))
                .collect(Collectors.toList());
        Assert.assertEquals("B", conAtomics.get(0).getLiteral().getSymbol());
        Assert.assertEquals("C", conAtomics.get(1).getLiteral().getSymbol());
    }

    @Test
    public void testNegativeLiteral() throws ParseException
    {
        IClause result = AxiomBuilder.fromString("TOP -> ¬A").getRight();
        Assert.assertTrue(result instanceof IAtomicClause);
        IAtomicClause clause = (IAtomicClause) result;
        Assert.assertEquals("A", clause.getLiteral().getSymbol());
        Assert.assertTrue(clause.isNegated());
    }

    @Test
    public void testNegativeLiteralInDisjunction() throws ParseException
    {
        IClause right = buildAxiom("TOP ->  ¬A | B").getRight();
        Assert.assertTrue(right instanceof IComplexClause);
        IComplexClause clause = (IComplexClause) right;
        Assert.assertSame(clause.getOperator(), GroupOperator.DISJUNCTION);

        Set<IClause> children = clause.getChildren();
        Assert.assertEquals(2, children.size());
        List<IAtomicClause> atomics = children.stream()
                .map(a -> (IAtomicClause) a)
                .sorted(Comparator.comparing(a -> a.getLiteral().getSymbol()))
                .collect(Collectors.toList());
        Assert.assertEquals("A", atomics.get(0).getLiteral().getSymbol());
        Assert.assertTrue(atomics.get(0).isNegated());
        Assert.assertEquals("B", atomics.get(1).getLiteral().getSymbol());
    }

    @Test
    public void testNegativeLiteralInConjunction() throws ParseException
    {
        IClause right = buildAxiom("TOP ->  ¬A & B").getRight();
        Assert.assertTrue(right instanceof IComplexClause);
        IComplexClause clause = (IComplexClause) right;
        Assert.assertSame(clause.getOperator(), GroupOperator.CONJUNCTION);
        Set<IClause> children = clause.getChildren();
        Assert.assertEquals(2, children.size());
        List<IAtomicClause> atomics = children.stream()
                .map(a -> (IAtomicClause) a)
                .sorted(Comparator.comparing(a -> a.getLiteral().getSymbol()))
                .collect(Collectors.toList());
        Assert.assertEquals("A", atomics.get(0).getLiteral().getSymbol());
        Assert.assertTrue(atomics.get(0).isNegated());
        Assert.assertEquals("B", atomics.get(1).getLiteral().getSymbol());
    }

    @Test
    public void testSimpleExistentialClause() throws ParseException
    {
        IClause right = buildAxiom("TOP -> EXISTS r.A").getRight();
        Assert.assertTrue(right instanceof IQuantifiedClause);
        IQuantifiedClause clause = (IQuantifiedClause) right;
        Assert.assertSame(Quantifier.Existential, clause.getQuantifier());
        Assert.assertEquals("Incorrect role symbol", "r", clause.getRole().getSymbol());
        Assert.assertTrue("expression should be atomic", clause.getSuccessor() instanceof IAtomicClause);
        Assert.assertEquals("Incorrect successor", "A",
                ((IAtomicClause) clause.getSuccessor()).getLiteral().getSymbol());
    }

    @Test
    public void testSimpleUniversalClause() throws ParseException
    {
        IClause right = buildAxiom("TOP -> FORALL r.A").getRight();
        Assert.assertTrue(right instanceof IQuantifiedClause);
        IQuantifiedClause clause = (IQuantifiedClause) right;
        Assert.assertSame(Quantifier.Universal, clause.getQuantifier());
        Assert.assertEquals("Incorrect role symbol", "r", clause.getRole().getSymbol());
        Assert.assertTrue("expression should be atomic", clause.getSuccessor() instanceof IAtomicClause);
        Assert.assertEquals("Incorrect successor", "A",
                ((IAtomicClause) clause.getSuccessor()).getLiteral().getSymbol());
    }

    @Test
    public void testExistentialClauseWithDisjunctiveExpression() throws ParseException
    {
        IClause right = buildAxiom("TOP -> EXISTS r.(A | B)").getRight();
        Assert.assertTrue(right instanceof IQuantifiedClause);
        IQuantifiedClause clause = (IQuantifiedClause) right;
        Assert.assertSame(Quantifier.Existential, clause.getQuantifier());
        Assert.assertEquals("Incorrect role symbol", "r", clause.getRole().getSymbol());
        Assert.assertTrue("expression should be complex", clause.getSuccessor() instanceof IComplexClause);
        IComplexClause successor = (IComplexClause) clause.getSuccessor();

        Assert.assertEquals("Successor should be disjunctive clause", GroupOperator.DISJUNCTION,
                successor.getOperator());
        Set<IClause> children = successor.getChildren();

        Assert.assertEquals(2, children.size());
        List<IAtomicClause> atomics = children.stream()
                .map(a -> (IAtomicClause) a)
                .sorted(Comparator.comparing(a -> a.getLiteral().getSymbol()))
                .collect(Collectors.toList());
        Assert.assertEquals("A", atomics.get(0).getLiteral().getSymbol());
        Assert.assertEquals("B", atomics.get(1).getLiteral().getSymbol());
    }

    @Test
    public void testDisjunctiveClauseWithExistentialClauseWithinIt() throws ParseException
    {
        IClause right = buildAxiom("TOP ->  A | EXISTS r.B").getRight();
        Assert.assertTrue(right instanceof IComplexClause);
        IComplexClause clause = (IComplexClause) right;
        Assert.assertSame(clause.getOperator(), GroupOperator.DISJUNCTION);
        Set<IClause> children = clause.getChildren();
        Assert.assertEquals(2, children.size());

        Optional<IClause> first = children.stream().filter(c -> c instanceof IAtomicClause).findFirst();
        Assert.assertTrue(first.isPresent());
        IAtomicClause atomic = (IAtomicClause) first.get();
        Assert.assertEquals("Incorrect atomic", "A", atomic.getLiteral().getSymbol());

        Optional<IQuantifiedClause> first1 =
                children.stream().filter(c -> c instanceof IQuantifiedClause).map(c -> (IQuantifiedClause) c)
                        .findFirst();
        Assert.assertTrue(first1.isPresent());
        IQuantifiedClause quantified = first1.get();

        Assert.assertSame(Quantifier.Existential, quantified.getQuantifier());
        Assert.assertEquals("Incorrect role symbol", "r", quantified.getRole().getSymbol());
        Assert.assertTrue("expression should be atomic", quantified.getSuccessor() instanceof IAtomicClause);
        IAtomicClause successor = (IAtomicClause) quantified.getSuccessor();

        Assert.assertEquals("Incorrect successor", "B", successor.getLiteral().getSymbol());
    }

    @Test public void testLongQuantifiedClause() throws ParseException
    {
        String clauseStr =
                "TOP -> exists bfo_0000051.(" +
                        "forall bfo_0000051.(" +
                        "!increased-concentration|forall ro_0000052.(" +
                        "!protein|forall bfo_0000050.(" +
                        "!urine|forall ro_0002180.!abnormal))))";
        IClause right = buildAxiom(clauseStr).getRight();
        Assert.assertTrue(right instanceof IQuantifiedClause);
        IQuantifiedClause clause = (IQuantifiedClause) right;
        Assert.assertEquals("Clause quantifier is not correct", Quantifier.Existential, clause.getQuantifier());
        IClause successor = clause.getSuccessor();
        Assert.assertTrue("Successor should be a quantified clause", successor instanceof IQuantifiedClause);
        Assert.assertEquals("Successor is a forall clause", Quantifier.Universal,
                ((IQuantifiedClause) successor).getQuantifier());
        successor = ((IQuantifiedClause) successor).getSuccessor();
        Assert.assertTrue("2-level successor should be a complex clause", successor instanceof IComplexClause);
    }

    @Test public void testAtomicSubsumee() throws ParseException
    {
        String axiomStr = "A -> B";
        IAxiom axiom = buildAxiom(axiomStr);
        IClause left = axiom.getLeft();
        Assert.assertTrue("Left is not atomic", left instanceof IAtomicClause);
        Assert.assertEquals("Left is not A", "A", ((IAtomicClause) left).getLiteral().getSymbol());
    }

    @Test public void testAtomicSubsumer() throws ParseException
    {
        String axiomStr = "A -> B";
        IAxiom axiom = buildAxiom(axiomStr);
        IClause left = axiom.getRight();
        Assert.assertTrue("Right is not atomic", left instanceof IAtomicClause);
        Assert.assertEquals("Right is not B", "B", ((IAtomicClause) left).getLiteral().getSymbol());
    }

    @Test public void testConjunctiveSubsumee() throws ParseException
    {
        String axiomStr = "(A & C -> B)";
        IAxiom axiom = buildAxiom(axiomStr);
        IClause left = axiom.getLeft();
        Assert.assertTrue("Left is not complex", left instanceof IComplexClause);
        IComplexClause complex = (IComplexClause) left;
        Assert.assertEquals("Left is not conjunction", GroupOperator.CONJUNCTION, complex.getOperator());
        Assert.assertEquals("Left is not A & C", "A&C", left.toString());
    }

    @Test public void testComplexSubsumee() throws ParseException
    {
        String axiomStr = "A & (B & C) & !(E | F) -> bottom";
        IAxiom axiom = buildAxiom(axiomStr);
        IClause left = axiom.getLeft();
        Assert.assertTrue("Left is not complex", left instanceof IComplexClause);
        IComplexClause complex = (IComplexClause) left;
        Assert.assertEquals("Left is not conjunction", GroupOperator.CONJUNCTION, complex.getOperator());
        Assert.assertEquals("Left does not have 4 children", 4, complex.getChildren().size());
        Set<IClause> children = new HashSet<>(complex.getChildren());
        children.removeIf(c -> c instanceof IAtomicClause);
        IClause disjunction = children.iterator().next();
        Assert.assertTrue("Left should have a disjunctive child", disjunction instanceof IComplexClause);
        Assert.assertTrue("Disjunctive child should be negated", disjunction.isNegated());
        Assert.assertEquals("Left is not correct", "A&B&C&!(E|F)", left.toString());
    }

    @Test
    public void testComplexExpression() throws ParseException
    {
        String axiomStr = "A & EXISTS r.(B | !(C & D)) -> FORALL r.E | Exists s.(E & (F & Exists r.!(G)))";
        IAxiom axiom = buildAxiom(axiomStr);
        Assert.assertEquals("Axiom not parsed correctly",
                "A&exists r.(B|!(C&D)) -> forall r.(E|exists s.(E&F&exists r.!G))",
                axiom.toString());
    }

    @Test
    public void testSignalingByNotch1() throws ParseException
    {
        String axiomStr = "(signaling-by-notch1) -> exists located_in.NCBITaxon_9606";
        IAxiom axiom = buildAxiom(axiomStr);
        Assert.assertEquals("Axiom not parsed correctly",
                "signaling-by-notch1 -> exists located_in.NCBITaxon_9606",
                axiom.toString());
    }
}
