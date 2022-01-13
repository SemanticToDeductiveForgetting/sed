package pc1.pc2.pc3.logic.helper;

import org.jetbrains.annotations.NotNull;
import pc1.pc2.pc3.norm.Normalizer;
import pc1.pc2.pc3.om.*;
import pc1.pc2.pc3.utils.FormulaeSimplifier;
import pc1.pc2.pc3.utils.NNFConverter;
import pc1.pc2.pc3.logic.factory.DefinerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class ClausalConverter
{
    private final DefinerFactory definerFactory;

    public ClausalConverter(DefinerFactory definerFactory)
    {
        this.definerFactory = definerFactory;
    }

    public Collection<IClause> toClausalForm(Collection<IClause> ontology,
                                             Collection<IConceptLiteral> forgettingSignature)
    {
        LinkedList<IClause> cnf = new LinkedList<>();
        Collection<IClause> expandedClauses = new LinkedList<>();
        ontology.stream()
                .flatMap(c -> new CnfConverter().convert(c).stream())
                .forEach(cnf::add);
        while (!cnf.isEmpty()) {
            IClause clause = cnf.pop();
            Collection<IClause> convesion = new ConjunctionExtractor(definerFactory).extract(clause);
            convesion.remove(clause);
            cnf.addAll(convesion);
            expandedClauses.add(clause);
        }
        if (!expandedClauses.isEmpty()) {
            for (IConceptLiteral literal : forgettingSignature) {
                DefinerExtractor definerExtractor = new DefinerExtractor(definerFactory, literal);
                expandedClauses = definerExtractor.normalise(expandedClauses);
            }
        }
        return expandedClauses;
    }

    public Collection<IClause> toClausalForm(@NotNull IAxiom axiom, Collection<IConceptLiteral> sig)
    {
        NNFConverter converter = new NNFConverter();
        IClause left = converter.negate(axiom.getLeft());
        IClause right = converter.toNNF(axiom.getRight());
        IComplexClause clause = FactoryMgr.getCommonFactory().createDisjunctiveClause();
        Normalizer.addSubClause(clause, left);
        Normalizer.addSubClause(clause, right);
        return toClausalForm(Collections.singletonList(new FormulaeSimplifier().simplify(clause)), sig);
    }

    /**
     * Converts a set of axioms to clausal form and simplifies the result
     *
     * @param ontology the axioms
     * @param sig      Structural transformation is performed relative to this signature
     * @return a set of clauses in clausal form, formulae under role restriction are transformed relative to the
     * given signature
     */
    public Collection<IClause> toClausalForm(@NotNull IOntology ontology,
                                             @NotNull Collection<IConceptLiteral> sig)
    {
        Set<IAxiom> clausalOntology = ontology.getAllActiveAxioms().stream()
                .filter(Objects::nonNull)
                .flatMap(axiom -> toClausalForm(axiom, sig).stream())
                .map(clause -> FactoryMgr.getCommonFactory()
                        .createSubsetAxiom(FactoryMgr.getCommonFactory().createAtomicClause(IConceptLiteral.TOP),
                                clause))
                .collect(Collectors.toSet());
        IOntology ont = FactoryMgr.getCommonFactory().createOntology();
        ont.setContent(clausalOntology);
        new OntologySimplifier(ont).eliminateUnitClausesAndTautologies();
        return ont.getAllActiveAxioms().stream()
                .map(IAxiom::getRight)
                .collect(Collectors.toSet());
    }
}
