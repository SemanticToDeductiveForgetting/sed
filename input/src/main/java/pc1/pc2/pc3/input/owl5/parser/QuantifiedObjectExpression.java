package pc1.pc2.pc3.input.owl5.parser;

import org.jetbrains.annotations.NotNull;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLQuantifiedObjectRestriction;
import pc1.pc2.pc3.om.FactoryMgr;
import pc1.pc2.pc3.om.IClause;
import pc1.pc2.pc3.om.IRoleLiteral;
import pc1.pc2.pc3.om.Quantifier;

import java.util.Optional;

public class QuantifiedObjectExpression extends BaseParser
{
    private final OWLQuantifiedObjectRestriction expression;
    private final Quantifier quantifier;

    public QuantifiedObjectExpression(@NotNull OWLQuantifiedObjectRestriction expression,
                                      @NotNull Quantifier quantifier)
    {
        this.expression = expression;
        this.quantifier = quantifier;
    }

    public IClause parse()
    {
        OWLObjectPropertyExpression property = expression.getProperty();
        IRI propertyIri = property.getNamedProperty().getIRI();
        Optional<String> propertyName = propertyIri.getRemainder();
        IClause filler = parseExpression(expression.getFiller());

        if (filler != null) {
            IRoleLiteral role = (IRoleLiteral) FactoryMgr.getSymbolFactory()
                    .getLiteralForText(propertyName.orElse(propertyIri.getIRIString()));
            if(role == null) {
                role = FactoryMgr.getSymbolFactory().createRoleLiteral(propertyName.orElse(propertyIri.getIRIString()));
            }
            return FactoryMgr.getCommonFactory().createQuantifiedClause(quantifier, role, filler);
        }

        return null;
    }
}
