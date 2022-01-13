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
fameJar = "/Users/mostafa/Projects/forget/fame/build/libs/fame-dep-1.0.jar"
signature = "100"
experiment = ""
ontology = ""

argv = sys.argv[1:]
opts, args = getopt.getopt(argv, "hd:n:o:s:", ["outDir=", "exName=", "ont=", "sig="])
for opt, arg in opts:
    if opt in ("-d", "--outDir"):
        outDir = arg
    elif opt in ("-n", "--exName"):
        experimentName = arg
    elif opt in ("-o", "--ont"):
        ontology = arg
    elif opt in ("-s", "--sig"):
        signature = arg
    else:
        print('semanticforgettingexperiment.py -n <experimentName> -c <signatureCoverage>')
        sys.exit()

print("Preparing Experiment")
########################################################Plan Experiment##############################################
plannerCmd = '{java} -cp {jar} {prog} -ontology {ontology} -signature {signature} -outDir {outDir}'.format(java=java,
                                                                                                             jar=jar,
                                                                                                             prog="uk.ac.man.ExperimentPlannerWithSignaturePortion",
                                                                                                             ontology=ontology,
                                                                                                             outDir=outDir,
                                                                                                             signature=signature)

print(plannerCmd, flush=True)
subprocess.call(plannerCmd, shell=True)

################################################################################################################

ontologyOWL = "{outDir}/Ontology.owl".format(outDir=outDir)
ontologyClausal = "{outDir}/Ontology.clausal".format(outDir=outDir)
sigFile = "{outDir}/signature".format(outDir=outDir)
############################################Forgetting##############################################################
print("Running...")
executorCmd = "{java} -cp {jar} {prog} -outDir {outDir} -sig {sig} -ont {ont} -timout 3600 -verbose".format(
    java=java,
    jar=jar,
    prog="uk.ac.man.SemanticForgetting",
    outDir=outDir,
    sig=sigFile,
    ont=ontologyClausal)

print(executorCmd, flush=True)
subprocess.call(executorCmd, shell=True)

############################################Fame##############################################################

fameCmd = '{java} -cp {jar} {prog} -ont {ont} -outDir {outDir} -sig {signature}'.format(java=java,
                                                                                        jar=fameJar,
                                                                                        prog="uk.ac.man.ForgetFame",
                                                                                        ont=ontologyOWL,
                                                                                        outDir=outDir,
                                                                                        signature=sigFile)

print(fameCmd, flush=True)
subprocess.call(fameCmd, shell=True)