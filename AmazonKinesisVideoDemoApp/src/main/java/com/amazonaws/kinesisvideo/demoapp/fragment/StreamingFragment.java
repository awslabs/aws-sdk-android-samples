package com.amazonaws.kinesisvideo.demoapp.fragment;

import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraCharacteristics;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.amazonaws.kinesisvideo.common.exception.KinesisVideoException;
import com.amazonaws.kinesisvideo.demoapp.KinesisVideoDemoApp;
import com.amazonaws.kinesisvideo.demoapp.R;
import com.amazonaws.kinesisvideo.demoapp.activity.SimpleNavActivity;
import com.amazonaws.kinesisvideo.client.KinesisVideoClient;
import com.amazonaws.mobileconnectors.kinesisvideo.client.KinesisVideoAndroidClientFactory;
import com.amazonaws.mobileconnectors.kinesisvideo.mediasource.android.AndroidCameraMediaSource;
import com.amazonaws.mobileconnectors.kinesisvideo.mediasource.android.AndroidCameraMediaSourceConfiguration;
import com.amazonaws.mobileconnectors.kinesisvideo.util.CameraHardwareCapabilitiesHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class StreamingFragment extends Fragment implements TextureView.SurfaceTextureListener {
    public static final String KEY_MEDIA_SOURCE_CONFIGURATION = "mediaSourceConfiguration";
    public static final String KEY_STREAM_NAME = "streamName";
    public static final String KEY_ROTATION = "rotation";
    public static final String KEY_SHOULD_MAINTAIN_ASPECT_RATIO = "maintainAspectRatio";
    public static final String KEY_SHOULD_FILL_SCREEN = "shouldFillScreen";
    public static final String KEY_IS_MIRRORED = "isMirrored";

    private static final String TAG = StreamingFragment.class.getSimpleName();

    private Button mStartStreamingButton;
    private KinesisVideoClient mKinesisVideoClient;
    private String mStreamName;
    private AndroidCameraMediaSourceConfiguration mConfiguration;
    private AndroidCameraMediaSource mCameraMediaSource;
    private TextureView mTextureView;
    private float mRotation;
    private boolean mShouldMaintainAspectRatio;
    private boolean mShouldFillScreen;
    private boolean mIsMirrored;
    private Size cameraPreviewSize;

    private SimpleNavActivity navActivity;

    public static StreamingFragment newInstance(SimpleNavActivity navActivity) {
        StreamingFragment s = new StreamingFragment();
        s.navActivity = navActivity;
        return s;
    }

    @Override
    public View onCreateView(final LayoutInflater inflater,
                             final ViewGroup container,
                             final Bundle savedInstanceState) {
        getArguments().setClassLoader(AndroidCameraMediaSourceConfiguration.class.getClassLoader());
        mStreamName = getArguments().getString(KEY_STREAM_NAME);
        mConfiguration = getArguments().getParcelable(KEY_MEDIA_SOURCE_CONFIGURATION);
        mRotation = getArguments().getFloat(KEY_ROTATION);
        mShouldMaintainAspectRatio = getArguments().getBoolean(KEY_SHOULD_MAINTAIN_ASPECT_RATIO);
        mShouldFillScreen = getArguments().getBoolean(KEY_SHOULD_FILL_SCREEN);
        mIsMirrored = getArguments().getBoolean(KEY_IS_MIRRORED);

        Log.d(TAG, "mStreamName=" + mStreamName);
        Log.d(TAG, "mRotation=" + mRotation);
        Log.d(TAG, "mShouldMaintainAspectRatio=" + mShouldMaintainAspectRatio);
        Log.d(TAG, "mShouldFillScreen=" + mShouldFillScreen);
        Log.d(TAG, "mIsMirrored=" + mIsMirrored);

        final View view = inflater.inflate(R.layout.fragment_streaming, container, false);
        mTextureView = (TextureView) view.findViewById(R.id.texture);
        mTextureView.setSurfaceTextureListener(this);
        cameraPreviewSize = null;
        return view;
    }

    private void createClientAndStartStreaming(final SurfaceTexture previewTexture) {

        try {
            mKinesisVideoClient = KinesisVideoAndroidClientFactory.createKinesisVideoClient(
                    getActivity(),
                    KinesisVideoDemoApp.KINESIS_VIDEO_REGION,
                    KinesisVideoDemoApp.getCredentialsProvider());

            mCameraMediaSource = (AndroidCameraMediaSource) mKinesisVideoClient
                    .createMediaSource(mStreamName, mConfiguration);

            mCameraMediaSource.setPreviewSurfaces(new Surface(previewTexture));

            resumeStreaming();
        } catch (final KinesisVideoException e) {
            Log.e(TAG, "unable to start streaming");
            throw new RuntimeException("unable to start streaming", e);
        }
    }

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        mStartStreamingButton = (Button) view.findViewById(R.id.start_streaming);
        mStartStreamingButton.setOnClickListener(stopStreamingWhenClicked());
    }

    @Override
    public void onResume() {
        super.onResume();
        resumeStreaming();
    }

    @Override
    public void onPause() {
        super.onPause();
        pauseStreaming();
    }

    private View.OnClickListener stopStreamingWhenClicked() {
        return new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                pauseStreaming();
                navActivity.startConfigFragment();
            }
        };
    }

    private void resumeStreaming() {
        try {
            if (mCameraMediaSource == null) {
                return;
            }

            mCameraMediaSource.start();
            Toast.makeText(getActivity(), "resumed streaming", Toast.LENGTH_SHORT).show();
            mStartStreamingButton.setText(getActivity().getText(R.string.stop_streaming));
        } catch (final KinesisVideoException e) {
            Log.e(TAG, "unable to resume streaming", e);
            Toast.makeText(getActivity(), "failed to resume streaming", Toast.LENGTH_SHORT).show();
        }
    }

    private void pauseStreaming() {
        try {
            if (mCameraMediaSource == null) {
                return;
            }

            mCameraMediaSource.stop();
            Toast.makeText(getActivity(), "stopped streaming", Toast.LENGTH_SHORT).show();
            mStartStreamingButton.setText(getActivity().getText(R.string.start_streaming));
        } catch (final KinesisVideoException e) {
            Log.e(TAG, "unable to pause streaming", e);
            Toast.makeText(getActivity(), "failed to pause streaming", Toast.LENGTH_SHORT).show();
        }
    }

    private void updatePreviewSize(int width, int height) {
        Log.d(TAG, "Choosing preview size...");
        if (mShouldMaintainAspectRatio) {
            cameraPreviewSize = chooseBestPreviewSize(width, height);
        } else if (mRotation % 180 == 0) {
            cameraPreviewSize = new Size(height, width);
        } else {
            cameraPreviewSize = new Size(width, height);
        }
        Log.d(TAG, "Preview size is: " + cameraPreviewSize.toString());
        updateTransform(width, height);
    }

    private Size chooseBestPreviewSize(int width, int height) {
        List<Size> allSizes = CameraHardwareCapabilitiesHelper.getSupportedResolutionsForYUV420_888(getContext(), mConfiguration.getCameraId());
        List<Size> possibleSizes = new ArrayList<>();
        for (Size possibility : allSizes) {
            int longDimension = Math.max(width, height);
            int shortDimension = Math.min(width, height);
            if (possibility.getWidth() > longDimension && possibility.getHeight() > shortDimension) {
                possibleSizes.add(possibility);
            }
        }
        if (possibleSizes.size() >= 1) {
            return Collections.min(possibleSizes, new Comparator<Size>() {
                @Override
                public int compare(Size s1, Size s2) {
                    return Long.signum(s1.getWidth() * s1.getHeight() - s2.getWidth() * s2.getHeight());
                }
            });
        }
        return allSizes.get(0);
    }

    private void updateTransform(int width, int height) {
        if (width != 0 && height != 0 && mTextureView != null && cameraPreviewSize != null) {
            Log.d(TAG, "Updating the matrix with width=" + width + ", height=" + height);
            Matrix transformationMatrix = new Matrix();
            RectF inputRectF = new RectF(0, 0, mTextureView.getWidth(), mTextureView.getHeight());
            RectF outputRectF = new RectF(0, 0, cameraPreviewSize.getHeight(), cameraPreviewSize.getWidth()); // since it's offset by 90, the height and width are switched
            // see https://source.android.com/compatibility/android-cdd#7_5_5_camera_orientation for more info
            float centerX = inputRectF.centerX();
            float centerY = inputRectF.centerY();
            outputRectF.offset(centerX - outputRectF.centerX(),
                    centerY - outputRectF.centerY());
            transformationMatrix.setRectToRect(inputRectF, outputRectF, Matrix.ScaleToFit.FILL);
            Log.d(TAG, "inputRectF: " + inputRectF.toShortString());
            Log.d(TAG, "outputRectF: " + outputRectF.toShortString());

            if (mShouldMaintainAspectRatio) {
                float scale1, scale2;
                if (mRotation % 180 == 0) {
                    scale1 = (float) height / cameraPreviewSize.getWidth();
                    scale2 = (float) width / cameraPreviewSize.getHeight();
                } else {
                    scale1 = (float) height / cameraPreviewSize.getHeight();
                    scale2 = (float) width / cameraPreviewSize.getWidth();
                }
                float scale = mShouldFillScreen ? Math.max(scale1, scale2) : Math.min(scale1, scale2);
                transformationMatrix.postScale(scale, scale, centerX, centerY);
            }
            transformationMatrix.postRotate(mRotation, centerX, centerY);

            mTextureView.setTransform(transformationMatrix);

            if (mIsMirrored) {
                if ((mConfiguration.getCameraFacing() == CameraCharacteristics.LENS_FACING_FRONT) == (mRotation % 180 == 0)) {
                    mTextureView.setScaleX(-1);
                } else {
                    mTextureView.setScaleY(-1);
                }
            }
        }
    }

    ////
    // TextureView.SurfaceTextureListener methods
    ////

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
        surfaceTexture.setDefaultBufferSize(1280, 720);
        updatePreviewSize(width, height);
        createClientAndStartStreaming(surfaceTexture);
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
        try {
            if (mCameraMediaSource != null)
                mCameraMediaSource.stop();
            if (mKinesisVideoClient != null)
                mKinesisVideoClient.stopAllMediaSources();
            KinesisVideoAndroidClientFactory.freeKinesisVideoClient();
        } catch (final KinesisVideoException e) {
            Log.e(TAG, "failed to release kinesis video client", e);
        }
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {

    }
}
