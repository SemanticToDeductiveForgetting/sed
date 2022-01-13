package pc1.pc2.pc3.logic.resolver;

import pc1.pc2.pc3.logic.om.ResolvableClause;

import java.util.Arrays;
import java.util.List;

public class ResolvableSubsumptionChecker
{
    private List<ResolvableClause> ontology;
    private List<ResolvableClause> theory;
    private List<ResolvableClause> interpolant;

    public ResolvableSubsumptionChecker(List<ResolvableClause> ontology, List<ResolvableClause> theory,
                                        List<ResolvableClause> interpolant)
    {
        this.ontology = ontology;
        this.theory = theory;
        this.interpolant = interpolant;
    }

    public void deleteSubsumed(List<ResolvableClause> clauses)
    {
        int i = 0;
        while (i < clauses.size()) {
            ResolvableClause clause = clauses.get(i);
            if (deleteSubsumed(clause, ontology) != null) {
                clauses.remove(i);
            }
            else if (deleteSubsumed(clause, interpolant) != null) {
                clauses.remove(i);
            }
            else if (deleteSubsumed(clause, theory) != null) {
                clauses.remove(i);
            }
            else {
                i++;
            }
        }
    }

    private ResolvableClause deleteSubsumed(ResolvableClause clause, List<ResolvableClause> ontology)
    {
        int i = 0;
        while (i < ontology.size()) {
            if (clause != ontology.get(i) && subsumes(clause, ontology.get(i))) {
                ontology.remove(i);
            }
            else if (clause != ontology.get(i) && subsumes(ontology.get(i), clause)) {
                return clause;
            }
            else {
                i++;
            }
        }
        return null;
    }

    private boolean subsumes(ResolvableClause subsumer, ResolvableClause subsumee)
    {
        if (subsumer.getQuantifier() == null && subsumer.getQuantifiedParts().isEmpty()) {
            return checkPropositionalSubsumption(subsumer, subsumee);
        }
        return false;
    }

    private boolean checkPropositionalSubsumption(ResolvableClause subsumer, ResolvableClause subsumee)
    {
        int[] firstPolarity = subsumer.getPolarity();
        int[] secondPolarity = subsumee.getPolarity();

        if (firstPolarity.length <= secondPolarity.length) {
            return subsumedPolarity(firstPolarity, secondPolarity);
        }
        else {
            return subsumedPolarity(firstPolarity, Arrays.copyOf(secondPolarity, firstPolarity.length));
        }
    }

    private boolean subsumedPolarity(int[] firstPolarity, int[] secondPolarity)
    {
        boolean subsumed = true;
        for (int literalIndex = 0; subsumed && literalIndex < firstPolarity.length; literalIndex++) {
            subsumed = (firstPolarity[literalIndex] == secondPolarity[literalIndex])
                    || (firstPolarity[literalIndex] == 0);
        }
        return subsumed;
    }
}
