import sys
import os
import subprocess
import shutil

java = "/usr/bin/java"
jar = "/Users/mostafa/Projects/forget/restclient/build/libs/restclient-dep-1.0.jar"
repository = "/Users/mostafa/Projects/forget/restclient/ontologies/cookedontologieswithequivalences"
outRep = "/Users/mostafa/Projects/forget/semanticforgettingexperiment/"
experimentScript = "/Users/mostafa/Projects/forget/restclient/src/main/python/semanticforgettingexperiment.py"
ontologyName = ""

for i in range(1, 84):
    name = "experiment" + str(i)
    ontologyName = "experiment" + str(i)
    outDir = outRep + name
#     try:
#         os.makedirs(outDir)
#     except FileExistsError:
#         print ("Output directory {dir} exists. Directory will be emptied and recycled".format(dir=outDir))
#
#     for file in os.listdir(outDir):
#         file_path = os.path.join(outDir, file)
#         try:
#             if os.path.isfile(file_path):
#                     os.unlink(file_path)
#             elif os.path.isdir(file_path): shutil.rmtree(file_path)
#         except Exception as e:
#             print(e)

    print(name + " started")
    executorCmd = "/Users/mostafa/anaconda3/bin/python {script} -n {name} -o {ontology} -c 50".format(script=experimentScript, name=name, ontology=ontologyName)
    with open(outDir + '/size.log', 'w') as out:
        subprocess.call(executorCmd, shell=True)
    print(name + " complete")

summaryPath = outRep + "/summary.csv"
if os.path.isfile(summaryPath):
    os.unlink(summaryPath)
    #print("Summary file exists. Statistics will be appended to file.")
print("Creating new summary at " + summaryPath)
summaryFile = open(summaryPath, "w")
summaryFile.write(
    "Experiment,Ontology Size,Sig Size, Forgetting Signature Size,Forgetting View Size,Fame Time, Total Time, Forgetting Time, DE Time, No. Introduced Definers, No. Remaining Definers")
summaryFile.close()



summaryCmd = '{java} -cp {jar} {prog} -experiments {repo} -outFile {outFile} -optimized'.format(java=java,
                                                                              jar=jar,
                                                                              prog="uk.ac.man.SemanticForgettingSummaryGenerator",
                                                                              repo=outRep,
                                                                              outFile=summaryPath)

print(summaryCmd, flush=True)
returned_value = subprocess.call(summaryCmd, shell=True)
