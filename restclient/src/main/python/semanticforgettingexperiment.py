import subprocess
from subprocess import Popen, TimeoutExpired
import glob
import random
import os
import json
import requests
import shutil
import sys
import getopt

java = "/usr/bin/java"
jar = "/Users/mostafa/Projects/forget/restclient/build/libs/restclient-dep-1.0.jar"
repository = "/Users/mostafa/Projects/forget/restclient/ontologies/cookedontologieswithequivalences"
coverageRepo = "/Users/mostafa/Projects/forget/restclient/ontologies/conceptcoveragewithequivalences"
outDir = "/Users/mostafa/Projects/forget/semanticforgettingexperiment/"
coverage = "100"
experimentName = ""
ontologyName = ""

argv = sys.argv[1:]
opts, args = getopt.getopt(argv, "hn:o:c:", ["exName=", "ont=", "cov="])
for opt, arg in opts:
    if opt in ("-n", "--exName"):
        outDir = outDir + arg
        experimentName = arg
    elif opt in ("-o", "--ont"):
        ontologyName = arg
    elif opt in ("-c", "--cov"):
        coverage = arg
    else:
        print('semanticforgettingexperiment.py -n <experimentName> -c <signatureCoverage>')
        sys.exit()

# try:
#     os.makedirs(outDir)
# except FileExistsError:
#     print ("Output directory exists.")
# else:
#     print ("Successfully created the directory %s " % outDir)
#
# for file in os.listdir(outDir):
#     file_path = os.path.join(outDir, file)
#     try:
#         if os.path.isfile(file_path):
#             if file != "output.log":
#                 os.unlink(file_path)
#         elif os.path.isdir(file_path): shutil.rmtree(file_path)
#     except Exception as e:
#         print(e)

ontology = "{repo}/{name}".format(repo=repository, name=ontologyName)

print("Preparing Experiment")
########################################################Plan Experiment##############################################

covData = "{repo}/{ont}_conceptCoverage.csv".format(repo=coverageRepo,ont=ontologyName)
plannerCmd = '{java} -cp {jar} {prog} -ont {ont} -coverage {cov} -outDir {outDir} -coverageData {covData}'.format(java=java,
                                                                                                             jar=jar,
                                                                                                             prog="uk.ac.man.OfflineExperimentPlanner",
                                                                                                             ont=ontology,
                                                                                                             outDir=outDir,
                                                                                                             cov=coverage,
                                                                                                             covData=covData)

# print(plannerCmd, flush=True)
# subprocess.call(plannerCmd, shell=True)

#################################################Load Data##########################################################
print("Loading Data")
ontology = "{outDir}/{name}.descriptor".format(outDir=outDir, name=ontologyName)
sig = outDir + "/signature"
print(ontology)

loaderCmd = "{java} -cp {jar} {prog} -outDir {outDir} -sig {sig} -ont {ont} -asAxioms -verbose".format(
    java=java,
    jar=jar,
    prog="uk.ac.man.OfflineDataLoader",
    outDir=outDir,
    sig=sig,
    ont=ontology)
# print(loaderCmd, flush=True)
# subprocess.call(loaderCmd, shell=True)


################################################################################################################


ontologyOWL = "{outDir}/Ontology.owl".format(outDir=outDir)
ontologyClausal = "{outDir}/Ontology.clausal".format(outDir=outDir)
out = outDir + "/WithOptimizations"
# os.makedirs(out)
# ###########################################Forgetting##############################################################
print("Running...")
executorCmd = "{java} -cp {jar} {prog} -outDir {outDir} -sig {sig} -ont {ont} -timout 3600 -verbose".format(
    java=java,
    jar=jar,
    prog="uk.ac.man.SemanticForgetting",
    outDir=out,
    sig=sig,
    ont=ontologyClausal)

# print(executorCmd, flush=True)
# subprocess.call(executorCmd, shell=True)

# print("Moving " + out)
# for file in os.listdir(out):
#     file_path = os.path.join(out, file)
#     new_path = os.path.join(outDir, "optimized_" + file)
#     shutil.move(file_path, new_path)
# shutil.rmtree(out)