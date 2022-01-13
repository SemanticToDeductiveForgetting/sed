package pc1.pc2.pc3.experiment;

import pc1.pc2.pc3.ExperimentHelper;
import pc1.pc2.pc3.logic.factory.DefinerFactory;
import pc1.pc2.pc3.logic.helper.DefinerExtractor;
import pc1.pc2.pc3.om.IClause;
import pc1.pc2.pc3.om.IConceptLiteral;

import java.io.Writer;
import java.util.List;

public class VerboseDefinerExtractor extends DefinerExtractor
{
    private final Writer out;

    public VerboseDefinerExtractor(DefinerFactory definerFactory, IConceptLiteral definer,
                                   Writer normalisationOut)
    {
        super(definerFactory, definer);
        out = normalisationOut;
    }

    @Override protected List<IClause> normalizeClause(IClause clause)
    {
        ExperimentHelper.writeLine(clause.toString(), out);
        List<IClause> clauses = super.normalizeClause(clause);
        if(!clauses.isEmpty()) {
            ExperimentHelper.writeLine(String.format("\t%s", clause.toString()), out);
            clauses.forEach(c -> ExperimentHelper.writeLine(String.format("\t%s", c.toString()), out));
        }
        return clauses;
    }
}
