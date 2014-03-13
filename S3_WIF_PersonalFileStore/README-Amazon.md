## Running the Amazon S3 Personal File Store Sample with Web Identity Federation and Amazon Authentication

This _Amazon S3 Personal File Store_ sample is fully detailed in the [web identity federation](http://aws.amazon.com/articles/4617974389850313) article. The sample demonstrates how to use AWS Security Token Service (STS) to give application users specific and constrained permissions to an Amazon S3 bucket. Each application user will get a "folder" of an Amazon S3 bucket as specified by the role policy. This README details all the steps necessary to get the sample running with Amazon Authentication. It assumes you've alredy completed the steps in the [base README](README.md) for setting up the application with Facebook.

###1. Create an Amazon application and Android API key
        
1. Visit the [Getting Started for Android](http://login.amazon.com/android) guide and follow the instructions to **Register a New Application**. Make note of your `App ID`. You'll use it in later steps. The other steps in this guide will be useful with your future Login with Amazon apps, but will not be necessary for this sample. 
	![App ID](images/Amazon_App_ID.png)

2. Under **Label** give the key a meaningful name.

3. Enter the following as your **Package name**: `com.amazonaws.demo.personalfilestore`.

4. Enter the appropriate value for your environment in **Signature**. 
	![Android API Key](images/Amazon_Android_API_Key.png)
	
5. Make sure to click **Get API Key Value** after creating your API key. You'll use this value in configuring the sample.
	![Get Android API Key](images/Amazon_Get_Andorid_API_Key.png)


###2. Create the AWS Resources

You can **automate** the creation of AWS Resources or create them **manually**. **Skip** to step 3 for creating the resources **manually**.

[AWS CloudFormation](https://console.aws.amazon.com/cloudformation/home) can be used to **automate** the creation of the resources thus **skipping step 3**.

This step assumes that you have already created a stack in [CloudFormation](console.aws.amazon.com/cloudformation) following the instructions in the [base README](README.md).

1. If you specified the Amazon App Id at the time of stack creation you will already have the user role ARN needed in step 5. **Skip step 3** and **proceed** directly to **step 4**.

2. Otherwise **Update Stack** using the [template](https://github.com/awslabs/aws-sdk-android-samples/blob/master/S3_WIF_PersonalFileStore/WIFCloudFormationTemplate.json) and follow the [instructions](https://mobile.awsblog.com/post/Tx3ILZHIKNTQQ83/Simplify-Web-Identity-Federation-Setup-with-AWS-CloudFormation) to add a new Web Identity Federation provider to the stack. Using the Amazon App Id, you will get the user role ARN required in later steps. **Skip to step 4**. 

## 3. Create your role for web identity federation

**Skip to step 4 if you have already user role using the instructions in step 2.**

1. Visit the [AWS Management Console](https://console.aws.amazon.com/iam/home) to create a **new** role.  
	![](images/Create_New_Role.png)

2. Give your role a meaningful name, such as **AmazonWIFS3FileStore**.  
	![](images/Amazon_Role_Name.png)

3. Select **Role For Web Identity Provider Access** as your Role Type.  
	![](images/Select_WIF_Role.png)

4. Select Login with Amazon as the Identity Provider and provide the application ID you generated with Amazon.  
	![](images/Role_With_Amazon.png)

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
             {"s3:prefix":"${www.amazon.com:user_id}/*"}
           }
        },
        {
         "Effect":"Allow",
         "Action":["s3:GetObject", "s3:PutObject", "s3:DeleteObject"],
         "Resource":[
             "arn:aws:s3:::__BUCKET_NAME__/${www.amazon.com:user_id}",
             "arn:aws:s3:::__BUCKET_NAME__/${www.amazon.com:user_id}/*"
         ]
        }
       ]
    }
  	```     

8. Review the information you entered and click **Create Role** to finish creating your role.  
	![](images/Confirm_Amazon_WIF_Role.png)

9. Select the Role and switch to the **Summary** tab. Take note of the **Role ARN**; you'll use it in configuring the sample.  
	![](images/Amazon_Role_ARN.png)

###4. Enable Amazon Code

We've bundled the Login with Amazon SDK as part of the sample, but have disabled the authentication code from running.
    
1. Open Eclipse and modify `Login.java`

2. Change `/* AMZN_LOGIN BEGIN` to `/* AMZN_LOGIN BEGIN */` to enable the Amazon login button.
  
###5. Update sample configuration
        
1. Open the `res/values/strings.xml` file in Eclipse.

2. Enter the API key and role ARN where noted:

	```
	<string name="amzn_api_key">AMZN_API_KEY</string>
	<string name="amzn_role_arn">ROLE_ARN</string>
	```

###6. Run the sample
    
Run the sample on your Android device or emulator.