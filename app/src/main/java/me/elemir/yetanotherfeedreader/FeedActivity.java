package me.elemir.yetanotherfeedreader;

import android.app.Activity;
import android.app.ListActivity;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import me.elemir.yetanotherfeedreader.provider.FeedStore;


public class FeedActivity extends ListActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {
    public static final String FEED_ID = "feed_id";

    private SimpleCursorAdapter adapter;
    private long feedId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed);

        feedId = getIntent().getLongExtra(FEED_ID, -1);

        if (feedId == -1)
            finish();

        final ListView itemsList = getListView();

        adapter = new SimpleCursorAdapter(this,
                R.layout.message_list_item,
                null,
                new String[] {
                        FeedStore.Items.TITLE,
                        FeedStore.Items.DESCRIPTION,
                },
                new int[] { R.id.message_title, R.id.message_description },
                CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);

        SimpleCursorAdapter.ViewBinder scavb =
                new SimpleCursorAdapter.ViewBinder() {
                    @Override
                    public boolean setViewValue(View view, Cursor cursor, int i) {
                        TextView tv;
                        switch (i) {
                            case FeedStore.TITLE_ITEMS_COLUMN:
                                tv = (TextView)
                                        view.findViewById(R.id.message_title);
                                String feedTitle = cursor.getString(i);
                                tv.setText(feedTitle);

                                break;
                            case FeedStore.DESCRIPTION_ITEMS_COLUMN:
                                tv = (TextView)
                                        view.findViewById(R.id.message_description);
                                String feedDescription = cursor.getString(i);
                                tv.setText(feedDescription);
                                break;
                        }

                        return true;
                    }
                };

        adapter.setViewBinder(scavb);
        setListAdapter(adapter);
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    protected void onListItemClick(android.widget.ListView l, View v,
                    int position, long id) {
        Intent intent = new Intent(this, ItemActivity.class);
        intent.putExtra(ItemActivity.FEED_ID, feedId);
        intent.putExtra(ItemActivity.ITEM_ID, id);
        startActivity(intent);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this, FeedStore.Items.getContentUri(feedId), null,
                        null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        adapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }
}
