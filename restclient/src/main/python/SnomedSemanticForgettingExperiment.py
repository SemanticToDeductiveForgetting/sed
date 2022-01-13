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
outDir = ""
fSigFile = ""
experimentName = ""
ontology = ""
ontologyName = ""

argv = sys.argv[1:]
opts, args = getopt.getopt(argv, "hn:o:c:u:p:", ["exName=", "ontology=", "fSigFile=", "outDir=", "percentage="])
for opt, arg in opts:
    if opt in ("-n", "--exName"):
        experimentName = arg
    elif opt in ("-c", "--fSigFile"):
        fSigFile = arg
    elif opt in ("-o", "--ontology"):
        ontology = arg
    elif opt in ("-u", "--outDir"):
        outDir = arg
    elif opt in ("-p", "--percentage"):
        percentage = arg
    else:
        print('SnomedSemanticForgettingExperiment.py -n <experimentName> -o <ontologyPath> -c <signatureFile> -u <outDir>')
        sys.exit()

print("Preparing Experiment")
ontologyName = os.path.basename(ontology)
########################################################Plan Experiment##############################################

plannerCmd = '{java} -cp {jar} {prog} -ont {ont} -outDir {outDir} -sigFile {sig} -percentage {percent}'.format(java=java,
                                                                                                             jar=jar,
                                                                                                             prog="uk.ac.man.SnomedProjectPlanner",
                                                                                                             ont=ontology,
                                                                                                             outDir=outDir,
                                                                                                             sig=fSigFile,
                                                                                                             percent=percentage)

print(plannerCmd, flush=True)
subprocess.call(plannerCmd, shell=True)

#################################################Load Data##########################################################
print("Loading Data")
ontologyDesc = "{outDir}/{name}.descriptor".format(outDir=outDir, name=ontologyName)
sig = outDir + "/signature"

loaderCmd = "{java} -cp {jar} {prog} -outDir {outDir} -sig {sig} -ont {ont} -asAxioms -label".format(
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
executorCmd = "{java} -Xmx1800m -cp {jar} {prog} -outDir {outDir} -sig {sig} -ont {ont} -verbose".format(
    java=java,
    jar=jar,
    prog="uk.ac.man.SemanticForgetting",
    outDir=outDir,
    sig=sig,
    ont=ontologyClausal)

print(executorCmd, flush=True)
subprocess.call(executorCmd, shell=True)