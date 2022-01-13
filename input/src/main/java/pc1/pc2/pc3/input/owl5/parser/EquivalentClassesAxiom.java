package pc1.pc2.pc3.input.owl5.parser;

import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import pc1.pc2.pc3.om.FactoryMgr;
import pc1.pc2.pc3.om.IAxiom;
import pc1.pc2.pc3.om.IClause;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class EquivalentClassesAxiom extends BaseParser
{
    private final OWLEquivalentClassesAxiom axiom;

    public EquivalentClassesAxiom(OWLEquivalentClassesAxiom axiom)
    {
        this.axiom = axiom;
    }

    public List<IAxiom> parse()
    {
        List<OWLClassExpression> expressions = axiom.classExpressions().collect(Collectors.toList());
        if (expressions.size() == 2) {
            IClause left = parseExpression(expressions.get(0));
            IClause right = parseExpression(expressions.get(1));
            return Collections.singletonList(FactoryMgr.getCommonFactory().createEquivalenceAxiom(left, right));
        }
        else {
            System.out.println("Equivalent class with too many expressions");
            return Collections.emptyList();
        }
    }
}
