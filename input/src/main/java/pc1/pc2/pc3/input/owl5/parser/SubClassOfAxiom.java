package pc1.pc2.pc3.input.owl5.parser;

import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import pc1.pc2.pc3.om.FactoryMgr;
import pc1.pc2.pc3.om.IAxiom;
import pc1.pc2.pc3.om.IClause;
import pc1.pc2.pc3.om.ICommonFactory;

public class SubClassOfAxiom extends BaseParser {
    private final OWLSubClassOfAxiom axiom;

    public SubClassOfAxiom(OWLSubClassOfAxiom axiom) {

        this.axiom = axiom;
    }

    public IAxiom parse() {
        IClause left = parseExpression(axiom.getSubClass());
        IClause right = parseExpression(axiom.getSuperClass());
        ICommonFactory factory = FactoryMgr.getCommonFactory();

        return factory.createSubsetAxiom(left, right);
    }

}
