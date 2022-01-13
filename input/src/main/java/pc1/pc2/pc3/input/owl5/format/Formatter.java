package pc1.pc2.pc3.input.owl5.format;

import org.semanticweb.owlapi.model.OWLAxiom;

import java.util.function.Function;

public enum Formatter
{
    Nnf(OWLAxiom::getNNF),
    Default(Function.identity());

    private Function<OWLAxiom, OWLAxiom> axiomFormatter;

    Formatter(Function<OWLAxiom, OWLAxiom> axiomFormatter)
    {
        this.axiomFormatter = axiomFormatter;
    }

    public OWLAxiom format(OWLAxiom axiom)
    {
        return axiomFormatter.apply(axiom);
    }
}
