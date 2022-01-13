import sys
import os
import subprocess
import shutil

repository = "c:/ccviews/forget/experiment"
outDir = "c:/ccviews/forget/restclient/ontologies"
java = "C:/Program Files/Java/jdk-11.0.2/bin/java.exe"
jar = "c:/ccviews/forget/restclient/build/libs/restclient-dep-1.0.jar"
lethejar = "c:/ccviews/forget/lethe/build/libs/lethe-dep-1.0.jar"


for file in os.listdir(repository):
    experiment = os.path.join(repository, file)
    print("Searching " + experiment)
    if os.path.isdir(experiment):
        ontology = os.path.join(experiment, "Ontology.owl")
        theory = os.path.join(experiment, "Theory.owl")
        out = os.path.join(outDir, file)
        if os.path.isfile(ontology) and os.path.isfile(theory):
            print("Building ontology of experiment " + file)
            cmd = '{java} -cp {jar} {prog} -out {out} -ont {ontology} -theory {theory}'.format(java=java,
                                                                            jar=lethejar,
                                                                            prog="uk.ac.man.MergeOntology",
                                                                            ontology=ontology,
                                                                            theory=theory,
                                                                            out=out)
            print(cmd, flush=True)
            returned_value = subprocess.call(cmd)
