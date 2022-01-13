package pc1.pc2.pc3.logic.helper;

import org.junit.Assert;
import org.junit.Test;
import pc1.pc2.pc3.error.ParseException;
import pc1.pc2.pc3.om.IClause;

import java.util.*;
import java.util.stream.Collectors;

public class CnfConverterTest extends pc1.pc2.pc3.logic.Test
{
    
    @Test public void testAtomicClause() throws ParseException
    {
        verify("A", "A");
    }

    @Test public void testDisjunction() throws ParseException
    {
        verify("A | B | C", "A|B|C");
    }

    @Test public void testConjunction() throws ParseException
    {
        verify("A & B & C", "A", "B", "C");
    }

    @Test public void testDisjunctionOverConjunctionDistribution() throws ParseException
    {
        verify("A | E | (B & C)", "A|B|E", "A|C|E");
    }

    @Test public void testComplexDisjunctionOverConjunction() throws ParseException
    {
        verify("(A & B & C) | (D & E)", "A|D", "A|E", "B|D", "B|E", "C|D", "C|E");
    }

    @Test public void testDisjunctionOverConjunctionReorder() throws ParseException
    {
        verify("A | (B & C) | E", "A|B|E", "A|C|E");
    }

    @Test public void testCNFUnderRoleSymbol() throws ParseException
    {
        verify("A | B | EXISTS r.((C&D) | (E&F))", "A|B|exists r.((C|E)&(C|F)&(D|E)&(D|F))");
    }

    private void verify(String ontology, String... golden) throws ParseException
    {
        List<IClause> ont = parseClauses(ontology);
        List<String> gold = new LinkedList<>(Arrays.asList(golden));
        List<String> actual = ont.stream()
                .flatMap(c -> new CnfConverter().convert(c).stream())
                .map(IClause::toString)
                .collect(Collectors.toList());

        System.out.println("Actual:");
        actual.forEach(a -> System.out.println(String.format("\t%s", a)));
        System.out.println("Golden:");
        gold.forEach(a -> System.out.println(String.format("\t%s", a)));

        for (Iterator<String> actualIter = actual.iterator(); actualIter.hasNext(); ) {
            String act = actualIter.next();
            for (Iterator<String> goldIter = gold.iterator(); goldIter.hasNext(); ) {
                String ref = goldIter.next();
                if(ref.equalsIgnoreCase(act)) {
                    actualIter.remove();
                    goldIter.remove();
                }
            }
        }

        if(!actual.isEmpty()) {
            System.out.println("Unmatched generated clauses:");
            actual.forEach(a -> System.out.println(String.format("\t%s", a)));
        }
        if(!gold.isEmpty()) {
            System.out.println("Unmatched golden clauses:");
            gold.forEach(a -> System.out.println(String.format("\t%s", a)));
        }
        Assert.assertEquals(0, actual.size());
        Assert.assertEquals(0, gold.size());
    }
}
