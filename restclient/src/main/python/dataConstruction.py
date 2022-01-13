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
lethejar = "/Users/mostafa/Projects/forget/lethe/build/libs/lethe-dep-1.0.jar"
repository = "/Users/mostafa/Projects/forget/restclient/ontologies/bioportal/all"
classes = "/Users/mostafa/Projects/forget/restclient/ontologies/classes.txt"
outOntologies = "/Users/mostafa/Projects/forget/data/ontologies"
outDir = "/Users/mostafa/Projects/forget/data/"
sigSize = "10"
name = ""

argv = sys.argv[1:]
opts, args = getopt.getopt(argv, "hn:f:", ["exName=", "sig="])
for opt, arg in opts:
    if opt in ("-n", "--exName"):
        name = arg
        outDir = outDir + arg
    elif opt in ("-f", "--sig"):
        sigSize = arg
    else:
        print('experiment.py -n <experimentName> -f <signatureSize>')
        sys.exit()

plannerCmd = '{java} -cp {jar} {prog} -repo {repo} -classes {classes} -sigSize {sigSize} -outDir {outDir}/data -verbose'.format(java=java,
                                                                                                             jar=jar,
                                                                                                             prog="uk.ac.man.ExperimentPlanner",
                                                                                                             repo=repository,
                                                                                                             classes=classes,
                                                                                                             outDir=outDir,
                                                                                                             sigSize=sigSize)

# print(plannerCmd, flush=True)
# returned_value = subprocess.call(plannerCmd, shell=True)

# ontologies = glob.glob("{outDir}/data/*.descriptor".format(outDir=outDir))
# sig = outDir + "/data/signature"
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
#             else:
#                 print("Download {ont} failed".format(ont=data['name']), flush=True)
#         except MemoryError as e:
#             print("Memory error occured while downloading " + data['name'], flush=True)
#
#     loaderCmd = "{java} -cp {jar} {prog} -repo {repo} -outDir {outDir}/data -sig {sig} -ont {ont} -asAxioms -verbose".format(
#         java=java,
#         jar=jar,
#         prog="uk.ac.man.DataLoader",
#         repo=repository,
#         outDir=outDir,
#         sig=sig,
#         ont=ontology)
#     print(loaderCmd, flush=True)
#     subprocess.call(loaderCmd, shell=True)

# ###########################################Input Generation#######################################################
ontologies = glob.glob("{outDir}/data/*.clausal".format(outDir=outDir))
ontologyOWL = "{outDir}/Ontology.owl".format(outDir=outDir)
theoryOWL = "{outDir}/Theory.owl".format(outDir=outDir)
ontologyClausal = "{outDir}/Ontology.clausal".format(outDir=outDir)
theoryClausal = "{outDir}/Theory.clausal".format(outDir=outDir)

randomOnt = random.choice(ontologies)

executorCmd = "{java} -cp {jar} {prog} -bg {bg} -outDir {outDir} -ont {ont}".format(
    java=java,
    jar=jar,
    prog="uk.ac.man.InputGenerator",
    outDir=outDir,
    bg=ontologies,
    ont=randomOnt
)

print(executorCmd, flush=True)
subprocess.call(executorCmd, shell=True)

outOntology = os.path.join(outOntologies, name)
shutil.copy(theoryOWL, outOntology)
