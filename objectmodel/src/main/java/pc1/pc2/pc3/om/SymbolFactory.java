package pc1.pc2.pc3.om;

import pc1.pc2.pc3.error.ParseException;
import pc1.pc2.pc3.om.privileged.IPrivilegedSymbolFactory;

import java.util.*;
import java.util.function.Function;

public class SymbolFactory implements ISymbolFactory, IPrivilegedSymbolFactory
{
    private final Map<String, ILiteral> createdLiterals = new HashMap<>();
    private int definerSuffix = 0;

    private final Map<String, List<String>> mappings = new HashMap<>();
    private final Map<String, String> preferredMapping = new HashMap<>();
    private final Map<String, String> reversePreferredMapping = new HashMap<>();

    @Override
    public ILiteral getLiteralForText(String text)
    {
        String value = getMapping(text);
        return createdLiterals.get(value.toLowerCase());
    }

    @Override
    public IConceptLiteral createConceptLiteral(String text)
    {
        String value = getMapping(text);
        ConceptLiteral conceptLiteral = new ConceptLiteral(value, false, false);
        register(conceptLiteral);
        return conceptLiteral;
    }

    private String getMapping(String id)
    {
        if (preferredMapping.containsKey(id)) {
            return preferredMapping.get(id);
        }
        List<String> values = mappings.get(id);
        if (values == null || values.isEmpty()) {
            return id;
        }
        values.sort(Comparator.comparing(Function.identity()));
        return values.get(0);
    }

    @Override
    public void register(ILiteral conceptLiteral)
    {
        createdLiterals.put(conceptLiteral.getSymbol().toLowerCase(), conceptLiteral);
    }

    @Override
    public IRoleLiteral createRoleLiteral(String text)
    {
        String value = getMapping(text);
        RoleLiteral literal = new RoleLiteral(value);
        register(literal);
        return literal;
    }

    @Override
    public IConceptLiteral createDefinerLiteral(boolean existential, String roleName)
    {
        final String prefix = existential ? EXISTENTIAL_DEFINER_PREFIX : UNIVERSAL_DEFINER_PREFIX;
        definerSuffix++;
        String definerName = prefix + (roleName.isEmpty() ? "" : "_" + roleName + "_") + definerSuffix;
        ILiteral literal = getLiteralForText(definerName);
        if (literal == null) {
            IConceptLiteral definer = new ConceptLiteral(definerName, true, existential);
            register(definer);
            return definer;
        }
        return createDefinerLiteral(existential, roleName);
    }

    @Override
    public void addMapping(String id, String representation)
    {
        if (!representation.isBlank()) {
            List<String> values = mappings.getOrDefault(id, new ArrayList<>());
            values.add(representation);
            mappings.put(id, values);
        }
    }

    @Override
    public String normaliseSymbol(String symbol)
    {
        return symbol.toLowerCase().replaceAll("[\\s\\(\\)]", "-").replaceAll("[^a-zA-Z0-9_-]", "");
    }

    @Override
    public void setPreferredMapping(String id, String representation) throws ParseException
    {
        if (!representation.isBlank()) {
            String registeredID = reversePreferredMapping.get(representation);
            if (registeredID != null && !registeredID.equals(id)) {
                throw new ParseException(
                        String.format("Pref Label \"%s\" already exists for \"%s\" but now also found for " +
                                "\"%s\"", representation, registeredID, id));
            }
            preferredMapping.put(id, representation);
            reversePreferredMapping.put(representation, id);
        }
    }

    @Override
    public Collection<ILiteral> getSignature()
    {
        return createdLiterals.values();
    }

    @Override
    public void reset()
    {
        createdLiterals.clear();
        mappings.clear();
        preferredMapping.clear();
    }
}
