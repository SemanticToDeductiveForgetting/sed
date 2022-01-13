package pc1.pc2.pc3.input.text.visitor;

import org.antlr.v4.runtime.tree.ParseTree;
import pc1.pc2.pc3.input.antlr.alcBaseVisitor;
import pc1.pc2.pc3.input.antlr.alcParser;
import pc1.pc2.pc3.norm.Normalizer;
import pc1.pc2.pc3.om.*;

public class ALCFormulaVisitor extends alcBaseVisitor<IClause>
{
    @Override
    public IClause visitFormula(alcParser.FormulaContext ctx)
    {
        int childCount = ctx.getChildCount();
        if(childCount == 1 && !ctx.getText().trim().isEmpty()) {
            if(ctx.atomic().TOP() != null) {
                return FactoryMgr.getCommonFactory().createAtomicClause(IConceptLiteral.TOP);
            }
            if(ctx.atomic().BOTTOM() != null) {
                return FactoryMgr.getCommonFactory().createAtomicClause(IConceptLiteral.TOP).negate();
            }
            return makeAtomicClause(ctx.getText());
        }
        if(childCount == 2) {
            return makeNegatedClause(ctx.getChild(1));
        }
        if(childCount == 3) {
            if(ctx.bin_connective() == null) {
                ParseTree child = ctx.getChild(1);
                return child.accept(new ALCFormulaVisitor());
            }
            else {
                return makeBinaryClause(ctx);
            }
        }
        if(childCount == 4) {
            // quantified
            boolean exists = ctx.EXISTS() != null;
            IRoleLiteral roleLiteral = null;
            IClause expression = null;
            ParseTree role = ctx.getChild(1);
            if(role instanceof alcParser.RoleContext) {
                roleLiteral = role.accept(new ALCRoleVisitor());
            }
            ParseTree form = ctx.getChild(3);
            if(form instanceof alcParser.FormulaContext) {
                expression = form.accept(new ALCFormulaVisitor());
            }
            if(expression == null) {
                expression = FactoryMgr.getCommonFactory().createAtomicClause(IConceptLiteral.TOP);
            }
            if(exists) {
                return FactoryMgr.getCommonFactory().createExistentialClause(roleLiteral, expression);
            }
            return FactoryMgr.getCommonFactory().createUniversalClause(roleLiteral, expression);
        }
        assert false : "No implementation";
        return null;
    }

    private IClause makeBinaryClause(alcParser.FormulaContext ctx)
    {
        IClause left = ctx.getChild(0).accept(new ALCFormulaVisitor());
        IClause right = ctx.getChild(2).accept(new ALCFormulaVisitor());
        IComplexClause clause = null;
        if(ctx.bin_connective().DISJ() != null) {
            clause = FactoryMgr.getCommonFactory().createDisjunctiveClause();
            Normalizer.addSubClause(clause, left);
            Normalizer.addSubClause(clause, right);
        }
        if(ctx.bin_connective().CONJ() != null) {
            clause = FactoryMgr.getCommonFactory().createConjunctiveClause();
            Normalizer.addSubClause(clause, left);
            Normalizer.addSubClause(clause, right);
        }
        assert clause != null;
        return clause;
    }

    private IClause makeNegatedClause(ParseTree child)
    {
        IClause clause = child.accept(this);
        return FactoryMgr.getCommonFactory().createShallowNegationStrategy().negate(clause);
    }

    private IAtomicClause makeAtomicClause(String text)
    {
        return FactoryMgr.getCommonFactory().createAtomicClause(text);
    }
}
