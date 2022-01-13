import sys
import os
import subprocess
import shutil

java = "/usr/bin/java"
jar = "/Users/mostafa/Projects/forget/restclient/build/libs/restclient-dep-1.0.jar"
repository = "/Users/mostafa/Projects/forget/restclient/ontologies/bioportal/some"
outRep = "/Users/mostafa/Projects/forget/semanticforgettingonbioportal/"
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
    experimentScript = "/Users/mostafa/Projects/forget/restclient/src/main/python/semanticforgettingonbioportal.py"
    makeDirectory(outDir)
    clearDirectory(outDir)
    print(name + " started")
    executorCmd = "/Users/mostafa/anaconda3/bin/python {script} -d {dir} -n {name} -o {ontology} -s {sig}".format(script=experimentScript, dir=outDir, name=name, ontology=ont, sig=sig)
    with open(outDir + '/output.log', 'w') as out:
        subprocess.call(executorCmd, stdout=out, stderr=out, shell=True)
    print(name + " complete")

# print("Run with 10 percent of signature")
# for file in os.listdir(repository):
#     name = "experiment" + str(experimentNo)
#     ontology = os.path.join(repository, file)
#     outDir = outRep + name
#     experiment(outDir, name, ontology, 10)
#     experimentNo = experimentNo + 1
# experimentNo = 41
# print("Run with 30 percent of signature")
# for file in os.listdir(repository):
#     name = "experiment" + str(experimentNo)
#     ontology = os.path.join(repository, file)
#     outDir = outRep + name
#     experiment(outDir, name, ontology, 30)
#     experimentNo = experimentNo + 1

# experimentNo = 81
# print("Run with 50 percent of signature")
# for file in os.listdir(repository):
#     name = "experiment" + str(experimentNo)
#     ontology = os.path.join(repository, file)
#     outDir = outRep + name
#     if experimentNo > 89:
#         experiment(outDir, name, ontology, 50)
#     experimentNo = experimentNo + 1


summaryPath = outRep + "/summary.csv"
if os.path.isfile(summaryPath):
    os.unlink(summaryPath)
print("Creating new summary at " + summaryPath)
summaryFile = open(summaryPath, "w")
summaryFile.write(
    "Experiment,Ontology Size,Forgetting Signature Size,Forgetting View Size,Fame Time, Total Time, Forgetting Time, DE Time, No. Introduced Definers, No. Remaining Definers")
summaryFile.close()



summaryCmd = '{java} -cp {jar} {prog} -experiments {repo} -outFile {outFile}'.format(java=java,
                                                                              jar=jar,
                                                                              prog="uk.ac.man.SemanticForgettingSummaryGenerator",
                                                                              repo=outRep,
                                                                              outFile=summaryPath)

print(summaryCmd, flush=True)
returned_value = subprocess.call(summaryCmd, shell=True)
