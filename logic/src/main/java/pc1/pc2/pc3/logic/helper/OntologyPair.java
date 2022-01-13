package pc1.pc2.pc3.logic.helper;

import pc1.pc2.pc3.om.IConceptLiteral;
import pc1.pc2.pc3.logic.om.ResolvableClause;

import java.util.List;

public class OntologyPair
{
    public List<ResolvableClause> ontology;
    public List<ResolvableClause> background;
    public List<IConceptLiteral> definers;

    public OntologyPair(List<ResolvableClause> ont, List<ResolvableClause> bg, List<IConceptLiteral> definers)
    {
        ontology = ont;
        background = bg;
        this.definers = definers;
    }
}
