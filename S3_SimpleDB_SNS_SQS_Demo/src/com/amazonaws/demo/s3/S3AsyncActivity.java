package com.amazonaws.demo.s3;

import com.amazonaws.demo.R;

import java.io.File;
import java.io.FileWriter;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.os.AsyncTask;

import com.amazonaws.demo.AlertActivity;
import com.amazonaws.demo.PropertyLoader;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ProgressListener;
import com.amazonaws.services.s3.model.ProgressEvent;
import com.amazonaws.services.s3.model.S3ObjectInputStream;

public class S3AsyncActivity extends AlertActivity {

	protected Button startUpButton;
	protected Button startDownButton;
	protected Button stopDownButton;
	protected TextView uploadAmount;
	protected TextView downloadAmount;
	protected S3UploadTask uploadTask;
	protected S3DownloadTask downloadTask;

	protected final String bucketName = "testing-async-with-s3-for"
			+ PropertyLoader.getInstance().getAccessKey().toLowerCase();
	protected final String objectName = "asyncTestFile";

	protected File tempFile;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.s3_async);
		startUpButton = (Button) findViewById(R.id.s3_async_start_up_button);
		startDownButton = (Button) findViewById(R.id.s3_async_start_down_button);
		stopDownButton = (Button) findViewById(R.id.s3_async_stop_down_button);
		uploadAmount = (TextView) findViewById(R.id.s3_upload_amount);
		downloadAmount = (TextView) findViewById(R.id.s3_download_amount);
		wireButtons();
		tempFile = new File(getFilesDir().getAbsolutePath() + "temp.txt");
		new GenereateTempFileTask().execute(tempFile);
	}

	public void generateTestFile() {

		new S3CreateBucketTask().execute();
	}

	public void wireButtons() {
		stopDownButton.setClickable(false);

		startUpButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				try {
					generateTestFile();
					uploadTask = new S3UploadTask();
					uploadTask.execute(new PutObjectRequest(bucketName,
							objectName, tempFile));
				} catch (Throwable e) {
					setStackAndPost(e);
				}
			}
		});

		startDownButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				try {
					downloadTask = new S3DownloadTask();
					downloadTask.execute(new GetObjectRequest(bucketName,
							objectName));
				} catch (Throwable e) {
					setStackAndPost(e);
				}
			}
		});

		stopDownButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				try {
					downloadTask.cancel(true);
				} catch (Throwable e) {
					setStackAndPost(e);
				}
			}
		});
	}

	private class GenereateTempFileTask extends AsyncTask<File, Long, Boolean> {
		protected void onPreExecute() {
			uploadAmount.setText("Prepping temp file");
			startUpButton.setClickable(false);
		}

		// From AsyncTask, the code to run in the background
		// DO NOT UPDATE UI HERE
		protected Boolean doInBackground(File... reqs) {
			if (tempFile.exists()) {
				return true;
			}

			try {
				FileWriter fw = new FileWriter(reqs[0]);

				for (int i = 0; i < 10000; i++) {
					fw.write("This is a test!This is a test!This is a test!This is a test!This is a test!This is a test!This is a test!This is a test!This is a test!This is a test!");
				}

				fw.close();
			} catch (Exception e) {
				return false;
			}
			return true;
		}

		protected void onPostExecute(Boolean result) {
			if (result) {
				uploadAmount.setText("0");
				startUpButton.setClickable(true);
			} else {
				uploadAmount.setText("ERROR");
			}
		}
	}

	private class S3UploadTask extends AsyncTask<PutObjectRequest, Long, Long>
			implements ProgressListener {
		protected Long totalSent;

		// From AsyncTask, run on UI thread before execution
		protected void onPreExecute() {
			startUpButton.setClickable(false);
		}

		// From AsyncTask, the code to run in the background
		// DO NOT UPDATE UI HERE
		protected Long doInBackground(PutObjectRequest... reqs) {
			totalSent = 0L;
			reqs[0].setProgressListener(this);
			try {
				S3.getInstance().putObject(reqs[0]);
			} catch (Exception e) {
				return 0L;
			}
			return totalSent;
		}

		// From AsyncTask, runs on UI thread when background calls
		// publishProgress
		protected void onProgressUpdate(Long... progress) {
			uploadAmount.setText(progress[0].toString());
		}

		// From AsyncTask, runs on UI thread when background is finished
		protected void onPostExecute(Long result) {
			uploadAmount.setText("DONE! " + result);
			startUpButton.setClickable(true);
		}

		// From ProgressListener, publish progress to AsyncTask
		// as this is still running in background
		public void progressChanged(ProgressEvent progressEvent) {
			totalSent += progressEvent.getBytesTransfered();
			publishProgress(totalSent);
		}
	}

	private class S3DownloadTask extends
			AsyncTask<GetObjectRequest, Long, Long> {

		// From AsyncTask, run on UI thread before execution
		protected void onPreExecute() {
			stopDownButton.setClickable(true);
			startDownButton.setClickable(false);
		}

		// From AsyncTask
		protected Long doInBackground(GetObjectRequest... reqs) {
			byte buffer[] = new byte[1024];
			S3ObjectInputStream is;
			try {
				is = S3.getInstance().getObject(reqs[0]).getObjectContent();
			} catch (Exception e) {
				return 0L;
			}
			Long totalRead = 0L;
			int bytesRead = 1;
			try {
				while ((bytesRead > 0) && (!this.isCancelled())) {
					bytesRead = is.read(buffer);
					totalRead += bytesRead;
					publishProgress(totalRead);
				}

				// abort the get object request
				if (this.isCancelled()) {
					is.abort();
				}

				// close our stream
				is.close();
			} catch (Exception e) {
				return 0L;
			}
			return totalRead;
		}

		// From AsyncTask, runs on UI thread when background calls
		// publishProgress
		protected void onProgressUpdate(Long... progress) {
			downloadAmount.setText(progress[0].toString());
		}

		// From AsyncTask, runs on UI thread when background calls
		// publishProgress
		protected void onPostExecute(Long result) {
			downloadAmount.setText("DONE! " + result);
			stopDownButton.setClickable(false);
			startDownButton.setClickable(true);
		}

		// From AsyncTask, runs on UI thread called when task is canceled from
		// any other thread
		protected void onCancelled() {
			stopDownButton.setClickable(false);
			startDownButton.setClickable(true);
		}
	}

	private class S3CreateBucketTask extends AsyncTask<Void, Void, Void> {

		protected Void doInBackground(Void... voids) {

			// create our test bucket
			try {
				S3.createBucket(bucketName);
			} catch (Exception e) {
				setStackAndPost(e);
			}

			// create a dummy file (if not already present)
			tempFile = new File(getFilesDir().getAbsolutePath() + "temp.txt");

			if (!tempFile.exists()) {
				try {
					FileWriter fw = new FileWriter(tempFile);

					for (int i = 0; i < 100000; i++) {
						fw.write("This is a test!");
					}

					fw.close();
				} catch (Exception e) {
					setStackAndPost(e);
				}
			}

			return null;
		}
	}
}
