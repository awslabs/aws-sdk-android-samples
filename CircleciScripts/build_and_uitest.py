"""
author: Chang Xu
owner: com.amazonaws
"""

from utility_functions import runcommand
from uitests_exceptions import *
import os
import json
from shutil import rmtree
from shutil import copyfile

# Get a list of test class by reading from json file
def get_test_names_from_json(test_names_json_file):
    with open(test_names_json_file) as json_file:
        test_names = json.load(json_file)
        ui_test_names = []
        for category in test_names.keys():
            for test_name in test_names[category]:
                  ui_test_names.append(test_name)
    return ui_test_names

def build_and_uitest(circleci_root_directory, app_name, app_repo_root_directory):
    # make a directory to store test results
    uitest_results_directory = circleci_root_directory + '/uitest_android_results'
    try:
        if os.path.exists(uitest_results_directory):
            # if the directory already exists, delete it
            rmtree(uitest_results_directory)
        os.mkdir(uitest_results_directory)
        print('uitest_android_results directory is created.')
    except OSError as err:
        print('Cannot locate uitest_android_results.\n')
        raise CreateResultsFolderException(appname=app_name, message=str(err))

    # cd into app root directory
    app_root_directory = '{0}/{1}'.format(app_repo_root_directory, app_name)
    try:
        os.chdir(app_root_directory)
    except OSError as err:
        print('Can not locate app_root_directory.\n')
        raise InvalidDirectoryBuildAndTest(appname=app_name, message=str(err))

    # build and run ui test
    print('Run UI Tests...')
    ui_tests = get_test_names_from_json("{0}/CircleciScripts/test_names.json".format(app_repo_root_directory))
    for ui_test in ui_tests:
        run_uitest(ui_test = ui_test, app_name = app_name)
        store_uitest_results(ui_test = ui_test,
                             app_root_directory = app_root_directory,
                             uitest_results_directory = uitest_results_directory,
                             app_name = app_name)

def run_uitest(ui_test, app_name):
    # command to run ui tests
    runcommand(command = "bash gradlew connectedAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.amazonaws.android.samples.photosharing.{0}".format(ui_test),
               exception_to_raise = RunTestSuiteException(appname=app_name))

def store_uitest_results(ui_test, app_root_directory, uitest_results_directory, app_name):
    filename = app_root_directory + '/app/build/reports/androidTests/connected/com.amazonaws.android.samples.photosharing.{0}.html'.format(
        ui_test)
    try:
        copyfile(filename, uitest_results_directory + '/com.amazonaws.android.samples.photosharing.{0}.html'.format(ui_test))
    except FileNotFoundError as err:
        print('Can not find file ' + filename + '\n')
        raise InvalidDirectoryBuildAndTest(appname=app_name, message=str(err))
