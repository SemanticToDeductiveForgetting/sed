package pc1.pc2.pc3.input.owl5.parser;

import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLObjectUnionOf;
import pc1.pc2.pc3.norm.Normalizer;
import pc1.pc2.pc3.om.FactoryMgr;
import pc1.pc2.pc3.om.IClause;
import pc1.pc2.pc3.om.IComplexClause;

public class ObjectUnionOfExpression extends BaseParser
{
    private final OWLObjectUnionOf expression;

    public ObjectUnionOfExpression(OWLObjectUnionOf expression)
    {
        this.expression = expression;
    }

    public IClause parse()
    {
        IComplexClause disjunction = FactoryMgr.getCommonFactory().createDisjunctiveClause();
        for (OWLClassExpression subExpression : expression.asDisjunctSet()) {
            IClause subClause = parseExpression(subExpression);
            Normalizer.addSubClause(disjunction, subClause);
        }

        if(disjunction.getChildren().size() == 1) {
            return disjunction.getChildren().iterator().next();
        }
        return disjunction;
    }
}
