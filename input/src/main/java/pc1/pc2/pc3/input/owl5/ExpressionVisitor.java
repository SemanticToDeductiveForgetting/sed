package pc1.pc2.pc3.input.owl5;

import org.jetbrains.annotations.NotNull;
import org.semanticweb.owlapi.model.*;
import pc1.pc2.pc3.input.owl5.parser.*;
import pc1.pc2.pc3.input.owl5.parser.*;
import pc1.pc2.pc3.om.FactoryMgr;
import pc1.pc2.pc3.om.IClause;
import pc1.pc2.pc3.om.IConceptLiteral;
import pc1.pc2.pc3.om.Quantifier;

public class ExpressionVisitor implements OWLClassExpressionVisitor
{
    private IClause expression = null;

    public IClause getExpression()
    {
        return expression;
    }

    @Override
    public void visit(@NotNull OWLObjectIntersectionOf ce)
    {
        expression = new ObjectIntersectionOfExpression(ce).parse();
    }

    @Override
    public void visit(@NotNull OWLObjectUnionOf ce)
    {
        expression = new ObjectUnionOfExpression(ce).parse();
    }

    @Override
    public void visit(@NotNull OWLObjectComplementOf ce)
    {
        expression = new ObjectComplementOfExpression(ce).parse();
    }

    @Override
    public void visit(@NotNull OWLObjectSomeValuesFrom ce)
    {
        expression = new QuantifiedObjectExpression(ce, Quantifier.Existential).parse();
    }

    @Override
    public void visit(@NotNull OWLObjectAllValuesFrom ce)
    {
        expression = new QuantifiedObjectExpression(ce, Quantifier.Universal).parse();
    }

    @Override
    public void visit(@NotNull OWLClass concept)
    {
        IRI iri = concept.getIRI();
        if (iri.isThing()) {
            expression = FactoryMgr.getCommonFactory().createAtomicClause(IConceptLiteral.TOP);
        }
        else if (iri.isNothing()) {
            expression = FactoryMgr.getCommonFactory().createAtomicClause(IConceptLiteral.TOP).negate();
        }
        else {
            String string = iri.getRemainder().orElse(iri.getIRIString());
            expression = FactoryMgr.getCommonFactory().createAtomicClause(string);
        }
    }


    @Override
    public void doDefault(Object object)
    {
        OWLClassExpression expression = (OWLClassExpression) object;
        throw new OWLParsingException(String.format("Cannot parse expression %s. " +
                "Expressions of type %s are not yet handled. ", expression, expression.getClassExpressionType()));
    }
}
