package pc1.pc2.pc3.om;

import java.util.Set;
import java.util.function.Predicate;

public interface IComplexClause extends IClause
{
    Set<IClause> getChildren();
    GroupOperator getOperator();
    void addChildren(Set<IClause> children);
    IComplexClause addChild(IClause child);
    boolean removeChild(IClause child);
    boolean removeChild(Predicate<IClause> removeCondition);

    void setOperator(GroupOperator operator);

    void removeAll();
}
