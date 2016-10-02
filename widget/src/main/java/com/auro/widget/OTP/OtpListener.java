package com.auro.widget.OTP;

import android.Manifest;
import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;

import java.util.List;

import com.auro.widget.OTP.Callbacks.OtpCallback;
import com.auro.widget.OTP.SMS.SmsRule;

/**
 * Created on 12/9/16.
 * @author Arijit Banerjee
 * @since 1.0
 */
class OtpListener {

    private static final String INTENT_ACTION = "android.provider.Telephony.SMS_RECEIVED";

    private InternalCallback mInternalCallback;
    private OtpView.InternalCallback mCallback;
    private OtpCallback mCustomOTPCallback;
    private OtpBroadcastReceiver mOTPBroadcastReceiver;
    private OtpContentProvider mOTPContentProvider;
    private long mTimeReceived;
    private StringBuilder mOTP;

    /**
     * @param context - context of the calling application
     * @param smsRules - list of Sms rules defined by the client
     * @param internalCallback - CustomView's internal callback
     * @param customOTPCallback - CustomView's external callback
     */
    OtpListener(@NonNull final Context context,
                final List<SmsRule> smsRules,
                @NonNull final OtpView.InternalCallback internalCallback,
                @NonNull final OtpCallback customOTPCallback) {

        if (internalCallback != null)
            mCallback = internalCallback;
        if (customOTPCallback != null)
            mCustomOTPCallback = customOTPCallback;
        init(context, smsRules);
        checkSMSReadPermission(context);

    }


    /**
     * To be called when the associated View is being detached
     * @param context - the context of the calling application
     */
    void onDestroy(final Context context) {
        if (mOTPBroadcastReceiver != null)
            context.unregisterReceiver(mOTPBroadcastReceiver);
        if (mOTPContentProvider != null)
            mOTPContentProvider.onDestroy();
        mCallback = null;
        mOTPBroadcastReceiver = null;
        mOTPContentProvider = null;
    }

    /**
     * The internal callback of OtpListener class
     * Contains the method to be invoked when a matching OTP is found
     * The method takes two params
     * 1. timeReceived - a long value
     * 2. otp - a String value
     */
    protected interface InternalCallback {void onOTPReceived(long timeReceived, String otp);}

    // Initialized the member variables
    private void init(final Context context, List<SmsRule> smsRules) {
        mTimeReceived = System.currentTimeMillis();
        mOTP = new StringBuilder("otp");
        if (mInternalCallback == null)
            mInternalCallback = new InternalCallback() {
                /**
                 * The method to be invoked when an OTP is received
                 * @param timeReceived - arrival time of OTP
                 * @param otp - OTP itself
                 */
                @Override
                public void onOTPReceived(long timeReceived, String otp) {
                    if (!mOTP.toString().equals(otp) && timeReceived > mTimeReceived) {
                        mTimeReceived = timeReceived;
                        mOTP.setLength(0);
                        mOTP.append(otp);
                        mCallback.onOTPReceived(mOTP.toString());
                    }
                }
            };
        if (mOTPBroadcastReceiver == null) {
            mOTPBroadcastReceiver = new OtpBroadcastReceiver(context, smsRules, mInternalCallback);
            IntentFilter itf = new IntentFilter();
            itf.addAction(INTENT_ACTION);
            itf.setPriority(999);
            context.registerReceiver(mOTPBroadcastReceiver,itf);
        }
        if (mOTPContentProvider == null)
            mOTPContentProvider = new OtpContentProvider(context, smsRules, mInternalCallback);
    }

    private void checkSMSReadPermission(final Context context) {
        PackageManager packageManager = context.getPackageManager();
        String packageName = context.getPackageName();
        if (!(packageManager.checkPermission(Manifest.permission.READ_SMS,packageName)
                == PackageManager.PERMISSION_GRANTED))
            mCustomOTPCallback.smsPermissionNotFound();
    }
}
