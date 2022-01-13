package pc1.pc2.pc3.logic.resolver;

import org.jetbrains.annotations.NotNull;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import pc1.pc2.pc3.error.ParseException;
import pc1.pc2.pc3.om.FactoryMgr;
import pc1.pc2.pc3.om.IAxiom;
import pc1.pc2.pc3.om.IClause;
import pc1.pc2.pc3.om.IConceptLiteral;
import pc1.pc2.pc3.logic.factory.DefinerFactory;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class StructuralTransformationTest extends pc1.pc2.pc3.logic.Test
{
    private static final String A = "A";
    private DefinerFactory factory;

    @Before
    public void setUp() {
        factory = new DefinerFactory();
    }

    @Test
    public void testPositivePolarity() throws ParseException
    {
        List<IAxiom> transform = transformAxioms("B -> EXISTS r.(A | B)");
        Assert.assertEquals("Positive definer should be extracted", 1, factory.getDefiners().size());
        verifyResult(Arrays.asList("B -> EXISTS r.D_1", "D_1 -> A | B"),
                Collections.singletonList("D_1"),
                transform);
    }

    @Test
    public void testNegativePolarity() throws ParseException {
        List<IAxiom> transform = transformAxioms("EXISTS r.(A | B) -> B");
        Assert.assertEquals("Negative definer should be extracted", 1, factory.getDefiners().size());
        verifyResult(Arrays.asList("EXISTS r.D_1 -> B", "(A | B) -> D_1"),
                Collections.singletonList("D_1"),
                transform);
    }

    @Test public void testNestedPositivePolarity() throws ParseException
    {
        List<IAxiom> transform = transformAxioms("B & EXISTS r.(C | !(B | FORALL s.A)) -> C");
        Assert.assertEquals("Expect two definers", 2, factory.getDefiners().size());
        verifyResult(Arrays.asList("B & EXISTS r.D_1 -> C", "C | !(B | FORALL s.D_2) -> D_1", "D_2 -> A"),
                Arrays.asList("D_1", "D_2"),
                transform);
    }

    @NotNull
    private List<IAxiom> transformAxioms(String... axioms) throws ParseException {
        List<IAxiom> ontology = parseAxioms(axioms);
        IConceptLiteral literal = getLiteralForText(StructuralTransformationTest.A);
        StructuralTransformation transformer = new StructuralTransformation(factory, literal);
        return ontology.stream()
                .flatMap(axiom -> transformer.apply(axiom).stream())
                .collect(Collectors.toList());
    }

    private IConceptLiteral getLiteralForText(String text) {
        return (IConceptLiteral) FactoryMgr.getSymbolFactory().getLiteralForText(text);
    }

    @Override
    protected boolean verifySemantically(IClause intClause, String goldenRef) throws ParseException {
        return false;
    }

    @Override
    protected @NotNull List<String> normalizeGoldenRefs(List<String> goldenRef) throws ParseException {
        return parseAxioms(goldenRef.toArray(new String[]{})).stream()
                .map(IAxiom::toString)
                .collect(Collectors.toList());
    }
}