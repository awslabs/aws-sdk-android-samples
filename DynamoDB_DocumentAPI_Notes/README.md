# Android Notes App

To configure the cloud backend:

*  Log in to the AWS Console.
*  Click the **Mobile Hub** console.
*  **Create a New Project**.
*  Click **NoSQL Database**.
*  Click **Enable NoSQL**.
*  Click **Add a new table**.
*  Click **Start with an example schema**.
*  Select **Notes** as the example schema.
*  Select **Public** for the permissions.
*  Click **Create Table**, then click **Create Table** in the dialog.

Alternatively, click the button below to import the project into Mobile Hub:

<p align="center"><a target="_blank" href="https://console.aws.amazon.com/mobilehub/home?#/?config=https://github.com/awslabs/aws-sdk-android-samples/blob/master/DynamoDB_DocumentAPI_Notes/mobile-hub-project.yml"><span><img height="100%" src="https://s3.amazonaws.com/deploytomh/button-deploy-aws-mh.png"/></span></a></p>

*  Click **Resources** in the left-hand sidebar.
*  Make a note of the Amazon Cognito Pool ID and the name of the Notes table.

To configure the app:

*  Open the project in Android Studio.
*  Open the DatabaseAccess.java file.
*  Replace the COGNITO_POOL_ID and DYNAMODB_TABLE variables with the values from your cloud backend.



