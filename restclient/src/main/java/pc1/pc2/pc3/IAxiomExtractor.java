package pc1.pc2.pc3;

import pc1.pc2.pc3.om.IAxiom;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Predicate;

public interface IAxiomExtractor
{
    List<IAxiom> extract(File inFile, Path outPath, Predicate<String> symbolFilter);
}
