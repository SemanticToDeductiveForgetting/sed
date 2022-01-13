package pc1.pc2.pc3.logic.om;

import pc1.pc2.pc3.om.IConceptLiteral;
import pc1.pc2.pc3.om.ILiteral;
import pc1.pc2.pc3.om.IRoleLiteral;
import pc1.pc2.pc3.om.Quantifier;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class ResolvableClause
{
    private SignatureMgr signatureMgr;
    private int[] polarity;
    private Quantifier quantifier = null;
    private IRoleLiteral role = null;
    private List<ResolvableClause> quantifiedParts = new LinkedList<>();

    public ResolvableClause(SignatureMgr signatureMgr, int... polarity)
    {
        this.signatureMgr = signatureMgr;
        this.polarity = polarity;
    }

    public int[] getPolarity()
    {
        return polarity;
    }

    public List<ResolvableClause> getQuantifiedParts()
    {
        return quantifiedParts;
    }

    public void setQuantifier(Quantifier quantifier)
    {
        this.quantifier = quantifier;
    }

    public void setRole(IRoleLiteral role)
    {
        this.role = role;
    }

    public void addQuantifiedClause(ResolvableClause resolvable)
    {
        quantifiedParts.add(resolvable);
    }

    public Quantifier getQuantifier()
    {
        return quantifier;
    }

    public IRoleLiteral getRole()
    {
        return role;
    }

    public int getPolarityOfLiteral(int index)
    {
        if (index >= 0) {
            return polarity[index];
        }
        return 0;
    }

    public List<ILiteral> getSignature()
    {
        return signatureMgr.getSignatureLiterals();
    }

    public int getPolarityOfLiteral(IConceptLiteral literal)
    {
        return getPolarityOfLiteral(literal, false);
    }

    public int getPolarityOfLiteral(IConceptLiteral literal, boolean includeQuantifiedParts)
    {
        int i = signatureMgr.indexOf(literal);
        int polarityOfLiteral = 0;
        if (i >= 0 && i < polarity.length) {
            polarityOfLiteral = polarity[i];
        }
        if (polarityOfLiteral == 0 && includeQuantifiedParts) {
            for (ResolvableClause quantifiedPart : quantifiedParts) {
                polarityOfLiteral += quantifiedPart.getPolarityOfLiteral(literal, includeQuantifiedParts);
                if (polarityOfLiteral != 0) {
                    return polarityOfLiteral;
                }
            }
        }
        return polarityOfLiteral;
    }

    public void setPolarityOf(IConceptLiteral literal, int value)
    {
        int index = signatureMgr.indexOf(literal);
        assert index >= 0 : String.format("Literal %s is not in signature", literal);

        if (index < polarity.length) {
            polarity[index] = value;
        }
        else {
            polarity = Arrays.copyOf(polarity, signatureMgr.getSignatureLiterals().size());
            polarity[signatureMgr.indexOf(literal)] = value;
        }
    }

    public SignatureMgr getSignatureMgr()
    {
        return signatureMgr;
    }

    public boolean isTautology()
    {
        return getPolarityOfLiteral(IConceptLiteral.TOP) == 1;
    }

    public boolean isBottom()
    {
        return getPolarityOfLiteral(IConceptLiteral.TOP) == -1 || Arrays.stream(polarity).allMatch(p -> p == 0);
    }
}
