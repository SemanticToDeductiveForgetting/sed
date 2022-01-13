package pc1.pc2.pc3.logic.reduction;

import org.jetbrains.annotations.NotNull;
import pc1.pc2.pc3.om.*;
import pc1.pc2.pc3.utils.CollectionUtils;
import pc1.pc2.pc3.logic.helper.ClauseHelper;
import pc1.pc2.pc3.logic.helper.DefinerEliminator;
import pc1.pc2.pc3.logic.om.ResolvableClause;
import pc1.pc2.pc3.logic.om.SignatureMgr;
import pc1.pc2.pc3.logic.resolver.MatrixResolver;

import java.util.*;
import java.util.stream.Collectors;

public class Alc
{
    private final List<ResolvableClause> ontology;
    private final List<ResolvableClause> bg;
    private final List<IConceptLiteral> definers;
    private final Map<IConceptLiteral, IRoleLiteral> definerRole;

    public Alc(List<ResolvableClause> ontology, List<ResolvableClause> bg, List<IConceptLiteral> definers,
               Map<IConceptLiteral, IRoleLiteral> definerRole)
    {
        this.ontology = ontology;
        this.bg = bg;
        this.definers = definers;
        this.definerRole = definerRole;
    }

    public List<IClause> reduce()
    {
        if (!ontology.isEmpty()) {
            reduceToALC();
            return eliminateDefinerSymbols();
        }
        return Collections.emptyList();
    }

    private List<IClause> eliminateDefinerSymbols()
    {
        List<IConceptLiteral> newDefiners = new LinkedList<>();
        for (IConceptLiteral definer : definers) {
            InterpolantRangeInfo clause = executeRolePropagations(definer);
            if (clause != null) {
                ontology.add(clause.range);
                ontology.addAll(clause.additionalClauses);
                if(clause.newDefiner != null) {
                    newDefiners.add(clause.newDefiner);
                    definerRole.put(clause.newDefiner, clause.role);
                }
            }
        }
        definers.addAll(newDefiners);
        DefinerEliminator definerEliminator = createDefinerEliminator(definers);
        return definerEliminator.eliminate(ontology, bg);
    }

    @NotNull
    protected DefinerEliminator createDefinerEliminator(List<IConceptLiteral> definers)
    {
        return new DefinerEliminator(definers);
    }

    protected InterpolantRangeInfo executeRolePropagations(IConceptLiteral definer)
    {
        List<ResolvableClause> rangeClausesOfDefiner = collectClausesWithNegativeDefiner(definer);
        Set<IConceptLiteral> rangeDefiners = rangeClausesOfDefiner.stream()
                .flatMap(c -> ClauseHelper.findNegativeDefiners(c, definers))
                .collect(Collectors.toSet());
        if (rangeDefiners.size() > 1) {
            LinkedList<ResolvableClause> allClauses = CollectionUtils.merge(ontology, bg);
            Map<ResolvableClause, Integer> definersInRange = mapClausesToDefiners(allClauses, rangeDefiners);
            SignatureMgr signatureMgr = ontology.get(0).getSignatureMgr();
            MatrixResolver resolver = new MatrixResolver(signatureMgr, definers);

            ResolvableClause interpolantResolvable =
                    getInterpolantWithoutQuantifier(rangeDefiners, allClauses, signatureMgr, resolver);
            boolean existential = rangeDefiners.stream().anyMatch(IConceptLiteral::isExistentialDefiner);
            InterpolantRangeInfo interpolatedRange = getInterpolatedRange(definersInRange, existential, definerRole.get(definer));
            removeHighestRankClauses(definersInRange);
            if (interpolantResolvable.getPolarityOfLiteral(IConceptLiteral.TOP) == 0) {
                interpolantResolvable.addQuantifiedClause(interpolatedRange.range);
                interpolatedRange.range = interpolantResolvable;
                return interpolatedRange;
            }
        }
        return null;
    }

    @NotNull
    private Map<ResolvableClause, Integer> mapClausesToDefiners(LinkedList<ResolvableClause> allClauses,
                                                                Set<IConceptLiteral> rangeDefiners)
    {
        Map<ResolvableClause, Integer> definersInRangeClauses = new HashMap<>();
        for (ResolvableClause clause : allClauses) {
            Set<IConceptLiteral> definers =
                    ClauseHelper.findNegativeDefiners(clause, this.definers).collect(Collectors.toSet());
            if (!definers.isEmpty() && rangeDefiners.containsAll(definers)) {
                definersInRangeClauses.put(clause, definers.size());
            }
        }
        return definersInRangeClauses;
    }

    private void removeHighestRankClauses(Map<ResolvableClause, Integer> definersInRangeClauses)
    {
        List<ResolvableClause> purifiedClauses = new ArrayList<>(definersInRangeClauses.keySet());
        purifiedClauses.sort(Comparator.comparing(definersInRangeClauses::get).reversed());
        ResolvableClause removed = purifiedClauses.remove(0);
        Set<ResolvableClause> toPurify = purifiedClauses.stream()
                .filter(r -> definersInRangeClauses.get(r).equals(definersInRangeClauses.get(removed)))
                .collect(Collectors.toSet());
        toPurify.add(removed);
        ontology.removeAll(toPurify);
        bg.removeAll(toPurify);
    }

    @NotNull
    private ResolvableClause getInterpolantWithoutQuantifier(Set<IConceptLiteral> rangeDefiners,
                                                             LinkedList<ResolvableClause> allClauses,
                                                             SignatureMgr signatureMgr,
                                                             MatrixResolver resolver)
    {
        List<ResolvableClause> premises = new LinkedList<>();
        rangeDefiners.forEach(d -> premises.addAll(ClauseHelper.getResolvablesWithQuantifiedLiteral(allClauses, d)));
        ResolvableClause propagatedClause = premises.stream()
                .reduce(new ResolvableClause(signatureMgr, Arrays.copyOf(new int[]{0}, signatureMgr.getSize())),
                        (clause1, clause2) -> resolver.resolve(clause1, clause2, IConceptLiteral.TOP));
        propagatedClause.getQuantifiedParts().clear();
        return propagatedClause;
    }

    private InterpolantRangeInfo getInterpolatedRange(Map<ResolvableClause, Integer> definersInRangeClauses, boolean existential,
                                                      IRoleLiteral role)
    {
        if (definersInRangeClauses.size() == 1) {
            ResolvableClause range =
                    definersInRangeClauses.keySet().stream().map(ClauseHelper::clone).map(this::clearDefiners)
                            .findFirst().get();
            range.setQuantifier(existential? Quantifier.Existential : Quantifier.Universal);
            range.setRole(role);
            return new InterpolantRangeInfo(range, Collections.emptyList(), null, null);
        }
        SignatureMgr signatureMgr = definersInRangeClauses.keySet().iterator().next().getSignatureMgr();
        IConceptLiteral newDefiner = FactoryMgr.getSymbolFactory().createDefinerLiteral(existential, role.getSymbol());
        signatureMgr.add(newDefiner);
        List<ResolvableClause> additionalClauses = definersInRangeClauses.keySet().stream()
                .map(ClauseHelper::clone)
                .map(this::clearDefiners)
                .map(resolvable -> addNegativeDefiner(resolvable, newDefiner))
                .collect(Collectors.toList());
        int[] polarity = new int[signatureMgr.getSize()];
        Arrays.fill(polarity, 0);
        polarity[signatureMgr.indexOf(newDefiner)] = 1;
        ResolvableClause range = new ResolvableClause(signatureMgr, polarity);
        range.setQuantifier(existential? Quantifier.Existential : Quantifier.Universal);
        range.setRole(role);
        return new InterpolantRangeInfo(range, additionalClauses, newDefiner, role);
    }

    private ResolvableClause addNegativeDefiner(ResolvableClause resolvable, IConceptLiteral newDefiner)
    {
        resolvable.setPolarityOf(newDefiner, -1);
        return resolvable;
    }

    private ResolvableClause clearDefiners(ResolvableClause clause)
    {
        definers.forEach(d -> clause.setPolarityOf(d, 0));
        return clause;
    }

    private List<ResolvableClause> collectClausesWithNegativeDefiner(IConceptLiteral definer)
    {
        List<ResolvableClause> clauses = ClauseHelper.getResolvablesWithLiteral(ontology, definer);
        clauses.addAll(ClauseHelper.getResolvablesWithLiteral(bg, definer));
        return clauses;
    }

    private void reduceToALC()
    {
        ontology.removeIf(this::clauseContainsTwoOrMoreNegativeDefiners);
        bg.removeIf(this::clauseContainsTwoOrMoreNegativeDefiners);

        ontology.removeIf(this::clauseContainsNegativeDefinersFromDifferentRoles);
        bg.removeIf(this::clauseContainsNegativeDefinersFromDifferentRoles);
    }

    protected boolean clauseContainsTwoOrMoreNegativeDefiners(ResolvableClause resolvable)
    {
        int numberOfNegativeDefiners = 0;
        for (IConceptLiteral definer : definers) {
            if (definer.isExistentialDefiner()) {
                if (resolvable.getPolarityOfLiteral(definer) == -1) {
                    numberOfNegativeDefiners++;
                }
            }
            if (numberOfNegativeDefiners > 1) {
                return true;
            }
        }
        return false;
    }

    protected boolean clauseContainsNegativeDefinersFromDifferentRoles(ResolvableClause resolvable)
    {
        IRoleLiteral role = null;
        for (IConceptLiteral definer : definers) {
            if (resolvable.getPolarityOfLiteral(definer) == -1) {
                if (role != null && role != definerRole.get(definer)) {
                    return true;
                }
                role = definerRole.get(definer);
            }
        }
        return false;
    }

    public static class InterpolantRangeInfo
    {
        private ResolvableClause range;
        public List<ResolvableClause> additionalClauses;
        private IConceptLiteral newDefiner;
        private IRoleLiteral role;

        public InterpolantRangeInfo(ResolvableClause range, List<ResolvableClause> additionalClauses,
                                    IConceptLiteral newDefiner, IRoleLiteral role)
        {
            this.range = range;
            this.additionalClauses = additionalClauses;
            this.newDefiner = newDefiner;
            this.role = role;
        }
    }
}
