# AWS SDK for Android: S3 Transfer Utility Tutorial

The Transfer Utility makes transferring data in and out of S3 quick, reliable, and easy. This tutorial will walk you through the Transfer Utility sample step-by-step to explain and demonstrate how to use the APIs. The Transfer Utility sample app allows users to select pictures and files from their phone, and transfer them into and out of S3 with the ability to pause, resume, cancel and delete transfers. ## PrerequisitesTo run this sample, you need the following:
- An IDE (Android Studio recommended)- The [AWS Mobile SDK for Android v2.2.3](http://docs-aws.amazon.com/mobile/sdkforandroid/developerguide)
## Setting up the SampleTo begin, clone the sample GitHub repo. In the S3 Transfer Utility folder, complete the README instructions to set up a Cognito identity pool, assign permissions, create a bucket, and import the sample into an IDE.## Understanding the structure of the sampleThe Transfer Utility sample is split into 6 classes, 4 of which are activities.- `MainActivity` is where the app starts and lets the user choose if they want to go to the upload or download screen.- The `UploadActivity` allows the user to upload files or pictures to S3. It shows all upload transfers and demonstrates pausing, resuming, cancelling, and deleting transfers.- The `DownloadActivity` allows the user to download files from S3. It shows all download transfers and also demonstrates pausing, resuming, cancelling, and deleting transfers.- The `DownloadSelectionActivity` allows the user to select which items from S3 to download.- The `Util` class contains various functions that are used by both the UploadActivity and DownloadActivity, most importantly it demonstrates holding a single reference to the Transfer Utility throughout the app.- The `Constants` class allows developers to specify their Cognito pool ID, and the name of the bucket they want the app to target.## Understanding the UIBefore we explain how uploading and downloading work, let’s look at the UI design, which is nearly identical for both the `UploadActivity` and `DownloadActivity`. The UIs for these activities contain a list view that shows all transfers along with their state and progress. Each list item in the list view contains a radio button, which is used to select transfers. There is a collection of buttons below the list, which lets the user interact with the selected transfer, or begin a new one.The list of transfers is created using a `SimpleAdapter` and is wired together in the `initUI()` function. The `SimpleAdapter` instance uses the class level variable `transferRecordsMaps`, which is a list of maps. Each map in the `transferRecordsMaps` represents a single transfer. Throughout the sample you will see variations of code that looks like the following:```javaTransferObserver observer = transferUtility.upload(Constants.BUCKET_NAME, file.getName(),
        file);
observers.add(observer);
HashMap<String, Object> map = new HashMap<String, Object>();
Util.fillMap(map, observer, false);
transferRecordMaps.add(map);
``` This is how the UI is updated with a new or refreshed transfer in the sample. New transfers are added to the observers list (so that they can be paused, canceled, deleted, or resumed). Then, we create a new map to represent the transfer. The `fillMap()` function adds data from the `TransferObserver` instance to the new map, which will represent it. The code for that is below:```java/*
 * Fills in the map with information in the observer so that it can be used
 * with a SimpleAdapter to populate the UI
 */
public static void fillMap(Map<String, Object> map, TransferObserver observer, boolean isChecked) {
    int progress = (int) ((double) observer.getBytesTransferred() * 100 / observer
            .getBytesTotal());
    map.put("id", observer.getId());
    map.put("checked", isChecked);
    map.put("fileName", observer.getAbsoluteFilePath());
    map.put("progress", progress);
    map.put("bytes",
            getBytesString(observer.getBytesTransferred()) + "/"
                    + getBytesString(observer.getBytesTotal()));
    map.put("state", observer.getState());
    map.put("percentage", progress + "%");
}
```Every time we add or change something in our list of maps, we must call `simpleAdapter.notifyDataSetChanged()` in order to let the UI know the data has changed.Now that we understand the basics of the UI, let’s see how the sample utilizes the Transfer Utility to interact with S3.## Instantiating the Transfer UtilityWe recommend keeping a single instance of the Transfer Utility (and `AmazonS3Client` if necessary) and sharing it throughout the app. In this sample, the Util class demonstrates how to instantiate an instance that can be shared across your application. To create the Transfer Utility, we must pass it an instance of an `AmazonS3Client`. To create an `AmazonS3Client`, we need a credentials provider. We’ll use Amazon Cognito to authenticate with AWS from our mobile application. We do this in the following way:1. Create three static variables to hold a shared instance of our S3 client, our credentials provider, and our Transfer Utility.
```java// We only need one instance of the clients and credentials provider
private static AmazonS3Client sS3Client;
private static CognitoCachingCredentialsProvider sCredProvider;
private static TransferUtility sTransferUtility;
``` 2. Use a static method to retrieve the credentials provider.
```java/**
 * Gets an instance of CognitoCachingCredentialsProvider which is
 * constructed using the given Context.
 *
 * @param context An Context instance.
 * @return A default credential provider.
 */
private static CognitoCachingCredentialsProvider getCredProvider(Context context) {
    if (sCredProvider == null) {
        sCredProvider = new CognitoCachingCredentialsProvider(
                context.getApplicationContext(),
                Constants.COGNITO_POOL_ID,
                Regions.US_EAST_1);
    }
    return sCredProvider;
}
```java 3. Use a static method to retrieve the S3 client. 
```
/**
 * Gets an instance of a S3 client which is constructed using the given
 * Context.
 *
 * @param context An Context instance.
 * @return A default S3 client.
 */
public static AmazonS3Client getS3Client(Context context) {
    if (sS3Client == null) {
        sS3Client = new AmazonS3Client(getCredProvider(context.getApplicationContext()));
    }
    return sS3Client;
}
```4. Use a static method to retrieve the Transfer Utility.	
```java/**
 * Gets an instance of the TransferUtility which is constructed using the
 * given Context
 * 
 * @param context
 * @return a TransferUtility instance
 */
public static TransferUtility getTransferUtility(Context context) {
    if (sTransferUtility == null) {
        sTransferUtility = new TransferUtility(getS3Client(context.getApplicationContext()),
                context.getApplicationContext());
    }

    return sTransferUtility;
}
```Now that we can access the Transfer Utility throughout the app, let’s look at uploading data to S3.## The UploadActivityThe `UploadActivity` begins in `onCreate()` where it sets up the UI with any existing transfers. 
```java@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_upload);

    // Initializes TransferUtility, always do this before using it.
    transferUtility = Util.getTransferUtility(this);

    // Get the data from any transfer's that have already happened,
    initData();
    initUI();
}
``` ### Getting data about existing transfersThe `initData()` function queries for all existing Upload transfers (even those that are completed), and adds them to the UI. It also registers a callback for active transfers, which will alert us when a state or progress change occurs so that we can update the UI:1. Query for all uploads.```java// Use TransferUtility to get all upload transfers.
observers = transferUtility.getTransfersWithType(TransferType.UPLOAD);
```2. Update the UI.```java// For each transfer we will will create an entry in
// transferRecordMaps which will display
// as a single row in the UI
HashMap<String, Object> map = new HashMap<String, Object>();
Util.fillMap(map, observer, false);
transferRecordMaps.add(map);
```		3. Register a callback that will notify us when active transfers have updates. Notice we only attach a listener to transfers that can have updates.
```java// We only care about updates to transfers that are in a
// non-terminal state
if (!TransferState.COMPLETED.equals(observer.getState())
        && !TransferState.FAILED.equals(observer.getState())
        && !TransferState.CANCELED.equals(observer.getState())) {

    observer.setTransferListener(new UploadListener());
}
``` 	The `UploadListener` implements the `TransferListener` interface, and updates the UI when updates are fired.
	
```java
/*
 * A TransferListener class that can listen to a upload task and be notified
 * when the status changes.
 */
private class UploadListener implements TransferListener {

    // Simply updates the UI list when notified.
    @Override
    public void onError(int id, Exception e) {
        Log.e(TAG, "Error during upload: " + id, e);
        updateList();
    }

    @Override
    public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
        updateList();
    }

    @Override
    public void onStateChanged(int id, TransferState newState) {
        updateList();
    }
}```### Uploading an imageWhen a user clicks the upload button we go through the following three steps:1. **Launch an intent for the user to select a local file or image.**		Note: The code for selecting an image or file does not guarantee that in every circumstance the selected image is available on the device, and is used only to make the sample easier to understand. In a real production app, you will need to create a custom image picker or use some other mechanism to guarantee the you can get the file path of the image selected.2. **Get the filepath of the selected image**. Once the user has returned to our app via onActivityResult, we extract the file path from the URI passed using the getPath() method. Again please remember this should not be used in a live application, and is only shown for demonstration purposes. 3. **Create a new upload with the Transfer utility and add it to the UI.** Once we have the file path of the image, we call upload and add the TransferObserver to the UI. The transfer will begin automatically, starting an Android service for transfers if one is not already active. Just like when we listed existing transfers, we attach an Upload listener to get progress updates:
```java/*
 * Begins to upload the file specified by the file path.
 */
private void beginUpload(String filePath) {
    if (filePath == null) {
        Toast.makeText(this, "Could not find the filepath of the selected file",
                Toast.LENGTH_LONG).show();
        return;
    }
    File file = new File(filePath);
    TransferObserver observer = transferUtility.upload(Constants.BUCKET_NAME, file.getName(),
            file);
    observers.add(observer);
    HashMap<String, Object> map = new HashMap<String, Object>();
    Util.fillMap(map, observer, false);
    transferRecordMaps.add(map);
    observer.setTransferListener(new UploadListener());
    simpleAdapter.notifyDataSetChanged();
}
```## Pausing, Resuming, Deleting, and CancellingPausing, resuming, deleting, and cancelling a single transfer all work the same way. We tell the `TransferUtility` the operation to perform, and if it returns false, it is likely because the transfer was in an invalid state for that operation (e.g. trying to pause a cancelled transfer). ```javabtnPause.setOnClickListener(new OnClickListener() {
    @Override
    public void onClick(View v) {
        // Make sure the user has selected a transfer
        if (checkedIndex >= 0 && checkedIndex < observers.size()) {
            Boolean paused = transferUtility.pause(observers.get(checkedIndex).getId());
            /**
             * If paused does not return true, it is likely because the
             * user is trying to pause an upload that is not in a
             * pausable state (For instance it is already paused, or
             * canceled).
             */
            if (!paused) {
                Toast.makeText(
                        UploadActivity.this,
                        "Cannot pause transfer.  You can only pause transfers in a IN_PROGRESS or WAITING state.",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }
});
```If we want to pause or cancel all transfers, we simply call pause all with the type we want to pause. If you wanted to pause all transfers, you can pass TransferType.ANY.```javabtnPauseAll.setOnClickListener(new OnClickListener() {
    @Override
    public void onClick(View v) {
        transferUtility.pauseAllWithType(TransferType.UPLOAD);
    }
});
```Now that we’ve explained how uploading works, , let’s demonstrate how to download using the Transfer Utility works.## The DownloadActivityThe UI of the `DownloadActivity` is very similar to the `UploadActivity`. When you selecting a file to download, the sample uses a separate activity called `DownloadSelectionActivity`. This activity is responsible for displaying all of the objects in the S3 bucket and lets the user select one to download, returning the key to the `DownloadActivity`. ### Listing Objects in S3To get a list of objects in our bucket, we must use the low level `AmazonS3Client`. We can use the same one we used to instantiate the `TransferUtility`. After we have the list of key names, we add them to the UI, and when any one is selected we end the activity and return the object’s key to the `DownloadActivity`.```java/**
 * This async task queries S3 for all files in the given bucket so that they
 * can be displayed on the screen
 */
private class GetFileListTask extends AsyncTask<Void, Void, Void> {
    // The list of objects we find in the S3 bucket
    private List<S3ObjectSummary> s3ObjList;
    // A dialog to let the user know we are retrieving the files
    private ProgressDialog dialog;

    @Override
    protected void onPreExecute() {
        dialog = ProgressDialog.show(DownloadSelectionActivity.this,
                getString(R.string.refreshing),
                getString(R.string.please_wait));
    }

    @Override
    protected Void doInBackground(Void... inputs) {
        // Queries files in the bucket from S3.
        s3ObjList = s3.listObjects(Constants.BUCKET_NAME).getObjectSummaries();
        transferRecordMaps.clear();
        for (S3ObjectSummary summary : s3ObjList) {
            HashMap<String, Object> map = new HashMap<String, Object>();
            map.put("key", summary.getKey());
            transferRecordMaps.add(map);
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        dialog.dismiss();
        simpleAdapter.notifyDataSetChanged();
    }
}
```### Downloading From S3All we need to do to download a file is to specify where we want the file placed. Then, we add the returned `TransferObserver` to the UI. If the Android Service was not running, it will start automatically.```java/*
 * Begins to download the file specified by the key in the bucket.
 */
private void beginDownload(String key) {
    // Location to download files from S3 to. You can choose any accessible
    // file.
    File file = new File(Environment.getExternalStorageDirectory().toString() + "/" + key);

    // Initiate the download
    TransferObserver observer = transferUtility.download(Constants.BUCKET_NAME, key, file);

    // Add the new download to our list of TransferObservers
    observers.add(observer);
    HashMap<String, Object> map = new HashMap<String, Object>();
    // Fill the map with the observers data
    Util.fillMap(map, observer, false);
    // Add the filled map to our list of maps which the simple adapter uses
    transferRecordMaps.add(map);
    observer.setTransferListener(new DownloadListener());
    simpleAdapter.notifyDataSetChanged();
}
```## Network Connectivity and Automatic pausingThe Transfer Utility will automatically pause and resume transfers if the device loses network connectivity. There is no code needed to enable this feature. The Transfer Utility will also automatically pause transfers if the phone crashes, which is one reason why it is recommended to check to existing transfers when your app begins.## Manifest and PermissionsWhen it’s time for you to incorporate the Transfer Utility in your own app, it’s important to declare the necessary permission and the service in the `AndroidManifest.xml`. The Transfer Utility requires at least the first two permissions shown below, which should be placed outside the application tags:```<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />


<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
``` The permissions for reading and writing external storage, shown above, may be necessary depending on where you want to store downloaded files and where the files you plan upload are located.The following shows how to declare the Transfer Utility service, and should be placed within the application tags:
```<service
    android:name="com.amazonaws.mobileconnectors.s3.transferutility.TransferService"
    android:enabled="true" />
```		Important: If this service declaration is spelled incorrectly, neither Android nor your IDE will display a warning. The Utility’s service will just never start and therefore downloads and uploads will forever stay in a waiting state.## ConclusionWe hope that you have found this tutorial helpful, and the Transfer Utility a powerful tool for developing your application. As always we greatly appreciate feedback as either a comment on this post, an issue on GitHub, or a post on our forums.For further help using the AWS Mobile SDK for Android, see any of the following:- [AWS Mobile SDK for Android: Developer Guide](https://docs.aws.amazon.com/mobile/sdkforandroid/developerguide/)- [AWS Mobile SDK for Android: API Reference](https://docs.aws.amazon.com/AWSAndroidSDK/latest/javadoc/)- [AWS Mobile Development Blog](http://mobile.awsblog.com/)