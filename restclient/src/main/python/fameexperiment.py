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
jar = "/Users/mostafa/Projects/forget/fame/build/libs/fame-dep-1.0.jar"
repository = "/Users/mostafa/Projects/forget/restclient/ontologies/cookedontologieswithequivalences"
coverageRepo = "/Users/mostafa/Projects/forget/restclient/ontologies/conceptcoveragewithequivalences"
outDir = "/Users/mostafa/Projects/forget/semanticforgettingonbioportal/"
coverage = "100"
experimentName = ""
ontology = ""
signature = ""

argv = sys.argv[1:]
opts, args = getopt.getopt(argv, "hn:", ["exName="])
for opt, arg in opts:
    if opt in ("-n", "--exName"):
        outDir = outDir + arg
        experimentName = arg
        ontology = outDir + '/' + 'ontology.clausal.owl'
        signature = outDir + '/' + 'signature'
    else:
        print('fameexperiment.py -n <experimentName>')
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



print("Running Fame")
########################################################Run Experiment##############################################

fameCmd = '{java} -cp {jar} {prog} -ont {ont} -outDir {outDir} -sig {signature}'.format(java=java,
                                                                                        jar=jar,
                                                                                        prog="uk.ac.man.ForgetFame",
                                                                                        ont=ontology,
                                                                                        outDir=outDir,
                                                                                        signature=signature)

print(fameCmd, flush=True)
subprocess.call(fameCmd, shell=True)