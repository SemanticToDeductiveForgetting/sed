package pc1.pc2.pc3.experiment;

import org.jetbrains.annotations.NotNull;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import pc1.pc2.pc3.ExperimentHelper;
import pc1.pc2.pc3.logic.helper.ClauseHelper;
import pc1.pc2.pc3.logic.helper.DefinerEliminator;
import pc1.pc2.pc3.logic.om.ResolvableClause;
import pc1.pc2.pc3.logic.reduction.Alc;
import pc1.pc2.pc3.om.IClause;
import pc1.pc2.pc3.om.IConceptLiteral;
import pc1.pc2.pc3.om.IRoleLiteral;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class VerboseAlc extends Alc
{
    private final String name;
    private final File outDir;

    private int numberOfAddedClauses = 0;
    private int numberOfRedundantClauses = 0;
    private VerboseDefinerEliminator definerEliminator;
    private final List<ResolvableClause> filteredClauses;

    public VerboseAlc(List<ResolvableClause> ont, List<ResolvableClause> bg, List<IConceptLiteral> definers,
                      Map<IConceptLiteral, IRoleLiteral> definerRole, String name, File outDir)
    {
        super(ont, bg, definers, definerRole);
        this.name = name;
        this.outDir = outDir;
        filteredClauses = new LinkedList<>();
    }

    @Override
    public List<IClause> reduce()
    {
        long before = System.nanoTime();
        List<IClause> reduce = super.reduce();
        long after = System.nanoTime();
        logTimeStat(before, after);

        int numberOfUsedBGClauses = 0;
        if (definerEliminator != null) {
            numberOfUsedBGClauses = definerEliminator.getNumberOfUsedBGClauses();
        }
        int semanticViewSize = reduce.size() - numberOfAddedClauses - numberOfRedundantClauses +
                numberOfUsedBGClauses;
        writeSemanticViewSize(semanticViewSize);
        writeFilteredClauses();
        return reduce;
    }

    private void writeFilteredClauses()
    {
        String path = outDir.getPath();
        List<IClause> filtered = filteredClauses.stream()
                .map(ClauseHelper::convert)
                .collect(Collectors.toList());

        try {
            ExperimentHelper.save(ClauseHelper.asAxioms(filtered), path, "FilteredByReduction",
                    ExperimentHelper.SaveFormat.CLAUSAL);
        } catch (OWLOntologyCreationException | OWLOntologyStorageException | IOException e) {
            e.printStackTrace();
        }
    }

    private void logTimeStat(long before, long after)
    {
        try (Writer out = new FileWriter(outDir.getPath() + File.separator + name + "Reduction.time")) {
            ExperimentHelper.logElapsedTime(after, before, out);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeSemanticViewSize(int semanticViewSize)
    {
        String path = outDir.getPath();
        try (FileWriter writer = new FileWriter(path + File.separator + name + "Semantic.size")) {
            writer.write(String.valueOf(semanticViewSize));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected InterpolantRangeInfo executeRolePropagations(IConceptLiteral definer)
    {
        InterpolantRangeInfo info = super.executeRolePropagations(definer);
        if (info != null) {
            numberOfAddedClauses++;
            numberOfRedundantClauses += info.additionalClauses.size();
        }
        return info;
    }

    @Override protected boolean clauseContainsTwoOrMoreNegativeDefiners(ResolvableClause resolvable)
    {
        boolean b = super.clauseContainsTwoOrMoreNegativeDefiners(resolvable);
        if (b) {
            filteredClauses.add(resolvable);
        }
        return b;
    }

    @Override protected boolean clauseContainsNegativeDefinersFromDifferentRoles(ResolvableClause resolvable)
    {
        boolean b = super.clauseContainsNegativeDefinersFromDifferentRoles(resolvable);
        if (b) {
            filteredClauses.add(resolvable);
        }
        return b;
    }

    @Override
    protected @NotNull DefinerEliminator createDefinerEliminator(List<IConceptLiteral> definers)
    {
        definerEliminator = new VerboseDefinerEliminator(definers);
        return definerEliminator;
    }
}
