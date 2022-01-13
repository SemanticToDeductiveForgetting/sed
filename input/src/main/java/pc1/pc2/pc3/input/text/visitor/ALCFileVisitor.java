package pc1.pc2.pc3.input.text.visitor;

import pc1.pc2.pc3.input.antlr.alcBaseVisitor;
import pc1.pc2.pc3.input.antlr.alcParser;
import pc1.pc2.pc3.om.IAxiom;

import java.util.List;
import java.util.stream.Collectors;

public class ALCFileVisitor extends alcBaseVisitor<List<IAxiom>>
{
    @Override
    public List<IAxiom> visitFile(alcParser.FileContext ctx) {
        return ctx.axiom().stream()
                .map(new ALCAxiomVisitor()::visitAxiom)
                .collect(Collectors.toList());
    }
}
