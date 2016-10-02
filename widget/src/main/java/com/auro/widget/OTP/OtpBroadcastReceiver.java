package com.auro.widget.OTP;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.telephony.SmsMessage;
import android.util.Log;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.auro.widget.OTP.Exception.SmsMissingRulesException;
import com.auro.widget.OTP.SMS.Sms;
import com.auro.widget.OTP.SMS.SmsRule;

/**
 * Created on 26/8/16.
 * @author Arijit Banerjee
 * @since 1.0
 */

class OtpBroadcastReceiver extends BroadcastReceiver {

    private static final String INTENT_ACTION = "android.provider.Telephony.SMS_RECEIVED";
    private static final String LOG_TAG = OtpBroadcastReceiver.class.getCanonicalName();

    private List<SmsRule> mSMSRules;
    private OtpListener.InternalCallback mInternalCallback;

    /**
     * @param context - the context of the calling application
     * @param smsRules - the list of Sms Rules defined by the client
     * @param internalCallback - the OtpListener callback
     */
    OtpBroadcastReceiver(@NonNull Context context,
                         @NonNull final List<SmsRule> smsRules,
                         @NonNull final OtpListener.InternalCallback internalCallback) {
        if (smsRules.size() == 0) {throw new SmsMissingRulesException("No Sms Rules Found");}
        mSMSRules = smsRules;
        if (internalCallback != null)
            mInternalCallback = internalCallback;
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        try {
            if (intent.getAction().equals(INTENT_ACTION))
                tryReceiveMessage(intent);
        }   catch (Exception e) {
            Log.i(LOG_TAG, "Failed to read Sms", e);
        }
    }


    //Method invoked at the time of Sms reception
    private void tryReceiveMessage(Intent intent) {
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            Object[] pdus = (Object[]) bundle.get("pdus");
            if (pdus != null) {
                SmsMessage[] messages = new SmsMessage[pdus.length];
                for (int i = 0; i < messages.length; i++) {
                    messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                    String messageFrom = messages[i].getOriginatingAddress();
                    String messageBody = messages[i].getMessageBody();
                    long messageTime = messages[i].getTimestampMillis();
                    Log.d(LOG_TAG, "Message is from: " + messageFrom + " and its content is: " + messageBody);
                    Sms sms = new Sms(messageFrom,messageBody,messageTime);
                    processOTP(sms);
                }
            }
        }
    }


    //Method to process the recently received Sms
    private void processOTP(Sms sms) {
        for (int i = 0; i < mSMSRules.size(); i ++) {
            if (sms.getAddress().contains(mSMSRules.get(i).getSender())) {
                Pattern pattern = mSMSRules.get(i).getOTPPattern();
                Matcher matcher = pattern.matcher(sms.getMessage());
                if (matcher.find()) {
                    String otp = matcher.group(1);
                    if (mInternalCallback != null) {
                        mInternalCallback.onOTPReceived(sms.getTimeReceived(), otp);
                        Log.i(LOG_TAG, "Extracted OTP is: " + otp);
                    }
                    return;
                } else
                    Log.d(LOG_TAG,"Failed to extract OTP");
            }
        }
    }
}
