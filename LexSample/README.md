Running Lex Sample
=============================================
This sample demonstrates how to use Amazon Lex interaction client library on Android.  This application uses AWS Cognito for authentication with Amazon Lex.

## Requirements

* [AndroidStudio 3.2+](https://developer.android.com/studio/)
* Android API 21+
* [Amplify CLI 0.1.45+](https://aws-amplify.github.io/docs/)

## Using the Sample

1. Import the LexSample project into Android Studio.
    - From the Welcome screen, click on "Import project".
    - Browse to the LexSample directory and press OK.
    - Accept the messages about adding Gradle to the project.
    - If the SDK reports some missing Android SDK packages (like Build Tools or the Android API package), follow the instructions to install them.

1. Import the libraries :
    - Gradle will take care of downloading these dependencies for you.

1. This sample uses the Amplify CLI to setup your backend resources including Amazon Cognito for authentication and Amazon Lex for interactions (the bot).
    - Inside LexSample, initialize an Amplify project: `cd LexSample && amplify init`
    - Create a sample bot: `amplify add interactions`

        Sample input/output:

        ```
        > amplify add interactions
        Using service: Lex, provided by: awscloudformation

        Welcome to the Amazon Lex chatbot wizard
        You will be asked a series of questions to help determine how to best construct your chatbot.

        ? Provide a friendly resource name that will be used to label this category in the project: lexsample
        ? Would you like to start with a sample chatbot, import a chatbot, or start from scratch? Start with a sample
        ? Choose a sample chatbot: BookTrip
        ? Please indicate if your use of this bot is subject to the Children's Online Privacy Protection Act (COPPA).
        Learn more: https://www.ftc.gov/tips-advice/business-center/guidance/complying-coppa-frequently-asked-questions No
        Successfully added resource
        ```

    - Provision these resources: `amplify push`

1. Run the app. Select Text Demo or Voice Demo as appropriate, and try out some of the sample utterance you have configured on the Amazon Lex console.
