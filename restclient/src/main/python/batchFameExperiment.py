import sys
import os
import subprocess
import shutil

java = "/usr/bin/java"
jar = "/Users/mostafa/Projects/forget/restclient/build/libs/restclient-dep-1.0.jar"
repository = "/Users/mostafa/Projects/forget/restclient/ontologies/cookedontologieswithequivalences"
outRep = "/Users/mostafa/Projects/forget/semanticforgettingonbioportal/"
experimentScript = "/Users/mostafa/Projects/forget/restclient/src/main/python/fameexperiment.py"
ontologyName = ""

for i in range(48, 50):
    name = "experiment" + str(i)
    ontologyName = "experiment" + str(i)
    outDir = outRep + name

    if os.path.isdir(outDir):
        print(name + " started")
        executorCmd = "/Users/mostafa/anaconda3/bin/python {script} -n {name}".format(script=experimentScript, name=name, ontology=ontologyName)
        with open(outDir + '/fameOutput.log', 'w') as out:
            subprocess.call(executorCmd, stdout=out, stderr=out, shell=True)
        print(name + " complete")

# summaryPath = outRep + "/summary.csv"
# if os.path.isfile(summaryPath):
#     os.unlink(summaryPath)
#
# print("Creating new summary at " + summaryPath)
# summaryFile = open(summaryPath, "w")
# summaryFile.write(
#     "Experiment,Ontology Size,Forgetting Signature Size,Forgetting View Size,Fame Time, Total Time, Forgetting Time, DE Time, No. Introduced Definers, No. Remaining Definers")
# summaryFile.close()
#
#
#
# summaryCmd = '{java} -cp {jar} {prog} -experiments {repo} -outFile {outFile}'.format(java=java,
#                                                                               jar=jar,
#                                                                               prog="uk.ac.man.SemanticForgettingSummaryGenerator",
#                                                                               repo=outRep,
#                                                                               outFile=summaryPath)
#
# print(summaryCmd, flush=True)
# returned_value = subprocess.call(summaryCmd, shell=True)
