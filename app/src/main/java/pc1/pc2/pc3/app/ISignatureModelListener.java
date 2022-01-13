package pc1.pc2.pc3.app;

public interface ISignatureModelListener
{
    void literalsAdded(int from, int to);

    void literalsRemoved(int from, int to);
}
