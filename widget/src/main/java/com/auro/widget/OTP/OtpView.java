package com.auro.widget.OTP;

import android.animation.Animator;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.annotation.StyleableRes;
import android.support.v4.content.ContextCompat;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Space;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import com.auro.widget.OTP.Callbacks.OtpCallback;
import com.auro.widget.OTP.SMS.SmsRule;
import com.auro.widget.R;

/**
 * Created on 29/8/16.
 * @author Arijit Banerjee
 * @since 1.0
 */
public class OtpView extends RelativeLayout {

    private static final int MAX_TRIES = 3;
    private static final int TIMERTASK_INTERVAL = 1000;
    private static final int COUNTDOWN_DURATION = 30000;
    private static final int ON_ERROR_DURATION = 3000;
    private static final int ANIMATION_DURATION = 300;
    private static final int COUNTDOWN_DELAY = 5000;
    private static final String TAG_DELETE_BUTTON = "deleteButton";


    //UI-Components
    private Button mSubmitButton;               // Submit Button
    private TextView mOTPTitleView;             // Contains the header
    private TextView mOTPNotReceivedView;       // TextView to display message when OTP is not received
    private TextView mPleaseWaitView;           // TextView to display a fixed message while waiting
    private TextView mTimerView;                // TextView to display the changing mTimer value
    private TextView mRegenerateTextView;       // TextView to regenerate the OTP
    private TableLayout mKeyPad;                // Contains the keypad
    private LinearLayout mWaitViewGroup;        // Wait ViewGroup
    private LinearLayout mOTPInputViewGroup;    // Contains only the EditTexts required for OTP
    private LinearLayout mRegenerateViewGroup;  // OTP Regenerate ViewGroup
    private RelativeLayout mRootViewGroup;      // Body of the entire view


    //UI-Component-Values
    private int mOTPLength;                     // Determines the number of EditTexts required for the OTP
    private int mBGColor;                       // Contains the color code for the entire body
    private int mOTPTitleColor;                 // Contains the color code of the header text
    private int mKeypadDigitColor;              // Contains the color code for the digits com Keypad
    private int mCursorPosition = 0;            // Points to the EditText currently otp_custom_underline_focused on
    private int mSubmitTextColor;               // Contains the color code for Submit button text
    private int mSubmitButtonColor;             // Contains the color code for Submit button
    private int mOTPDigitColor;                 // Contains the color code for the OTP digits
    private int mOTPTitleStyle;                 // Contains the style value for OTP title
    private int mKeypadDigitStyle;              // Contains the style value for Keypad digits
    private int mOTPDigitStyle;                 // Contains the style value for OTP digits
    private int mOTPOnErrorButtonTextColor;     // Contains the color code for the Submit button text com case of an error
    private int mOTPNotReceivedTextColor;       // Contains the color code for OTP Not Received text
    private int mOTPNotReceivedTextStyle;       // Contains the style value for OTP Not Received text
    private int mWaitTextColor;                 // Contains the color code for the fixed Wait text
    private int mWaitTextStyle;                 // Contains the style value for fixed Wait text
    private int mWaitTimerTextStyle;            // Contains the style value for mTimer text
    private int mWaitTimerTextColor;            // Contains the color code for mTimer text
    private int mRegenerateOTPTextColor;        // Contains the color code for regenerate text
    private int mRegenerateOTPTextStyle;        // Contains the style value for regenerate text
    private int mOnErrorDuration;               // Time duration for error message to be displayed
    private int mCountdownDelay;                // Delay until the first Countdown starts
    private float mWaitTextSize;                // Contains text size for both Wait fixed and mTimer texts
    private float mOTPTitleSize;                // Contains the size value for the header text
    private float mKeypadDigitSize;             // Contains the size value for the digits com Keypad
    private float mOTPDigitSize;                // Contains the size value for the OTP digits
    private float mKeypadCellHeight;            // Contains the height value for Keypad cells
    private float mOTPNotReceivedTextSize;      // Contains the size value for OTP Not Received text
    private float mRegenerateOTPTextSize;       // Contains the size value for regenerate text
    private String mOTPTitle;                   // Contains the String to be displayed at the header
    private String mSubmitText;                 // Contains the String to be displayed as Submit button's text
    private String mOTPNotReceivedText;         // Contains the OTP Not Received text
    private String mWaitText;                   // Contains the text to be displayed while waiting
    private String mRegenerateOTPText;          // Contains the regenerate text
    private Typeface mTypeface;                 // Defines the typeface to be used on all the texts


    private int mNumTries;
    private int mTimeLeft;
    private int mCountdownDuration;
    private Timer mTimer;
    private Handler mHandler;
    private StringBuilder mStringBuilder;
    private TransitionDrawable mSubmitButtonTransition;
    private Runnable mDisplayRegenerateVG, mChangeTimerText, mDisplayRegenerateTextView;
    private OtpCallback mCallbacks;
    private InternalCallback mInternalCallback;
    private OtpListener mOtpListener;


    public OtpView(Context context) {
        this(context, null);
    }

    public OtpView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public OtpView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        inflate(getContext(), R.layout.otplayout, this);
        ((Activity) context).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        init(attrs);
        generateMainView();
        generateOTPField();
        generateKeyPad();
        callRegenerateTimer(mCountdownDelay, TIMERTASK_INTERVAL);
        mHandler.postDelayed(mDisplayRegenerateVG, mCountdownDelay);
        onClickFunctions();
    }


    /**
     * Sets the otp length
     * @param length the length of the OTP. Default: 0
     * @return this view
     */
    public OtpView setOTPLength(int length) {
        mOTPLength = length;
        mStringBuilder = new StringBuilder(mOTPLength);
        generateOTPField();
        return this;
    }


    /**
     * Method to be invoked com case of an error
     * @param message - the error message
     */
    public void setOnError(final String message) {
        if (message != null) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mSubmitButton.setBackground(mSubmitButtonTransition);
                    mSubmitButtonTransition.startTransition(ANIMATION_DURATION);
                    mSubmitButton.setTextColor(mOTPOnErrorButtonTextColor);
                    mSubmitButton.setText(message.toUpperCase());
                }
            });
            mHandler.postDelayed(new Runnable() {

                @Override
                public void run() {
                    mSubmitButtonTransition.reverseTransition(ANIMATION_DURATION);
                    mSubmitButton.setTextColor(mSubmitTextColor);
                    mSubmitButton.setText(mSubmitText);
                }
            }, mOnErrorDuration);

            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mSubmitButton.setEnabled(true);
                }
            }, mOnErrorDuration + 300);
        }
    }

    /**
     * Method to set the callback
     * @param smsRules - list of Sms rules
     * @param callback - callback to be invoked
     */
    public void setCustomOTPCallback(@NonNull final List<SmsRule> smsRules,
                                     @NonNull final OtpCallback callback) {

        mCallbacks = callback;
        if (mInternalCallback == null) {
            mInternalCallback = new InternalCallback() {
                @Override
                public void onOTPReceived(String otp) {
                    displayOTP(otp);
                }


            };
        }
        if (mOtpListener == null) {
            mOtpListener = new OtpListener(getContext(),smsRules, mInternalCallback, mCallbacks);
        }
    }

    /**
     * The internal callback of OtpView
     * This view is passed to the OtpListener
     */
    protected interface InternalCallback {
        /**
         * Method to be invoked when OTP is received
         * @param otp - the OTP itself
         */
        void onOTPReceived(final String otp);
    }


    // Initializes all the attributes
    private void init(AttributeSet attrs) {

        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.OtpView, 0, 0);

        //UI-Components
        mRootViewGroup = (RelativeLayout) findViewById(R.id.main_container);
        mOTPInputViewGroup = (LinearLayout) findViewById(R.id.otp_input_container);
        mKeyPad = (TableLayout) findViewById(R.id.otp_keypad);
        mOTPTitleView = (TextView) findViewById(R.id.otp_title);
        mSubmitButton = (Button) findViewById(R.id.otp_submit_button);
        mRegenerateViewGroup = (LinearLayout) findViewById(R.id.otp_regenerate_pane);
        mWaitViewGroup = (LinearLayout) findViewById(R.id.otp_waiting_pane);
        mOTPNotReceivedView = (TextView) findViewById(R.id.otp_not_received);
        mPleaseWaitView = (TextView) findViewById(R.id.otp_wait_fixed_text);
        mTimerView = (TextView) findViewById(R.id.otp_wait_timer_text);
        mRegenerateTextView = (TextView) findViewById(R.id.otp_regenerate);
        mOTPInputViewGroup.setWeightSum(8f);
        LayerDrawable layerDrawable = (LayerDrawable) ContextCompat
                .getDrawable(getContext(), R.drawable.otp_custom_underline_normal);
        LayerDrawable layerFocused = (LayerDrawable) ContextCompat
                .getDrawable(getContext(), R.drawable.otp_custom_underline_focused);
        GradientDrawable normalFront = (GradientDrawable) layerDrawable
                .findDrawableByLayerId(R.id.normal_front);
        GradientDrawable normalBack = (GradientDrawable) layerDrawable
                .findDrawableByLayerId(R.id.normal_back);
        GradientDrawable focusedFront = (GradientDrawable) layerFocused
                .findDrawableByLayerId(R.id.focused_front);
        GradientDrawable focusedBack = (GradientDrawable) layerFocused
                .findDrawableByLayerId(R.id.focused_back);


        mTypeface = Typeface.createFromAsset(getContext().getAssets(), "font/Roboto-Thin.ttf");

        // Color values
        mOTPTitleColor = a.getColor(
                R.styleable.OtpView_otp_title_color, Color.WHITE);
        mKeypadDigitColor = a.getColor(
                R.styleable.OtpView_otp_keypad_digit_color, Color.WHITE);
        mSubmitTextColor = a.getColor(
                R.styleable.OtpView_otp_submitbutton_text_color, Color.WHITE);
        mOTPDigitColor = a.getColor(
                R.styleable.OtpView_otp_digit_color, Color.WHITE);
        mOTPOnErrorButtonTextColor = a.getColor(
                R.styleable.OtpView_otp_onerror_button_text_color,Color.WHITE);
        mOTPNotReceivedTextColor = a.getColor(
                R.styleable.OtpView_otp_notreceived_text_color, Color.WHITE);
        mWaitTextColor = a.getColor(
                R.styleable.OtpView_otp_wait_text_color,Color.WHITE);
        mWaitTimerTextColor = a.getColor(
                R.styleable.OtpView_otp_timer_text_color,Color.YELLOW);
        mRegenerateOTPTextColor = a.getColor(
                R.styleable.OtpView_otp_regenerate_text_color,Color.WHITE);
        mBGColor = a.getColor(
                R.styleable.OtpView_otp_background_color,
                ContextCompat.getColor(getContext(), R.color.otp_background));
        mSubmitButtonColor = a.getColor(
                R.styleable.OtpView_otp_submitbutton_color,
                ContextCompat.getColor(getContext(), R.color.otp_submit_button));
        int underlineNormalBack = a.getColor(
                R.styleable.OtpView_otp_underline_normal,
                ContextCompat.getColor(getContext(), R.color.otp_underline_normal));
        int underlineFocusedBack = a.getColor(
                R.styleable.OtpView_otp_underline_focused,
                ContextCompat.getColor(getContext(), R.color.otp_underline_focused));
        int mOTPOnErrorButtonColor = a.getColor(
                R.styleable.OtpView_otp_onerror_button_color, Color.RED);

        //  Size Values
        mKeypadDigitSize = a.getDimension(
                R.styleable.OtpView_otp_keypad_digit_size,
                getResources().getDimension(R.dimen.otp_key_digit_size));
        mOTPTitleSize = a.getDimension(
                R.styleable.OtpView_otp_title_size,
                getResources().getDimension(R.dimen.otp_title_size));
        mOTPDigitSize = a.getDimension(
                R.styleable.OtpView_otp_digit_size,
                getResources().getDimension(R.dimen.otp_text_size));
        mKeypadCellHeight = a.getDimension(
                R.styleable.OtpView_otp_keycell_height,
                getResources().getDimension(R.dimen.otp_key_height));
        mOTPNotReceivedTextSize = a.getDimension(
                R.styleable.OtpView_otp_notreceived_text_size,
                getResources().getDimension(R.dimen.otp_not_received_size));
        mWaitTextSize = a.getDimension(
                R.styleable.OtpView_otp_wait_text_size,
                getResources().getDimension(R.dimen.otp_wait_text_size));
        mRegenerateOTPTextSize = a.getDimension(
                R.styleable.OtpView_otp_regenerate_text_size,
                getResources().getDimension(R.dimen.otp_wait_text_size));
        mOTPLength = a.getInt(
                R.styleable.OtpView_otp_length, 0);

        //  Style Values
        mOTPTitleStyle = getStyle(a.getInt(
                R.styleable.OtpView_otp_title_style,0));
        mKeypadDigitStyle = getStyle(a.getInt(
                R.styleable.OtpView_otp_keypad_digit_style,1));
        mOTPDigitStyle = getStyle(a.getInt(
                R.styleable.OtpView_otp_digit_style,1));
        mOTPNotReceivedTextStyle = getStyle(a.getInt(
                R.styleable.OtpView_otp_notreceived_text_style,0));
        mWaitTextStyle = getStyle(a.getInt(
                R.styleable.OtpView_otp_wait_text_style,0));
        mWaitTimerTextStyle = getStyle(a.getInt(
                R.styleable.OtpView_otp_timer_text_style,1));
        mRegenerateOTPTextStyle = getStyle(a.getInt(
                R.styleable.OtpView_otp_regenerate_text_style,1));

        //  Setting the texts of the TextViews
        mOTPTitle = getTextFromAttr(a, R.styleable.OtpView_otp_title, R.string.otp_verification_title);
        mSubmitText = getTextFromAttr(a, R.styleable.OtpView_otp_submitbutton_text, R.string.otp_submit_text);
        mOTPNotReceivedText = getTextFromAttr(a, R.styleable.OtpView_otp_notreceived_text, R.string.otp_not_received);
        mWaitText = getTextFromAttr(a, R.styleable.OtpView_otp_wait_text, R.string.otp_please_wait);
        mRegenerateOTPText = getTextFromAttr(a, R.styleable.OtpView_otp_regenerate_text, R.string.otp_regenerate);

        //  Setting the numeric values of countdown, number of tries and other durations
        mCountdownDuration = a.getInt(
                R.styleable.OtpView_otp_countdown_duration, COUNTDOWN_DURATION);
        mNumTries = a.getInt(
                R.styleable.OtpView_otp_max_num_tries, MAX_TRIES);
        mOnErrorDuration = a.getInt(
                R.styleable.OtpView_otp_onerror_duration, ON_ERROR_DURATION);
        mCountdownDelay = a.getInt(
                R.styleable.OtpView_otp_countdown_delay, COUNTDOWN_DELAY);

        a.recycle();

        mTimeLeft = mCountdownDuration;
        mHandler = new Handler();
        normalFront.setColor(mBGColor);
        focusedFront.setColor(mBGColor);
        normalBack.setColor(underlineNormalBack);
        focusedBack.setColor(underlineFocusedBack);
        setOTPLength(mOTPLength);

        mSubmitButtonTransition = new TransitionDrawable(new Drawable[]
                {new ColorDrawable(mSubmitButtonColor), new ColorDrawable(mOTPOnErrorButtonColor)});


        //  Runnables
        mDisplayRegenerateVG = new Runnable() {
            @Override

            public void run() {
                mRegenerateViewGroup.setVisibility(VISIBLE);
            }
        };
        mChangeTimerText = new Runnable() {

            @Override
            public void run() {
                mTimerView.setText(String.format(Locale.ENGLISH,"%02d:%02d",
                        TimeUnit.MILLISECONDS.toMinutes(mTimeLeft),
                        TimeUnit.MILLISECONDS.toSeconds(mTimeLeft) -
                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(mTimeLeft))));
                mTimeLeft -= 1000;
            }
        };
        mDisplayRegenerateTextView = new Runnable() {
            @Override
            public void run() {
               if (mNumTries == 0) {
                   mRegenerateViewGroup.setVisibility(INVISIBLE);
                   Toast.makeText(getContext(),R.string.otp_no_more_tries, Toast.LENGTH_LONG).show();
               } else {
                   mWaitViewGroup.setVisibility(GONE);
                   mRegenerateTextView.setVisibility(VISIBLE);
               }
            }
        };
    }


    //Generate the body with defined attributes
    private void generateMainView() {
        mRootViewGroup.setBackgroundColor(mBGColor);

        mOTPTitleView.setText(mOTPTitle);
        mOTPTitleView.setTypeface(Typeface.defaultFromStyle(mOTPTitleStyle));
        mOTPTitleView.setTextColor(mOTPTitleColor);
        mOTPTitleView.setTextSize(mOTPTitleSize);

        mSubmitButton.setText(mSubmitText);
        mSubmitButton.setTextColor(mSubmitTextColor);
        mSubmitButton.setBackgroundColor(mSubmitButtonColor);

        mOTPNotReceivedView.setTextColor(mOTPNotReceivedTextColor);
        mOTPNotReceivedView.setTextSize(mOTPNotReceivedTextSize);
        mOTPNotReceivedView.setTypeface(mTypeface,mOTPNotReceivedTextStyle);
        mOTPNotReceivedView.setText(mOTPNotReceivedText);

        mPleaseWaitView.setTextColor(mWaitTextColor);
        mPleaseWaitView.setTextSize(mWaitTextSize);
        mPleaseWaitView.setTypeface(mTypeface,mWaitTextStyle);
        mPleaseWaitView.setText(mWaitText);

        mTimerView.setTypeface(Typeface.DEFAULT,mWaitTimerTextStyle);
        mTimerView.setTextColor(mWaitTimerTextColor);
        mTimerView.setTextSize(mWaitTextSize);

        mRegenerateTextView.setTextSize(mRegenerateOTPTextSize);
        mRegenerateTextView.setTextColor(mRegenerateOTPTextColor);
        mRegenerateTextView.setTypeface(Typeface.DEFAULT,mRegenerateOTPTextStyle);
        mRegenerateTextView.setText(mRegenerateOTPText);

    }

    //For generating the OTP fields
    private void generateOTPField() {

        if (mOTPLength > 0) {
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0,
                    ViewGroup.LayoutParams.MATCH_PARENT, 1f);
            params.setMargins(dpToPx(8), dpToPx(4), dpToPx(8), dpToPx(4));

            if (mOTPInputViewGroup != null)
                mOTPInputViewGroup.removeAllViews();

            for (int i = 0; i < mOTPLength; i++) {
                EditText editText = new EditText(getContext());
                editText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(1)});
                editText.setLayoutParams(params);
                editText.setBackgroundResource(R.drawable.otp_custom_underline);
                editText.setTextColor(mOTPDigitColor);
                editText.setTypeface(mTypeface, mOTPDigitStyle);
                editText.setTextSize(mOTPDigitSize);
                editText.setGravity(Gravity.CENTER);
                editText.setInputType(InputType.TYPE_NULL);
                editText.setId(100 + i);
                editText.setOnTouchListener(new OnTouchListener() {
                    @Override
                    public boolean onTouch(View view, MotionEvent motionEvent) {
                        mCursorPosition = view.getId() - 100;
                        view.requestFocus();
                        return true;
                    }
                });
                mOTPInputViewGroup.addView(editText, params);
            }

            invalidate();
            requestLayout();
        }
    }

    //For generating the Keypad
    private void generateKeyPad() {
        int digitValue = 1;
        TypedValue outValue = new TypedValue();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP )
            getContext().getTheme().resolveAttribute(android.R.attr.selectableItemBackgroundBorderless, outValue, true);
        else
            getContext().getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);

        TableRow.LayoutParams params = new TableRow.LayoutParams();
        params.width = TableRow.LayoutParams.MATCH_PARENT;
        params.height = (int) mKeypadCellHeight;
        params.weight = 1;
        for (int rowIndex = 0; rowIndex < 4; rowIndex++) {
            TableRow row = new TableRow(getContext());
            row.setWeightSum(3);
            for (int columnIndex = 0; columnIndex < 3; columnIndex++) {
                if (rowIndex < 3) {
                    TextView textView = createDigitView(digitValue, params, outValue);
                    row.addView(textView);
                    digitValue++;
                } else {
                    switch (columnIndex) {
                        case 0:
                            Space space = new Space(getContext());
                            space.setLayoutParams(params);
                            row.addView(space);
                            break;
                        case 1:
                            TextView textView = createDigitView(0, params, outValue);
                            row.addView(textView);
                            break;
                        case 2:
                            Drawable image = ContextCompat.getDrawable(getContext(), R.drawable.otp_ic_action_backspace);
                            if (image != null)
                                image.setColorFilter(mKeypadDigitColor, PorterDuff.Mode.MULTIPLY);
                            ImageView imageView = new ImageView(getContext());
                            imageView.setImageDrawable(image);
                            imageView.setLayoutParams(params);
                            imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                            imageView.setTag(TAG_DELETE_BUTTON);
                            imageView.setBackgroundResource(outValue.resourceId);
                            imageView.setOnClickListener(new OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    numPadFunction(view);
                                }
                            });
                            row.addView(imageView);
                            break;
                    }
                }
            }
            mKeyPad.addView(row);
        }
    }

    /*
     * Handles the keypad function
     * Backspace deletes the character currently pointed by mCursorPosition
     * If there's no character at all, it moves to the previous location
     * and deletes the character there.
     */
    private void numPadFunction(View pressedButton) {

        if (mOTPInputViewGroup.getChildCount() > 0) {
            if (pressedButton.getTag() == TAG_DELETE_BUTTON) {
                if (getOtpDigitView(mCursorPosition).getText().toString().equals("")) {
                    if (mCursorPosition > 0)
                        getOtpDigitView(--mCursorPosition).setText("");
                    getOtpDigitView(mCursorPosition).requestFocus();
                } else {
                    getOtpDigitView(mCursorPosition).setText("");
                }
            } else {
                (getOtpDigitView(mCursorPosition)).setText(((TextView) pressedButton).getText());
                if (mCursorPosition < mOTPInputViewGroup.getChildCount() - 1) {
                    mOTPInputViewGroup.getChildAt(++mCursorPosition).requestFocus();
                }
            }
            if (isOTPComplete()) {
                buttonExpand();
            } else {
                buttonCollapse();
            }
        }
    }

    //  Invoked to start the Timer
    private void callRegenerateTimer(long delay, long interval) {
        if (mTimer != null) {
            mTimer.cancel();
            mTimer.purge();
            mTimer = null;
        }
        mTimer = new Timer();

        mTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                ((Activity) getContext()).runOnUiThread(mChangeTimerText);
                if (mTimeLeft < 0) {
                    ((Activity) getContext()).runOnUiThread(mDisplayRegenerateTextView);
                    mTimer.cancel();
                    mTimer.purge();
                    mTimer = null;
                }
            }
        }, delay, interval);
    }

    private void onClickFunctions() {
        mSubmitButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                mSubmitButton.setText(R.string.otp_processing);
                if (mCallbacks != null) {
                    mCallbacks.onSubmit(mStringBuilder.toString());
                }
                mSubmitButton.setEnabled(false);
            }
        });
        mRegenerateTextView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mNumTries > 0) {
                    if (mCallbacks != null)
                        mCallbacks.onRegenerate();
                    mTimeLeft = mCountdownDuration;
                    callRegenerateTimer(0, TIMERTASK_INTERVAL);
                    mRegenerateTextView.setVisibility(GONE);
                    mWaitViewGroup.setVisibility(VISIBLE);
                    --mNumTries;
                    if (mNumTries == 1)
                        Toast.makeText(getContext(), R.string.otp_last_try_left,Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    // Receives the OTP and displays it com the mOTPInputViewGroup
    private void displayOTP(final String OTP) {
        if (mOTPInputViewGroup != null && mOTPInputViewGroup.getChildCount() > 0) {
            for (int i = 0; i < OTP.length(); i++) {
                EditText otpInputArea = (EditText) mOTPInputViewGroup.getChildAt(i);
                otpInputArea.setText(String.valueOf(OTP.charAt(i)));
                mCursorPosition = i;
                if (i < mOTPInputViewGroup.getChildCount() - 1) {
                    mOTPInputViewGroup.getChildAt(i + 1).requestFocus();
                }
                if (isOTPComplete()) {buttonExpand();}
                else {buttonCollapse();}
            }
        }
    }


    // Returns the text style based on the styleValue parameter.
    // Default is Normal
    private int getStyle(int styleValue) {
        if (styleValue == 1)
            return Typeface.BOLD;
        else if (styleValue == 2)
            return Typeface.ITALIC;
        else if (styleValue == 3)
            return Typeface.BOLD_ITALIC;
        return Typeface.NORMAL;
    }

    // Sets the text of String member variable, returns the variable itself
    private String getTextFromAttr(TypedArray a, @StyleableRes final int stringAttr, @StringRes final int defaultAttr) {
        String text = a.getString(stringAttr);
        if (!TextUtils.isEmpty(text)) {
            return text;
        } else {
            return getContext().getString(defaultAttr);
        }
    }

    //Determines if the OTP field is complete or not
    private boolean isOTPComplete() {
        mStringBuilder.delete(0, mStringBuilder.length());
        for (int i = 0; i < mOTPInputViewGroup.getChildCount(); i++) {
            mStringBuilder.append(((EditText) mOTPInputViewGroup.getChildAt(i)).getText());
        }

        return mStringBuilder.length() == mOTPLength;
    }

    //Button Expand Animation
    private void buttonExpand() {

        mSubmitButton.animate()
                .scaleX(1)
                .scaleY(1)
                .alpha(1)
                .setDuration(ANIMATION_DURATION)
                .setInterpolator(new OvershootInterpolator())
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animator) {
                        mSubmitButton.setAlpha(0);
                        mSubmitButton.setVisibility(VISIBLE);
                    }

                    @Override public void onAnimationEnd(Animator animator) { }

                    @Override public void onAnimationCancel(Animator animator) { }

                    @Override public void onAnimationRepeat(Animator animator) { }
                })
                .start();

    }

    //Button Collapse Animation
    private void buttonCollapse() {
        mSubmitButton.animate()
                .scaleX(0)
                .scaleY(0)
                .alpha(0)
                .setDuration(ANIMATION_DURATION)
                .setInterpolator(new AccelerateInterpolator())
                .setListener(new Animator.AnimatorListener() {
                    @Override public void onAnimationStart(Animator animator) {
                        mSubmitButton.setAlpha(1);
                    }

                    @Override
                    public void onAnimationEnd(Animator animator) {
                        mSubmitButton.setVisibility(INVISIBLE);
                    }

                    @Override public void onAnimationCancel(Animator animator) { }

                    @Override public void onAnimationRepeat(Animator animator) { }
                })
                .start();
    }


    @NonNull
    private TextView createDigitView(int digit, TableRow.LayoutParams params, TypedValue outValue) {
        TextView textView = new TextView(getContext());
        textView.setLayoutParams(params);
        textView.setGravity(Gravity.CENTER);
        textView.setText(String.valueOf(digit));
        textView.setTextColor(mKeypadDigitColor);
        textView.setTextSize(mKeypadDigitSize);
        textView.setBackgroundResource(outValue.resourceId);
        textView.setTypeface(mTypeface, mKeypadDigitStyle);
        textView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                numPadFunction(view);
            }
        });
        return textView;
    }

    private EditText getOtpDigitView(int position) {
        return (EditText) mOTPInputViewGroup.getChildAt(position);
    }

    private int dpToPx(int dp) {
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        return (int) (displayMetrics.density * dp);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if(mOtpListener !=null) mOtpListener.onDestroy(getContext());
        mCallbacks = null;
        mInternalCallback = null;
        mDisplayRegenerateTextView = null;
        mDisplayRegenerateVG = null;
        mHandler = null;
        mChangeTimerText = null;
        mTimer = null;
    }
}