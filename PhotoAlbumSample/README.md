# AWS Android SDK Tutorial - Auth, Storage and AppSync

This tutorial will show how to run Photo Album sample app and UI tests locally and walk you through the sample step-by-step to explain and demonstrate how to use the APIs. The Photo Album sample app allows users to sign up, sign in and sign out using Auth with Cognito User Pool; add, delete, update and query albums using AppSync GraphQL; upload and download through S3 buckets using S3 TransferUtility.

## AWS Android SDK

The AWS Android SDK provides a library and documentation for developers to build connected mobile applications using AWS.

### Features / APIs

- [__Authentication__](https://aws-amplify.github.io/docs/android/authentication): APIs and building blocks for developers who want to create user authentication experiences.  
- [__API__](https://aws-amplify.github.io/docs/android/api): Provides a solution for making HTTP requests to REST and GraphQL endpoints.
- [__Storage__](https://aws-amplify.github.io/docs/android/storage): Provides a simple mechanism for managing user content for your app in public, protected or private storage buckets.  

## Run Photo Album Sample App locally

### Prerequisites

To run this sample, you need the following:

- An IDE (Android Studio recommended)
- Amplify CLI to generate required AWS resources for Auth, Storage and API
- Add AWS dependencies to your project in gradle

### Setting up the Sample app

- Git clone the sample GitHub repo. The sample app root is PhotoAlbumSample.
- Add required AWS resources using Amplify CLI. You can do this either manually or automatically.
- Import the sample as a project in an IDE.

### AWS Amplify CLI

The AWS Amplify CLI is a toolchain which includes a robust feature set for simplifying mobile and web application development. 

* [Install the CLI](#install-the-cli)
* [Start building your app](https://aws-amplify.github.io/docs)

### Install the CLI

 - Requires Node.js® version 8.11.x or later

Install and configure the Amplify CLI as follows:

```bash
$ npm install -g @aws-amplify/cli
$ amplify configure
```

### Generate AWS resources for the sample app using Amplify CLI

Manual steps:

  * In command line, `cd <your-project-root>` to enter your project root.
  * Run `amplify configure` to configure the AWS access credentials, AWS Region and sets up a new AWS User Profile.
  * Run `amplify init` to initialize a new project, that is set up deployment resources in the cloud and prepares your project for Amplify.
  * Run `amplify add <category>` to Adds cloud features to your app. Here, we use `auth`, `storage` and `api` as `<category>`. You can add them one by one.
  * Run `amplify push` to provise cloud resources with the latest local developments.
  
Automatic steps: 

  * Run `git clone https://github.com/AaronZyLee/amplify-cli.git -b integtest --single-branch` to clone amplify-cli repo.
  * `cd amplify-cli` and run `npm run setup-dev` to build amplify-cli
  * `cd packages/amplify-ui-tests`
  * Run `npm run config <your-project-root> android <list: categories>`. `<list: categories>` includes `auth`, `storage` and `api`
  * Before you run adding api for your cloud, paste your own GraphQL schema file under `./schemas`, change the file name to `simple_model.graphql`. The GraphQL schema is defined [here](https://github.com/changxu0306/aws-android-sdk-sample/blob/master/PhotoAlbumSample/simple_model.graphql).

## Run Android Instrumental tests in command line

 * `cd <your-project-root>` to enter your project root.
 * Run `bash gradlew connectedAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.amazonaws.android.samples.photosharing.<test_name>` to run one UI test. `<test_name>` is the test class name under androidTest directory.

## Structure of the sample app

The Photo Album sample app is split into 11 classes, 4 of which are activities.

Activities:

- `LoginActivity` is where the app starts. It provides a drop-in UI for a user to sign up or sign in.
- `AlbumActivity` displays all the albums belong to the user who signed in by querying AppSync. User can create an album, delete an album, update an album's name or sign out in this activity.
- `PhotoActivity` allows user upload an image file or download image files from an S3 bucket. After download each image file, it will show up on the screen in a grid view.
- `DownloadSelectionActivity` allows the user to select which items from S3 to download.

Other classes:

- `AppSyncHelper` class provides AppSync utilities for the sample app. When an AppSyncHelper object is created, it will initialize AWS AppSyncClient with Cognito User Pools by its constructor. Within the class, we provide multiple queries and mutations (create, update, delete) for albums and photos.
- `StorageHelper` class provides S3 TransferUtility for the sample app. It also provides useful type convertion APIs between bitmaps, files, drawables and URIs.
- `Album` and `Photo` classes are defined for album and photo objects. Make it clear for Object-Oriented Programming.
- `AlbumAdapter` and `PhotoAdapter` are customized adapters which bind attributes of an Album or Photo object with a UI cell in grid view. Listeners are defined when clicking add button and delete button. It allows UI changes dynamically with the latest view.
