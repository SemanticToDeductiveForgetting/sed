import sys
import os
import subprocess
import shutil

repository = "c:/ccviews/forget/restclient/ontologies"
java = "C:/Program Files/Java/jdk-11.0.2/bin/java.exe"
jar = "c:/ccviews/forget/restclient/build/libs/restclient-dep-1.0.jar"

for file in os.listdir(repository):
    file_path = os.path.join(repository, file)
    if os.path.isfile(file_path):
        if file != "blacklist" and file != "classes.txt" and file != "metrics.csv":
            print("Checking expressivity of " + file)
            summaryCmd = '{java} -cp {jar} {prog} {repo} {ontology}'.format(java=java,
                                                                            jar=jar,
                                                                            prog="uk.ac.man.ExpressivityChecker",
                                                                            repo=repository,
                                                                            ontology=file)
            print(summaryCmd, flush=True)
            returned_value = subprocess.call(summaryCmd)
