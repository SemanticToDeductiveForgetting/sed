import sys
import os
import subprocess
import shutil

java = "C:/Program Files/Java/jdk-11.0.2/bin/java.exe"
jar = "c:/ccviews/forget/restclient/build/libs/restclient-dep-1.0.jar"
repository = "c:/ccviews/forget/restclient/ontologies"
outRep = "c:/ccviews/forget/experiment/"
experimentScript = "c:/ccviews/forget/restclient/src/main/python/forgettingSignatureSize.py"

for i in range(1, 201):
    name = "experiment" + str(i)
    outDir = outRep + name
    # try:
    #     os.makedirs(outDir)
    # except FileExistsError:
    #     print ("Output directory exists. Directory will be emptied and recycled")
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

    print(name + " started")
    executorCmd = "python {script} -n {name} -f 30".format(script=experimentScript, name=name)
    subprocess.call(executorCmd)
    print(name + " complete")
