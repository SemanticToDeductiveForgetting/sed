import sys
import os
import subprocess
import shutil
from shutil import copyfile

java = "/usr/bin/java"
jar = "/Users/mostafa/Projects/forget/restclient/build/libs/restclient-dep-1.0.jar"
repository = "/Users/mostafa/Projects/forget/restclient/ontologies/bioportal/some"
outRep = "/Users/mostafa/Projects/forget/restclient/ontologies/bioportal/data"
ontologyName = ""
experimentNo = 1

def clearDirectory(dir):
    for file in os.listdir(dir):
        file_path = os.path.join(dir, file)
        try:
            if os.path.isfile(file_path):
                os.unlink(file_path)
            elif os.path.isdir(file_path): shutil.rmtree(file_path)
        except Exception as e:
            print(e)

def makeDirectory(dir):
    try:
        os.makedirs(dir)
    except FileExistsError:
        print ("Output directory {dir} exists. Directory will be emptied and recycled".format(dir=outDir))

def experiment(outDir, name, ont, sig):
    experimentScript = "/Users/mostafa/Projects/forget/restclient/src/main/python/fineGrainedOnBioportal.py"
    makeDirectory(outDir)
#     clearDirectory(outDir)
    print(name + " started")
    executorCmd = "/Users/mostafa/anaconda3/bin/python {script} -d {dir} -n {name} -o {ontology} -s {sig}".format(script=experimentScript, dir=outDir, name=name, ontology=ont, sig=sig)
    subprocess.call(executorCmd, shell=True)
    print(name + " complete")

print("Run with 10 percent of signature")
for file in os.listdir(repository):
    if(experimentNo > 0):
        if(experimentNo < 51):
            name = "experiment" + str(experimentNo)
            ontology = os.path.join(repository, file)
            outOntology = os.path.join(outRep, file)
            copyfile(ontology, outOntology)
#             outDir = outRep + name
#             experiment(outDir, name, ontology, 10)
    experimentNo = experimentNo + 1

# experimentNo = 54
# print("Run with 30 percent of signature")
# for file in os.listdir(repository):
#     if(experimentNo > 76):
#         if(experimentNo < 107):
#             name = "experiment" + str(experimentNo)
#             ontology = os.path.join(repository, file)
#             outDir = outRep + name
#             experiment(outDir, name, ontology, 30)
#     experimentNo = experimentNo + 1

# experimentNo = 107
# print("Run with 50 percent of signature")
# for file in os.listdir(repository):
#     if experimentNo < 160:
#         if experimentNo > 141:
#             name = "experiment" + str(experimentNo)
#             ontology = os.path.join(repository, file)
#             outDir = outRep + name
#             experiment(outDir, name, ontology, 50)
#     experimentNo = experimentNo + 1
# redo experiment115

summaryPath = outRep + "/summary.csv"
if os.path.isfile(summaryPath):
    os.unlink(summaryPath)
print("Creating new summary at " + summaryPath)
summaryFile = open(summaryPath, "w")
summaryFile.write(
    "Experiment,Ontology Size,Forgetting Signature Size,Deductive View Size,Lethe Size,Fame Time,Lethe Time,Final Time,Semantic Time,Semantic DE Time,Semantic Simplification Time,Reduction Time,Deductive DE Time,Deductive Simplification Time, No. Introduced Definers, No. Cyclic Definers, No. Delta Definers, Delta Size")
summaryFile.close()



summaryCmd = '{java} -cp {jar} {prog} -experiments {repo} -outFile {outFile}'.format(java=java,
                                                                              jar=jar,
                                                                              prog="uk.ac.man.SemanticToDeductiveSummaryGenerator",
                                                                              repo=outRep,
                                                                              outFile=summaryPath)

# print(summaryCmd, flush=True)
# returned_value = subprocess.call(summaryCmd, shell=True)
