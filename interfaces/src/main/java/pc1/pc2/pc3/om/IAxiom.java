package pc1.pc2.pc3.om;

public interface IAxiom extends IStatement
{
    /**
     * Gets the left hand side.
     *
     * @return Head clause
     */
    IClause getLeft();

    /**
     * Gets the right hand side.
     *
     * @return Body clause
     */
    IClause getRight();

    /**
     * Gets the type of the axiom.
     *
     * @return axiom type
     */
    AxiomType getType();
}
