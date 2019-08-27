"""
author: Phani Srikar Edupuganti
owner: com.amazonaws
"""

from utility_functions import runcommand
from uitests_exceptions import *
from bump_sdk_version import bump_sdk_version
from bump_appsync_version import bump_appsync_version
from add_local_dependency_app_build_gradle import add_local_dependency_app_build_gradle
from shutil import rmtree


def fetch_sources_and_build_artifacts(appname, maven_artifacts_directory, app_root_directory):

    ## fetch and build sdk source files from default(aws android) repo

    branches_to_uitest = {'default': os.environ['android_sdk_branch_to_uitest'],
                          'appsync': os.environ['appSync_branch_to_uitest']}

    newsdkversion = '100.100.100'

    print('********* branches to uitest: ', branches_to_uitest, '\n')

    sdk_branch_to_uitest = branches_to_uitest['default']
    source_url = 'https://github.com/aws-amplify/aws-sdk-android.git'
    sdk_source_path = app_root_directory + '/sdk_source'

    try:
        if os.path.isdir(sdk_source_path):
            rmtree(sdk_source_path)
        os.mkdir(sdk_source_path)
        os.chdir(sdk_source_path)
    except OSError as err:
        raise InvalidDirectoryException(appname = appname, message = str(err))


    if sdk_branch_to_uitest != 'master':

        runcommand(command = "git clone {0} -b {1} --depth 1 default".format(source_url, sdk_branch_to_uitest),
                   exception_to_raise = FetchRemoteSourceException(appname, 'default'))

        ## Change source version to 100.100.100
        bump_sdk_version(root = "{0}/{1}".format(sdk_source_path, 'default'),
                         newsdkversion = newsdkversion)

        ## Build default SDK source files
        try:
            os.chdir("{0}/{1}".format(sdk_source_path, 'default'))
        except OSError as err:
            raise InvalidDirectoryException(appname=appname, message=str(err))

        runcommand(command = "MAVEN_OPTS='-Xms1024m -Xmx2048m -XX:PermSize=512m -XX:MaxPermSize=1024m'",
                   exception_to_raise = BuildSourceException(appname, 'default'))

        runcommand(command="mvn clean install -DskipTests=true -Dmaven.repo.local={0}".format(maven_artifacts_directory),
                   exception_to_raise=BuildSourceException(appname, 'default'))

    sdk_branch_to_uitest = branches_to_uitest['appsync']
    source_url = 'https://github.com/awslabs/aws-mobile-appsync-sdk-android.git'

    try:
        os.chdir(sdk_source_path)
    except OSError as err:
        raise InvalidDirectoryException(appname = appname, message = str(err))

    if sdk_branch_to_uitest != 'master':

        runcommand(command = "git clone {0} -b {1} --depth 1 appsync".format(source_url, sdk_branch_to_uitest),
                   exception_to_raise = FetchRemoteSourceException(appname, 'appsync'))

        ## Change source version to 100.100.100
        bump_appsync_version(root = "{0}/{1}".format(sdk_source_path, 'appsync'),
                             newsdkversion = newsdkversion,
                             appname = appname)

        ## Build default SDK source files
        try:
            os.chdir("{0}/{1}".format(sdk_source_path, 'appsync'))
        except OSError as err:
            raise InvalidDirectoryException(appname=appname, message=str(err))

        runcommand(command = "MAVEN_OPTS='-Xms1024m -Xmx2048m -XX:PermSize=512m -XX:MaxPermSize=1024m'",
                   exception_to_raise = BuildSourceException(appname, 'default'))

        runcommand(command="bash gradlew publishToMavenLocal -Dmaven.repo.local={0}".format(maven_artifacts_directory),
                   exception_to_raise=BuildSourceException(appname, 'appsync'))

    add_local_dependency_app_build_gradle(app_root_directory = app_root_directory,
                                          branches_to_uitest = branches_to_uitest,
                                          maven_artifacts_directory = maven_artifacts_directory,
                                          newsdkversion = newsdkversion,
                                          appname = appname)

    ## clean up source repos
    try:
        if os.path.isdir(sdk_source_path):
            rmtree(sdk_source_path)
    except OSError as err:
        raise InvalidDirectoryException(appname = appname, message = str(err))
