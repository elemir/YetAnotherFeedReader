package me.elemir.yetanotherfeedreader.provider;

import android.net.Uri;
import android.provider.BaseColumns;

public class FeedStore {

    public static final String AUTHORITY =
            "me.elemir.yetanotherfeedreader.YetAnotherFeedReader";

    public static final int ID_FEEDS_COLUMN = 0;
    /* Required RSS channel fields */
    public static final int LINK_FEEDS_COLUMN = 1;
    public static final int TITLE_FEEDS_COLUMN = 2;
    public static final int DESCRIPTION_FEEDS_COLUMN = 3;

    public static final int ID_ITEMS_COLUMN = 0;
    public static final int FEED_ID_ITEMS_COLUMN = 1;
    public static final int TITLE_ITEMS_COLUMN = 2;
    public static final int DESCRIPTION_ITEMS_COLUMN = 3;
    public static final int PUBDATE_ITEMS_COLUMN = 4;
    public static final int LINK_ITEMS_COLUMN = 5;
    public static final int GUID_ITEMS_COLUMN = 6;

    public static final class Feeds implements BaseColumns {
        public static final String DEFAULT_SORT_ORDER = "modified DESC";

        private Feeds() {}

        public static final Uri FEEDS_URI = Uri.parse("content://" +
                AUTHORITY + "/" + Feeds.FEED_NAME);

        public static Uri getContentUri(long feedId) {
            return Uri.parse("content://" +
                    AUTHORITY + "/" + Feeds.FEED_NAME + "/" + feedId);
        }

        public static final Uri CONTENT_URI = FEEDS_URI;

        public static final String CONTENT_TYPE =
                "vnd.android.cursor.dir/vnd.feedstore.feed";

        public static final String CONTENT_FEED_TYPE =
                "vnd.android.cursor.item/vnd.feedstore.feed";

        public static final String FEED_NAME = "feed";

        public static final String TITLE = "title";
        public static final String DESCRIPTION = "description";
        public static final String LINK = "link";
    }

    public static final class Items implements BaseColumns {
        public static final String DEFAULT_SORT_ORDER = "modified DESC";

        private Items() {}

        public static Uri getContentUri(long feedId) {
            return Uri.parse("content://" +
                    AUTHORITY + "/" + Feeds.FEED_NAME + "/" + feedId + "/" +
                    Items.ITEM_NAME);
        }

        public static Uri getContentUri(long feedId, long itemId) {
            return Uri.parse("content://" +
                    AUTHORITY + "/" + Feeds.FEED_NAME + "/" + feedId + "/" +
                    Items.ITEM_NAME + "/" + itemId);
        }

        public static final String CONTENT_TYPE =
                "vnd.android.cursor.dir/vnd.feedstore.item";

        public static final String CONTENT_ITEM_TYPE =
                "vnd.android.cursor.item/vnd.feedstore.item";

        public static final String ITEM_NAME = "feed";

        public static final String FEED_ID = "feed_id";
        public static final String TITLE = "title";
        public static final String DESCRIPTION = "description";
        public static final String LINK = "link";
        public static final String GUID = "guid";
        public static final String PUBDATE = "pubdate";
    }
}
