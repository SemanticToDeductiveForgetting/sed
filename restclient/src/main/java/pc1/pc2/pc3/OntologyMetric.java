package pc1.pc2.pc3;

public class OntologyMetric
{
    private long concepts;
    private long roles;
    private long maxRoleDepth;
    private boolean inAlc;

    public OntologyMetric()
    {

    }

    public long getConcepts()
    {
        return concepts;
    }

    public long getRoles()
    {
        return roles;
    }

    public long getMaxRoleDepth()
    {
        return maxRoleDepth;
    }

    public boolean isAlc()
    {
        return inAlc;
    }


    public void setConcepts(int concepts)
    {
        this.concepts = concepts;
    }

    public void setRoles(int roles)
    {
        this.roles = roles;
    }

    public void setMaxDepth(int maxDepth)
    {
        maxRoleDepth = maxDepth;
    }

    public void setAlc(boolean alc)
    {
        inAlc = alc;
    }
}
