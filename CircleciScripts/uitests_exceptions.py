"""
author: Phani Srikar Edupuganti
owner: com.amazonaws
"""

import sys
import os

### setup local maven exceptions
class SetupLocalMavenException(Exception):
    def __init__(self, appname, message=None):
        self.appname = appname
        self.message = self.construct_error_message_setup_maven(message)

    def construct_error_message_setup_maven(self, message):
        localmessage = ["...Unable to setup local maven required for {0} app. Skipping.... ".format(self.appname)]
        if message == None:
            return localmessage
        else: return localmessage.extend(message)


class InvalidDirectoryException(SetupLocalMavenException):
    def __init__(self, appname, message):
        localmessage = ["...Unable to find required directory for {0} app...".format(appname)]
        if message != None:
            message = localmessage.extend(message)
        else: message = localmessage
        super(InvalidDirectoryException, self).__init__(appname, message = message)

class FetchBuildException(SetupLocalMavenException):
    def __init__(self, appname, message=None):
        self.appname = appname
        self.message = self.construct_error_message_fetch_source(message)
        super(FetchBuildException, self).__init__(appname, message=self.message)

    def construct_error_message_fetch_source(self, message):
        localmessage = ["...Failed to fetch source and build artifacts for {0} app...".format(self.appname)]
        if message == None:
            return localmessage
        else: return localmessage.extend(message)


class FetchRemoteSourceException(FetchBuildException):
    def __init__(self, appname, source_name, message=None):
        localmessage = ["...Unable to fetch {0} source for app {1}. check source url and retry...".format(source_name, appname)]
        if message != None:
            message = localmessage.extend(message)
        else: message = localmessage
        super(FetchRemoteSourceException, self).__init__(appname, message = message)

class BuildSourceException(FetchBuildException):
    def __init__(self, appname, source_name, message=None):
        localmessage = ["...Unable to fetch {0} source for app {1}. check source url and retry...".format(source_name, appname)]
        if message != None:
            message = localmessage.extend(message)
        else: message = localmessage
        super(BuildSourceException, self).__init__(appname, message = message)


### Build and uitest exceptions

class BuildAndUItestException(Exception):
    def __init__(self, appname, message=None):
        self.appname = appname
        self.message = self.construct_error_message_build_and_uitest(message)

    def construct_error_message_build_and_uitest(self, message):
        localmessage = ["...Unable to build and test {0} app. Skipping.... ".format(self.appname)]
        if message == None:
            return localmessage
        else: return localmessage.extend(message)

class CreateResultsFolderException(BuildAndUItestException):
    def __init__(self, appname, message=None):
        localmessage = ["...Unable to create results folder for {0} app...".format(appname)]
        if message != None:
            message = localmessage.extend(message)
        else: message = localmessage
        super(CreateResultsFolderException, self).__init__(appname, message = message)

class InvalidDirectoryBuildAndTest(BuildAndUItestException):
    def __init__(self, appname, message=None):
        localmessage = ["...Unable to find required directory for {0} app...".format(appname)]
        if message != None:
            message = localmessage.extend(message)
        else: message = localmessage
        super(InvalidDirectoryBuildAndTest, self).__init__(appname, message = message)

class RunTestSuiteException(BuildAndUItestException):
    def __init__(self, appname):
        message = ["...Build and Test command failed for app {0}. Skip...".format(appname)]
        super(RunTestSuiteException, self).__init__(appname, message = message)

### Configure AWS Resources Excepitons

class ConfigureAWSResourcesException(Exception):
    def __init__(self, appname, message=None):
        self.appname = appname
        self.message = self.construct_error_message_build_and_uitest(message)

    def construct_error_message_build_and_uitest(self, message):
        localmessage = ["...Unable to Configure AWS Resources needed for {0} app. Skipping.... ".format(self.appname)]
        if message == None:
            return localmessage
        else: return localmessage.extend(message)

class OSErrorConfigureResources(ConfigureAWSResourcesException):
    def __init__(self, appname, message=None):
        localmessage = ["...Unable to replace api schema for app {0}...".format(appname)]
        if message != None:
            message = localmessage.extend(message)
        else: message = localmessage
        super(OSErrorConfigureResources, self).__init__(appname, message = message)

class GitCloneCliException(ConfigureAWSResourcesException):
    def __init__(self, appname):
        message = ["...Failed to clone amplify-cli repo for app {0}. Skip...".format(appname)]
        super(GitCloneCliException, self).__init__(appname, message = message)

class CliSetupDevException(ConfigureAWSResourcesException):
    def __init__(self, appname):
        message = ["...Failed to run npm setup-dev for app {0}. Skip...".format(appname)]
        super(CliSetupDevException, self).__init__(appname, message = message)

class CliConfigException(ConfigureAWSResourcesException):
    def __init__(self, appname):
        message = ["...Failed to run npm config to create AWS Resources for app {0}. Skip...".format(appname)]
        super(CliConfigException, self).__init__(appname, message = message)

class OSErrorDeleteResources(ConfigureAWSResourcesException):
    def __init__(self, appname, message=None):
        localmessage = ["...Failed to navigate to cli delete resources script for app {0}. Skip...".format(appname)]
        if message != None:
            message = localmessage.extend(message)
        else: message = localmessage
        super(OSErrorDeleteResources, self).__init__(appname, message = message)

class CliDeleteResourcesException(ConfigureAWSResourcesException):
    def __init__(self, appname):
        message = ["...Failed to run npm delete to bring down AWS Resources for app {0}. Skip...".format(appname)]
        super(CliDeleteResourcesException, self).__init__(appname, message = message)

class OSErrorDeleteCliRepo(ConfigureAWSResourcesException):
    def __init__(self, appname, message=None):
        localmessage = ["...Failed to delete the cloned amplify-cli repo for app {0}. Skip...".format(appname)]
        if message != None:
            message = localmessage.extend(message)
        else: message = localmessage
        super(OSErrorDeleteCliRepo, self).__init__(appname, message = message)
