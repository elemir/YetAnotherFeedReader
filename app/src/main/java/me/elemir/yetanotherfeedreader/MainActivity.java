package me.elemir.yetanotherfeedreader;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import me.elemir.yetanotherfeedreader.provider.FeedStore;


public class MainActivity extends ListActivity
        implements AddFeedFragment.OnFragmentInteractionListener,
        LoaderManager.LoaderCallbacks<Cursor> {
    private final int REQUEST_NEW_FEED = 0;
    private SimpleCursorAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        adapter = new SimpleCursorAdapter(this,
                R.layout.feed_list_item,
                null,
                new String[] {
                        FeedStore.Feeds.TITLE
                },
                new int[] { R.id.feed_title },
                CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);

        SimpleCursorAdapter.ViewBinder scavb =
                new SimpleCursorAdapter.ViewBinder() {
                    @Override
                    public boolean setViewValue(View view, Cursor cursor, int i) {
                        TextView tv;
                        switch (i) {
                            case FeedStore.TITLE_FEEDS_COLUMN:
                                tv = (TextView)
                                        view.findViewById(R.id.feed_title);
                                String feedTitle = cursor.getString(i);
                                tv.setText(feedTitle);

                                break;
                        }

                        return true;
                    }
                };

        adapter.setViewBinder(scavb);
        setListAdapter(adapter);
        registerForContextMenu(getListView());
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add:
                AddFeedFragment addFeedFragment = new AddFeedFragment();
                addFeedFragment.show(getFragmentManager(), "add_feed");
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onListItemClick(android.widget.ListView l, View v,
                                   int position, long id) {
        Intent intent = new Intent(this, FeedActivity.class);
        intent.putExtra(FeedActivity.FEED_ID, id);
        startActivityForResult(intent, REQUEST_NEW_FEED);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.feed_context_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item
                .getMenuInfo();
        String feedName = ((TextView) info.targetView.findViewById(R.id.feed_title)).getText().toString();

        switch (item.getItemId()) {
            case R.id.delete:
                buildDeleteDialog(info.id, feedName).show();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }


    private AlertDialog buildDeleteDialog(final long feedId, String feedName) {
        return new AlertDialog.Builder(this)
                .setTitle(feedName)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, final int which) {
                        getContentResolver().delete(FeedStore.Feeds.getContentUri(feedId), null, null);
                    }
                }).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, final int which) {
                        dialog.dismiss();
                    }
                }).setMessage(R.string.feed_delete).create();
    }

    @Override
    public void onFeedAdded(String link) {
        ContentValues values = new ContentValues();

        values.put(FeedStore.Feeds.LINK, link);
        getContentResolver().insert(FeedStore.Feeds.CONTENT_URI, values);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this, FeedStore.Feeds.CONTENT_URI, null,
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
