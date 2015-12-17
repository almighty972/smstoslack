package com.jeanlambert.androidslackconnector;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.apache.http.Header;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;

import java.io.UnsupportedEncodingException;

/**
 * Created by gyl on 01/07/15.
 */
public class IncomingSmsBroadcastReceiver extends BroadcastReceiver {

    public static final String TAG = "AndroidSlackConnector";

    public static final String CA_SMS_PHONE_NUMBER = "20199";
    public static final String SG_SMS_PHONE_NUMBER_1 = "38651";
    public static final String SG_SMS_PHONE_NUMBER_2 = "38600";
    public static final String SG_SMS_PHONE_NUMBER_3 = "38005";
    /* add new number here */

    /**
     * Enum holding the slack webhooks
     */
    private enum Hook {
        CA_EWALLET(CA_SMS_PHONE_NUMBER, "https://hooks.slack.com/services/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"),
        SG_CDN_REFONTE1(SG_SMS_PHONE_NUMBER_1, "https://hooks.slack.com/services/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");

        private String phoneNumber;
        private String slackHookUrl;

        Hook(String smsPhoneNumber, String slackHookUrl) {
            Hook.this.phoneNumber = smsPhoneNumber;
            Hook.this.slackHookUrl = slackHookUrl;
        }

        public String getPhoneNumber() {
            return phoneNumber;
        }

        public String getSlackHookUrl() {
            return slackHookUrl;
        }
    }


    public void onReceive(Context context, Intent intent) {

        // Retrieves a map of extended data from the intent.
        final Bundle bundle = intent.getExtras();

        try {

            if (bundle != null) {

                final Object[] pdusObj = (Object[]) bundle.get("pdus");

                if (pdusObj == null) {
                    return;
                }

                String smsContent = null;
                Hook hookToUse = null;

                for (Object obj : pdusObj) {

                    SmsMessage currentMessage = SmsMessage.createFromPdu((byte[]) obj);
                    String phoneNumber = currentMessage.getDisplayOriginatingAddress();
                    smsContent = currentMessage.getDisplayMessageBody();
                    Log.d(TAG, "SMS PHONE NUMBER : " + phoneNumber);

                    // Dirty below :( should be refactored when possible
                    if (phoneNumber.equals(CA_SMS_PHONE_NUMBER)) {
                        hookToUse = Hook.CA_EWALLET;
                        break;
                    } else if ( phoneNumber.equals(SG_SMS_PHONE_NUMBER_1) ||
                            phoneNumber.equals(SG_SMS_PHONE_NUMBER_2) ||
                            phoneNumber.equals(SG_SMS_PHONE_NUMBER_3)) {
                        hookToUse = Hook.SG_CDN_REFONTE1;
                        break;
                    }
                }

                // Sending sms content to slack channel if authorized
                if (canRedirectSms(context)) {
                    sendDatasToSlack(hookToUse, smsContent);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception smsReceiver" +e);
        }
    }

    private boolean canRedirectSms(Context context) {
        if (context == null) {
            return false;
        }
        SharedPreferences sharedPrefs = context.getSharedPreferences(
                context.getPackageName(), Context.MODE_PRIVATE);
        return sharedPrefs.getBoolean("enableSlackRedirection", false);
    }

    // Should be refactored with an http library (eg. Retrofit)
    private void sendDatasToSlack(Hook hook, String message) {

        String json = "{ \"text\": \"" + message + "\"}";
        String hookUrl = hook.getSlackHookUrl();
        AsyncHttpClient client = new AsyncHttpClient();

        StringEntity stringEntity = null;
        try {
            stringEntity = new StringEntity(json, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            Log.d(TAG, e.getMessage(), e);
        }

        if (stringEntity != null) {
            stringEntity.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));

            client.post(null, hookUrl, stringEntity, "application/json", new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    Log.i(TAG, "onSuccess() - OTP sent to slack webhook");
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                    Log.e(TAG, "onFailure() - OTP not sent to slack webhook");
                }
            });
        }
    }
}
