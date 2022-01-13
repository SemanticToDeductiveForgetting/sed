package pc1.pc2.pc3.experiment;

import org.jetbrains.annotations.NotNull;
import pc1.pc2.pc3.logic.helper.ClauseHelper;
import pc1.pc2.pc3.logic.om.ResolvableClause;
import pc1.pc2.pc3.logic.om.SignatureMgr;
import pc1.pc2.pc3.logic.resolver.MatrixResolver;
import pc1.pc2.pc3.om.IConceptLiteral;
import pc1.pc2.pc3.om.ILiteral;
import pc1.pc2.pc3.utils.ClauseStringifier;

import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.function.BiFunction;

public class VerboseMatrixResolver extends MatrixResolver
{
    private final ResolvableClauseIDMgr idMgr;
    private final BiFunction<ResolvableClause, ResolvableClauseIDMgr, String> register;
    private final Writer out;
    private final Writer restrictionWriter;
    private final List<IConceptLiteral> definers;
    private final SignatureMgr signatureMgr;
    private int resolutionOnRestrictedClause = 0;
    private int allResolutions = 0;

    public VerboseMatrixResolver(SignatureMgr signatureMgr, List<IConceptLiteral> definers, ResolvableClauseIDMgr idMgr,
                                 BiFunction<ResolvableClause, ResolvableClauseIDMgr, String> register,
                                 Writer ontologyOut, Writer restrictionWriter)
    {
        super(signatureMgr, definers);
        this.definers = definers;
        this.idMgr = idMgr;
        this.register = register;
        this.out = ontologyOut;
        this.signatureMgr = signatureMgr;
        this.restrictionWriter = restrictionWriter;
    }

    @Override public @NotNull ResolvableClause resolve(ResolvableClause first, ResolvableClause second,
                                                          ILiteral resolutionSymbol)
    {
        ResolvableClause resolvant = super.resolve(first, second, resolutionSymbol);
        String resolvantId = register.apply(resolvant, idMgr);
        String resolvantStr = new ClauseStringifier().asString(ClauseHelper.convert(resolvant));
        String operation = String.format("Res(%s, %s)", idMgr.getID(first), idMgr.getID(second));
        writeLine(String.format("%-10s,%-80s,%s", resolvantId, resolvantStr, operation));

        return resolvant;
    }

    @Override protected void tryResolve(ResolvableClause clause, ResolvableClause secondClause,
                                        List<ResolvableClause> forgettingSolution, ILiteral resolutionSymbol,
                                        boolean hasOppositeLiterals, boolean satisfyRank)
    {
        if(hasOppositeLiterals && ! satisfyRank) {
            if(hasTwoNegativeDefiners(clause) || hasTwoNegativeDefiners(secondClause)) {
                resolutionOnRestrictedClause++;
            }
            allResolutions++;
            String first = idMgr.getID(clause);
            String second = idMgr.getID(secondClause);
            writeLine(String.format("%s,%s", first, second), restrictionWriter);
        }
        super.tryResolve(clause, secondClause, forgettingSolution, resolutionSymbol, hasOppositeLiterals, satisfyRank);
    }

    private boolean hasTwoNegativeDefiners(ResolvableClause clause)
    {
        return definers.stream()
                .map(clause::getPolarityOfLiteral)
                .filter(i -> i < 0)
                .count() >= 2 ;
    }

    private void writeLine(String message, Writer writer)
    {
        try {
            writer.append(message).append('\n');
            writer.flush();
        } catch (IOException ignored) {

        }
    }

    private void writeLine(String message)
    {
        writeLine(message, out);
    }

    public int getTotalResolutions()
    {
        return allResolutions;
    }

    public int getResolutionsFromReducibleClauses()
    {
        return resolutionOnRestrictedClause;
    }
}
