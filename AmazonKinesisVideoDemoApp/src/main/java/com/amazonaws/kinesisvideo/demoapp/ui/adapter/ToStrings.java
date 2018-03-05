package com.amazonaws.kinesisvideo.demoapp.ui.adapter;

import android.content.Context;
import android.media.MediaCodecInfo;

import com.amazonaws.kinesisvideo.client.mediasource.CameraMediaSourceConfiguration;
import com.amazonaws.kinesisvideo.demoapp.R;
import com.amazonaws.services.kinesisvideo.model.StreamInfo;

public class ToStrings {

    private static final int FACING_FRONT = 0x00;
    private static final int FACING_BACK = 0x01;
    private static final int FACING_EXTERNAL = 0x02;

    public static final ToString<CameraMediaSourceConfiguration> CAMERA_DESCRIPTION =
            new ToString<CameraMediaSourceConfiguration>() {
                @Override
                public String toString(final Context context,
                                       final int itemIndex,
                                       final CameraMediaSourceConfiguration cameraMediaSource) {
                    return getCameraName(context, itemIndex, cameraMediaSource);
                }
            };

    public static final ToString<StreamInfo> STREAM_NAME =
            new ToString<StreamInfo>() {
                @Override
                public String toString(final Context context,
                                       final int itemIndex,
                                       final StreamInfo streamInfo) {
                    return streamInfo.getStreamName();
                }
            };

    private static String getCameraName(final Context context,
                                      final int itemIndex,
                                      final CameraMediaSourceConfiguration cameraMediaSource) {
        switch (cameraMediaSource.getCameraFacing()) {
            case FACING_BACK:
                return context.getString(R.string.camera_back_facing);
            case FACING_FRONT:
                return context.getString(R.string.camera_front_facing);
            case FACING_EXTERNAL:
                return context.getString(R.string.camera_external);
            default:
                return context.getString(R.string.camera, itemIndex);
        }
    }

    public static final ToString<MediaCodecInfo> MEDIA_CODEC_DESCRIPTION =
            new ToString<MediaCodecInfo>() {
                @Override
                public String toString(final Context context,
                                       final int itemIndex,
                                       final MediaCodecInfo mediaCodec) {
                    return mediaCodec.getName();
                }
            };

    private ToStrings() {
        // no op
    }
}
