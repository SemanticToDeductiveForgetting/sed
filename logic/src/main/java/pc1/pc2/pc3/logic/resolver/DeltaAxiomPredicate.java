package pc1.pc2.pc3.logic.resolver;

import org.jetbrains.annotations.NotNull;
import pc1.pc2.pc3.om.IAxiom;
import pc1.pc2.pc3.om.IClause;
import pc1.pc2.pc3.om.IConceptLiteral;
import pc1.pc2.pc3.om.ILiteral;
import pc1.pc2.pc3.logic.factory.DefinerFactory;
import pc1.pc2.pc3.logic.helper.ClauseHelper;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class DeltaAxiomPredicate implements Predicate<IAxiom>
{
    private final DefinerFactory definerFactory;
    private Collection<IConceptLiteral> deltaDefiners;

    public DeltaAxiomPredicate(DefinerFactory definerFactory)
    {
        this.definerFactory = definerFactory;
    }

    @Override
    public boolean test(IAxiom axiom)
    {
        return isDeltaAxiom(axiom);
    }

    private boolean isDeltaAxiom(IAxiom axiom)
    {
        deltaDefiners = new HashSet<>();
        return isDeltaClause(axiom.getLeft()) || isDeltaClause(axiom.getRight());
    }

    private boolean isDeltaClause(IClause clause)
    {
        List<IConceptLiteral> definers = definerFactory.getDefiners();
        Collection<ILiteral> sig = clause.getSignature();
        sig.retainAll(definers);
        if (sig.size() > 1) {
            Collection<IConceptLiteral> unquantifiedDefiners =
                    ClauseHelper.getUnquantifiedConcepts(clause, definers);
            Collection<IConceptLiteral> existential = getExistentialDefiners(unquantifiedDefiners);
            unquantifiedDefiners.removeAll(existential);
            if (existential.size() >= 2 || unquantifiedDefiners.size() >= 2) {
                deltaDefiners.addAll(unquantifiedDefiners);
                deltaDefiners.addAll(existential);
                return true;
            }
        }
        return false;
    }

    @NotNull
    private Collection<IConceptLiteral> getExistentialDefiners(Collection<IConceptLiteral> definers)
    {
        return definers.stream().filter(IConceptLiteral::isExistentialDefiner).collect(Collectors.toSet());
    }

    public Collection<IConceptLiteral> getDeltaDefiners()
    {
        return deltaDefiners;
    }
}
