package pc1.pc2.pc3.experiment;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import pc1.pc2.pc3.ArgsHelper;
import pc1.pc2.pc3.rest.OntologyData;
import pc1.pc2.pc3.app.Bootstrap;
import pc1.pc2.pc3.input.owl5.OWLHelper;
import pc1.pc2.pc3.om.IAxiom;
import pc1.pc2.pc3.om.IClause;
import pc1.pc2.pc3.om.IComplexClause;

import java.io.File;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class OntologyLoaderTest
{
    @BeforeClass public static void init()
    {
        Bootstrap.initializeApplication();
    }

    @Test
    public void testLoadEFO() throws OWLOntologyCreationException
    {
        File signatureFile = new File("./src/test/resources/uk/ac/man/experiment/OntologyLoaderTest/signature");
        File ontologyFile = new File("./ontologies/EFO");
        OntologyLoader loader = new OntologyLoader();
        OntologyData data = new OntologyData("EFO", IRI.create(ontologyFile));
        OWLOntology owlOntology = loader.loadOntology(data.getIRI(), null, OWLManager.createOWLOntologyManager());
        Set<IRI> signature = ArgsHelper.parseSignatureFileArg(signatureFile.getPath(), "").stream()
                .filter(s -> s.getIRI("EFO") != null)
                .map(s -> s.getIRI("EFO"))
                .map(IRI::create)
                .collect(Collectors.toSet());
        OWLOntology module = OWLHelper.extractModule(owlOntology, IRI.create("module"), signature);
        List<IAxiom> axioms = loader.parseOntologyAsClauses(module);
        for (IAxiom axiom : axioms) {
            IClause clause = axiom.getRight();
            if (clause instanceof IComplexClause) {
                Set<IClause> children = ((IComplexClause) clause).getChildren();
                for (IClause child : children) {
                    Assert.assertNotNull(child);
                }
            }
        }
    }
}
