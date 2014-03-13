##Running the Amazon S3 Personal File Store Sample with Web Identity Federation and Google+ Authentication

This *Amazon S3 Personal File Store* sample is fully detailed in the [web identity federation](http://aws.amazon.com/articles/4617974389850313) article.  The sample demonstrates how to use AWS Security Token Service (STS) to give application users specific and constrained permissions to an Amazon S3 bucket.  Each application user will get a "folder" of an Amazon S3 bucket as specified by the role policy.  This README details all the steps necessary to get the sample running with Google+ Authentication.  It assumes you've alredy completed the steps in the [base README](README.md) for setting up the application with Facebook:

###1. Create a Google application and Android client ID
        
1. Visit the [Quick start for Android](https://developers.google.com/+/quickstart/android) guide and follow the instructions to **Enable the Google+ API**. The other steps in this guide will be useful with your future Google+ Apps, but are not necessary for this sample.

2. Under **Application type** select **Installed application**.

3. Under **Installed application type**, select **Android**.

4. Enter the following as your *Package name*: `com.amazonaws.demo.personalfilestore`.

5. Enter the appropriate value for your environment in **Signing certificate fingerprint**.
	![Create Client ID](images/Google_Install_Android_App.png)

###2. Create a second client ID

The kind of credentials required by web identity federation require a second client ID.

1. In the API Console, select your existing project and then click *Create another Client ID*.
	![Another client ID](images/Create_another_client_ID.png)
	
2. Select **Web Application** as **Application Type**. 

3. You do not need to set a redirect URL for this application. 
	![Web application settings](images/Google_Web_Application.png)
	
4. Make note of this client ID, you will use this when configuring the sample. 
	![Web client ID](images/Google_Web_Client_ID.png)

###3. Create the AWS Resources

You can **automate** the creation of AWS Resources or create them **manually**. **Skip** to step 4 for creating the resources **manually**. 

[AWS CloudFormation](https://console.aws.amazon.com/cloudformation/home) can be used to **automate** the creation of the resources thus **skipping step 4**. 

1. If you specified the Google Client Id (Android Application) at the time of stack creation you will already have the user role ARN needed in step 6. **Skip step 4** and **proceed** to step 5.

2. Otherwise **Update Stack** using the [template](https://github.com/awslabs/aws-sdk-android-samples/blob/master/S3_WIF_PersonalFileStore/WIFCloudFormationTemplate.json) and follow the [instructions](https://mobile.awsblog.com/post/Tx3ILZHIKNTQQ83/Simplify-Web-Identity-Federation-Setup-with-AWS-CloudFormation) to add a new Web Identity Federation provider to the stack. Using the Google Client ID (Android Application), you will get the user role ARN required in later steps. **Skip to step 5**. 

###4. Create your role for web identity federation

**Skip to step 5 if you have already created user role using the instructions in step 3.**

1. Visit the [AWS Management Console](https://console.aws.amazon.com/iam/home) to create a **new** role.  
	![](images/Create_New_Role.png)
	
2. Give your role a meaningful name, such as **GoogleWIFS3FileStore**.  
	![](images/Google_Role_Name.png)
	
3. Select **Role for Web Identity Provider Access** as your role type.  
	![](images/Select_WIF_Role.png)
	
4. Select Google as the Identity Provider and provide the **Client ID** for the **Android Application** you generated with Google.  
	![](images/Role_With_Google.png)
	
  5. Click Continue when prompted to verify the role trust policy.
  
  6. Select **Custom Policy** when asked to set permissions. This allows us to enter our policy as JSON.  
	![](images/Select_Custom_Policy.png)

  7. Give the policy a name and enter the following JSON as the **Policy Document**, replacing `__BUCKET_NAME__` with the S3 bucket you created earlier: 
    
  	```
    {
       "Version":"2012-10-17",
       "Statement":[{
         "Effect":"Allow",
         "Action":["s3:ListBucket"],
         "Resource":["arn:aws:s3:::__BUCKET_NAME__"],
         "Condition": 
           {"StringLike": 
             {"s3:prefix":"${accounts.google.com:sub}/*"}
           }
        },
        {
         "Effect":"Allow",
         "Action":["s3:GetObject", "s3:PutObject", "s3:DeleteObject"],
         "Resource":[
             "arn:aws:s3:::__BUCKET_NAME__/${accounts.google.com:sub}",
             "arn:aws:s3:::__BUCKET_NAME__/${accounts.google.com:sub}/*"
         ]
        }
       ]
    }
  	```

8. Review the information you entered and click **Create Role** to finish creating your role.  
	![](images/Confirm_Google_WIF_Role.png)

9. Select the Role and switch to the **Summary** tab. Take note of the **Role ARN**; you'll use it in configuring the sample.  
	![](images/Google_Role_ARN.png)


###5. Enable Google Code

1. Open Eclipse and modify `Login.java`  

	Change `/* GOOGLE_LOGIN BEGIN` to `/* GOOGLE_LOGIN BEGIN */` to enable the Google login button.
	
2. Right click `GoogleLogin.java_` and select *Refactor*->*Rename*.  Remove the underscore and click **OK**.
    
###6. Update sample configuration

1. Open the `res/values/strings.xml` file in Eclipse.

2. Enter the **client ID** for the **Web Application** and role ARN where noted:

	```
	<string name="google_client_id">GOOGLE_CLIENT_ID</string>
	<string name="google_role_arn">ROLE_ARN</string>
	```

###7. Run the sample
	 
Run the sample on your Android device.

**Note:** The sample requires the **Google Play Services** application, which cannot be installed in the emulator.