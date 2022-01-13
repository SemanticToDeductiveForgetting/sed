package pc1.pc2.pc3.input.owl5.parser;

import org.semanticweb.owlapi.model.OWLObjectComplementOf;
import pc1.pc2.pc3.norm.InPlaceShallowNegationStrategy;
import pc1.pc2.pc3.om.IClause;

public class ObjectComplementOfExpression extends BaseParser
{
    private final OWLObjectComplementOf expression;

    public ObjectComplementOfExpression(OWLObjectComplementOf expression)
    {
        this.expression = expression;
    }

    public IClause parse()
    {
        InPlaceShallowNegationStrategy strategy = new InPlaceShallowNegationStrategy();
        IClause expression = parseExpression(this.expression.getOperand());
        return strategy.negate(expression);
    }
}
