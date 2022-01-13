package pc1.pc2.pc3.logic.factory;

import pc1.pc2.pc3.norm.Normalizer;
import pc1.pc2.pc3.om.*;

import java.util.*;

public class DefinerFactory {
    private Map<IConceptLiteral, IClause> definerDefinitions = new LinkedHashMap<>();
    private Map<DefinerPair, IConceptLiteral> definerHierarchy = new HashMap<>();
    private Set<IConceptLiteral> definers = new LinkedHashSet<>();
    private Map<IConceptLiteral, IRoleLiteral> definerRole = new HashMap<>();
    private Map<IConceptLiteral, Boolean> polarity = new HashMap<>();

    public IConceptLiteral createDefinerForClause(IClause successor, IRoleLiteral role,
                                                  boolean existential) {
        IConceptLiteral definer;
        definer = FactoryMgr.getSymbolFactory().createDefinerLiteral(existential, role.getSymbol());
        definerDefinitions.put(definer, successor);
        definerRole.put(definer, role);
        registerDefiner(definer);
        return definer;
    }

    private void registerDefiner(IConceptLiteral definer) {
        definers.add(definer);
    }

    public List<IConceptLiteral> getDefiners() {
        return new LinkedList<>(definers);
    }

    public IClause getDefinerDefinitionAsClause(IConceptLiteral definer) {
        IComplexClause extractedClause = FactoryMgr.getCommonFactory().createDisjunctiveClause();
        IAtomicClause definerClause = FactoryMgr.getCommonFactory().createAtomicClause(definer);
        definerClause.negate();
        Normalizer.addSubClause(extractedClause, definerClause);
        Normalizer.addSubClause(extractedClause, definerDefinitions.get(definer));

        return extractedClause;
    }

    public IAxiom getDefinerDefinitionAsAxiom(IConceptLiteral definer) {
        if (polarity.getOrDefault(definer, true)) {
            return FactoryMgr.getCommonFactory().createSubsetAxiom(FactoryMgr.getCommonFactory().createAtomicClause(definer),
                    definerDefinitions.get(definer));
        } else {
            return FactoryMgr.getCommonFactory().createSubsetAxiom(definerDefinitions.get(definer),
                    FactoryMgr.getCommonFactory().createAtomicClause(definer));
        }
    }

    public IConceptLiteral getDefinerFor(IConceptLiteral definer1, IConceptLiteral definer2) {
        DefinerPair pair = new DefinerPair(definer1, definer2);
        return definerHierarchy.get(pair);
    }

    public boolean isParentDefiner(IConceptLiteral parent, IConceptLiteral child) {
        for (Map.Entry<DefinerPair, IConceptLiteral> entry : definerHierarchy.entrySet()) {
            if (entry.getKey().hasDefiner(parent) && isParentDefiner(entry.getValue(), child)) {
                return true;
            }
        }
        return false;
    }

    public IConceptLiteral createDefinerFrom(IConceptLiteral definer1, IConceptLiteral definer2) {
        DefinerPair pair = new DefinerPair(definer1, definer2);

        IConceptLiteral newDefiner = FactoryMgr.getSymbolFactory()
                .createDefinerLiteral(definer1.isExistentialDefiner() || definer2.isExistentialDefiner(), "");

        definerHierarchy.put(pair, newDefiner);
        definers.add(newDefiner);
        definerRole.put(newDefiner, definerRole.get(definer1));
        return newDefiner;
    }

    public IRoleLiteral getRole(IConceptLiteral definer) {
        return definerRole.get(definer);
    }

    public Map<IConceptLiteral, IRoleLiteral> getDefinerRoleMap() {
        return definerRole;
    }

    public void setPolarityOfDefiner(IConceptLiteral definer, int polarity) {
        this.polarity.put(definer, polarity == 1);
    }

    private static class DefinerPair {
        IConceptLiteral definer1;
        IConceptLiteral definer2;

        private DefinerPair(IConceptLiteral definer1, IConceptLiteral definer2) {
            Comparator<IConceptLiteral> comparator = Comparator.comparing(IConceptLiteral::getSymbol);
            int compare = comparator.compare(definer1, definer2);
            if (compare <= 0) {
                this.definer1 = definer1;
                this.definer2 = definer2;
            } else {
                this.definer1 = definer2;
                this.definer2 = definer1;
            }
        }

        public boolean hasDefiner(IConceptLiteral definer) {
            return definer1 == definer || definer2 == definer;
        }

        @Override
        public int hashCode() {
            return (definer1.getSymbol() + "::" + definer2.getSymbol()).hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof DefinerPair) {
                return ((DefinerPair) obj).definer1 == definer1 && ((DefinerPair) obj).definer2 == definer2;
            }
            return false;
        }
    }
}
