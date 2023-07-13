package com.amazonaws.kinesisvideo.demoapp.fragment;

import android.Manifest;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraCharacteristics;
import android.os.Bundle;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;
import android.util.Log;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import com.amazonaws.kinesisvideo.client.KinesisVideoClient;
import com.amazonaws.kinesisvideo.client.mediasource.CameraMediaSourceConfiguration;
import com.amazonaws.kinesisvideo.common.exception.KinesisVideoException;
import com.amazonaws.kinesisvideo.demoapp.KinesisVideoDemoApp;
import com.amazonaws.kinesisvideo.demoapp.R;
import com.amazonaws.kinesisvideo.demoapp.activity.SimpleNavActivity;
import com.amazonaws.kinesisvideo.demoapp.ui.adapter.ToStrings;
import com.amazonaws.kinesisvideo.demoapp.ui.widget.StringSpinnerWidget;
import com.amazonaws.kinesisvideo.producer.StreamInfo;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobileconnectors.kinesisvideo.client.KinesisVideoAndroidClientFactory;
import com.amazonaws.mobileconnectors.kinesisvideo.data.MimeType;
import com.amazonaws.mobileconnectors.kinesisvideo.mediasource.android.AndroidCameraMediaSourceConfiguration;

import java.util.ArrayList;

import static com.amazonaws.mobileconnectors.kinesisvideo.util.CameraUtils.getCameras;
import static com.amazonaws.mobileconnectors.kinesisvideo.util.CameraUtils.getSupportedResolutions;
import static com.amazonaws.mobileconnectors.kinesisvideo.util.VideoEncoderUtils.getSupportedMimeTypes;

public class StreamConfigurationFragment extends Fragment {
    private static final String TAG = StreamConfigurationFragment.class.getSimpleName();
    private static final Size RESOLUTION_320x240 = new Size(320, 240);
    private static final int FRAMERATE_20 = 20;
    private static final int BITRATE_384_KBPS = 384 * 1024;
    private static final int RETENTION_PERIOD_48_HOURS = 2 * 24;

    private Button mStartStreamingButton;
    private EditText mStreamName;
    private KinesisVideoClient mKinesisVideoClient;
    private CheckBox mAspectRatioCheckBox, mFillCheckBox, mMirrorCheckBox;

    private StringSpinnerWidget<CameraMediaSourceConfiguration> mCamerasDropdown;
    private StringSpinnerWidget<Size> mResolutionDropdown;
    private StringSpinnerWidget<MimeType> mMimeTypeDropdown;
    private StringSpinnerWidget<Float> mRotationDropdown;

    private SimpleNavActivity navActivity;

    public static StreamConfigurationFragment newInstance(SimpleNavActivity navActivity) {
        StreamConfigurationFragment s = new StreamConfigurationFragment();
        s.navActivity = navActivity;
        return s;
    }

    @Override
    public View onCreateView(final LayoutInflater inflater,
                             final ViewGroup container,
                             final Bundle savedInstanceState) {
        if (ContextCompat.checkSelfPermission(this.getActivity(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this.getActivity(), new String[]{Manifest.permission.CAMERA}, 9393);
        }

        getActivity().setTitle(getActivity().getString(R.string.title_fragment_stream));

        final View view = inflater.inflate(R.layout.fragment_stream_configuration, container, false);

        final Thread thread = new Thread(() -> {
            try {
                ((AWSMobileClient) KinesisVideoDemoApp.getCredentialsProvider()).getAWSCredentials();
            } catch (final Exception e) {
                Log.e(TAG, "Exception while fetching credentials", e);
            }
        });
        thread.start();

        try {
            thread.join();
            mKinesisVideoClient = KinesisVideoAndroidClientFactory.createKinesisVideoClient(
                    getActivity(),
                    KinesisVideoDemoApp.KINESIS_VIDEO_REGION,
                    KinesisVideoDemoApp.getCredentialsProvider());
        } catch (final InterruptedException | KinesisVideoException e) {
            Log.e(TAG, "Failed to create Kinesis Video client", e);
        }

        mCamerasDropdown = new StringSpinnerWidget<>(
                getActivity(),
                view,
                R.id.cameras_spinner,
                ToStrings.CAMERA_DESCRIPTION,
                getCameras(mKinesisVideoClient));

        mCamerasDropdown.setItemSelectedListener(
                new StringSpinnerWidget.ItemSelectedListener<CameraMediaSourceConfiguration>() {
                    @Override
                    public void itemSelected(final CameraMediaSourceConfiguration mediaSource) {
                        mResolutionDropdown = new StringSpinnerWidget<>(
                                getActivity(),
                                view,
                                R.id.resolutions_spinner,
                                getSupportedResolutions(getActivity(), mediaSource.getCameraId()));
                        select640orBelow();
                        updateMirroredCheckBox();
                    }
                });

        mMimeTypeDropdown = new StringSpinnerWidget<>(
                getActivity(),
                view,
                R.id.codecs_spinner,
                getSupportedMimeTypes());

        mRotationDropdown = new StringSpinnerWidget<>(
                getActivity(),
                view,
                R.id.rotation_spinner,
                getRotations());
        return view;
    }

    private ArrayList<Float> getRotations() {
        final ArrayList<Float> rotations = new ArrayList<>();
        rotations.add(0f);
        rotations.add(90f);
        rotations.add(180f);
        rotations.add(270f);
        return rotations;
    }

    private void select640orBelow() {
        Size tmpSize = new Size(0, 0);
        int indexToSelect = 0;
        for (int i = 0; i < mResolutionDropdown.getCount(); i++) {
            final Size resolution = mResolutionDropdown.getItem(i);
            if (resolution.getWidth() <= RESOLUTION_320x240.getWidth()
                    && tmpSize.getWidth() <= resolution.getWidth()
                    && resolution.getHeight() <= RESOLUTION_320x240.getHeight()
                    && tmpSize.getHeight() <= resolution.getHeight()) {

                tmpSize = resolution;
                indexToSelect = i;
            }
        }

        mResolutionDropdown.selectItem(indexToSelect);
    }

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        mStartStreamingButton = (Button) view.findViewById(R.id.start_streaming);
        mStartStreamingButton.setOnClickListener(startStreamingActivityWhenClicked());
        mStreamName = (EditText) view.findViewById(R.id.stream_name);
        mAspectRatioCheckBox = (CheckBox) view.findViewById(R.id.aspect_ratio_checkbox);
        mAspectRatioCheckBox.setOnClickListener(onAspectRatioCheckBoxClick());
        mFillCheckBox = (CheckBox) view.findViewById(R.id.maximize_checkbox);
        mMirrorCheckBox = (CheckBox) view.findViewById(R.id.mirror_checkbox);
    }

    private View.OnClickListener startStreamingActivityWhenClicked() {
        return new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                startStreamingActivity();
            }
        };
    }

    private View.OnClickListener onAspectRatioCheckBoxClick() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!mAspectRatioCheckBox.isChecked()) {
                    mFillCheckBox.setChecked(true);
                }
                mFillCheckBox.setEnabled(mAspectRatioCheckBox.isChecked());
            }
        };
    }

    // front facing camera preview is mirrored, so we need to mirror it again to un-mirror it.
    // see https://source.android.com/compatibility/android-cdd#7_5_2_front-facing_camera
    private void updateMirroredCheckBox() {
        mMirrorCheckBox.setChecked(mCamerasDropdown.getSelectedItem().getCameraFacing() == CameraCharacteristics.LENS_FACING_FRONT);
    }

    private void startStreamingActivity() {
        final Bundle extras = new Bundle();

        extras.putParcelable(
                StreamingFragment.KEY_MEDIA_SOURCE_CONFIGURATION,
                getCurrentConfiguration());

        extras.putString(
                StreamingFragment.KEY_STREAM_NAME,
                mStreamName.getText().toString());

        extras.putFloat(StreamingFragment.KEY_ROTATION, mRotationDropdown.getSelectedItem() -
                mCamerasDropdown.getSelectedItem().getCameraOrientation());
        extras.putBoolean(StreamingFragment.KEY_SHOULD_MAINTAIN_ASPECT_RATIO, mAspectRatioCheckBox.isChecked());
        extras.putBoolean(StreamingFragment.KEY_SHOULD_FILL_SCREEN, mFillCheckBox.isChecked());
        extras.putBoolean(StreamingFragment.KEY_IS_MIRRORED, mMirrorCheckBox.isChecked());

        navActivity.startStreamingFragment(extras);
    }

    private AndroidCameraMediaSourceConfiguration getCurrentConfiguration() {
        return new AndroidCameraMediaSourceConfiguration(
                AndroidCameraMediaSourceConfiguration.builder()
                        .withCameraId(mCamerasDropdown.getSelectedItem().getCameraId())
                        .withEncodingMimeType(mMimeTypeDropdown.getSelectedItem().getMimeType())
                        .withHorizontalResolution(mResolutionDropdown.getSelectedItem().getWidth())
                        .withVerticalResolution(mResolutionDropdown.getSelectedItem().getHeight())
                        .withCameraFacing(mCamerasDropdown.getSelectedItem().getCameraFacing())
                        .withIsEncoderHardwareAccelerated(
                                mCamerasDropdown.getSelectedItem().isEndcoderHardwareAccelerated())
                        .withFrameRate(FRAMERATE_20)
                        .withRetentionPeriodInHours(RETENTION_PERIOD_48_HOURS)
                        .withEncodingBitRate(BITRATE_384_KBPS)
                        .withCameraOrientation(-mCamerasDropdown.getSelectedItem().getCameraOrientation())
                        .withNalAdaptationFlags(StreamInfo.NalAdaptationFlags.NAL_ADAPTATION_ANNEXB_CPD_AND_FRAME_NALS)
                        .withIsAbsoluteTimecode(false));
    }
}