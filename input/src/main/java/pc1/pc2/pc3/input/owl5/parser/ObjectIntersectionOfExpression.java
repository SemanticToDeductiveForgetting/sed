package pc1.pc2.pc3.input.owl5.parser;

import org.semanticweb.owlapi.model.OWLClassExpression;
import pc1.pc2.pc3.om.FactoryMgr;
import pc1.pc2.pc3.om.IClause;
import pc1.pc2.pc3.om.IComplexClause;

import java.util.Set;

public class ObjectIntersectionOfExpression extends BaseParser
{

    private OWLClassExpression expression;

    public ObjectIntersectionOfExpression(OWLClassExpression expression)
    {
        this.expression = expression;
    }

    public IClause parse()
    {
        Set<OWLClassExpression> conjunctSet = expression.asConjunctSet();
        if(conjunctSet.size() <= 1) {
            return parseExpression(conjunctSet.iterator().next());
        }
        else {
            IComplexClause conjunction = FactoryMgr.getCommonFactory().createConjunctiveClause();
            for (OWLClassExpression subExpression : conjunctSet) {
                conjunction.addChild(parseExpression(subExpression));
            }
            return conjunction;
        }
    }
}
