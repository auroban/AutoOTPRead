package com.auro.widget.OTP;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.auro.widget.OTP.Exception.SmsMissingRulesException;
import com.auro.widget.OTP.SMS.SmsRule;

/**
 * Created by on 12/9/16.
 * @author Arijit Banerjee
 * @since 1.0
 */

class OtpContentProvider {

    private static final String ADDRESS = "address";
    private static final String DATE = "date";
    private static final String BODY = "body";
    private static final Uri mUri = Uri.parse("content://sms/inbox");
    private static final String[] mProjection = new String[]{ADDRESS,DATE,BODY};
    private List<SmsRule> mSMSRules;
    private OtpListener.InternalCallback mInternalCallback;
    private List<Pair<Long,String>> mOTPList;
    private ScheduledExecutorService mScheduler;
    private Runnable mPeriodicalCheckRunnable;
    private Runnable mPassOTPRunnable;
    private long mLastPollingTime;


    /**
     * @param smsRules - passes the Sms rules defined by the client
     * @param internalCallback - callback received from OtpListener
     */
    OtpContentProvider(@NonNull final Context context,
                       @NonNull final List<SmsRule> smsRules,
                       @NonNull final OtpListener.InternalCallback internalCallback) {

        if (smsRules == null ||smsRules.size() == 0) throw new SmsMissingRulesException("No Rules Specified");
            mSMSRules = smsRules;
        if (internalCallback != null) mInternalCallback = internalCallback;
        init(context);
        long initialDelay = 2;
        long period = 3;
        mScheduler.scheduleAtFixedRate(mPeriodicalCheckRunnable, initialDelay, period, TimeUnit.SECONDS);
    }

    //Initializes all the member variables
    private void init(final Context context) {
        mOTPList = new ArrayList<>();
        mLastPollingTime = System.currentTimeMillis();

        // The runnable required for periodical check
        mPeriodicalCheckRunnable = new Runnable() {
            @Override
            public void run() {
                try {
                    fetchSMS(context);
                } catch (IllegalArgumentException e) {
                    Log.i(getClass().getCanonicalName(),"ERROR: "+e.toString());
                }
            }
        };

        // The runnable required for passing the OTP to the calling class. This runnable must be run on a UI thread
        mPassOTPRunnable = new Runnable() {
            @Override
            public void run() {
                if (mOTPList.size() > 0) {
                    long timeReceived = mOTPList.get(0).first;
                    String otp = mOTPList.get(0).second;
                    if (mInternalCallback != null)
                        mInternalCallback.onOTPReceived(timeReceived,otp);
                }
                mOTPList.clear();
                mLastPollingTime = System.currentTimeMillis();
            }
        };
        mScheduler = Executors.newSingleThreadScheduledExecutor();
    }

    //To run the query
    private void fetchSMS(final Context context) throws IllegalArgumentException {
        String mSelectionClause = DATE+">="+mLastPollingTime;
        Cursor cursor = context.getContentResolver().query(mUri,mProjection,mSelectionClause,null,null);
        if (cursor != null) {
            int messageCount = cursor.getCount();
            Log.d(getClass().getCanonicalName(),String.valueOf(messageCount));
            for (int i = 0; i < mSMSRules.size(); i++) {
                if (cursor.moveToFirst()) {
                    for (int j = 0; j < messageCount; j++) {
                        String address = cursor.getString(cursor.getColumnIndexOrThrow(ADDRESS));
                        if (address.contains(mSMSRules.get(i).getSender())) {
                            Pattern pattern = mSMSRules.get(i).getOTPPattern();
                            String body = cursor.getString(cursor.getColumnIndexOrThrow(BODY));
                            Matcher matcher = pattern.matcher(body);
                            if (matcher.find()) {
                                String otp = matcher.group(1);
                                long timeReceived = cursor.getLong(cursor.getColumnIndexOrThrow(DATE));
                                mOTPList.add(new Pair<Long, String>(timeReceived,otp));
                                Log.d(getClass().getCanonicalName(),"Received OTP: "+otp+" at time "+timeReceived);
                            }
                        }
                        cursor.moveToNext();
                    }
                }
            }
            if (mInternalCallback != null)
                try {
                    ((Activity) context).runOnUiThread(mPassOTPRunnable);
                } catch (Exception e) {
                    Log.d(getClass().getCanonicalName(),"ERROR:"+e.toString());
                }
            cursor.close();
        }
    }

    /**
     * Method to be called when the related View is being detached from the current Window
     */
    void onDestroy() {
        if (mScheduler != null)
            mScheduler.shutdown();
        mPassOTPRunnable = null;
        mPeriodicalCheckRunnable = null;
    }
}
