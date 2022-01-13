package pc1.pc2.pc3.experiment;

import org.junit.Assert;
import org.junit.Test;
import pc1.pc2.pc3.ExperimentPlanner;
import pc1.pc2.pc3.input.owl5.LoadingException;

import java.io.File;
import java.io.IOException;

public class ExperimentPlannerTest
{
    @Test
    public void testIncorrectArgs() throws IOException, LoadingException
    {
        try {
            ExperimentPlanner.main(new String[]{"Hello World!"});
        } catch (ExperimentException e) {
            Assert.assertEquals("Incorrect number of arguements. Args: Hello World!\n" +
                    "ExperimentPlanner:\n" +
                    "Select a random signature from a list of classes repository/classes.txt.\n" +
                    "The program connects to the Bioportal repository. and writes descriptor files \n" +
                    "for the ontologies that will be required for the experiment\n" +
                    "Usage: ExperimentPlanner -repo repository -outDir output-directory -sigSize signature-size " +
                    "-classes classes-file\n" +
                    "\trepository: a path to the repository that contains local copies of ontologies and the list of " +
                    "classes.\n" +
                    "\toutput-directory: a path to the directory where the descriptor files will be written\n" +
                    "\tsignature-size: size of seed signature\n" +
                    "\tclasses-file: file of forgetting concept names", e.getMessage());
        }
    }

    @Test
    public void testRunExperimentPlanner() throws LoadingException, IOException, ExperimentException
    {
        String outputDir = "./build/test-output/" + getClass().getName().replace('.', '/');
        String[] args = {"-repo", "./ontologies/bioportal", "-outDir", outputDir, "-sigSize", "5", "-classes",
                "./ontologies/classes.txt"};
        ExperimentPlanner.main(args);

        File outputDirectory = new File(outputDir);
        Assert.assertNotEquals(0, outputDirectory.list().length);
    }
}
