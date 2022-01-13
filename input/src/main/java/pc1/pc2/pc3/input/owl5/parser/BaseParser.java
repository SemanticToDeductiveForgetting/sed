package pc1.pc2.pc3.input.owl5.parser;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import pc1.pc2.pc3.input.owl5.AxiomVisitor;
import pc1.pc2.pc3.input.owl5.ExpressionVisitor;
import pc1.pc2.pc3.om.IAxiom;
import pc1.pc2.pc3.om.IClause;

import java.util.stream.Stream;

class BaseParser
{
    IClause parseExpression(OWLClassExpression subClass)
    {
        ExpressionVisitor visitor = new ExpressionVisitor();
        subClass.accept(visitor);
        return visitor.getExpression();
    }

    Stream<IAxiom> parseAxiom(OWLAxiom axiom)
    {
        AxiomVisitor visitor = new AxiomVisitor();
        axiom.accept(visitor);
        return visitor.getAxioms().stream();
    }
}
