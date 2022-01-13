package pc1.pc2.pc3.logic;

import org.jetbrains.annotations.NotNull;
import org.junit.Assert;
import org.junit.BeforeClass;
import pc1.pc2.pc3.app.Bootstrap;
import pc1.pc2.pc3.error.ParseException;
import pc1.pc2.pc3.logic.resolver.ClauseSubsumptionChecker;
import pc1.pc2.pc3.om.IAxiom;
import pc1.pc2.pc3.om.IClause;
import pc1.pc2.pc3.input.AxiomBuilder;

import java.util.*;
import java.util.function.BiPredicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Test
{
    @BeforeClass
    public static void init()
    {
        Bootstrap.initializeApplication();
    }

    protected List<IClause> parseClauses(String... statements) throws ParseException
    {
        List<IClause> ontology = new LinkedList<>();
        for (String statement : statements) {
            ontology.add(parseClause(statement));
        }
        return ontology;
    }

    protected List<IAxiom> parseAxioms(String... axioms) throws ParseException
    {
        List<IAxiom> ontology = new LinkedList<>();
        for (String axiom : axioms) {
            ontology.add(AxiomBuilder.fromString(axiom));
        }
        return ontology;
    }

    private IClause parseClause(String gold) throws ParseException {
        return AxiomBuilder.fromString(String.format("TOP -> %s", gold)).getRight();
    }

    protected void updateGoldenRefs(Map<String, String> definerValues, List<Replacement> replacements)
    {
        for (Replacement replacement : replacements) {
            List<VariablePosition> variablePositions = replacement.getPositions();
            if (variablePositions != null && !variablePositions.isEmpty()) {
                for (int j = 0; j < variablePositions.size(); j++) {
                    String value = definerValues.get(variablePositions.get(j).getVariable());
                    if (value != null) {
                        VariablePosition variablePosition = variablePositions.remove(j);
                        int idx = variablePosition.getIndex();
                        String golden = replacement.getGoldenRef();
                        golden = golden.substring(0, idx) + value + golden.substring(idx + 5);
                        replacement.setGoldenRef(golden);
                        replacement.getPositions().stream()
                                .filter(p -> p.getIndex() > idx)
                                .forEach(p -> p.setIndex(p.getIndex() + value.length() - 5));
                    }
                }
            }
        }
    }

    protected boolean matchWithGolden(Object statement, Replacement replacement,
                                      Map<String, String> definerValues) throws ParseException
    {
        // Replacement replacement = findReplacement(golden, replacements);
        String goldenRef = replacement.getGoldenRef();
        if (replacement.getPositions() == null || replacement.getPositions().isEmpty()) {
            if(statement instanceof IClause) {
                return verifySemantically((IClause) statement, goldenRef);
            }
        }

        return verifySyntactically(statement, replacement, definerValues, goldenRef);
    }

    protected boolean verifySyntactically(Object intClause, Replacement replacement, Map<String, String> definerValues, String goldenRef) {
        String regex = goldenRef;
        regex = escapeCharacter(regex, "(", (s, i) -> !s.startsWith("(\\w+)", i));
        regex = escapeCharacter(regex, ")", (s, i) -> i < 4 || !s.startsWith("(\\w+)", i - 4));
        regex = escapeCharacter(regex, "*", (s, i) -> true);
        Pattern p = Pattern.compile(regex.replaceAll("\\|", "\\\\|"));
        Matcher matcher = p.matcher(intClause.toString());
        if (matcher.matches()) {
            List<VariablePosition> variablePositions = replacement.getPositions();
            for (int i = 1; i <= matcher.groupCount(); i++) {
                String variable = variablePositions.get(i - 1).getVariable();
                String value = matcher.group(i);
                definerValues.put(variable, value);
            }
            return true;
        }
        return false;
    }

    private String escapeCharacter(String regex, String character, BiPredicate<String, Integer> match) {
        int idx = regex.indexOf(character);
        while(idx != -1) {
            if(match.test(regex, idx)) {
                regex = regex.substring(0, idx).concat("\\").concat(character).concat(regex.substring(idx + 1));
                idx = regex.indexOf(character, idx + 2);
            }
            else {
                idx = regex.indexOf(character, idx + 1);
            }
        }
        return regex;
    }

    protected boolean verifySemantically(IClause intClause, String goldenRef) throws ParseException {
        IClause goldenClause = parseClause(goldenRef);
        ClauseSubsumptionChecker checker = new ClauseSubsumptionChecker();
        return checker.subsumes(goldenClause, intClause) && checker.subsumes(intClause, goldenClause);
    }

    protected List<Replacement> replaceVariablesWithRegex(List<String> goldenRefs,
                                                          List<String> definerVariables)
    {
        List<Replacement> replacements = new LinkedList<>();
        for (int i = 0; i < goldenRefs.size(); i++) {
            String golden = goldenRefs.get(i);
            List<VariablePosition> positions = new LinkedList<>();
            LinkedList<String> definers = new LinkedList<>(definerVariables);
            definers.sort(Comparator.comparing(golden::indexOf));
            for (String variable : definers) {
                int idx = golden.indexOf(variable);
                while (idx != -1) {
                    golden = golden.replaceFirst(variable, Matcher.quoteReplacement("(\\w+)"));
                    positions.add(new VariablePosition(variable, idx));
                    idx = golden.indexOf(variable);
                }
            }
            goldenRefs.set(i, golden);
            replacements.add(new Replacement(golden, positions));
        }
        replacements.forEach(l -> l.getPositions().sort(Comparator.comparing(VariablePosition::getIndex)));
        return replacements;
    }

    @NotNull
    protected List<String> normalizeGoldenRefs(List<String> goldenRef) throws ParseException
    {
        List<String> goldenRefs = new LinkedList<>();
        for (String gold : goldenRef) {
            IClause goldenClause = parseClause(gold);
            goldenRefs.add(goldenClause.toString());
        }
        return goldenRefs;
    }

    protected void verifyResult(List<String> goldenRefs, List<String> definerVariables, Collection<?> interpolant)
            throws ParseException
    {
        Map<String, String> definerValues = new HashMap<>();
        goldenRefs = normalizeGoldenRefs(goldenRefs);
        List<Replacement> replacements = replaceVariablesWithRegex(goldenRefs, definerVariables);
        boolean fail;
        do {
            fail = true;
            for (Iterator<?> intIter = interpolant.iterator(); intIter.hasNext(); ) {
                Object intClause = intIter.next();
                for (Iterator<Replacement> repIter = replacements.iterator(); repIter.hasNext(); ) {
                    Replacement replacement = repIter.next();
                    if (matchWithGolden(intClause, replacement, definerValues)) {
                        intIter.remove();
                        repIter.remove();
                        fail = false;
                        updateGoldenRefs(definerValues, replacements);
                        break;
                    }
                }
            }
        } while (!fail);


        interpolant.forEach(intClause -> System.out
                .println(String.format("Interpolant contains unexpected clause %s", intClause.toString())));
        replacements.forEach(rep -> System.out.println(String.format("Could not find clause for golden ref %s",
                rep.getGoldenRef())));

        Assert.assertTrue(interpolant.isEmpty() && replacements.isEmpty());
    }

    private static class VariablePosition
    {

        private final String variable;
        private int idx;

        VariablePosition(String variable, int idx)
        {

            this.variable = variable;
            this.idx = idx;
        }

        int getIndex()
        {
            return idx;
        }

        void setIndex(int index)
        {
            idx = index;
        }

        String getVariable()
        {
            return variable;
        }
    }

    protected static class Replacement
    {
        private final List<VariablePosition> positions;
        private String goldenRef;

        private Replacement(String goldenRef, List<VariablePosition> positions)
        {
            this.goldenRef = goldenRef;
            this.positions = positions;
        }


        List<VariablePosition> getPositions()
        {
            return positions;
        }

        String getGoldenRef()
        {
            return goldenRef;
        }

        void setGoldenRef(String golden)
        {
            goldenRef = golden;
        }
    }
}
