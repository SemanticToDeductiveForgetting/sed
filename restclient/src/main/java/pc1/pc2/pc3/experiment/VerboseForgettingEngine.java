package pc1.pc2.pc3.experiment;

import org.jetbrains.annotations.NotNull;
import pc1.pc2.pc3.ExperimentHelper;
import pc1.pc2.pc3.error.TimeException;
import pc1.pc2.pc3.logic.ForgettingEngine;
import pc1.pc2.pc3.logic.factory.DefinerFactory;
import pc1.pc2.pc3.logic.helper.ClauseHelper;
import pc1.pc2.pc3.logic.helper.OntologyPair;
import pc1.pc2.pc3.logic.om.ResolvableClause;
import pc1.pc2.pc3.logic.om.SignatureMgr;
import pc1.pc2.pc3.logic.resolver.MatrixResolver;
import pc1.pc2.pc3.logic.resolver.RankChecker;
import pc1.pc2.pc3.logic.resolver.ResolvableSubsumptionChecker;
import pc1.pc2.pc3.logic.resolver.RolePropagator;
import pc1.pc2.pc3.om.IClause;
import pc1.pc2.pc3.om.IConceptLiteral;
import pc1.pc2.pc3.utils.ClauseStringifier;

import java.io.*;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class VerboseForgettingEngine extends ForgettingEngine
{

    private final Writer out;
    private Writer totalResolutionsOut;
    private Writer resolutionsOfReducedOut;
    private Writer normalisationOut;
    private Writer restrictedBG;
    private Writer ontologyOut;
    private Writer bgOut;
    private List<ResolvableClause> ont;
    private final ResolvableClauseIDMgr idMgr;
    private Collection<IClause> ontology;
    private boolean executeResolutionRoundOnMainOntology = true;
    private final File restrictedResFile;
    private VerboseMatrixResolver resolver;
    private int totalResolutions = 0;
    private int resolutionsFromReducibleClauses = 0;

    public VerboseForgettingEngine(Writer out, File logDir, String name)
    {
        this.out = out;
        String logDirPath = logDir.getPath();
        restrictedResFile = new File(logDirPath + File.separator + name + ".restricted");
        try {
            ontologyOut = new FileWriter(new File(logDirPath + File.separator + name + "Ontology" + ".resolution"));
            bgOut = new FileWriter(new File(logDirPath + File.separator + name + "Theory" + ".resolution"));
            normalisationOut = new FileWriter(new File(logDirPath, File.separator + name + ".normalisation"));
            restrictedBG = new FileWriter(restrictedResFile);
            totalResolutionsOut = new FileWriter(new File(logDirPath, File.separator + "ResolutionCount"));
            resolutionsOfReducedOut =
                    new FileWriter(new File(logDirPath, File.separator + "ResolutionFromReducedClausesCount"));
        } catch (IOException e) {
            ontologyOut = out;
            bgOut = out;
            normalisationOut = out;
            totalResolutionsOut = out;
            resolutionsOfReducedOut = out;
        }
        idMgr = new ResolvableClauseIDMgr();
    }

    @Override
    public List<IClause> forgetClauses(Collection<IClause> ontology, Collection<IClause> backgroundTheory,
                                       List<IConceptLiteral> forgettingSignature)
    {
        this.ontology = ontology;
        try {
            return super.forgetClauses(ontology, backgroundTheory, forgettingSignature);
        } finally {
            closeStreams();
        }
    }

    @Override
    public OntologyPair forgetSemantic(Collection<IClause> ontology, Collection<IClause> backgroundTheory,
                                       List<IConceptLiteral> forgettingSignature) throws TimeException
    {
        this.ontology = ontology;
        try {
            OntologyPair ontologyPair = super.forgetSemantic(ontology, backgroundTheory, forgettingSignature);
            return ontologyPair;
        } finally {
            try {
                totalResolutionsOut.write(String.valueOf(totalResolutions));
                resolutionsOfReducedOut.write(String.valueOf(resolutionsFromReducibleClauses));
            } catch (IOException e) {
                e.printStackTrace();
            }
            closeStreams();
        }
    }

    private void closeStreams()
    {
        try {
            ontologyOut.close();
            bgOut.close();
            restrictedBG.close();
            normalisationOut.close();
            resolutionsOfReducedOut.close();
            totalResolutionsOut.close();
        } catch (IOException e) {
            ExperimentHelper.writeLine("Cannot close ontology and theory streams", out);
            ExperimentHelper.writeLine(e.getMessage(), out);
        }
    }

    @Override
    protected Collection<IClause> normalize(Collection<IClause> ontology, List<IConceptLiteral> forgettingSignature)
    {
        Collection<IClause> normalizedClauses = super.normalize(ontology, forgettingSignature);
        if (ontology == this.ontology) {
            this.ontology = normalizedClauses;
        }

        return normalizedClauses;
    }

    @Override protected List<ResolvableClause> convertToResolvable(Collection<IClause> normalisedOntology,
                                                                   SignatureMgr signatureMgr)
    {
        List<ResolvableClause> resolvableClauses = super.convertToResolvable(normalisedOntology, signatureMgr);
        if (normalisedOntology == this.ontology) {
            registerAndWriteToFile(resolvableClauses, true, ontologyOut);
        }
        else {
            registerAndWriteToFile(resolvableClauses, false, bgOut);
        }
        return resolvableClauses;
    }

    private void registerAndWriteToFile(List<ResolvableClause> resolvableClauses, boolean ontologyClause, Writer out)
    {
        for (ResolvableClause clause : resolvableClauses) {
            String id;
            if (ontologyClause) {
                id = idMgr.addOntologyClause(clause);
            }
            else {
                id = idMgr.addTheoryClause(clause);
            }
            String str = new ClauseStringifier().asString(ClauseHelper.convert(clause));
            ExperimentHelper.writeLine(String.format("%-10s,%s", id, str), out);
        }
    }

    @Override protected void forgetLiteral(List<ResolvableClause> ont, List<ResolvableClause> theory,
                                           LinkedList<ResolvableClause> interpolant, IConceptLiteral literal,
                                           SignatureMgr signatureMgr, boolean doRolePropagation,
                                           boolean restrictResolution)
    {
        this.ont = ont;
        ExperimentHelper.writeLine(String.format("Forgetting literal %s", literal), out);
        long start = System.nanoTime();
        super.forgetLiteral(ont, theory, interpolant, literal, signatureMgr, doRolePropagation, restrictResolution);
        long end = System.nanoTime();
        ExperimentHelper.logElapsedTime(end, start, out);
    }

    @Override protected @NotNull RolePropagator createRolePropagator(List<ResolvableClause> ontology,
                                                                     List<ResolvableClause> theory,
                                                                     IConceptLiteral forgettingSymbol,
                                                                     SignatureMgr signatureMgr,
                                                                     List<IConceptLiteral> definers,
                                                                     DefinerFactory definerFactory)
    {
//        if (ontology == ont) {
//            return new VerboseRolePropagator(ontology, theory, forgettingSymbol, definers, definerFactory, signatureMgr,
//                    idMgr, (clause, mgr) -> mgr.addOntologyClause(clause), ontologyOut);
//        }
//        else {
//            return new VerboseRolePropagator(ontology, theory, forgettingSymbol, definers, definerFactory, signatureMgr,
//                    idMgr, (clause, mgr) -> mgr.addTheoryClause(clause), bgOut);
//        }
        return super.createRolePropagator(ontology, theory, forgettingSymbol, signatureMgr, definers, definerFactory);
    }

    @Override protected void executeForgettingRound(List<ResolvableClause> ontology, List<ResolvableClause> theory,
                                                    LinkedList<ResolvableClause> interpolant,
                                                    IConceptLiteral forgettingSymbol, SignatureMgr signatureMgr,
                                                    RankChecker rankChecker, boolean doRolePropagation,
                                                    boolean restrictResolution)
    {
        executeResolutionRoundOnMainOntology = ont == ontology;
        super.executeForgettingRound(ontology, theory, interpolant, forgettingSymbol, signatureMgr, rankChecker,
                doRolePropagation, restrictResolution);
    }

    @Override protected void resolve(LinkedList<ResolvableClause> ont, LinkedList<ResolvableClause> theory,
                                     List<ResolvableClause> interpolant, IConceptLiteral forgettingSymbol,
                                     List<IConceptLiteral> definers, SignatureMgr signatureMgr,
                                     ResolvableSubsumptionChecker checker, RankChecker rankChecker,
                                     boolean restrictResolution)
    {
        super.resolve(ont, theory, interpolant, forgettingSymbol, definers, signatureMgr, checker, rankChecker,
                restrictResolution);
        totalResolutions += resolver.getTotalResolutions();
        resolutionsFromReducibleClauses += resolver.getResolutionsFromReducibleClauses();
    }

    @Override
    protected @NotNull MatrixResolver createResolver(List<IConceptLiteral> definers, SignatureMgr signatureMgr)
    {
        //return super.createResolver(definers, signatureMgr);
        if (executeResolutionRoundOnMainOntology) {
            resolver = new VerboseMatrixResolver(signatureMgr, definers, idMgr,
                    (clause, mgr) -> mgr.addOntologyClause(clause), ontologyOut, new CharArrayWriter());
            return resolver;
        }
        else {
            resolver = new VerboseMatrixResolver(signatureMgr, definers, idMgr,
                    (clause, mgr) -> mgr.addTheoryClause(clause), bgOut, restrictedBG);
            return resolver;
        }
    }
}
