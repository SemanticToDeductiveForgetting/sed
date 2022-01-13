package pc1.pc2.pc3.utils;

import pc1.pc2.pc3.om.*;
import pc1.pc2.pc3.om.*;
import pc1.pc2.pc3.om.*;
import pc1.pc2.pc3.om.*;

import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ClauseStringifier implements IClauseVisitor
{
    private String rep = "";
    private final Function<String, String> formatter;

    public ClauseStringifier()
    {
        this(Function.identity());
    }

    public ClauseStringifier(Function<String, String> formatter)
    {
        this.formatter = formatter;
    }

    public String asString(IClause clause) {
        clause.accept(this);
        return rep;
    }

    @Override
    public void visitAtomic(IAtomicClause clause) {
        rep = clause.isNegated() ? "!" + clause.getLiteral().getSymbol() : clause.getLiteral().getSymbol();
    }

    @Override
    public void visitComplex(IComplexClause clause) {
        GroupOperator operator = clause.getOperator();
        List<IAtomicClause> atomics = clause.getChildren().stream()
                .filter(c -> c instanceof IAtomicClause)
                .map(c -> (IAtomicClause) c)
                .collect(Collectors.toList());
        List<IComplexClause> complex = clause.getChildren().stream()
                .filter(c -> c instanceof IComplexClause)
                .map(c -> (IComplexClause) c)
                .collect(Collectors.toList());
        List<IQuantifiedClause> quantified = clause.getChildren().stream()
                .filter(c -> c instanceof IQuantifiedClause)
                .map(c -> (IQuantifiedClause) c)
                .collect(Collectors.toList());

        atomics.sort(Comparator.comparing(c -> c.getLiteral().getSymbol()));
        complex.sort(Comparator.comparing(c -> c.getChildren().size()));
        quantified.sort(Comparator.comparing(c -> c.getRole().getSymbol()));

        String atmoicsStr = atomics.stream()
                .map(a -> new ClauseStringifier().asString(a))
                .collect(Collectors.joining(operator.toString()));

        String complexStr = complex.stream()
                .map(c -> new ClauseStringifier(s -> String.format("(%s)", s)).asString(c))
                .map(s -> String.format("%s", s))
                .collect(Collectors.joining(operator.toString()));

        String quantifiedStr = quantified.stream()
                .map(c -> new ClauseStringifier().asString(c))
                .collect(Collectors.joining(operator.toString()));

        rep = formatter.apply(Stream.of(atmoicsStr, complexStr, quantifiedStr)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.joining(String.format(" %s ", operator.toString()))));
        if(clause.isNegated()) {
            rep = "!" + rep;
        }
    }

    @Override
    public void visitQuantified(IQuantifiedClause clause) {
        IClause successor = clause.getSuccessor();
        if (successor instanceof IAtomicClause) {
            rep = String.format("%s %s.%s", clause.getQuantifier(), clause.getRole(),
                    new ClauseStringifier().asString(successor));
        } else {
            rep = String.format("%s %s.%s", clause.getQuantifier(), clause.getRole(),
                    new ClauseStringifier(s -> String.format("(%s)", s)).asString(successor));
        }
        rep = formatter.apply(rep);
        if (clause.isNegated()) {
            rep = "!" + rep;
        }
    }
}
