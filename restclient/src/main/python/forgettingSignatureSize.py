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
baseDir = "c:/ccviews/forget/experiment/"
sigSize = "10"
outDir = "NA"
experimentName = "NA"

# argv = sys.argv[1:]
# opts, args = getopt.getopt(argv, "hn:f:", ["exName=", "sig="])
# for opt, arg in opts:
#     if opt in ("-n", "--exName"):
#         outDir = baseDir + arg
#         experimentName = arg
#     elif opt in ("-f", "--sig"):
#         sigSize = arg
#     else:
#         print('forgettingSignatureSize.py -n <experimentName> -f <signatureSize>')
#         sys.exit()


sig = outDir + "/data/signature"
outFile = baseDir + "/sigSize.csv"
executorCmd = "{java} -cp {jar} {prog} -sig {sig} -name {name} -outFile {outFile}".format(
    java=java,
    jar=jar,
    prog="uk.ac.man.ForgettingSignatureSize",
    sig=sig,
    outFile=outFile,
    name=experimentName
)

print(executorCmd, flush=True)
subprocess.call(executorCmd)
