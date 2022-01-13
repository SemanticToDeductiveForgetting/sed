import sys
import os
import subprocess
import shutil

java = "/usr/bin/java"
jar = "/Users/mostafa/Projects/forget/restclient/build/libs/restclient-dep-1.0.jar"
repository = "/Users/mostafa/Projects/forget/restclient/ontologies/bioportal/EX25"
outRep = "/Users/mostafa/Projects/forget/definersinsemanticforgettingexperiment/"
experimentScript = "/Users/mostafa/Projects/forget/restclient/src/main/python/definersInSemanticForgettingExperiment.py"

ontologies = os.listdir(repository)
ontologies.sort()
for i in range(1, len(ontologies)):
    name = "experiment" + str(i)
    outDir = outRep + name
    os.makedirs(outDir)
    ont = ontologies[i]
    ontology = os.path.join(repository, ont)

    print("Experiment " + str(i) + " started")
    executorCmd = "/Users/mostafa/anaconda3/bin/python {script} -n {name} -o {ontology} -c 25 -u {outDir}".format(script=experimentScript, name=name, ontology=ontology, outDir=outDir)
    with open(outDir + '/output.log', 'w') as out:
        subprocess.call(executorCmd, stdout=out, stderr=out, shell=True)
    print(name + " complete")

summaryPath = outRep + "/summary.csv"
if os.path.isfile(summaryPath):
    os.unlink(summaryPath)
    #print("Summary file exists. Statistics will be appended to file.")
print("Creating new summary at " + summaryPath)
summaryFile = open(summaryPath, "w")
summaryFile.write(
    "Experiment,Ontology Size,Forgetting Signature Size,Forgetting View Size,Forgetting Time,No. Introduced Definers,No. Remaining Definers")
summaryFile.close()



summaryCmd = '{java} -cp {jar} {prog} -experiments {repo} -outFile {outFile}'.format(java=java,
                                                                              jar=jar,
                                                                              prog="uk.ac.man.SemanticForgettingSummaryGenerator",
                                                                              repo=outRep,
                                                                              outFile=summaryPath)

print(summaryCmd, flush=True)
returned_value = subprocess.call(summaryCmd, shell=True)
