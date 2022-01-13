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
covFiles = "/Users/mostafa/Projects/forget/restclient/ontologies/conceptCoverage"
outDir = ""
forgettingSignatureSize = ""
experimentName = ""
ontology = ""
ontologyName = ""
covData = ""
timeout = "3600000000000"

argv = sys.argv[1:]
opts, args = getopt.getopt(argv, "hn:o:c:u:", ["exName=", "ontology=", "fSigSize=", "outDir="])
for opt, arg in opts:
    if opt in ("-n", "--exName"):
        experimentName = arg
    elif opt in ("-c", "--fSigSize"):
        forgettingSignatureSize = arg
    elif opt in ("-o", "--ontology"):
        ontology = arg
    elif opt in ("-u", "--outDir"):
        outDir = arg
    else:
        print('semanticforgettingexperiment.py -n <experimentName> -o <ontologyPath> -c <signatureCoverage> -u <outDir>')
        sys.exit()

print("Preparing Experiment")
ontologyName = os.path.basename(ontology)
covData = "{cov}/{ont}_conceptCoverage.csv".format(cov=covFiles,ont=ontologyName)
########################################################Plan Experiment##############################################

plannerCmd = '{java} -cp {jar} {prog} -ont {ont} -outDir {outDir} -sigSize {sig} -coverageData {cov}'.format(java=java,
                                                                                                             jar=jar,
                                                                                                             prog="uk.ac.man.ExperimentPlannerBasedOnStarModule",
                                                                                                             ont=ontology,
                                                                                                             outDir=outDir,
                                                                                                             sig=forgettingSignatureSize,
                                                                                                             cov=covData)

print(plannerCmd, flush=True)
subprocess.call(plannerCmd, shell=True)

#################################################Load Data##########################################################
print("Loading Data")
ontologyDesc = "{outDir}/{name}.descriptor".format(outDir=outDir, name=ontologyName)
sig = outDir + "/signature"

loaderCmd = "{java} -cp {jar} {prog} -outDir {outDir} -sig {sig} -ont {ont} -asAxioms".format(
    java=java,
    jar=jar,
    prog="uk.ac.man.OfflineDataLoader",
    outDir=outDir,
    sig=sig,
    ont=ontologyDesc)
print(loaderCmd, flush=True)
subprocess.call(loaderCmd, shell=True)


################################################################################################################


ontologyOWL = "{outDir}/Ontology.owl".format(outDir=outDir)
ontologyClausal = "{outDir}/Ontology.clausal".format(outDir=outDir)

# ###########################################Forgetting##############################################################
print("Running...")
executorCmd = "{java} -cp {jar} {prog} -outDir {outDir} -sig {sig} -ont {ont} -timeout {timeout} -verbose".format(
    java=java,
    jar=jar,
    prog="uk.ac.man.SemanticForgetting",
    outDir=outDir,
    sig=sig,
    ont=ontologyClausal,
    timeout = timeout)

print(executorCmd, flush=True)
subprocess.call(executorCmd, shell=True)