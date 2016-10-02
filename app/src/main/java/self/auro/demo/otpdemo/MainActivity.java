package self.auro.demo.otpdemo;



import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import com.auro.widget.OTP.Callbacks.OtpCallback;
import com.auro.widget.OTP.OtpView;
import com.auro.widget.OTP.SMS.SmsRule;


public class MainActivity extends AppCompatActivity {

    private String OTP;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final OtpView mOtpView = (OtpView) findViewById(R.id.customOTPView);
        List<SmsRule> smsRules = new ArrayList<>();

        {
            Pattern pattern = Pattern.compile("One time password is (\\d{6})");
            SmsRule rule = new SmsRule("JUSPAY",pattern);
            smsRules.add(rule);
        }

        {
            Pattern pattern = Pattern.compile("One time pw (\\d{5})");
            SmsRule rule = new SmsRule("JUSPAY", pattern);
            smsRules.add(rule);
        }

        mOtpView.setCustomOTPCallback(smsRules, new OtpCallback() {


            @Override
            public void onSubmit(String otp) {
                OTP = otp;
                Toast.makeText(getApplicationContext(),"Received OTP is:" + OTP,Toast.LENGTH_SHORT).show();

                mOtpView.setOnError("SUCCESSFULLY RECEIVED OTP");
            }

            @Override
            public void onRegenerate() {

                Toast.makeText(getApplicationContext(),"OTP Regenerated",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void smsPermissionNotFound() {

                Toast.makeText(getApplicationContext(),"Sms PERMISSION NOT GRANTED", Toast.LENGTH_SHORT).show();
            }

        });




    }

}
