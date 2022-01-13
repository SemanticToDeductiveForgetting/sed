package pc1.pc2.pc3.experiment;

import pc1.pc2.pc3.logic.om.ResolvableClause;

import java.util.HashMap;
import java.util.Map;

public class ResolvableClauseIDMgr
{
    private static final String ontologyPrefix = "O";
    private static final String bgPrefix = "BG";
    private int ontologyCounter = 1;
    private int bgCounter = 1;
    private Map<ResolvableClause, String> clauseIDMap = new HashMap<>();

    public String addOntologyClause(ResolvableClause clause)
    {
        String id = ontologyPrefix + ontologyCounter++;
        clauseIDMap.put(clause, id);
        return id;
    }

    public String addTheoryClause(ResolvableClause clause)
    {
        String id = bgPrefix + ontologyCounter++;
        clauseIDMap.put(clause, id);
        return id;
    }

    public String getID(ResolvableClause clause)
    {
        return clauseIDMap.getOrDefault(clause, "");
    }
}
