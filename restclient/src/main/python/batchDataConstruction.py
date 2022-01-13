import sys
import os
import subprocess
import shutil

java = "/usr/bin/java"
jar = "/Users/mostafa/Projects/forget/restclient/build/libs/restclient-dep-1.0.jar"
repository = "/Users/mostafa/Projects/forget/restclient/ontologies"
outRep = "/Users/mostafa/Projects/forget/data/"
outOntologies = "/Users/mostafa/Projects/forget/data/ontologies"
experimentScript = "/Users/mostafa/Projects/forget/restclient/src/main/python/dataConstruction.py"

try:
    os.makedirs(outOntologies)
except FileExistsError:
    print("ontologies folder already exists")
    for file in os.listdir(outOntologies):
        file_path = os.path.join(outOntologies, file)
        try:
            if os.path.isfile(file_path): os.unlink(file_path)
            elif os.path.isdir(file_path): shutil.rmtree(file_path)
        except Exception as e:
            print(e)

for i in range(4, 29):
    name = "experiment" + str(i)
    outDir = outRep + name
#     try:
#         os.makedirs(outDir)
#     except FileExistsError:
#         print ("Output directory exists. Directory will be emptied and recycled")
#
#     for file in os.listdir(outDir):
#         file_path = os.path.join(outDir, file)
#         try:
#             if os.path.isfile(file_path):
#                 if file != "output.log":
#                     os.unlink(file_path)
#             elif os.path.isdir(file_path): shutil.rmtree(file_path)
#         except Exception as e:
#             print(e)

    print(name + " started")
    executorCmd = "/Users/mostafa/anaconda3/bin/python {script} -n {name} -f 50".format(script=experimentScript, name=name)
    with open(outDir + '/output.log', 'w') as out:
        subprocess.call(executorCmd, stdout=out, stderr=out, shell=True)
    print(name + " complete")
