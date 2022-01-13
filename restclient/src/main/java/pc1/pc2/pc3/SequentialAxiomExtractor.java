package pc1.pc2.pc3;

import pc1.pc2.pc3.error.ParseException;
import pc1.pc2.pc3.input.AxiomBuilder;
import pc1.pc2.pc3.om.IAxiom;

import java.io.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

public class SequentialAxiomExtractor implements IAxiomExtractor
{
    @Override
    public List<IAxiom> extract(File inFile, Path outPath, Predicate<String> symbolFilter)
    {
        List<IAxiom> axioms = new ArrayList<>();
        try (BufferedWriter out = new BufferedWriter(new FileWriter(outPath.toFile()))) {
            try (BufferedReader bufferedReader = new BufferedReader(new FileReader(inFile))) {
                Set<Integer> observedLines = new HashSet<>();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    if (!line.isBlank() && !observedLines.contains(line.hashCode())) {
                        observedLines.add(line.hashCode());
                        if (symbolFilter.test(line)) {
                            IAxiom axiom;
                            try {
                                axiom = AxiomBuilder.fromString(line);
                                axioms.add(axiom);
                            } catch (ParseException ignored) {
                            }
                        }
                        else {
                            out.write(String.format("%s\n", line));
                        }

                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return axioms;
    }
}
