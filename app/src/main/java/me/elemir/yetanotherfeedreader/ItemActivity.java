package me.elemir.yetanotherfeedreader;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import android.widget.TextView;

import me.elemir.yetanotherfeedreader.provider.FeedStore;


public class ItemActivity extends Activity {
    public static final String FEED_ID = "feed_id";
    public static final String ITEM_ID = "item_id";

    private long feedId, itemId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item);

        feedId = getIntent().getLongExtra(FEED_ID, -1);
        itemId = getIntent().getLongExtra(ITEM_ID, -1);

        if (feedId == -1 || itemId == -1)
            finish();

        Cursor cursor =
                managedQuery(FeedStore.Items.getContentUri(feedId, itemId), null,
                        null, null, null);

        if (cursor.moveToFirst()) {
            ((TextView) findViewById(R.id.item_title)).setText(cursor.getString(FeedStore.TITLE_ITEMS_COLUMN));
            ((WebView) findViewById(R.id.item_preview)).loadData(cursor.getString(FeedStore.DESCRIPTION_ITEMS_COLUMN),
                    "text/html; charset=UTF-8", null);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent upIntent = new Intent(this, FeedActivity.class);
                upIntent.putExtra(FeedActivity.FEED_ID, feedId);
                startActivity(upIntent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
