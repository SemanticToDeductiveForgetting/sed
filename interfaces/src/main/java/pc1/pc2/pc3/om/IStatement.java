package pc1.pc2.pc3.om;

import java.util.Collection;

public interface IStatement
{
    /**
     * Retrieves the signature of this object
     *
     * @return signature
     */
    Collection<ILiteral> getSignature();
}
