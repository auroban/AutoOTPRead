package com.auro.widget.OTP.SMS;

import android.support.annotation.NonNull;

/**
 * Created on 26/8/16.
 * @author Arijit Banerjee
 * @since 1.0
 */
public class Sms {

    private String mAddress;
    private String mMessage;
    private long mTimeReceived;

    public Sms(@NonNull final String address,
               @NonNull final String message,
               final long timeReceived) {

        mAddress = address;
        mMessage = message;
        mTimeReceived = timeReceived;

    }

    public String getAddress() {

        return mAddress;
    }

    public String getMessage() {
        return mMessage;
    }

    public long getTimeReceived() {
        return mTimeReceived;
    }
}
