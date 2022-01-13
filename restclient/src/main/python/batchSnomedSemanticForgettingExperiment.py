import sys
import os
import subprocess
import shutil

java = "/usr/bin/java"
jar = "/Users/mostafa/Projects/forget/restclient/build/libs/restclient-dep-1.0.jar"
experimentScript = "/Users/mostafa/Projects/forget/restclient/src/main/python/SnomedSemanticForgettingExperiment.py"

ontology = "/Users/mostafa/Projects/forget/restclient/ontologies/snomed/era_sct_intl_20200904_sct_intl_20200731_star-module.owl"
sigFile = "/Users/mostafa/Projects/forget/restclient/ontologies/snomed/era_sct_intl_20200904_extended_sig.owl"
outDir = "/Users/mostafa/Projects/forget/SnomedExperiment"
name = "SNOMED"

if not os.path.exists(outDir):
    os.makedirs(outDir)

for i in range(1, 2):
    name = "experiment" + str(i)
    ExperimentDir = outDir + "/" + name
    os.makedirs(ExperimentDir)
    percentage = i
    print("Experiment {name} started".format(name=name))
    executorCmd = "/Users/mostafa/anaconda3/bin/python {script} -n {name} -o {ontology} -c {sigFile} -u {outDir} -p {percent}".format(script=experimentScript, name=name, ontology=ontology, sigFile=sigFile, outDir=ExperimentDir, percent=percentage)
    with open(ExperimentDir + '/output.log', 'w') as out:
        subprocess.call(executorCmd, stdout=out, stderr=out, shell=True)
    print(name + " complete")