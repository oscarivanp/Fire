package com.rmasc.fireroad;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.twitter.sdk.android.tweetcomposer.TweetUploadService;

/**
 * Created by rafaelmartinez on 11/03/16.
 */
class MyResultReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (TweetUploadService.UPLOAD_SUCCESS.equals(intent.getAction())) {
            // success
            final Long tweetId = intent.getExtras().getLong(TweetUploadService.EXTRA_TWEET_ID);
        } else {
            // failure
            final Intent retryIntent = getResultExtras(true).getParcelable(TweetUploadService.EXTRA_RETRY_INTENT);
        }
    }
}