package pc1.pc2.pc3.logic.factory;


import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import pc1.pc2.pc3.error.ParseException;
import pc1.pc2.pc3.om.*;
import pc1.pc2.pc3.om.privileged.IPrivilegedSymbolFactory;
import pc1.pc2.pc3.input.AxiomBuilder;
import pc1.pc2.pc3.logic.helper.ClauseHelper;
import pc1.pc2.pc3.logic.om.ResolvableClause;
import pc1.pc2.pc3.logic.om.SignatureMgr;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class LogicFactoryTest extends pc1.pc2.pc3.logic.Test
{
    @After
    public void tearDown()
    {
        ISymbolFactory symbolFactory = FactoryMgr.getSymbolFactory();
        ((IPrivilegedSymbolFactory) symbolFactory).reset();
    }

    @Test
    public void createAtomicResolvable() throws ParseException
    {
        ResolvableClause resolvable = createResolvable("TOP -> A");

        Assert.assertNull("Clause should not be quantified", resolvable.getQuantifier());
        Assert.assertEquals("Signature should be a single literal", 1, resolvable.getPolarity().length);
        Assert.assertEquals("Literal A should exist positively in resolvable", 1, resolvable.getPolarity()[0]);
    }

    @Test
    public void createSimpleDisjunctiveResolvable() throws ParseException
    {
        ResolvableClause resolvable = createResolvable("TOP -> A | ¬B");

        Assert.assertNull("Clause should not be quantified", resolvable.getQuantifier());
        Assert.assertEquals("Signature should be two literals", 2, resolvable.getPolarity().length);
        Assert.assertEquals("Literal A should exist positively in resolvable", 1, resolvable.getPolarity()[0]);
        Assert.assertEquals("Literal B should exist negatively in resolvable", -1, resolvable.getPolarity()[1]);
    }

    @Test
    public void testPolarityOfLiteralsNotInClauseIsZero() throws ParseException
    {
        ResolvableClause resolvable = createResolvable("TOP -> A", "B");

        Assert.assertNull("Clause should not be quantified", resolvable.getQuantifier());
        Assert.assertEquals("Signature should be two literals", 2, resolvable.getPolarity().length);
        Assert.assertEquals("Literal A should exist positively in resolvable", 1, resolvable.getPolarity()[0]);
        Assert.assertEquals("Literal B should not exist in signature", 0, resolvable.getPolarity()[1]);
    }

    @Test
    public void testSimpleTopLevelQuantifiedStatement() throws ParseException
    {
        ResolvableClause resolvable = createResolvable("TOP -> EXISTS r.A");

        Assert.assertEquals("Resolvable should be existentially quantified", Quantifier.Existential,
                resolvable.getQuantifier());
        Assert.assertEquals("Role is not set on quantified resolvable",
                FactoryMgr.getSymbolFactory().getLiteralForText("r"), resolvable.getRole());
        Assert.assertEquals("Literal A should have positive polarity", 1, resolvable.getPolarity()[0]);
    }

    @Test
    public void testNestedResolvables() throws ParseException
    {
        ResolvableClause resolvable = createResolvable("TOP -> B | EXISTS r.A");

        Assert.assertNull("Top resolvable should not be quantified", resolvable.getQuantifier());
        Assert.assertEquals("Top resolvable should not have Literal A", 0, resolvable.getPolarity()[0]);
        Assert.assertEquals("Literal B should exist positively in top resolvable", 1, resolvable.getPolarity()[1]);
        Assert.assertEquals("Top resolvable should have one quantified resolvable", 1,
                resolvable.getQuantifiedParts().size());

        ResolvableClause quantified = resolvable.getQuantifiedParts().get(0);
        Assert.assertEquals("Quantified part should be existentially quantified", Quantifier.Existential,
                quantified.getQuantifier());
        Assert.assertEquals("Role is not set on quantified resolvable",
                FactoryMgr.getSymbolFactory().getLiteralForText("r"), quantified.getRole());
        Assert.assertEquals("Literal A should have positive polarity in quantified resolvable", 1,
                quantified.getPolarity()[0]);
        Assert.assertEquals("Literal B should not exist in quantified resolvable", 0,
                quantified.getPolarity()[1]);
    }


    private ResolvableClause createResolvable(String statement, String... additionalSignature) throws ParseException
    {
        IClause clause = AxiomBuilder.fromString(statement).getRight();
        List<ILiteral> signature = ClauseHelper.calculateSignature(Collections.singletonList(clause),
                Comparator.comparing(ILiteral::getSymbol));
        for (String s : additionalSignature) {
            IConceptLiteral l = FactoryMgr.getSymbolFactory().createConceptLiteral(s);
            FactoryMgr.getSymbolFactory().register(l);
            signature.add(l);
        }
        signature.sort(Comparator.comparing(ILiteral::getSymbol));
        LogicFactory factory = new LogicFactory(new SignatureMgr(signature));
        return factory.createResolvable(clause);
    }
}
