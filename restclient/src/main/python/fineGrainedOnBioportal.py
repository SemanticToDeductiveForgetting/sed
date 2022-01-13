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
import datetime

def printStartDate():
    now = datetime.datetime.now()
    print (now.strftime("Process started at %Y-%m-%d %H:%M:%S"))

def printEndDate():
    now = datetime.datetime.now()
    print (now.strftime("Process ended at %Y-%m-%d %H:%M:%S"))

java = "/usr/bin/java"
jar = "/Users/mostafa/Projects/forget/restclient/build/libs/restclient-dep-1.0.jar"
fameJar = "/Users/mostafa/Projects/forget/fame/build/libs/fame-dep-1.0.jar"
letheJar = "/Users/mostafa/Projects/forget/lethe/build/libs/lethe-dep-1.0.jar"
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

################################################################################################################

ontologyOWL = "{outDir}/Ontology.owl".format(outDir=outDir)
ontologyClausal = "{outDir}/Ontology.clausal".format(outDir=outDir)
sigFile = "{outDir}/signature".format(outDir=outDir)

printStartDate()
########################################################Plan Experiment##############################################
plannerCmd = '{java} -cp {jar} {prog} -ontology {ontology} -signature {signature} -outDir {outDir} -quantifiedSignature'.format(java=java,
                                                                                                             jar=jar,
                                                                                                             prog="uk.ac.man.ExperimentPlannerWithSignaturePortion",
                                                                                                             ontology=ontology,
                                                                                                             outDir=outDir,
                                                                                                             signature=signature)

# if not (os.path.exists(ontologyOWL) and os.path.exists(ontologyClausal) and os.path.exists(sigFile)):
#     print("Preparing Experiment")
#     print(plannerCmd, flush=True)
#     with open(outDir + '/ExperimentPlan.log', 'w') as out:
#         subprocess.call(plannerCmd, stdout=out, stderr=out, shell=True)
#     printEndDate()

############################################Fame##############################################################

fameCmd = '{java} -cp {jar} {prog} -ont {ont} -outDir {outDir} -sig {signature}'.format(java=java,
                                                                                        jar=fameJar,
                                                                                        prog="uk.ac.man.ForgetFame",
                                                                                        ont=ontologyOWL,
                                                                                        outDir=outDir,
                                                                                        signature=sigFile)

# if not os.path.exists(outDir + '/' + "Fame.time"):
#     print(fameCmd, flush=True)
#     with open(outDir + '/Fame.log', 'w') as out:
#         subprocess.call(fameCmd, stdout=out, stderr=out, shell=True)
#     printEndDate()

############################################Lethe##############################################################

letheCmd = '{java} -cp {jar} {prog} -ont {ont} -outDir {outDir} -sig {signature}'.format(java=java,
                                                                                        jar=letheJar,
                                                                                        prog="uk.ac.man.ForgetLethe",
                                                                                        ont=ontologyOWL,
                                                                                        outDir=outDir,
                                                                                        signature=sigFile)

# if not os.path.exists(outDir + '/' + "Lethe.time"):
#     print(letheCmd, flush=True)
#     with open(outDir + '/Lethe.log', 'w') as out:
#         subprocess.call(letheCmd, stdout=out, stderr=out, shell=True)
#     printEndDate()

############################################Semantic Forgetting##############################################################
# newRunDir = outDir
# try:
#     os.makedirs(newRunDir)
# except FileExistsError:
#     print ("Output directory {dir} exists. Directory will be emptied and recycled".format(dir=outDir))

executorCmd = "{java} -cp {jar} {prog} -outDir {outDir}/newRun -sig {sig} -ont {ont} -reduceToDeductive -verbose".format(
    java=java,
    jar=jar,
    prog="uk.ac.man.SemanticForgetting",
    outDir=outDir,
    sig=sigFile,
    ont=ontologyClausal)

if not os.path.exists(outDir + '/newRun/' + "time"):
    print(executorCmd, flush=True)
    with open(outDir + '/SemanticToDeductive.log', 'w') as out:
        subprocess.call(executorCmd, stdout=out, stderr=out, shell=True)
    printEndDate()