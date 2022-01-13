package pc1.pc2.pc3;

import org.semanticweb.owlapi.model.OWLOntology;
import scala.collection.Iterator;
import scala.collection.JavaConversions;
import uk.ac.man.cs.lethe.internal.dl.datatypes.DLStatement;
import uk.ac.man.cs.lethe.internal.dl.datatypes.Ontology;
import uk.ac.man.cs.lethe.internal.dl.datatypes.TBox;
import uk.ac.man.cs.lethe.internal.dl.owlapi.OWLApiConverter$;

import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class LetheHelper
{
    public static Ontology toLetheOntology(OWLOntology ontology)
    {
        Set<DLStatement> dlStatements = ontology.getLogicalAxioms().stream()
                .flatMap(s -> JavaConversions.setAsJavaSet(OWLApiConverter$.MODULE$.convert(s)).stream())
                .collect(Collectors.toSet());

        return Ontology.buildFrom(JavaConversions.collectionAsScalaIterable(dlStatements));
    }

    static OWLOntology toOWLOntology(Ontology... ontologies)
    {
        Ontology ontology = mergeOntologies(ontologies);
        return new uk.ac.man.cs.lethe.internal.dl.owlapi.OWLExporter().toOwlOntology(ontology);
    }

    static Ontology mergeOntologies(Ontology... ontologies)
    {
        scala.collection.mutable.HashSet<uk.ac.man.cs.lethe.internal.dl.datatypes.Axiom> tBox =
                new scala.collection.mutable.HashSet<>();
        scala.collection.mutable.HashSet<uk.ac.man.cs.lethe.internal.dl.datatypes.Assertion> aBox
                = new scala.collection.mutable.HashSet<>();
        scala.collection.mutable.HashSet<uk.ac.man.cs.lethe.internal.dl.datatypes.RoleAxiom> rBox
                = new scala.collection.mutable.HashSet<>();
        for (Ontology ont : ontologies) {
            iterate(ont.tbox().axioms().iterator(), tBox::add);
            iterate(ont.abox().assertions().iterator(), aBox::add);
            iterate(ont.rbox().axioms().iterator(), rBox::add);
        }
        return new Ontology(new TBox(tBox.toSet()),
                new uk.ac.man.cs.lethe.internal.dl.datatypes.ABox(aBox.toSet()),
                new uk.ac.man.cs.lethe.internal.dl.datatypes.RBox(rBox.toSet()));
    }

    static <T> void iterate(Iterator<T> it, Consumer<T> consumer)
    {
        while (it.hasNext()) {
            consumer.accept(it.next());
        }
    }
}
