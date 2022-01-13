package pc1.pc2.pc3.om;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Predicate;

public class ComplexClause extends Clause implements IComplexClause {
    private final Set<IClause> subClauses = new LinkedHashSet<>();
    private GroupOperator operator;

    ComplexClause(GroupOperator operator) {
        this.operator = operator;
    }

    @Override
    public Set<IClause> getChildren() {
        return subClauses;
    }

    @Override
    public GroupOperator getOperator() {
        return operator;
    }

    @Override
    public void addChildren(Set<IClause> children) {
        subClauses.addAll(children);
        clauseUpdated();
    }

    @Override
    public IComplexClause addChild(IClause child) {
        subClauses.add(child);
        clauseUpdated();
        return this;
    }

    @Override
    public boolean removeChild(IClause child)
    {
        boolean remove = subClauses.remove(child);
        if (remove) {
            clauseUpdated();
        }
        return remove;
    }

    @Override
    public boolean removeChild(Predicate<IClause> removeCondition)
    {
        boolean childrenRemoved = subClauses.removeIf(removeCondition);
        if (childrenRemoved) {
            clauseUpdated();
        }
        return childrenRemoved;
    }

    @Override
    public void setOperator(GroupOperator operator) {
        this.operator = operator;
    }

    @Override public void removeAll()
    {
        subClauses.clear();
        clauseUpdated();
    }

    @Override
    public void accept(IClauseVisitor visitor) {
        visitor.visitComplex(this);
    }

    @Override
    public boolean isBottom() {
        return isNegated() ? isTopNoNegation() : isBottomNoNegation();
    }

    @Override
    public boolean isTop() {
        return isNegated() ? isBottomNoNegation() : isTopNoNegation();
    }

    private boolean isTopNoNegation() {
        if (operator == GroupOperator.DISJUNCTION) {
            return getChildren().stream().anyMatch(IClause::isTop);
        } else {
            return getChildren().stream().allMatch(IClause::isTop);
        }
    }

    private boolean isBottomNoNegation() {
        if (operator == GroupOperator.DISJUNCTION) {
            return getChildren().stream().allMatch(IClause::isBottom);
        } else {
            return getChildren().stream().anyMatch(IClause::isBottom);
        }
    }
}
