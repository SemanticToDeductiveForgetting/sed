package pc1.pc2.pc3.logic.helper;

import org.jetbrains.annotations.NotNull;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import pc1.pc2.pc3.error.ParseException;
import pc1.pc2.pc3.om.FactoryMgr;
import pc1.pc2.pc3.om.IClause;
import pc1.pc2.pc3.om.IConceptLiteral;
import pc1.pc2.pc3.logic.factory.DefinerFactory;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;


public class DefinerExtractorTest extends pc1.pc2.pc3.logic.Test
{
    private static final String A = "A";
    private DefinerFactory factory;

    @Before
    public void setUp() {
        factory = new DefinerFactory();
    }

    @Test
    public void testExtractSimpleDefiner() throws ParseException
    {
        List<IClause> transform = transformClauses("EXISTS r.A");
        Assert.assertEquals("One symbol should be generated", 1, factory.getDefiners().size());
        verifyResult(Arrays.asList("EXISTS r.D_1", "!D_1 | A"),
                Collections.singletonList("D_1"),
                transform);
    }

    @Test
    public void testExtractDisjunctiveSuccessor() throws ParseException {
        List<IClause> transform = transformClauses("EXISTS r.(A | B)");
        Assert.assertEquals("One symbol should be generated", 1, factory.getDefiners().size());
        verifyResult(Arrays.asList("EXISTS r.D_1", "!D_1 | A | B"),
                Collections.singletonList("D_1"),
                transform);
    }

    @Test
    public void testQuantifiedClauseInsideDisjunctiveClause() throws ParseException {
        List<IClause> transform = transformClauses("C | EXISTS r.(A | B)");
        Assert.assertEquals("One symbol should be generated", 1, factory.getDefiners().size());
        verifyResult(Arrays.asList("C | EXISTS r.D_1", "!D_1 | A | B"),
                Collections.singletonList("D_1"),
                transform);
    }

    @Test
    public void testOnlySuccessorsWithForgettingLiteralsAreExtracted() throws ParseException {
        List<IClause> transform = transformClauses("EXISTS r.(D | B)");
        Assert.assertEquals("No definer symbols should be generated", 0, factory.getDefiners().size());
        verifyResult(Collections.singletonList("EXISTS r.(D | B)"),
                Collections.emptyList(),
                transform);
    }

    @Test
    public void testForgettingLiteralOutsideQuantifier() throws ParseException {
        List<IClause> transform = transformClauses("A | EXISTS r.(D | B)");
        Assert.assertEquals("No definer symbols should be generated", 0, factory.getDefiners().size());
        verifyResult(Collections.singletonList("A | EXISTS r.(D | B)"),
                Collections.emptyList(),
                transform);
    }

    @Test
    public void testReuseOfDefinerSymbols() throws ParseException {
        List<IClause> transform = transformClauses("C1 | EXISTS r.A", "C2 | FORALL s.A");
        Assert.assertEquals("Two definer symbols should be generated", 2, factory.getDefiners().size());
        verifyResult(Arrays.asList("C1 | EXISTS r.D_1", "!D_1 | A", "C2 | FORALL s.D_2", "!D_2 | A"),
                Arrays.asList("D_1", "D_2"),
                transform);
    }

    @Test
    public void testNestedDefiners() throws ParseException {
        List<IClause> transformed = transformClauses("C1 | EXISTS r.(A | EXISTS s.(!B | A))");
        Assert.assertEquals("Tow definer symbols should be generated", 2, factory.getDefiners().size());
        verifyResult(Arrays.asList("C1 | EXISTS r.D_1", "!D_1 | A | EXISTS s.D_2", "!D_2 | !B | A"),
                Arrays.asList("D_1", "D_2"),
                transformed);
    }

    @NotNull
    private List<IClause> transformClauses(String... clauses) throws ParseException {
        List<IClause> ontology = parseClauses(clauses);
        IConceptLiteral literal = getLiteralForText(DefinerExtractorTest.A);
        DefinerExtractor extractor = new DefinerExtractor(factory, literal);
        return new LinkedList<>(extractor.normalise(ontology));
    }

    private IConceptLiteral getLiteralForText(String text) {
        return (IConceptLiteral) FactoryMgr.getSymbolFactory().getLiteralForText(text);
    }
}
