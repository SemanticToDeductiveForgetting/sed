package pc1.pc2.pc3.logic.helper;

import org.junit.Test;
import pc1.pc2.pc3.error.ParseException;
import pc1.pc2.pc3.om.IClause;
import pc1.pc2.pc3.om.IConceptLiteral;
import pc1.pc2.pc3.logic.factory.DefinerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class ClausalConverterTest extends pc1.pc2.pc3.logic.Test
{
    @Test
    public void testAtomicClause() throws ParseException
    {
        verify("A", Collections.emptyList(), "A");
    }

    @Test
    public void testDisjunction() throws ParseException
    {
        verify("A | B | C", Collections.emptyList(), "A|B|C");
    }

    @Test
    public void testConjunction() throws ParseException
    {
        verify("A & B & C", Collections.emptyList(), "A", "B", "C");
    }

    @Test
    public void testDisjunctionOverConjunctionDistribution() throws ParseException
    {
        verify("A | E | (B & C)", Collections.emptyList(), "A|B|E", "A|C|E");
    }

    @Test
    public void testComplexDisjunctionOverConjunction() throws ParseException
    {
        verify("(A & B & C) | (D & E)", Collections.emptyList(), "A|D", "A|E", "B|D", "B|E", "C|D", "C|E");
    }

    @Test
    public void testDisjunctionOverConjunctionReorder() throws ParseException
    {
        verify("A | (B & C) | E", Collections.emptyList(), "A|B|E", "A|C|E");
    }

    @Test
    public void testCNFUnderRoleSymbol() throws ParseException
    {
        verify("A | B | *EXISTS* r.((C&D) | (E&F))", Collections.singletonList("D1"), "A|B| *EXISTS* r.D1",
                "!D1 | C | E", "!D1 | C | F", "!D1 | D | E", "!D1 | D | F");
    }

    private void verify(String ontology, List<String> definers, String... golden) throws ParseException
    {
        DefinerFactory factory = new DefinerFactory();
        List<IClause> ont = parseClauses(ontology);
        List<IConceptLiteral> signature = ont.stream().flatMap(clause -> clause.getSignature().stream())
                .filter(l -> l instanceof IConceptLiteral)
                .map(l -> (IConceptLiteral) l).collect(Collectors.toList());
        List<String> gold = new LinkedList<>(Arrays.asList(golden));
        Collection<IClause> clausealForm = new ClausalConverter(factory).toClausalForm(ont, signature);

        System.out.println("Actual:");
        clausealForm.forEach(a -> System.out.printf("\t%s%n", a.toString()));
        System.out.println("Golden:");
        gold.forEach(a -> System.out.printf("\t%s%n", a));

        verifyResult(gold, definers, clausealForm);
    }
}