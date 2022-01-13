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
repository = "c:/ccviews/forget/restclient/ontologies/bioportal"
classes = "c:/ccviews/forget/restclient/ontologies/classes.txt"
outDir = "c:/ccviews/forget/experiment/"
sigSize = "10"

argv = sys.argv[1:]
opts, args = getopt.getopt(argv, "hn:f:", ["exName=", "sig="])
for opt, arg in opts:
    if opt in ("-n", "--exName"):
        outDir = outDir + arg
    elif opt in ("-f", "--sig"):
        sigSize = arg
    else:
        print('experiment.py -n <experimentName> -f <signatureSize>')
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
#         # elif os.path.isdir(file_path): shutil.rmtree(file_path)
#     except Exception as e:
#         print(e)

plannerCmd = '{java} -cp {jar} {prog} -repo {repo} -classes {classes} -sigSize {sigSize} -outDir {outDir}/data -verbose'.format(java=java,
                                                                                                             jar=jar,
                                                                                                             prog="uk.ac.man.ExperimentPlanner",
                                                                                                             repo=repository,
                                                                                                             classes=classes,
                                                                                                             outDir=outDir,
                                                                                                             sigSize=sigSize)

# print(plannerCmd, flush=True)
# returned_value = subprocess.call(plannerCmd)

# print('Experiment planner executed with returned value:', returned_value)

ontologies = glob.glob("{outDir}/data/*.descriptor".format(outDir=outDir))
sig = outDir + "/data/signature"
# for ontology in ontologies:
#     # print("Processing ontology from " + ontology)
#     fileDownloaded = 0
#     data = {}
#     with open(ontology) as json_file:
#         data = json.load(json_file)
#     if "local_iri" not in data:
#         iri = data['online_iri']
#         local_iri = "{repo}/{ont}".format(repo=repository, ont=data['name'])
#         print("Downloading {ont} to {loc}...".format(ont=data['name'], loc=local_iri), flush=True)
#         try:
#             response = requests.get(iri)
#             if response.status_code == 200:
#                 with open(local_iri, 'wb') as f:
#                     f.write(response.content)
#                     print("Download complete: {ont}".format(ont=data['name']), flush=True)
#                 data['local_iri'] = local_iri
#                 with open(ontology, 'w') as descriptor_file:
#                     json.dump(data, descriptor_file)
#                 print(loaderCmd)
#                 subprocess.call(loaderCmd)
#             else:
#                 print("Download {ont} failed".format(ont=data['name']), flush=True)
#         except MemoryError as e:
#             print("Memory error occured while downloading " + data['name'], flush=True)
#     else:
#         loaderCmd = "{java} -cp {jar} {prog} -repo {repo} -outDir {outDir}/data -sig {sig} -ont {ont} -verbose".format(
#             java=java,
#             jar=jar,
#             prog="uk.ac.man.DataLoader",
#             repo=repository,
#             outDir=outDir,
#             sig=sig,
#             ont=ontology)
#         print(loaderCmd, flush=True)
#         subprocess.call(loaderCmd)

# ###########################################Input Generation#######################################################
ontologies = glob.glob("{outDir}/data/*.clausal".format(outDir=outDir))
ontologyOWL = "{outDir}/Ontology.owl".format(outDir=outDir)
theoryOWL = "{outDir}/Theory.owl".format(outDir=outDir)
ontologyClausal = "{outDir}/Ontology.clausal".format(outDir=outDir)
theoryClausal = "{outDir}/Theory.clausal".format(outDir=outDir)

# randomOnt = random.choice(ontologies)
#
# executorCmd = "{java} -cp {jar} {prog} -bg {bg} -outDir {outDir} -ont {ont}".format(
#     java=java,
#     jar=jar,
#     prog="uk.ac.man.InputGenerator",
#     outDir=outDir,
#     bg=ontologies,
#     ont=randomOnt
# )
#
# print(executorCmd, flush=True)
# subprocess.call(executorCmd)

###################################################Lethe########################################################

executorCmd = "{java} -cp {jar} {prog} -ont {ont} -bg {bg} -sig {sig} -outDir {outDir}".format(
    java=java,
    jar=lethejar,
    prog="uk.ac.man.ForgetLethe",
    outDir=outDir,
    ont=ontologyOWL,
    bg=theoryOWL,
    sig=sig)

#print(executorCmd, flush=True)
#subprocess.call(executorCmd)

# ###########################################Glass Box##############################################################
executorCmd = "{java} -cp {jar} {prog} -bg {bg} -outDir {outDir} -sig {sig} -ont {ont} -verbose -semantic".format(
    java=java,
    jar=jar,
    prog="uk.ac.man.WhiteBoxForgetting",
    bg=theoryClausal,
    outDir=outDir,
    sig=sig,
    ont=ontologyClausal)

print(executorCmd, flush=True)
subprocess.call(executorCmd)
# outOntology = "GlassBoxSemanticView.clausal"
# outTheory = "GlassBoxSemanticBG.clausal"
# outDefiners = "GlassBox.definers"

executorCmd = "{java} -cp {jar} {prog} -bg {outDir}/{bg} -ont {outDir}/{ont} -definers {outDir}/{definers} -outDir {outDir} -name {name} -verbose".format(
    java=java,
    jar=jar,
    prog="uk.ac.man.AlcReduction",
    ont="GlassBoxSemanticView.clausal",
    bg="GlassBoxSemanticBG.clausal",
    outDir=outDir,
    definers="GlassBox.definers",
    name="GlassBox")

# print(executorCmd, flush=True)
# subprocess.call(executorCmd)

executorCmd = "{java} -cp {jar} {prog} -bg {outDir}/Theory.owl -outDir {outDir} -ont {outDir}/{intFile} -redundanciesFileName {redFile} -timeLogFileName {timeFile} -verbose".format(
    java=java,
    jar=jar,
    prog="uk.ac.man.RedundancyChecker",
    outDir=outDir,
    intFile="GlassBox.owl",
    redFile="GlassBoxRedundant",
    timeFile="GlassBoxRedundant"
)

#print(executorCmd, flush=True)
#subprocess.call(executorCmd)

executorCmd = "{java} -cp {jar} {prog} -ref {outDir}/{ref} -ont {outDir}/{ontFile} -outDir {outDir} -unentailed {unentailed}".format(
    java=java,
    jar=jar,
    prog="uk.ac.man.ForgettingViewCompare",
    ref="glassbox.owl",
    ontFile="Lethe.owl",
    outDir=outDir,
    unentailed="MissingFromGlassBox"
)

#print(executorCmd, flush=True)
#subprocess.call(executorCmd)

debugDir = "{outDir}/debug".format(outDir=outDir)
executorCmd = "{java} -cp {jar} {prog} -bg {debugDir}/{bg} -outDir {debugDir} -ont {debugDir}/{ont} -restrictions {debugDir}/{redundancies}".format(
    java=java,
    jar=jar,
    prog="uk.ac.man.RedundantResolutions",
    debugDir=debugDir,
    ont="GlassBoxOntology.resolution",
    bg="GlassBoxTheory.resolution",
    redundancies="GlassBox.restricted"
)

#print(executorCmd, flush=True)
#subprocess.call(executorCmd)

############################################Black Box#############################################################

executorCmd = "{java} -cp {jar} {prog} -bg {outDir}/background.clausal -outDir {outDir} -sig {sig} -ont {outDir}/ontology.clausal -verbose -semantic".format(
    java=java,
    jar=jar,
    prog="uk.ac.man.ForgetRTEmptyBG",
    outDir=outDir,
    sig=sig)

# print(executorCmd, flush=True)
# subprocess.call(executorCmd)

executorCmd = "{java} -cp {jar} {prog} -bg {outDir}/{bg} -ont {outDir}/{ont} -definers {outDir}/{definers} -outDir {outDir} -name {name} -verbose".format(
    java=java,
    jar=jar,
    prog="uk.ac.man.AlcReduction",
    bg="BlackBoxSemanticBG.clausal",
    outDir=outDir,
    ont="BlackBoxSemanticView.clausal",
    definers="BlackBox.definers",
    name="BlackBox")

# print(executorCmd, flush=True)
# subprocess.call(executorCmd)

executorCmd = "{java} -cp {jar} {prog} -bg {outDir}/background.owl -outDir {outDir} -ont {outDir}/{intFile} -redundanciesFileName {redFile} -timeLogFileName {timeFile} -verbose".format(
    java=java,
    jar=jar,
    prog="uk.ac.man.RedundancyChecker",
    outDir=outDir,
    intFile="BlackBox.owl",
    redFile="BlackBoxRedundant",
    timeFile="BlackBoxRedundant"
)

# print(executorCmd, flush=True)
# subprocess.call(executorCmd)

executorCmd = "{java} -cp {jar} {prog} -ref {outDir}/{ref} -ont {outDir}/{ontFile} -outDir {outDir} -unentailed {unentailed}".format(
    java=java,
    jar=jar,
    prog="uk.ac.man.ForgettingViewCompare",
    ref="BlackBox.owl",
    ontFile="Lethe.owl",
    outDir=outDir,
    unentailed="MissingFromBlackBox"
)

# print(executorCmd, flush=True)
# subprocess.call(executorCmd)
