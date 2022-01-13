package pc1.pc2.pc3.experiment;

import org.junit.Assert;
import pc1.pc2.pc3.ArgsHelper;
import pc1.pc2.pc3.OfflineExperimentPlanner;
import pc1.pc2.pc3.Test;
import pc1.pc2.pc3.rest.OntologyData;
import pc1.pc2.pc3.rest.SymbolData;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class OfflineExperimentPlannerTest extends Test
{
    @org.junit.Test
    public void testIncompleteParameters() throws IOException
    {
        try {
            OfflineExperimentPlanner.main(new String[]{"Hello World!"});
        } catch (ExperimentException e) {
            Assert.assertEquals(
                    "Incorrect number of arguements. Args: Hello World!\n" +
                            "OfflineExperimentPlanner\n" +
                            "Plans experiment based on estimated number of forgetting symbols in the axioms\n" +
                            "Usage: OfflineExperimentPlanner -ont ontology -outDir ouput-directory -coverage " +
                            "coverage-rate -coverageData coverage-file\n" +
                            "\tont: Ontology file.\n" +
                            "\toutDir: Output directory where files will be written, The directory should be a valid " +
                            "existing directory.\n" +
                            "\tcoverage: Required coverage (percentage e.g. 150) of forgetting symbols.\n" +
                            "\tcoverageData: A coverage file which contains the coverage of the concept symbols in " +
                            "the ontology\n",
                    e.getMessage());
        }
    }

    @org.junit.Test
    public void testExperimentPlanning() throws IOException, ExperimentException
    {
        String[] args = {"-ont", getCookedOntologiesDir() + File.separator + "experiment1",
                "-outDir", getOutDir(),
                "-coverage", "50",
                "-coverageData", getCoverageDir() + File.separator + "experiment1_conceptCoverage.csv"};

        OfflineExperimentPlanner.main(args);

        OntologyData ontologyData = ArgsHelper.parseOntologyDescriptorFileArg(
                getOutDir() + File.separator + "experiment1.descriptor",
                "Could not read ontology descriptor file");

        List<SymbolData> signature = ArgsHelper.parseSignatureFileArg(getOutDir() + File.separator + "signature",
                "Could not read signature file");

        Assert.assertEquals("experiment1", ontologyData.getName());
        Assert.assertEquals(2, signature.size());
    }
}
