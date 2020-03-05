package com.amazonaws.kinesisvideo.demoapp.util;

import android.support.annotation.NonNull;
import android.util.Log;

import com.amazonaws.kinesisvideo.producer.FragmentAckType;
import com.amazonaws.kinesisvideo.producer.KinesisVideoFragmentAck;
import com.amazonaws.kinesisvideo.producer.ProducerException;
import com.amazonaws.kinesisvideo.producer.StreamCallbacks;

public class CustomStreamCallbacks implements StreamCallbacks {
    private static final String TAG = CustomStreamCallbacks.class.getSimpleName();

    /**
     * Reports the stream underflow.
     */
    @Override
    public void streamUnderflowReport() throws ProducerException {

    }

    /**
     * Reports the stream latency pressure.
     * @param duration The buffer duration in 100ns.
     * @throws ProducerException
     */
    @Override
    public void streamLatencyPressure(long duration) throws ProducerException {

    }

    /**
     * Reports the stream staleness when the data is read and sent but no ACKs are received.
     * @param lastAckDuration The duration of time window when the last "buffering" ACK is received in 100ns.
     * @throws ProducerException
     */
    @Override
    public void streamConnectionStale(long lastAckDuration) throws ProducerException {

    }

    /**
     * Reports the received ACK.
     * @param uploadHandle The client stream upload handle.
     * @param fragmentAck The received fragment ACK.
     * @throws ProducerException
     */
    @Override
    public void fragmentAckReceived(long uploadHandle, @NonNull final KinesisVideoFragmentAck fragmentAck) throws ProducerException {
        String ackType = null;
        switch (fragmentAck.getAckType().getIntType()) {
            case FragmentAckType.FRAGMENT_ACK_TYPE_BUFFERING:
                ackType = "FRAGMENT_ACK_TYPE_BUFFERING";
                break;
            case FragmentAckType.FRAGMENT_ACK_TYPE_RECEIVED:
                ackType = "FRAGMENT_ACK_TYPE_RECEIVED";
                break;
            case FragmentAckType.FRAGMENT_ACK_TYPE_PERSISTED:
                ackType = "FRAGMENT_ACK_TYPE_PERSISTED";
                break;
            case FragmentAckType.FRAGMENT_ACK_TYPE_ERROR:
                ackType = "FRAGMENT_ACK_TYPE_ERROR";
                break;
            default:
                ackType = "FRAGMENT_ACK_TYPE_UNKNOWN";
        }
        if (fragmentAck.getAckType().getIntType() == FragmentAckType.FRAGMENT_ACK_TYPE_ERROR) {
            Log.e(TAG, String.format("fragmentAckReceived: AckType %s ErrorCode %d Timestamp %s FragmentNumber %s",
                    ackType, fragmentAck.getResult(), fragmentAck.getTimestamp(), fragmentAck.getSequenceNumber()));
        } else {
            Log.d(TAG, String.format("fragmentAckReceived: AckType %s Timestamp %s FragmentNumber %s",
                    ackType, fragmentAck.getTimestamp(), fragmentAck.getSequenceNumber()));
        }
    }

    /**
     * Reports a dropped frame for the stream.
     * @param frameTimecode Frame time code of the dropped frame.
     * @throws ProducerException
     */
    @Override
    public void droppedFrameReport(long frameTimecode) throws ProducerException {

    }

    /**
     * Reports a dropped fragment for the stream.
     * @param fragmentTimecode Fragment time code of the dropped fragment.
     * @throws ProducerException
     */
    @Override
    public void droppedFragmentReport(long fragmentTimecode) throws ProducerException {

    }

    /**
     * Reports an error for the stream. The client should terminate the connection
     * as the inlet host would have/has already terminated the connection.
     *
     * @param uploadHandle The client stream upload handle.
     * @param fragmentTimecode Fragment time code of the errored fragment.
     * @param statusCode Status code of the failure.
     * @throws ProducerException
     */
    @Override
    public void streamErrorReport(long uploadHandle, long fragmentTimecode, long statusCode) throws ProducerException {

    }

    /**
     * New data is available for the stream.
     * @param uploadHandle The client stream upload handle.
     * @param duration The duration of content available in the stream.
     * @param availableSize The size of the content available in the stream.
     * @throws ProducerException
     */
    @Override
    public void streamDataAvailable(long uploadHandle, long duration, long availableSize) throws ProducerException {

    }

    /**
     * Ready to stream data.
     * @throws ProducerException
     */
    @Override
    public void streamReady() throws ProducerException {

    }

    /**
     * Stream has been closed.
     * @param uploadHandle The client stream upload handle.
     * @throws ProducerException
     */
    @Override
    public void streamClosed(long uploadHandle) throws ProducerException {

    }

    /**
     * Stream temporal buffer pressure.
     * @param remainDuration Remaining duration in the buffer in hundreds of nanos.
     * @throws ProducerException
     */
    @Override
    public void bufferDurationOverflowPressure(long remainDuration) throws ProducerException {

    }
}
