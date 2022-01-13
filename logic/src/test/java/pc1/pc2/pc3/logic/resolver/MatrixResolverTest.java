package pc1.pc2.pc3.logic.resolver;

import org.junit.Assert;
import org.junit.Test;
import pc1.pc2.pc3.error.ParseException;
import pc1.pc2.pc3.om.*;
import pc1.pc2.pc3.input.AxiomBuilder;
import pc1.pc2.pc3.logic.factory.LogicFactory;
import pc1.pc2.pc3.logic.helper.ClauseHelper;
import pc1.pc2.pc3.logic.om.ResolvableClause;
import pc1.pc2.pc3.logic.om.SignatureMgr;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MatrixResolverTest extends pc1.pc2.pc3.logic.Test
{

    private static final String A = "A";

    @Test
    public void testResolveTwoUnitClausesReturnsEmptyClause() throws ParseException
    {
        ResolvableClause resolvent = resolve("TOP -> A", "TOP -> !A");
        Assert.assertTrue("Resolver should return empty clause", resolvent.isBottom());
    }

    @Test
    public void testResolveTwoUnitClausesReturnsTautologyWhenAnotherLiteralExistWithOppositePolarity()
            throws ParseException
    {
        ResolvableClause resolvent = resolve("TOP -> A | B", "TOP -> ¬A | ¬B");
        Assert.assertTrue("Resolver should return tautology", resolvent.isTautology());
    }

    @Test
    public void testResolveTwoClauses() throws ParseException
    {
        ResolvableClause resolvent = resolve("TOP -> A | B", "TOP -> ¬A | ¬C");
        Assert.assertEquals("Resolvent should not contain any A", 0, getPolarity(resolvent, "A"));
        Assert.assertEquals("Resolvent should contain positive B", 1, getPolarity(resolvent, "B"));
        Assert.assertEquals("Resolvent should contain negative C", -1, getPolarity(resolvent, "C"));
    }

    private int getPolarity(ResolvableClause resolvent, String b)
    {
        return resolvent.getPolarityOfLiteral(getConceptLiteral(b));
    }

    private IConceptLiteral getConceptLiteral(String text)
    {
        return (IConceptLiteral) FactoryMgr.getSymbolFactory().getLiteralForText(text);
    }

    @Test
    public void testResolverRemovesDuplicates() throws ParseException
    {
        ResolvableClause resolvent = resolve("TOP -> A | B | ¬C", "TOP -> ¬A | ¬C | B");
        Assert.assertEquals("Resolvent should not contain any A", 0, getPolarity(resolvent, "A"));
        Assert.assertEquals("Resolvent should contain positive B", 1, getPolarity(resolvent, "B"));
        Assert.assertEquals("Resolvent should contain negative C", -1, getPolarity(resolvent, "C"));
    }

    @Test
    public void testClauseHasQuantifier() throws ParseException
    {
        ResolvableClause resolvent = resolve("TOP -> A | B | ¬C | EXISTS r.E", "TOP -> ¬A | ¬C | B");
        Assert.assertEquals("Resolvent should not contain any A", 0, getPolarity(resolvent, "A"));
        Assert.assertEquals("Resolvent should contain positive B", 1, getPolarity(resolvent, "B"));
        Assert.assertEquals("Resolvent should contain negative C", -1, getPolarity(resolvent, "C"));
        List<ResolvableClause> quantifiedResolvables = resolvent.getQuantifiedParts();
        Assert.assertEquals("Resolvent should contain one quantified part", 1, quantifiedResolvables.size());
    }

    @Test public void testResolventIsQuantifiedClause() throws ParseException
    {
        ResolvableClause resolvent = resolve("TOP -> A | EXISTS r.E", "TOP -> ¬A");
        Assert.assertEquals("Resolvent should be an existential quantifier", Quantifier.Existential,
                resolvent.getQuantifier());
        ILiteral role = FactoryMgr.getSymbolFactory().getLiteralForText("r");
        Assert.assertEquals("Resolvent should be quantified under r", role, resolvent.getRole());
        Assert.assertEquals("Resolvent should not have any A", 0, getPolarity(resolvent, "A"));
        Assert.assertEquals("Resolvent should have one E", 1, getPolarity(resolvent, "E"));
    }

    private ResolvableClause resolve(String statement1, String statement2)
            throws ParseException
    {
        IClause clause1 = AxiomBuilder.fromString(statement1).getRight();
        IClause clause2 = AxiomBuilder.fromString(statement2).getRight();
        ILiteral resolutionLiteral = FactoryMgr.getSymbolFactory().getLiteralForText(A);
        List<ILiteral> signature = ClauseHelper
                .calculateSignature(Arrays.asList(clause1, clause2), Comparator.comparing(ILiteral::getSymbol));
        if (!signature.contains(IConceptLiteral.TOP)) {
            signature.add(0, IConceptLiteral.TOP);
        }
        SignatureMgr signatureMgr = new SignatureMgr(signature);
        MatrixResolver resolver = new MatrixResolver(signatureMgr, Collections.emptyList());
        LogicFactory factory = new LogicFactory(signatureMgr);
        return resolver
                .resolve(factory.createResolvable(clause1), factory.createResolvable(clause2), resolutionLiteral);
    }
}