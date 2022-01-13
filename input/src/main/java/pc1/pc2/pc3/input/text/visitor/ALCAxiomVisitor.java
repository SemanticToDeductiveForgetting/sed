package pc1.pc2.pc3.input.text.visitor;

import org.antlr.v4.runtime.tree.ParseTree;
import pc1.pc2.pc3.input.antlr.alcParser;
import pc1.pc2.pc3.input.antlr.alcBaseVisitor;
import pc1.pc2.pc3.om.FactoryMgr;
import pc1.pc2.pc3.om.IAxiom;
import pc1.pc2.pc3.om.IClause;

public class ALCAxiomVisitor extends alcBaseVisitor<IAxiom>
{
    @Override
    public IAxiom visitAxiom(alcParser.AxiomContext ctx) {

        int childCount = ctx.getChildCount();
        if(childCount == 3) {
            IClause left = parseFormula(ctx.getChild(0));
            IClause right = parseFormula(ctx.getChild(2));
            if (ctx.SUBSET() != null) {
                return FactoryMgr.getCommonFactory().createSubsetAxiom(left, right);
            }
            else if (ctx.EQUIV() != null) {
                return FactoryMgr.getCommonFactory().createEquivalenceAxiom(left, right);
            }
        }
        assert false : "No implementation";
        return null;
    }

    private IClause parseFormula(ParseTree child) {
        return child.accept(new ALCFormulaVisitor());
    }
}
