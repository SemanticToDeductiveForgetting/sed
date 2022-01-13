package pc1.pc2.pc3.experiment;

import pc1.pc2.pc3.logic.helper.DefinerEliminator;
import pc1.pc2.pc3.logic.om.ResolvableClause;
import pc1.pc2.pc3.om.IConceptLiteral;

import java.util.Collection;
import java.util.List;

public class VerboseDefinerEliminator extends DefinerEliminator
{
    private int numberOfUsedBGClauses;

    public VerboseDefinerEliminator(List<IConceptLiteral> definers)
    {
        super(definers);
    }

    @Override
    protected void moveDefinerClauseFromTheoryToOntology(List<ResolvableClause> ontology,
                                                         Collection<ResolvableClause> theory, IConceptLiteral definer)
    {
        int before = ontology.size();
        super.moveDefinerClauseFromTheoryToOntology(ontology, theory, definer);
        numberOfUsedBGClauses = ontology.size() - before;
    }

    public int getNumberOfUsedBGClauses()
    {
        return numberOfUsedBGClauses;
    }
}
