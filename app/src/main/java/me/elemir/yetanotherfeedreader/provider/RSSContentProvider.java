package me.elemir.yetanotherfeedreader.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.provider.BaseColumns;
import android.text.TextUtils;
import android.util.Log;

import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;

import java.util.HashMap;
import java.util.Map;

public class RSSContentProvider extends ContentProvider {
    public static final String FEED = "feed";
    public static final String DATABASE_NAME = FEED + ".db";
    public static final String FEEDS_TABLE_NAME = "feeds";
    public static final String ITEMS_TABLE_NAME = "items";

    static int DATABASE_VERSION = 2;

    private static final int FEEDS = 1;
    private static final int FEED_ID = 2;
    private static final int ITEMS = 3;
    private static final int ITEM_ID = 4;


    private static UriMatcher sUriMatcher;
    private final Map<String, FeedRequestTask> mRequestsInProgress =
            new HashMap<String, FeedRequestTask>();

    static {
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(FeedStore.AUTHORITY, FeedStore.Feeds.FEED_NAME, FEEDS);
        sUriMatcher.addURI(FeedStore.AUTHORITY, FeedStore.Feeds.FEED_NAME + "/#",
                FEED_ID);
        sUriMatcher.addURI(FeedStore.AUTHORITY, FeedStore.Feeds.FEED_NAME + "/#/" +
                FeedStore.Items.ITEM_NAME, ITEMS);
        sUriMatcher.addURI(FeedStore.AUTHORITY, FeedStore.Feeds.FEED_NAME + "/#/" +
                FeedStore.Items.ITEM_NAME + "/#", ITEM_ID);
    }

    private DatabaseHelper mOpenHelper;
    private SQLiteDatabase mDb;

    private static class DatabaseHelper extends SQLiteOpenHelper {
        private DatabaseHelper(Context context, String name,
                               SQLiteDatabase.CursorFactory factory) {
            super(context, name, factory, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase sqLiteDatabase) {
            createTables(sqLiteDatabase);
            initFeeds(sqLiteDatabase);
        }

        private void createTables(SQLiteDatabase sqLiteDatabase) {
            String createFeeds =
                    "CREATE TABLE " + FEEDS_TABLE_NAME + " (" +
                            BaseColumns._ID +
                            " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                            FeedStore.Feeds.LINK + " TEXT UNIQUE, " +
                            FeedStore.Feeds.TITLE + " TEXT, " +
                            FeedStore.Feeds.DESCRIPTION + " TEXT" +
                            "); ";
            String createItems =
                    "CREATE TABLE " + ITEMS_TABLE_NAME + " (" +
                            BaseColumns._ID +
                            " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                            FeedStore.Items.FEED_ID + " INTEGER, " +
                            FeedStore.Items.TITLE + " TEXT, " +
                            FeedStore.Items.DESCRIPTION + " TEXT, " +
                            FeedStore.Items.PUBDATE + " TEXT, " +
                            FeedStore.Items.LINK + " TEXT, " +
                            FeedStore.Items.GUID + " TEXT UNIQUE" +
                            ");";
            sqLiteDatabase.execSQL(createFeeds);
            sqLiteDatabase.execSQL(createItems);
        }

        private void initFeeds(SQLiteDatabase sqLiteDatabase) {
            ContentValues values = new ContentValues();

            values.put(FeedStore.Feeds.TITLE, "Яндекс.Новости: Главные новости");
            values.put(FeedStore.Feeds.LINK, "http://news.yandex.ru/index.rss");
            sqLiteDatabase.insert(FEEDS_TABLE_NAME, "", values);

            values.clear();
            values.put(FeedStore.Feeds.TITLE, "Liftoff News");
            values.put(FeedStore.Feeds.LINK, "http://www.rssboard.org/files/sample-rss-2.xml");
            sqLiteDatabase.insert(FEEDS_TABLE_NAME, "", values);

            values.clear();
            values.put(FeedStore.Feeds.TITLE, "World news | The Guardian");
            values.put(FeedStore.Feeds.LINK, "http://www.theguardian.com/world/rss");
            sqLiteDatabase.insert(FEEDS_TABLE_NAME, "", values);
        }

        @Override
        public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldv,
                              int newv) {
            /* Serious upgrade code will be here*/
        }
    }

    public RSSContentProvider() {
    }

    public void requestComplete(String mQueryText) {
        synchronized (mRequestsInProgress) {
            mRequestsInProgress.remove(mQueryText);
        }
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        long feedId;
        int match = sUriMatcher.match(uri);
        int affected;

        SQLiteDatabase db = mOpenHelper.getWritableDatabase();

        switch (match) {
            case FEED_ID:
                feedId = ContentUris.parseId(uri);
                affected = db.delete(FEEDS_TABLE_NAME,
                        BaseColumns._ID + "=" + feedId
                                + (!TextUtils.isEmpty(selection) ?
                                " AND (" + selection + ')' : ""),
                        selectionArgs);
                affected += db.delete(ITEMS_TABLE_NAME,
                        FeedStore.Items.FEED_ID + "=" + feedId
                                + (!TextUtils.isEmpty(selection) ?
                                " AND (" + selection + ')' : ""),
                        selectionArgs);
                getContext().getContentResolver().notifyChange(uri, null);
                break;
            default:
                throw new IllegalArgumentException("unknown feed element: " +
                        uri);
        }

        return affected;
    }

    @Override
    public String getType(Uri uri) {
        switch (sUriMatcher.match(uri)) {
            case FEEDS:
                return FeedStore.Feeds.CONTENT_TYPE;

            case FEED_ID:
                return FeedStore.Feeds.CONTENT_FEED_TYPE;

            case ITEMS:
                return FeedStore.Items.CONTENT_TYPE;

            case ITEM_ID:
                return FeedStore.Items.CONTENT_ITEM_TYPE;

            default:
                throw new IllegalArgumentException("Unknown feed type: " +
                        uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        Long rowID;

        switch (sUriMatcher.match(uri)) {
            case FEEDS:
                String link = values.getAsString(FeedStore.Feeds.LINK);
                rowID = feedExists(link);

                if (rowID == null) {
                    long rowId = mDb.insert(FEEDS_TABLE_NAME,
                            FeedStore.Feeds.FEED_NAME, values);
                    if (rowId >= 0) {
                        asyncFeedRequest(link, rowId);
                        Uri insertUri =
                                ContentUris.withAppendedId(
                                        FeedStore.Feeds.CONTENT_URI, rowId);
                        getContext().getContentResolver().notifyChange(insertUri, null);
                        return insertUri;
                    }
                }

                return ContentUris.withAppendedId(FeedStore.Feeds.CONTENT_URI, rowID);
            case ITEMS:
                long feedId = Long.valueOf(uri.getPathSegments().get(1));
                String guid = values.getAsString(FeedStore.Items.GUID);
                rowID = itemExists(guid);

                if (rowID == null) {
                    values.put(FeedStore.Items.FEED_ID, feedId);
                    long rowId = mDb.insert(ITEMS_TABLE_NAME,
                            FeedStore.Items.ITEM_NAME, values);
                    if (rowId >= 0) {
                        Uri insertUri =
                                ContentUris.withAppendedId(FeedStore.Items.getContentUri(feedId), rowId);
                        getContext().getContentResolver().notifyChange(insertUri, null);
                        getContext().getContentResolver().notifyChange(uri, null);
                        return insertUri;
                    }
                }

                return ContentUris.withAppendedId(FeedStore.Items.getContentUri(feedId), rowID);
            default:
                throw new IllegalArgumentException("Unknown feed type: " +
                        uri);
        }
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new DatabaseHelper(getContext(), DATABASE_NAME, null);
        mDb = mOpenHelper.getWritableDatabase();

        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        String sFeedId;
        Cursor queryCursor;

        switch (sUriMatcher.match(uri)) {
            case FEEDS:
                queryCursor = mDb.query(FEEDS_TABLE_NAME, projection, selection,
                        selectionArgs, null, null, sortOrder);
                break;

            case FEED_ID:
                sFeedId = uri.getPathSegments().get(1);
                queryCursor = mDb.query(FEEDS_TABLE_NAME, projection,
                        BaseColumns._ID + "=" + sFeedId
                                + (!TextUtils.isEmpty(selection) ?
                                " AND (" + selection + ')' : ""),
                        selectionArgs, null, null, sortOrder);
                break;
            case ITEMS:
                sFeedId = uri.getPathSegments().get(1);
                Long lFeedId = Long.valueOf(sFeedId);
                asyncFeedRequest(getFeedLink(lFeedId), lFeedId);

                queryCursor = mDb.query(ITEMS_TABLE_NAME, projection,
                        FeedStore.Items.FEED_ID + "=" + sFeedId
                                + (!TextUtils.isEmpty(selection) ?
                                " AND (" + selection + ')' : ""),
                        selectionArgs, null, null, sortOrder);
                break;
            case ITEM_ID:
                sFeedId = uri.getPathSegments().get(1);
                String itemId = uri.getPathSegments().get(3);

                queryCursor = mDb.query(ITEMS_TABLE_NAME, projection,
                        BaseColumns._ID + "=" + itemId
                                + " AND " + FeedStore.Items.FEED_ID + "=" + sFeedId
                                + (!TextUtils.isEmpty(selection) ?
                                " AND (" + selection + ')' : ""),
                        selectionArgs, null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        queryCursor.setNotificationUri(getContext().getContentResolver(), uri);

        return queryCursor;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        getContext().getContentResolver().notifyChange(uri, null);

        int count;
        switch (sUriMatcher.match(uri)) {
            case FEEDS:
                count = mDb.update(FEEDS_TABLE_NAME, values, selection, selectionArgs);
                break;
            case FEED_ID:
                String feedId = uri.getPathSegments().get(1);
                count = mDb.update(FEEDS_TABLE_NAME, values,
                        BaseColumns._ID + "=" + feedId
                                + (!TextUtils.isEmpty(selection) ?
                                " AND (" + selection + ')' : ""),
                        selectionArgs);
                break;
            case ITEMS:
                count = mDb.update(ITEMS_TABLE_NAME, values, selection, selectionArgs);
                break;
            case ITEM_ID:
                String itemId = uri.getPathSegments().get(1);
                count = mDb.update(FEEDS_TABLE_NAME, values,
                        BaseColumns._ID + "=" + itemId
                                + (!TextUtils.isEmpty(selection) ?
                                " AND (" + selection + ')' : ""),
                        selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    private Long feedExists(String link) {
        Cursor cursor = null;
        Long rowID = null;
        try {
            cursor = mDb.query(FEEDS_TABLE_NAME, null,
                    FeedStore.Feeds.LINK + " = '" +link + "'",
                    null, null, null, null);
            if (cursor.moveToFirst())
                rowID = cursor.getLong(FeedStore.ID_FEEDS_COLUMN);
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return rowID;
    }

    private Long itemExists(String guid) {
        Cursor cursor = null;
        Long rowID = null;
        try {
            cursor = mDb.query(ITEMS_TABLE_NAME, null,
                    FeedStore.Items.GUID + " = '" + guid + "'",
                    null, null, null, null);
            if (cursor.moveToFirst())
                rowID = cursor.getLong(FeedStore.ID_ITEMS_COLUMN);
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return rowID;
    }

    String getFeedLink(long feedId) {
        Cursor cursor = null;
        String link = null;
        try {
            cursor = mDb.query(FEEDS_TABLE_NAME, null,
                    BaseColumns._ID + "=" + feedId,
                    null, null, null, null);
            if (cursor.moveToFirst())
                link = cursor.getString(FeedStore.LINK_FEEDS_COLUMN);
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return link;
    }

    public void asyncFeedRequest(String queryUri, long feedId) {
        if (queryUri != null) {
            synchronized (mRequestsInProgress) {
                FeedRequestTask requestTask = mRequestsInProgress.get(queryUri);
                if (requestTask == null) {
                    final HttpGet get = new HttpGet(queryUri);
                    RSSResponseHandler handler = new RSSResponseHandler(this, feedId);
                    requestTask = new FeedRequestTask(queryUri, this, get, handler);

                    mRequestsInProgress.put(queryUri, requestTask);
                    Thread t = new Thread(requestTask);
                    t.start();
                }
            }
        }
    }
}