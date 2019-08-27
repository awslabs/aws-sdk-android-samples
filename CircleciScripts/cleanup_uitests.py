"""
author: Phani Srikar Edupuganti
owner: com.amazonaws
"""


from utility_functions import runcommand
from uitests_exceptions import *
from shutil import rmtree
import os

## Requirements: aws-cli, npm, yarn, git cli tools

def delete_aws_resources(app_repo_root_directory, appname):

    app_root_directory = "{0}/{1}".format(app_repo_root_directory, appname)
    pathToCliRepo = app_root_directory + '/configure-aws-resources/amplify-cli'

    try:
        os.chdir(app_root_directory + '/configure-aws-resources/amplify-cli/packages/amplify-ui-tests')
    except OSError as err:
        raise OSErrorDeleteResources(appname, [str(err)])

    configure_command = "npm run delete {0}".format(app_root_directory)

    print("Running delete_aws_resources... \n")
    runcommand(command = configure_command,
               exception_to_raise = CliDeleteResourcesException(appname))

    try:
        if os.path.isdir(pathToCliRepo):
            rmtree(pathToCliRepo)
    except OSError as err:
        raise OSErrorDeleteCliRepo(appname, [str(err)])