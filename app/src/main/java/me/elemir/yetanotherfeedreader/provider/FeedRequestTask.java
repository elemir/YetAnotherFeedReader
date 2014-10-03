package me.elemir.yetanotherfeedreader.provider;

import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;

public class FeedRequestTask implements Runnable {
    private static final String LOG_TAG = "me.elemir.YetAnotherFeedReader.provider.FeedQueryTask";
    private HttpUriRequest mRequest;
    private RSSContentProvider mProvider;
    private String mRequestUri;
    private RSSResponseHandler mHandler;

    public FeedRequestTask(String requestUri, RSSContentProvider provider,
                           HttpUriRequest request,RSSResponseHandler handler) {
        mRequest = request;
        mProvider = provider;
        mRequestUri = requestUri;
        mHandler = handler;
    }

    public void run() {
        HttpResponse response;

        try {
            HttpClient client = new DefaultHttpClient();
            response = client.execute(mRequest);
            mHandler.handleResponse(response);
        } catch (IOException e) {
            Log.w(LOG_TAG, "exception processing asynch request", e);
        } finally {
            if (mProvider != null) {
                mProvider.requestComplete(mRequestUri);
            }
        }
    }
}
