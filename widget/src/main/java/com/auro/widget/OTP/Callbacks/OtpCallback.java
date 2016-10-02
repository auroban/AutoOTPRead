package com.auro.widget.OTP.Callbacks;

/**
 * Created on 12/9/16.
 * @author Arijit Banerjee
 * @since 1.0
 */
public interface OtpCallback {

    void onSubmit(final String otp);
    void onRegenerate();
    void smsPermissionNotFound();
}
