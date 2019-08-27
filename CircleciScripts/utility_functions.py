import sys
from subprocess import Popen, PIPE, TimeoutExpired
import os
from datetime import datetime
import platform
import re

def runcommand(command, timeout=0, pipein = None, pipeout = None, logcommandline = True,  workingdirectory = None, exception_to_raise = None):
    if logcommandline:
        print("running command: ", command, "......")
    process = Popen(command, shell = True, stdin = pipein, stdout = pipeout, cwd = workingdirectory)
    wait_times = 0
    while True:
        try:
            process.communicate(timeout = 10)
        except TimeoutExpired:
            # tell circleci I am still alive, don't kill me
            if wait_times % 30 == 0 :
                print(str(datetime.now()) + ": I am still alive")
            # if time costed exceed timeout, quit
            if timeout > 0 and wait_times > timeout * 6 :
                print(str(datetime.now()) + ": time out")
                return 1
            wait_times += 1

            continue
        break
    exit_code = process.wait()
    if exit_code != 0 and exception_to_raise != None:
        raise exception_to_raise
    return exit_code

def getmodules(root):
    with open(os.path.join(root, "settings.gradle")) as f:
        lines = f.readlines()
    modules = []
    for line in lines:
        m = re.match(".*':(aws-android-sdk-.*).*'", line)
        if m is not None:
            modules.append(m.group(1))
        else:
            print("{0} is not a sdk module ".format(line))
    return modules

def replacefiles(root, replaces):
    for replaceaction in replaces:
        match = replaceaction["match"]
        replace = replaceaction["replace"]
        files = replaceaction["files"]
        paramters = "-r -i''"
        if platform.system() == "Darwin":
            paramters = "-E -i ''"
        exclude=""
        if 'exclude' in replaceaction:
            exclude = "/{0}/ ! ".format(replaceaction['exclude'])
        for file in files:
            targetfile = os.path.join(root, file)
            runcommand(command = "sed {4}   '{3}s/{0}/{1}/'  '{2}'".format(match, replace, targetfile, exclude, paramters), logcommandline = True)
