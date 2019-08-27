"""
author: Phani Srikar Edupuganti
owner: com.amazonaws
"""

from uitests_exceptions import *
import re
from os import remove, rename

def add_local_dependency_app_build_gradle(app_root_directory,
                                          branches_to_uitest,
                                          maven_artifacts_directory,
                                          newsdkversion ,
                                          appname):

    ## Add maven_artifacts_directory as local repo
    ## Update SDK dependencies to 100.100.100 version

    try:
        os.chdir(app_root_directory + '/app')
    except OSError as err:
        raise InvalidDirectoryException(appname=appname, message=str(err))

    build_gradle_path = "{0}/build.gradle".format(app_root_directory + '/app')
    build_gradle_copy_path = "{0}/build.gradle_copy".format(app_root_directory + '/app')

    with open(build_gradle_copy_path, 'w+') as new_file:
        with open(build_gradle_path) as old_file:
            for line in old_file:
                line = re.sub(re.compile(r"####LOCAL_REPO_PATH####"), 'file:/' + maven_artifacts_directory, line)
                if branches_to_uitest['default'] != 'master':
                    line = re.sub(re.compile(r"^\s*def\s+aws_version\s*=\s*\".+\""), "  def aws_version = '{0}'  ".format(newsdkversion), line)
                if branches_to_uitest['appsync'] != 'master':
                    line = re.sub(re.compile(r"^\s*def\s+aws_appsync_version\s*=\s*\".+\""), "  def aws_appsync_version = '{0}-SNAPSHOT'  ".format(newsdkversion), line)
                new_file.write(line)

    # Remove original file
    remove(build_gradle_path)

    # Rename new file
    rename(build_gradle_copy_path, build_gradle_path)
