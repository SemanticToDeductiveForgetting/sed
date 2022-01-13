package pc1.pc2.pc3.logic.helper;

import pc1.pc2.pc3.logic.factory.DefinerFactory;
import pc1.pc2.pc3.om.*;
import pc1.pc2.pc3.om.privileged.IPrivilegedQuantifiedClause;

import java.util.*;

public class DefinerExtractor implements IClauseVisitor
{
    private DefinerFactory definerFactory;
    private final IConceptLiteral forgettingLiteral;
    private List<IConceptLiteral> createdDefiners;
    private Map<IConceptLiteral, Integer> definerPolarity;
    private int polarity;

    public DefinerExtractor(DefinerFactory definerFactory, IConceptLiteral forgettingLiteral)
    {
        this(definerFactory, forgettingLiteral, 1);
    }

    public DefinerExtractor(DefinerFactory definerFactory, IConceptLiteral forgettingLiteral, int polarity)
    {
        this.definerFactory = definerFactory;
        this.forgettingLiteral = forgettingLiteral;
        createdDefiners = new LinkedList<>();
        definerPolarity = new HashMap<>();
        this.polarity = polarity;
    }

    public Collection<IClause> normalise(Collection<IClause> ontology)
    {
        List<IClause> normalisedOntology = new LinkedList<>(ontology);
        Collection<IClause> clauses = ClauseHelper.getClausesWithQuantifiedLiteral(ontology, forgettingLiteral);
        for (IClause clause : clauses) {
            normalisedOntology.addAll(normalizeClause(clause));
            createdDefiners.clear();
        }
        return normalisedOntology;
    }

    protected List<IClause> normalizeClause(IClause clause)
    {
        List<IClause> normalisation = new LinkedList<>();
        clause.accept(this);
        for (IConceptLiteral definer : createdDefiners) {
            IClause definition = definerFactory.getDefinerDefinitionAsClause(definer);
            assert definition != null : "Cannot find definition clause for definer " + definer.getSymbol();
            normalisation.add(definition);
        }
        return normalisation;
    }

    @Override
    public void visitAtomic(IAtomicClause clause)
    {
    }

    @Override
    public void visitComplex(IComplexClause clause)
    {
        for (IClause child : clause.getChildren()) {
            polarity *= clause.isNegated() ? -1 : 1;
            child.accept(this);
            polarity *= clause.isNegated() ? -1 : 1;
        }
    }

    @Override
    public void visitQuantified(IQuantifiedClause clause)
    {
        if (ClauseHelper.containsQuantifiedLiteral(forgettingLiteral, clause)) {
            polarity *= clause.isNegated() ? -1 : 1;
            IClause successor = clause.getSuccessor();
            IConceptLiteral definer = null; //definerFactory.getDefinerForSuccessor(successor);
            definer = definerFactory.createDefinerForClause(successor, clause.getRole(),
                    clause.getQuantifier() == Quantifier.Existential);
            createdDefiners.add(definer);
            definerFactory.setPolarityOfDefiner(definer, polarity);
            if (clause instanceof IPrivilegedQuantifiedClause) {
                ((IPrivilegedQuantifiedClause) clause)
                        .setSuccessor(FactoryMgr.getCommonFactory().createAtomicClause(definer));
            }
            successor.accept(this);
            polarity *= clause.isNegated() ? -1 : 1;
        }
//        IQuantifiedClause oldParent = parent;
//        parent = clause;
//        clause.getSuccessor().accept(this);
//        if (literalFound) {
//            // replace successor with definer
//            IConceptLiteral definer = definerFactory.getDefinerForSuccessor(clause.getSuccessor());
//            if (definer == null) {
//                definer = definerFactory.createDefinerForClause(clause.getSuccessor(), clause.getRole(),
//                        clause.getQuantifier() == Quantifier.Existential);
//                createdDefiners.add(definer);
//            }
//            if (clause instanceof IPrivilegedQuantifiedClause) {
//                ((IPrivilegedQuantifiedClause) clause)
//                        .setSuccessor(FactoryMgr.getCommonFactory().createAtomicClause(definer));
//            }
//        }
//        literalFound = false;
//        parent = oldParent;
    }
}
