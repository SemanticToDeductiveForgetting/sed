package pc1.pc2.pc3.app;

import org.jetbrains.annotations.NotNull;
import pc1.pc2.pc3.om.IAxiom;
import pc1.pc2.pc3.om.IClause;
import pc1.pc2.pc3.om.IConceptLiteral;
import pc1.pc2.pc3.om.ILiteral;
import pc1.pc2.pc3.utils.SignatureCollector;

import java.util.*;
import java.util.stream.Collectors;

public class SignatureModel implements IOntologyModelListener
{
    private final List<ISignatureModelListener> availableModelListener = new LinkedList<>();
    private final List<ISignatureModelListener> selectedModelListener = new LinkedList<>();
    private final LinkedHashSet<ILiteral> availableSignature = new LinkedHashSet<>();
    private final LinkedHashSet<ILiteral> selectedSignature = new LinkedHashSet<>();
    private final Map<ILiteral, Integer> referenceCount = new HashMap<>();


    public List<ILiteral> getAvailableLiterals()
    {
        return new ArrayList<>(availableSignature);
    }

    public List<ILiteral> getSelectedLiterals()
    {
        return new ArrayList<>(selectedSignature);
    }

    @Override public void axiomAdded(IAxiom axiom)
    {
        List<ILiteral> concepts = getConcepts(axiom.getLeft());
        concepts.addAll(getConcepts(axiom.getRight()));
        concepts.remove(IConceptLiteral.TOP);
        List<ILiteral> newConcepts = new LinkedList<>();
        int offset = availableSignature.size();
        for (ILiteral concept : concepts) {
            referenceCount.merge(concept, 1, Integer::sum);
            if (availableSignature.add(concept)) {
                newConcepts.add(concept);
            }
        }

        availableModelListener.forEach(l -> l.literalsAdded(offset, offset + newConcepts.size() - 1));
    }

    @Override public void axiomRemoved(IAxiom axiom)
    {
        List<ILiteral> concepts = getConcepts(axiom.getLeft());
        concepts.addAll(getConcepts(axiom.getRight()));
        concepts.remove(IConceptLiteral.TOP);
        List<ILiteral> removedConcepts = new LinkedList<>();
        for (ILiteral concept : concepts) {
            Integer newValue = referenceCount.computeIfPresent(concept, (x, y) -> y - 1);
            if(newValue != null && newValue == 0) {
                removedConcepts.add(concept);
            }
        }

        removeLiterals(removedConcepts, availableSignature, availableModelListener);
        removeLiterals(removedConcepts, selectedSignature, selectedModelListener);
    }

    @Override public void ontologyCleared(List<IAxiom> clauses)
    {

    }

    @Override public void axiomsAdded(List<IAxiom> axioms)
    {

    }

    private void removeLiterals(List<ILiteral> literals, LinkedHashSet<ILiteral> collection,
                                List<ISignatureModelListener> listeners)
    {
        List<ILiteral> removed = literals.stream().filter(collection::contains).collect(Collectors.toList());
        List<ILiteral> collectionAsList = new ArrayList<>(collection);
        List<ILiteral> sorted = removed.stream()
                .sorted(Comparator.comparing(collectionAsList::lastIndexOf))
                .collect(Collectors.toList());
        for (ILiteral literal : sorted) {
            collection.remove(literal);
            int index = collectionAsList.lastIndexOf(literal);
            notifyRemoved(listeners, index, index);
        }
    }

    @NotNull private List<ILiteral> getConcepts(IClause clause)
    {
        SignatureCollector collector = new SignatureCollector();
        return collector.getSignature(clause).stream()
                .filter(l -> l instanceof IConceptLiteral)
                .collect(Collectors.toList());
    }

    public void addAvailableSignatureListener(ISignatureModelListener listener)
    {
        availableModelListener.add(listener);
    }

    public void addSelectedSignatureListener(ISignatureModelListener listener)
    {
        selectedModelListener.add(listener);
    }

    public void selectLiterals(List<ILiteral> literals)
    {
        moveLiterals(literals,
                getAvailableLiterals(),
                availableSignature,
                availableModelListener,
                selectedSignature,
                selectedModelListener);
    }

    public void deSelectLiterals(List<ILiteral> literals)
    {
        moveLiterals(literals,
                getSelectedLiterals(),
                selectedSignature,
                selectedModelListener,
                availableSignature,
                availableModelListener);
    }

    private void moveLiterals(List<ILiteral> literals, List<ILiteral> fromList, LinkedHashSet<ILiteral> fromSignature,
                              List<ISignatureModelListener> fromListeners, LinkedHashSet<ILiteral> toSignature,
                              List<ISignatureModelListener> toListeners)
    {
        int from = fromList.indexOf(literals.get(0));
        int to = fromList.indexOf(literals.get(literals.size() - 1));
        fromSignature.removeAll(literals);
        notifyRemoved(fromListeners, from, to);
        int offset = toSignature.size();
        toSignature.addAll(literals);
        notifyAdded(toListeners, from, to, offset);
    }

    private void notifyAdded(List<ISignatureModelListener> toListeners, int from, int to, int offset)
    {
        for (ISignatureModelListener listener : toListeners) {
            listener.literalsAdded(offset + from, offset + to);
        }
    }

    private void notifyRemoved(List<ISignatureModelListener> fromListeners, int from, int to)
    {
        for (ISignatureModelListener listener : fromListeners) {
            listener.literalsRemoved(from, to);
        }
    }
}
