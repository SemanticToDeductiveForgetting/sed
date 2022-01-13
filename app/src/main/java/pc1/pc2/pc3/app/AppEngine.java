package pc1.pc2.pc3.app;

import pc1.pc2.pc3.logic.ForgettingEngine;
import pc1.pc2.pc3.om.IAxiom;
import pc1.pc2.pc3.om.IConceptLiteral;
import pc1.pc2.pc3.om.ILiteral;
import pc1.pc2.pc3.utils.AxiomHelper;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class AppEngine
{
    private OntologyModel ontology;
    private OntologyModel theory;
    private OntologyModel interpolant;
    private SignatureModel signature;

    AppEngine()
    {
        signature = new SignatureModel();
        ontology = new OntologyModel(false);
        theory = new OntologyModel(false);
        interpolant = new OntologyModel(true);
        ontology.addListener(signature);
        theory.addListener(signature);
    }

    public void forget()
    {
        interpolant.clear();
        ForgettingEngine forgetting = new ForgettingEngine();
        List<IAxiom> solutions = forgetting.forgetAxioms(
                new LinkedList<>(cloneAxioms(ontology.getAxioms())),
                new LinkedList<>(cloneAxioms(theory.getAxioms())),
                toConceptLiterals(signature.getSelectedLiterals()));
        interpolant.setAxioms(solutions);
    }

    private List<IAxiom> cloneAxioms(List<IAxiom> axioms)
    {
        List<IAxiom> clonedAxioms = new LinkedList<>();
        for (IAxiom axiom : axioms) {
            clonedAxioms.add(AxiomHelper.clone(axiom));
        }
        return clonedAxioms;
    }

    private List<IConceptLiteral> toConceptLiterals(List<ILiteral> selectedLiterals)
    {
        return selectedLiterals.stream()
                .filter(l -> l instanceof IConceptLiteral)
                .map(l -> (IConceptLiteral) l)
                .collect(Collectors.toList());
    }

    public OntologyModel getBackgroundTheory()
    {
        return theory;
    }

    public OntologyModel getOntology()
    {
        return ontology;
    }

    public OntologyModel getInterpolant()
    {
        return interpolant;
    }

    public SignatureModel getSignature()
    {
        return signature;
    }
}
