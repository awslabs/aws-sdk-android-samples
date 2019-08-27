"""
author: Phani Srikar Edupuganti
owner: com.amazonaws
"""


from uitests_exceptions import *
import re
from os import remove, rename

def bump_appsync_version(root, newsdkversion, appname):

    # Change appsync source version to 100.100.100

    try:
        os.chdir(root)
    except OSError as err:
        raise InvalidDirectoryException(appname=appname, message=str(err))

    gradle_properties_path = "{0}/gradle.properties".format(root)
    gradle_properties_copy_path = "{0}/gradle.properties_copy".format(root)

    pattern = re.compile(r"^\s*VERSION_NAME\s*=.*")
    sub = "VERSION_NAME={0}".format(newsdkversion)

    with open(gradle_properties_copy_path, 'w+') as new_file:
        with open(gradle_properties_path) as old_file:
            for line in old_file:
                print(line)
                new_file.write(re.sub(pattern, sub, line))

    # Remove original file
    remove(gradle_properties_path)

    # Rename new file
    rename(gradle_properties_copy_path, gradle_properties_path)


