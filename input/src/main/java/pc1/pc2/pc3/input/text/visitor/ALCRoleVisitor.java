package pc1.pc2.pc3.input.text.visitor;

import pc1.pc2.pc3.input.antlr.alcBaseVisitor;
import pc1.pc2.pc3.input.antlr.alcParser;
import pc1.pc2.pc3.error.ParseException;
import pc1.pc2.pc3.om.FactoryMgr;
import pc1.pc2.pc3.om.ILiteral;
import pc1.pc2.pc3.om.IRoleLiteral;

public class ALCRoleVisitor extends alcBaseVisitor<IRoleLiteral>
{

    @Override
    public IRoleLiteral visitRole(alcParser.RoleContext ctx)
    {
        ILiteral literal = FactoryMgr.getSymbolFactory().getLiteralForText(ctx.getText());
        if (literal instanceof IRoleLiteral) {
            return (IRoleLiteral) literal;
        }
        if (literal != null) {
            throw new RuntimeException(new ParseException(String.format("Symbol %s is already used and cannot be used as a role symbol.", ctx.getText()), null));
        }

        return FactoryMgr.getSymbolFactory().createRoleLiteral(ctx.getText());
    }
}
