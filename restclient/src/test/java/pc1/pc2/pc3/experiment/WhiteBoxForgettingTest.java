package pc1.pc2.pc3.experiment;

import org.junit.Assert;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import pc1.pc2.pc3.OfflineDataLoader;
import pc1.pc2.pc3.OfflineExperimentPlanner;
import pc1.pc2.pc3.Test;
import pc1.pc2.pc3.WhiteBoxForgetting;
import pc1.pc2.pc3.error.TimeException;
import pc1.pc2.pc3.utils.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.Collections;

public class WhiteBoxForgettingTest extends Test
{
    @org.junit.Test
    public void testPipelineWithOfflineExperiment()
            throws IOException, ExperimentException, OWLOntologyCreationException, OWLOntologyStorageException,
            TimeException
    {
        String ontology = getCookedOntologiesDir() + File.separator + "experiment1";
        String[] plannerArgs = {"-ont", ontology, "-outDir", getOutDir(), "-coverage", "50",
                "-coverageData", getCoverageDir() + File.separator + "experiment1_conceptCoverage.csv"};
        OfflineExperimentPlanner.main(plannerArgs);

        String signature = getOutDir() + File.separator + "signature";
        ontology = getOutDir() + File.separator + "experiment1.descriptor";
        String[] loaderArgs = {"-ont", ontology, "-outDir", getOutDir(), "-sig", signature};
        OfflineDataLoader.main(loaderArgs);

        String bg = getOutDir() + File.separator + "bg.clausal";
        ontology = getOutDir() + File.separator + "Ontology.clausal";
        FileUtils.writeFile(bg, Collections.emptyList());
        String[] forgettingArgs = {"-bg", bg, "-outDir", getOutDir(), "-sig", signature, "-ont", ontology, "-semantic"};
        WhiteBoxForgetting.main(forgettingArgs);

        String view = getOutDir() + File.separator + "GlassBoxSemanticView.clausal";
        Assert.assertEquals(375, FileUtils.loadFile(new File(view)).size());
    }
}
