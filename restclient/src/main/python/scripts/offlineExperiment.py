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

java = "C:/Program Files/Java/jdk-11.0.2/bin/java.exe"
jar = "c:/ccviews/forget/restclient/build/libs/restclient-dep-1.0.jar"
lethejar = "c:/ccviews/forget/lethe/build/libs/lethe-dep-1.0.jar"
lethejar1 = "c:/ccviews/forget/restclient/build/libs/restclient-dep-1.0.jar;c:/ccviews/forget/restclient/lib/trove-3.0.0.jar;" \
            "c:/ccviews/forget/restclient/lib/lethe_2.11-0.026.jar;c:/ccviews/forget/restclient/lib/scala-library-2.11.6.jar;" \
            "c:/ccviews/forget/restclient/lib/context_2.11-0.4.1.jar" \
            ";c:/ccviews/forget/restclient/lib/fastring_2.11-0.2.4.jar" \
            ";c:/ccviews/forget/restclient/lib/org.semanticweb.HermiT.jar" \
            ";c:/ccviews/forget/restclient/lib/scala-parser-combinators-2.11.0-M4.jar" \
            ";c:/ccviews/forget/restclient/lib/scala-swing-2.10.5.jar" \
            ";c:/ccviews/forget/restclient/lib/zero-log_2.11-0.3.6.jar"
repository = "c:/ccviews/forget/restclient/ontologies"
outDir = "c:/ccviews/forget/experimentNoBG/"
coverage = "100"
experimentName = ""

argv = sys.argv[1:]
opts, args = getopt.getopt(argv, "hn:c:", ["exName=", "cov="])
for opt, arg in opts:
    if opt in ("-n", "--exName"):
        outDir = outDir + arg
        experimentName = arg
    elif opt in ("-c", "--cov"):
        coverage = arg
    else:
        print('experiment.py -n <experimentName> -c <signatureCoverage>')
        sys.exit()

try:
    os.makedirs(outDir)
except FileExistsError:
    print ("Output directory exists.")
# else:
#     print ("Successfully created the directory %s " % outDir)
#
# for file in os.listdir(outDir):
#     file_path = os.path.join(outDir, file)
#     try:
#         if os.path.isfile(file_path):
#             if file != "output.log":
#                 os.unlink(file_path)
#         # elif os.path.isdir(file_path): shutil.rmtree(file_path)
#     except Exception as e:
#         print(e)

ontology = "{repo}/{name}".format(repo=repository, name=experimentName)

########################################################Plan Experiment##############################################

covData = "{repo}/conceptCoverage/{ont}_conceptCoverage.csv".format(repo=repository,ont=experimentName)
plannerCmd = '{java} -cp {jar} {prog} -ont {ont} -coverage {cov} -outDir {outDir}/data -coverageData {covData}'.format(java=java,
                                                                                                             jar=jar,
                                                                                                             prog="uk.ac.man.OfflineExperimentPlanner",
                                                                                                             ont=ontology,
                                                                                                             outDir=outDir,
                                                                                                             cov=coverage,
                                                                                                             covData=covData)

# print(plannerCmd, flush=True)
# returned_value = subprocess.call(plannerCmd)

# print('Experiment planner executed with returned value:', returned_value)

#################################################Load Data##########################################################

ontologies = glob.glob("{outDir}/data/*.descriptor".format(outDir=outDir))
sig = outDir + "/data/signature"
for ontology in ontologies:
        loaderCmd = "{java} -cp {jar} {prog} -repo {repo} -outDir {outDir}/data -sig {sig} -ont {ont} -verbose".format(
            java=java,
            jar=jar,
            prog="uk.ac.man.OfflineDataLoader",
            repo=repository,
            outDir=outDir,
            sig=sig,
            ont=ontology)
        # print(loaderCmd, flush=True)
        # subprocess.call(loaderCmd)


################################################################################################################


ontologyOWL = "{outDir}/data/Ontology.owl".format(outDir=outDir)
ontologyClausal = "{outDir}/data/Ontology.clausal".format(outDir=outDir)
theoryClausal = "{outDir}/data/Theory.clausal".format(outDir=outDir)

with open(theoryClausal, 'w') as fp:
    pass

###################################################Lethe########################################################

executorCmd = "{java} -cp {jar} {prog} -ont {ont} -sig {sig} -outDir {outDir}".format(
    java=java,
    jar=lethejar,
    prog="uk.ac.man.ForgetLethe",
    outDir=outDir,
    ont=ontologyOWL,
    sig=sig)

# print(executorCmd, flush=True)
# subprocess.call(executorCmd)


# ###########################################Glass Box##############################################################
executorCmd = "{java} -cp {jar} {prog} -bg {bg} -outDir {outDir} -sig {sig} -ont {ont} -semantic".format(
    java=java,
    jar=jar,
    prog="uk.ac.man.WhiteBoxForgetting",
    bg=theoryClausal,
    outDir=outDir,
    sig=sig,
    ont=ontologyClausal)

# print(executorCmd, flush=True)
# subprocess.call(executorCmd)

executorCmd = "{java} -cp {jar} {prog} -bg {outDir}/{bg} -ont {outDir}/{ont} -definers {outDir}/{definers} -outDir {outDir} -name {name}".format(
    java=java,
    jar=jar,
    prog="uk.ac.man.AlcReduction",
    ont="GlassBoxSemanticView.clausal",
    bg="GlassBoxSemanticBG.clausal",
    outDir=outDir,
    definers="GlassBox.definers",
    name="GlassBox")

print(executorCmd, flush=True)
subprocess.call(executorCmd)


#######################################################Comparison#####################################################

executorCmd = "{java} -cp {jar} {prog} -ref {outDir}/{ref} -ont {outDir}/{ontFile} -outDir {outDir} -unentailed {unentailed}".format(
    java=java,
    jar=jar,
    prog="uk.ac.man.ForgettingViewCompare",
    ref="glassbox.owl",
    ontFile="Lethe.owl",
    outDir=outDir,
    unentailed="MissingFromGlassBox"
)

# print(executorCmd, flush=True)
# subprocess.call(executorCmd)