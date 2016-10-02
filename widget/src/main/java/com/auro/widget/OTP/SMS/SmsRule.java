package com.auro.widget.OTP.SMS;

import android.support.annotation.NonNull;

import java.util.regex.Pattern;

/**
 * Created on 26/8/16.
 * @author Arijit Banerjee
 * @since 1.0
 */
public class SmsRule {

    private String mSender;
    private Pattern mOTPPattern;

    /**
     * @param sender - the Sender of the message
     * @param otpPattern - the RegEx pattern to look for
     */
    public SmsRule(@NonNull final String sender, @NonNull final Pattern otpPattern) {

        mSender = sender;
        mOTPPattern = otpPattern;
    }

    public String getSender() {return mSender;}

    public Pattern getOTPPattern() {return mOTPPattern;}

}
