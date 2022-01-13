package pc1.pc2.pc3.experiment;

import org.jetbrains.annotations.Nullable;
import pc1.pc2.pc3.logic.factory.DefinerFactory;
import pc1.pc2.pc3.logic.helper.ClauseHelper;
import pc1.pc2.pc3.logic.om.ResolvableClause;
import pc1.pc2.pc3.logic.om.SignatureMgr;
import pc1.pc2.pc3.logic.resolver.RolePropagator;
import pc1.pc2.pc3.om.IConceptLiteral;
import pc1.pc2.pc3.utils.ClauseStringifier;

import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.function.BiFunction;

public class VerboseRolePropagator extends RolePropagator
{
    private final List<ResolvableClause> ontology;
    private final List<ResolvableClause> theory;
    private final ResolvableClauseIDMgr idMgr;
    private final BiFunction<ResolvableClause, ResolvableClauseIDMgr, String> register;
    private final Writer out;

    public VerboseRolePropagator(List<ResolvableClause> ontology,
                                 List<ResolvableClause> theory,
                                 IConceptLiteral forgettingSymbol,
                                 List<IConceptLiteral> definers,
                                 DefinerFactory factory,
                                 SignatureMgr signatureMgr,
                                 ResolvableClauseIDMgr idMgr,
                                 BiFunction<ResolvableClause, ResolvableClauseIDMgr, String> register,
                                 Writer out)
    {
        super(ontology, theory, forgettingSymbol, definers, factory, signatureMgr);
        this.ontology = ontology;
        this.theory = theory;
        this.idMgr = idMgr;
        this.register = register;
        this.out = out;
    }

    @Override protected @Nullable ResolvableClause propagate(ResolvableClause clause1, ResolvableClause clause2,
                                                             IConceptLiteral definer1, IConceptLiteral definer2,
                                                             IConceptLiteral newDefiner)
    {
        ResolvableClause propagate = super.propagate(clause1, clause2, definer1, definer2, newDefiner);
        if(propagate != null) {
            String propageteStr = new ClauseStringifier().asString(ClauseHelper.convert(propagate));
            String operation = String.format("RP(%s, %s)", idMgr.getID(clause1), idMgr.getID(clause2));
            String propagateID = register.apply(propagate, idMgr);
            writeLine(String.format("%-10s,%-80s,%s", propagateID, propageteStr, operation));
        }
        return propagate;
    }

    @Override protected List<ResolvableClause> propagateNewDefiner(IConceptLiteral oldDefiner,
                                                                   List<ResolvableClause> oldDefinerClauses,
                                                                   IConceptLiteral newDefiner)
    {
        List<ResolvableClause> resolvableClauses = super.propagateNewDefiner(oldDefiner, oldDefinerClauses, newDefiner);
        for (ResolvableClause clause : resolvableClauses) {
            String id = register.apply(clause, idMgr);
            String clauseStr = new ClauseStringifier().asString(ClauseHelper.convert(clause));
            writeLine(String.format("%-10s)%-80s", id, clauseStr));
        }
        return resolvableClauses;
    }

    private void writeLine(String message)
    {
        try {
            out.append(message).append('\n');
            out.flush();
        } catch (IOException ignored) {

        }
    }
}
