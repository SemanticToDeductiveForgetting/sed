package pc1.pc2.pc3.om;

import pc1.pc2.pc3.error.ParseException;

import java.util.Collection;

public interface ISymbolFactory
{
    String EXISTENTIAL_DEFINER_PREFIX = "__de";

    String UNIVERSAL_DEFINER_PREFIX = "__du";

    ILiteral getLiteralForText(String text);

    IConceptLiteral createConceptLiteral(String text);

    void register(ILiteral conceptLiteral);

    IRoleLiteral createRoleLiteral(String text);

    IConceptLiteral createDefinerLiteral(boolean existential, String roleName);

    void addMapping(String id, String representation);

    String normaliseSymbol(String symbol);

    void setPreferredMapping(String id, String representation) throws ParseException;

    Collection<ILiteral> getSignature();
}
