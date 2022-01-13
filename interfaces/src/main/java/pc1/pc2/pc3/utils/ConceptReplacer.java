package pc1.pc2.pc3.utils;

import pc1.pc2.pc3.norm.Normalizer;
import pc1.pc2.pc3.om.*;
import pc1.pc2.pc3.om.*;
import pc1.pc2.pc3.om.*;
import pc1.pc2.pc3.om.*;
import pc1.pc2.pc3.om.privileged.IPriviledgedAxiom;
import pc1.pc2.pc3.om.privileged.IPrivilegedQuantifiedClause;

import java.util.LinkedList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

public class ConceptReplacer implements IClauseVisitor
{
    private final IConceptLiteral replace;
    private final Supplier<? extends IClause> replaceBy;
    private boolean replacementFound;

    public ConceptReplacer(IConceptLiteral replace, Supplier<? extends IClause> replaceBy)
    {

        this.replace = replace;
        this.replaceBy = replaceBy;
    }

    @Override
    public void visitAtomic(IAtomicClause clause)
    {
        replacementFound = clause.getLiteral() == replace;
    }

    private static IClause getReplacement(IClause original, Supplier<? extends IClause> replaceBy)
    {
        IClause newChild = replaceBy.get();
        if (original.isNegated()) {
            newChild = FactoryMgr.getCommonFactory().createShallowNegationStrategy().negate(newChild);
        }
        return newChild;
    }

    public static void replaceConcept(IAxiom axiom, IConceptLiteral concept, Supplier<? extends IClause> replaceBy)
    {
        if (axiom.getLeft().getSignature().contains(concept)) {
            replaceConcept(axiom.getLeft(), axiom, concept, replaceBy, IPriviledgedAxiom::setLeft);
        }
        if (axiom.getRight().getSignature().contains(concept)) {
            replaceConcept(axiom.getRight(), axiom, concept, replaceBy, IPriviledgedAxiom::setRight);
        }
    }

    private static void replaceConcept(IClause clause, IAxiom parentAxiom, IConceptLiteral concept,
                                       Supplier<? extends IClause> replaceBy,
                                       BiConsumer<IPriviledgedAxiom, IClause> consumer)
    {
        if (clause instanceof IAtomicClause && ((IAtomicClause) clause).getLiteral() == concept) {
            if (parentAxiom instanceof IPriviledgedAxiom) {
                IClause newSide = getReplacement(clause, replaceBy);
                consumer.accept((IPriviledgedAxiom) parentAxiom, newSide);
            }
        }
        else {
            ConceptReplacer replacer = new ConceptReplacer(concept, replaceBy);
            clause.accept(replacer);
        }
    }

    @Override
    public void visitComplex(IComplexClause clause)
    {
        List<IAtomicClause> replacements = new LinkedList<>();
        for (IClause child : clause.getChildren()) {
            child.accept(this);
            if (replacementFound) {
                assert child instanceof IAtomicClause;
                replacements.add((IAtomicClause) child);
                replacementFound = false;
            }
        }

        for (IAtomicClause replacement : replacements) {
            clause.removeChild(replacement);
            IClause newChild = ConceptReplacer.getReplacement(replacement, replaceBy);
            Normalizer.addSubClause(clause, newChild);
        }
    }

    @Override
    public void visitQuantified(IQuantifiedClause clause)
    {
        clause.getSuccessor().accept(this);
        if (replacementFound) {
            if (clause instanceof IPrivilegedQuantifiedClause && clause.getSuccessor() instanceof IAtomicClause) {
                ((IPrivilegedQuantifiedClause) clause)
                        .setSuccessor(ConceptReplacer.getReplacement(clause.getSuccessor(), replaceBy));
            }
            replacementFound = false;
        }
    }
}
