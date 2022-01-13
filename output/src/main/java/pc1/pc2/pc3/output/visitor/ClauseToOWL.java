package pc1.pc2.pc3.output.visitor;

import org.jetbrains.annotations.NotNull;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import pc1.pc2.pc3.om.*;

import java.util.stream.Stream;

public class ClauseToOWL implements IClauseVisitor
{
    private final OWLDataFactory factory;
    private OWLClassExpression expression;

    public ClauseToOWL(OWLDataFactory factory)
    {
        this.factory = factory;
    }

    @Override public void visitAtomic(IAtomicClause clause)
    {
        if (clause.getLiteral() == IConceptLiteral.TOP) {
            expression = factory.getOWLThing();
            if (clause.isNegated()) {
                expression = factory.getOWLNothing();
            }
        }
        else {
            expression = factory.getOWLClass(clause.getLiteral().getSymbol());
            if (clause.isNegated()) {
                expression = factory.getOWLObjectComplementOf(expression);
            }
        }
    }

    @Override public void visitComplex(IComplexClause clause)
    {
        Stream<OWLClassExpression> subExpressions = clause.getChildren().stream()
                .map(this::toOwl);
        if (clause.getOperator() == GroupOperator.CONJUNCTION) {
            expression = factory.getOWLObjectIntersectionOf(subExpressions);
        }
        else {
            expression = factory.getOWLObjectUnionOf(subExpressions);
        }
    }

    @Override public void visitQuantified(IQuantifiedClause clause)
    {
        OWLClassExpression successor = toOwl(clause.getSuccessor());
        OWLObjectProperty property = factory.getOWLObjectProperty(clause.getRole().getSymbol());
        if(clause.getQuantifier() == Quantifier.Existential) {
            expression = factory.getOWLObjectSomeValuesFrom(property, successor);
        }
        else {
            expression = factory.getOWLObjectAllValuesFrom(property, successor);
        }
    }

    @NotNull private OWLClassExpression toOwl(IClause clause)
    {
        ClauseToOWL cto = new ClauseToOWL(factory);
        clause.accept(cto);
        return cto.getExpression();
    }

    public OWLClassExpression getExpression()
    {
        return expression;
    }
}
