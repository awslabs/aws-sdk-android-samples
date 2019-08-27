"""
author: Phani Srikar Edupuganti 
owner: com.amazonaws
"""

from uitests_exceptions import *
from fetch_sources_and_build_artifacts import fetch_sources_and_build_artifacts
from shutil import rmtree
import os


def setup_local_maven(app_repo_root_directory, appname):

    current_directory = os.getcwd()

    ## cd into app root directory
    app_root_directory = '{0}/{1}'.format(app_repo_root_directory, appname)
    try:
        os.chdir(app_root_directory)
    except OSError as err:
        os.chdir(current_directory)
        raise InvalidDirectoryException(appname = appname, message = str(err))


    maven_artifacts_directory = '{0}/uitests_maven_artifacts'.format(app_root_directory)

    if os.path.isdir(maven_artifacts_directory):
        rmtree(maven_artifacts_directory)

    try:
        os.mkdir(maven_artifacts_directory)
        os.chdir(maven_artifacts_directory)
    except OSError as err:
        os.chdir(current_directory)
        raise InvalidDirectoryException(appname = appname, message = str(err))

    print("step: 1/1 Fetch source files and Generate Artifacts \n ")
    fetch_sources_and_build_artifacts(appname = appname,
                                      maven_artifacts_directory = maven_artifacts_directory,
                                      app_root_directory = app_root_directory)
