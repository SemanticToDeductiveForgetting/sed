package pc1.pc2.pc3.experiment;

import org.semanticweb.owlapi.model.OWLOntology;

import java.io.File;
import java.io.OutputStreamWriter;

public class VerboseRedundancyChecker extends RedundancyChecker
{
    private final OutputStreamWriter out;

    public VerboseRedundancyChecker(OWLOntology ontology, OWLOntology bg, File outDir, OutputStreamWriter out)
    {
        super(ontology, bg);
        this.out = out;
    }
}
