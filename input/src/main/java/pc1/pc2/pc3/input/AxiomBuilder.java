package pc1.pc2.pc3.input;

import org.antlr.v4.runtime.CommonTokenFactory;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.UnbufferedCharStream;
import pc1.pc2.pc3.input.antlr.alcLexer;
import pc1.pc2.pc3.input.antlr.alcParser;
import pc1.pc2.pc3.input.text.visitor.ALCAxiomVisitor;
import pc1.pc2.pc3.error.ParseException;
import pc1.pc2.pc3.om.IAxiom;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static pc1.pc2.pc3.utils.StringUtils.toByteArray;

public class AxiomBuilder
{
    public static IAxiom fromString(String str) throws ParseException
    {
        try (ByteArrayInputStream in = new ByteArrayInputStream(toByteArray(str))) {
            alcParser parser = initializeParser(in);
            ALCAxiomVisitor visitor = new ALCAxiomVisitor();
            return parser.axiom().accept(visitor);
        } catch (IOException e) {
            throw new ParseException("Cannot parse formula " + str, e);
        }
    }

    public static alcParser initializeParser(InputStream in)
    {
        UnbufferedCharStream charStream = new UnbufferedCharStream(in);
        alcLexer lexer = new alcLexer(charStream);
        lexer.setTokenFactory(new CommonTokenFactory(true));
        TokenStream tokenStream = new CommonTokenStream(lexer);
        return new alcParser(tokenStream);
    }
}
