package pc1.pc2.pc3.logic.helper;

import org.jetbrains.annotations.NotNull;
import pc1.pc2.pc3.logic.factory.LogicFactory;
import pc1.pc2.pc3.logic.om.ResolvableClause;
import pc1.pc2.pc3.logic.om.SignatureMgr;
import pc1.pc2.pc3.om.*;
import pc1.pc2.pc3.utils.ConceptReplacer;
import pc1.pc2.pc3.utils.UnquantifiedRestrictedSignatureCollector;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ClauseHelper
{
    private static List<IAtomicClause> collectAtomics(IClause clause)
    {
        AtomicCollector collector = new AtomicCollector();
        clause.accept(collector);
        return collector.getAtomics();
    }

    public static List<ILiteral> calculateSignature(Collection<IClause> clauses, Comparator<ILiteral> comparator)
    {
        return clauses.stream()
                .flatMap(c -> c.getSignature().stream())
                .distinct()
                .sorted(comparator)
                .collect(Collectors.toList());
    }

    private static boolean containsLiteral(IClause clause, IConceptLiteral literal)
    {
        return clause.getSignature().contains(literal);
    }

    static Collection<IClause> getClausesWithQuantifiedLiteral(Collection<IClause> ontology,
                                                               IConceptLiteral literal)
    {
        List<IClause> clauses = new LinkedList<>();
        for (IClause clause : ontology) {
            boolean clauseContainsQuantifiedLiteral = containsQuantifiedLiteral(literal, clause);
            if (clauseContainsQuantifiedLiteral) {
                clauses.add(clause);
            }
        }
        return clauses;
    }

    public static boolean containsQuantifiedLiteral(IConceptLiteral literal, IClause clause)
    {
        if (clause.getSignature().contains(literal)) {
            ClauseWithResolvableQuantifierPredicate visitor =
                    new ClauseWithResolvableQuantifierPredicate(a -> a.getLiteral() == literal);
            clause.accept(visitor);
            return visitor.isSuccessful();
        }
        return false;
    }

    public static Collection<IClause> getQuantifiedClauses(IClause parent)
    {
        return Collections.emptySet();
    }

    public static boolean containsUnQuantifiedLiteral(IConceptLiteral literal, IClause clause)
    {
        if (clause.getSignature().contains(literal)) {
            ClauseWithUnquantifiedPredicate visitor =
                    new ClauseWithUnquantifiedPredicate(a -> a.getLiteral() == literal);
            clause.accept(visitor);
            return visitor.isSuccessful();
        }
        return false;
    }

    public static Collection<IClause> getClausesWithLiteral(Collection<IClause> ontology, IConceptLiteral literal)
    {
        return ontology.stream().filter(c -> containsLiteral(c, literal)).collect(Collectors.toList());
    }

    public static ResolvableClause clone(ResolvableClause clause)
    {
        ResolvableClause newClause = new ResolvableClause(clause.getSignatureMgr(),
                Arrays.copyOf(clause.getPolarity(), clause.getSignatureMgr().getSize()));
        newClause.setQuantifier(clause.getQuantifier());
        newClause.setRole(clause.getRole());
        clause.getQuantifiedParts().forEach(c -> newClause.addQuantifiedClause(clone(c)));
        return newClause;
    }

    public static List<ResolvableClause> getResolvablesWithQuantifiedLiteral(List<ResolvableClause> ontology,
                                                                             IConceptLiteral literal)
    {
        List<ResolvableClause> resolvables = new LinkedList<>();
        for (ResolvableClause clause : ontology) {
            if (containsQuantifiedLiteral(clause, literal)) {
                resolvables.add(clause);
            }
        }
        return resolvables;
    }

    private static boolean containsQuantifiedLiteral(ResolvableClause clause, IConceptLiteral literal)
    {
        return clause.getQuantifier() != null && clause.getPolarityOfLiteral(literal) != 0 ||
                clause.getQuantifiedParts().stream().anyMatch(c -> containsQuantifiedLiteral(c, literal));
    }

    public static IConceptLiteral findFirstNegativeDefiner(ResolvableClause clause, List<IConceptLiteral> definers)
    {
        Optional<IConceptLiteral> first =
                definers.stream().filter(d -> clause.getPolarityOfLiteral(d) < 0).findFirst();

        return first.orElse(null);
    }

    public static Stream<IConceptLiteral> findNegativeDefiners(ResolvableClause clause,
                                                               List<IConceptLiteral> searchDefiners)
    {
        return searchDefiners.stream().filter(d -> clause.getPolarityOfLiteral(d) < 0);
    }

    public static List<IClause> convertToClauses(Collection<ResolvableClause> resolvables)
    {
        return resolvables.stream()
                .map(ClauseHelper::convert)
                .collect(Collectors.toCollection(LinkedList::new));
    }

    public static List<IAxiom> convertToAxioms(Collection<ResolvableClause> resolvables)
    {
        ICommonFactory fact = FactoryMgr.getCommonFactory();
        return resolvables.stream()
                .map(ClauseHelper::convert)
                .map(c -> fact.createSubsetAxiom(fact.createAtomicClause(IConceptLiteral.TOP), c))
                .collect(Collectors.toCollection(LinkedList::new));
    }

    public static List<IAxiom> asAxioms(Collection<IClause> clauses)
    {
        ICommonFactory fact = FactoryMgr.getCommonFactory();
        return clauses.stream()
                .map(c -> fact.createSubsetAxiom(fact.createAtomicClause(IConceptLiteral.TOP), c))
                .collect(Collectors.toList());
    }

    public static IClause convert(ResolvableClause clause)
    {
        return new LogicFactory(clause.getSignatureMgr()).createClause(clause);
    }

    public static ResolvableClause convert(SignatureMgr signature, IClause clause)
    {
        return new LogicFactory(signature).createResolvable(clause);
    }

    public static List<ResolvableClause> getResolvablesWithLiteral(Collection<ResolvableClause> clauses,
                                                                   IConceptLiteral literal)
    {
        return clauses.stream()
                .filter(c -> c.getPolarityOfLiteral(literal) != 0)
                .collect(Collectors.toList());
    }

    public static IClause replace(IClause clause, IConceptLiteral symbol, Supplier<IAtomicClause> replaceBy)
    {
        if (clause instanceof IAtomicClause && ((IAtomicClause) clause).getLiteral() == symbol) {
            IAtomicClause newAtomic = replaceBy.get();
            if (clause.isNegated()) {
                newAtomic.negate();
            }
            return newAtomic;
        }
        ConceptReplacer replacer = new ConceptReplacer(symbol, replaceBy);
        clause.accept(replacer);
        return clause;
    }

    @NotNull
    public static Collection<IConceptLiteral> getUnquantifiedConcepts(IClause clause, List<IConceptLiteral> concepts)
    {
        UnquantifiedRestrictedSignatureCollector collector = new UnquantifiedRestrictedSignatureCollector(concepts);
        return collector.getSignature(clause).stream().map(l -> (IConceptLiteral) l).collect(Collectors.toSet());
    }

    public static Set<IConceptLiteral> getConceptNamesInStatement(IStatement statement)
    {
        return statement.getSignature()
                .stream()
                .filter(literal -> literal instanceof IConceptLiteral)
                .map(literal -> (IConceptLiteral) literal)
                .collect(Collectors.toSet());
    }
}
