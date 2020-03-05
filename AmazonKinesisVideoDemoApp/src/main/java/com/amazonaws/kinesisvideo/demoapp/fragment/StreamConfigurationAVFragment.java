package com.amazonaws.kinesisvideo.demoapp.fragment;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.amazonaws.kinesisvideo.client.KinesisVideoClient;
import com.amazonaws.kinesisvideo.client.mediasource.CameraMediaSourceConfiguration;
import com.amazonaws.kinesisvideo.common.exception.KinesisVideoException;
import com.amazonaws.kinesisvideo.demoapp.KinesisVideoDemoApp;
import com.amazonaws.kinesisvideo.demoapp.R;
import com.amazonaws.kinesisvideo.demoapp.activity.SimpleNavActivity;
import com.amazonaws.kinesisvideo.demoapp.ui.adapter.ToStrings;
import com.amazonaws.kinesisvideo.demoapp.ui.widget.StringSpinnerWidget;
import com.amazonaws.kinesisvideo.demoapp.util.CustomStreamCallbacks;
import com.amazonaws.kinesisvideo.producer.MkvTrackInfoType;
import com.amazonaws.kinesisvideo.producer.StreamInfo;
import com.amazonaws.kinesisvideo.producer.TrackInfo;
import com.amazonaws.mobileconnectors.kinesisvideo.client.KinesisVideoAndroidClientFactory;
import com.amazonaws.mobileconnectors.kinesisvideo.data.MimeType;
import com.amazonaws.mobileconnectors.kinesisvideo.mediasource.android.AndroidAudioVideoMediaSourceConfiguration;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import static com.amazonaws.kinesisvideo.util.StreamInfoConstants.AUDIO_CODEC_ID;
import static com.amazonaws.kinesisvideo.util.StreamInfoConstants.AUDIO_TRACK_ID;
import static com.amazonaws.kinesisvideo.util.StreamInfoConstants.VIDEO_CODEC_ID;
import static com.amazonaws.kinesisvideo.util.StreamInfoConstants.VIDEO_TRACK_ID;
import static com.amazonaws.mobileconnectors.kinesisvideo.util.CameraUtils.getCameras;
import static com.amazonaws.mobileconnectors.kinesisvideo.util.CameraUtils.getSupportedResolutions;
import static com.amazonaws.mobileconnectors.kinesisvideo.util.VideoEncoderUtils.getSupportedMimeTypes;

public class StreamConfigurationAVFragment extends Fragment {
    private static final String TAG = StreamConfigurationAVFragment.class.getSimpleName();
    private static final Size RESOLUTION_320x240 = new Size(320, 240);
    private static final int FRAMERATE_20 = 20;
    private static final int BITRATE_384_KBPS = 384 * 1024;
    private static final int RETENTION_PERIOD_48_HOURS = 2 * 24;

    private static final int SAMPLE_RATE_44100 = 44100;
    private static final int SAMPLES_PER_FRAME = 1024;
    private static final int FRAMES_PER_BUFFER = 25;
    private static final int BITRATE_64_KBPS = 64000;

    private Button mStartStreamingButton;
    private EditText mStreamName;
    private KinesisVideoClient mKinesisVideoClient;

    private StringSpinnerWidget<CameraMediaSourceConfiguration> mCamerasDropdown;
    private StringSpinnerWidget<Size> mResolutionDropdown;
    private StringSpinnerWidget<MimeType> mMimeTypeDropdown;

    private SimpleNavActivity navActivity;

    public static StreamConfigurationAVFragment newInstance(SimpleNavActivity navActivity) {
        StreamConfigurationAVFragment s = new StreamConfigurationAVFragment();
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

        if (ContextCompat.checkSelfPermission(this.getActivity(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this.getActivity(), new String[]{Manifest.permission.RECORD_AUDIO}, 9394);
        }

        getActivity().setTitle(getActivity().getString(R.string.title_fragment_stream));

        final View view = inflater.inflate(R.layout.fragment_stream_configuration, container, false);

        try {
            mKinesisVideoClient = KinesisVideoAndroidClientFactory.createKinesisVideoClient(
                    getActivity(),
                    KinesisVideoDemoApp.KINESIS_VIDEO_REGION,
                    KinesisVideoDemoApp.getCredentialsProvider());
        } catch (KinesisVideoException | CertificateException | UnrecoverableKeyException | NoSuchAlgorithmException | KeyManagementException | KeyStoreException | IOException e) {
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
                    }
                });

        mMimeTypeDropdown = new StringSpinnerWidget<>(
                getActivity(),
                view,
                R.id.codecs_spinner,
                getSupportedMimeTypes());

        return view;
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
        mStartStreamingButton = (Button) view.findViewById(R.id.start_streaming_audio_video);
        mStartStreamingButton.setOnClickListener(startStreamingActivityWhenClicked());
        mStreamName = (EditText) view.findViewById(R.id.stream_name);
    }

    private View.OnClickListener startStreamingActivityWhenClicked() {
        return new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                startStreamingActivity();
            }
        };
    }

    private void startStreamingActivity() {
        final Bundle extras = new Bundle();

        extras.putParcelable(
                StreamingAVFragment.KEY_AV_MEDIA_SOURCE_CONFIGURATION,
                getCurrentConfiguration());

        extras.putString(
                StreamingAVFragment.KEY_STREAM_NAME,
                mStreamName.getText().toString());

        navActivity.startStreamingAVFragment(extras);
    }

    private AndroidAudioVideoMediaSourceConfiguration getCurrentConfiguration() {
        return new AndroidAudioVideoMediaSourceConfiguration.AudioVideoBuilder()
                        .withCameraId(mCamerasDropdown.getSelectedItem().getCameraId())
                        .withEncodingMimeType(mMimeTypeDropdown.getSelectedItem().getMimeType())
                        .withHorizontalResolution(mResolutionDropdown.getSelectedItem().getWidth())
                        .withVerticalResolution(mResolutionDropdown.getSelectedItem().getHeight())
                        .withCameraFacing(mCamerasDropdown.getSelectedItem().getCameraFacing())
                        .withIsEncoderHardwareAccelerated(
                                mCamerasDropdown.getSelectedItem().isEndcoderHardwareAccelerated())
                        .withFps(FRAMERATE_20)
                        .withRetentionPeriodInHours(RETENTION_PERIOD_48_HOURS)
                        .withEncodingBitRate(getBitrate(mResolutionDropdown.getSelectedItem().getWidth(),
                                mResolutionDropdown.getSelectedItem().getHeight()))
                        .withCameraOrientation(-mCamerasDropdown.getSelectedItem().getCameraOrientation())
                        .withNalAdaptationFlag(StreamInfo.NalAdaptationFlags.NAL_ADAPTATION_ANNEXB_CPD_AND_FRAME_NALS)
                        .withAbsoluteTimecode(true)
                        .withAudioEncodingMimeType("audio/mp4a-latm")
                        .withAudioIsEncoderHardwareAccelerated(true)
                        .withAudioSampleRate(SAMPLE_RATE_44100)
                        .withAudioSamplesPerFrame(SAMPLES_PER_FRAME)
                        .withAudioFramesPerBuffer(FRAMES_PER_BUFFER)
                        .withAudioEncodingBitRate(BITRATE_64_KBPS)
                        .withTrackInfoList(new TrackInfo[] {
                                new TrackInfo(VIDEO_TRACK_ID, VIDEO_CODEC_ID, "AndroidVideoTrack",
                                        null, MkvTrackInfoType.VIDEO),
                                new TrackInfo(AUDIO_TRACK_ID, AUDIO_CODEC_ID, "AndroidAudioTrack",
                                        null, MkvTrackInfoType.AUDIO)
                        })
                        .withStreamCallbacks(new CustomStreamCallbacks())
                        .build();
    }

    private int getBitrate(int width, int height) {
        return (int) (width * height * 4);
    }
}