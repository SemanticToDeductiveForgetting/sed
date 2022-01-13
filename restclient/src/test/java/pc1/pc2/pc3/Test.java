package pc1.pc2.pc3;

public class Test
{
    protected String getOutDir()
    {
        return "./build/test-output/" + getClass().getName().replace('.', '/');
    }

    protected String getBioportalOntologiesDir()
    {
        return "./ontologies/bioportal";
    }

    protected String getCookedOntologiesDir()
    {
        return "./ontologies/cookedontologies";
    }

    protected String getCoverageDir()
    {
        return "./ontologies/conceptCoverage";
    }
}
