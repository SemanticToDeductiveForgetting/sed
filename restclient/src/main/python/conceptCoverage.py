import sys
import os
import subprocess
import shutil

repository = "/Users/mostafa/Projects/forget/restclient/ontologies/cookedontologieswithequivalences"
outDir = "/Users/mostafa/Projects/forget/restclient/ontologies/conceptcoveragewithequivalences"
java = "/usr/bin/java"
jar = "/Users/mostafa/Projects/forget/restclient/build/libs/restclient-dep-1.0.jar"

try:
    os.makedirs(outDir)
except FileExistsError:
    print ("Output directory exists. Directory will be emptied and recycled")

for file in os.listdir(outDir):
    file_path = os.path.join(outDir, file)
    try:
        if os.path.isfile(file_path):
            os.unlink(file_path)
        elif os.path.isdir(file_path): shutil.rmtree(file_path)
    except Exception as e:
        print(e)

summaryPath = outDir + "/CoverageSummary.csv"
if os.path.isfile(summaryPath):
    print("Summary file exists. Statistics will be appended to file.")
else:
    summaryFile = open(outDir + "/CoverageSummary.csv", "w")
    summaryFile.write("Ontology,is ALC,avg concept in axiom,No. Concepts,No. Axioms,50%,100%,150%,200%")
    summaryFile.close()

for file in os.listdir(repository):
    file_path = os.path.join(repository, file)
    if os.path.isfile(file_path):
        if file != "blacklist" and file != "classes.txt" and file != "metrics.csv" and not file.startswith("experiment"):
            print("Checking concept coverage of " + file)
            summaryCmd = '{java} -cp {jar} {prog} -repo {repo} -outDir {outDir} -ont {ontology} -summary {summary}'.format(java=java,
                                                                            jar=jar,
                                                                            prog="uk.ac.man.CoverageCalculator",
                                                                            repo=repository,
                                                                            outDir=outDir,
                                                                            ontology=file,
                                                                            summary=summaryPath)
            print(summaryCmd, flush=True)
            returned_value = subprocess.call(summaryCmd, shell=True)



summaryPath = outDir + "/ExperimentsCoverageSummary.csv"
if os.path.isfile(summaryPath):
    print("Summary file exists. Statistics will be appended to file.")
else:
    summaryFile = open(summaryPath, "w")
    summaryFile.write("Ontology,is ALC,avg concept in axiom,No. Concepts,No. Axioms,50%,100%,150%,200%")
    summaryFile.close()
for file in os.listdir(repository):
    file_path = os.path.join(repository, file)
    if os.path.isfile(file_path):
        if file != "blacklist" and file != "classes.txt" and file != "metrics.csv" and file.startswith("experiment"):
            print("Checking concept coverage of " + file)
            summaryCmd = '{java} -cp {jar} {prog} -repo {repo} -outDir {outDir} -ont {ontology} -summary {summary}'.format(java=java,
                                                                                                                           jar=jar,
                                                                                                                           prog="uk.ac.man.CoverageCalculator",
                                                                                                                           repo=repository,
                                                                                                                           outDir=outDir,
                                                                                                                           ontology=file,
                                                                                                                           summary=summaryPath)
            print(summaryCmd, flush=True)
            returned_value = subprocess.call(summaryCmd, shell=True)