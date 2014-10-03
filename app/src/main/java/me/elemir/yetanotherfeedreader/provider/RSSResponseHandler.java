package me.elemir.yetanotherfeedreader.provider;

import android.content.ContentValues;
import android.provider.BaseColumns;
import android.util.Log;
import android.util.Xml;

import org.apache.http.HttpResponse;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;


public class RSSResponseHandler {
    private final static String LOG_TAG = "me.elemir.YetAnotherFeedReader.provider.RSSResponseHandler";
    private final DateFormat dateFormatter = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.ENGLISH);
    private RSSContentProvider mContentProvider;
    private long mFeedId;

    public RSSResponseHandler(RSSContentProvider contentProvider, long feedId) {
        mContentProvider = contentProvider;
        mFeedId = feedId;
    }

    public void handleResponse(HttpResponse response) {
        RSSFeed rssFeed = null;

        try {
            InputStream feedContent = response.getEntity().getContent();
            InputStreamReader inputReader = new InputStreamReader(feedContent);

            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(false);
            XmlPullParser parser = factory.newPullParser();

            parser.setInput(inputReader);

            rssFeed = processFeed(parser);

            ContentValues feedValues = new ContentValues();
            feedValues.put(FeedStore.Feeds.DESCRIPTION, rssFeed.getDescription());
            feedValues.put(FeedStore.Feeds.TITLE, rssFeed.getTitle());

            mContentProvider.update(FeedStore.Feeds.CONTENT_URI, feedValues,
                    BaseColumns._ID + " = ?", new String [] { Long.toString(mFeedId) });

            for (RSSFeed.RSSItem rssItem : rssFeed.getItems()) {
                ContentValues itemValues = new ContentValues();
                itemValues.put(FeedStore.Items.DESCRIPTION, rssItem.getDescription());
                itemValues.put(FeedStore.Items.TITLE, rssItem.getTitle());
                itemValues.put(FeedStore.Items.PUBDATE, rssItem.getPubDate());
                itemValues.put(FeedStore.Items.GUID, rssItem.getGuid());

                mContentProvider.insert(FeedStore.Items.getContentUri(mFeedId), itemValues);
            }
            /* TODO: notify changes */
        } catch (XmlPullParserException e) {
            Log.d(LOG_TAG, "could not parse rss feed", e);
        } catch (ParseException e) {
            Log.d(LOG_TAG, "could not parse rss feed", e);
        } catch (IOException e) {
            Log.d(LOG_TAG, "network problems", e);
        }
    }

    private RSSFeed processFeed(XmlPullParser parser) throws IOException,
            XmlPullParserException, ParseException {
        RSSFeed rssFeed = new RSSFeed();

        while (parser.next() != XmlPullParser.END_DOCUMENT) {
            if (parser.getEventType() != XmlPullParser.START_TAG)
                continue;

            String name = parser.getName();

            if (name.equalsIgnoreCase("title"))
                rssFeed.setTitle(parser.nextText());
            else if (name.equalsIgnoreCase("link"))
                rssFeed.setLink(parser.nextText());
            else if (name.equalsIgnoreCase("description"))
                rssFeed.setDescription(parser.nextText());
            else if (name.equalsIgnoreCase("item")) {
                RSSFeed.RSSItem rssItem = processItem(parser);
                rssFeed.getItems().add(rssItem);
            }
        }

        return rssFeed;
    }

    private RSSFeed.RSSItem processItem(XmlPullParser parser) throws IOException,
            XmlPullParserException, ParseException {
        boolean cont = true;
        RSSFeed.RSSItem rssItem = new RSSFeed.RSSItem();

        parser.require(XmlPullParser.START_TAG, null, "item");

        while (cont) {
            if (parser.next() == XmlPullParser.END_TAG)
                cont = !parser.getName().equalsIgnoreCase("item");

            if (parser.getEventType() != XmlPullParser.START_TAG)
                continue;

            String name = parser.getName();

            if(name.equalsIgnoreCase("guid"))
                rssItem.setGuid(parser.nextText());
            else if(name.equalsIgnoreCase("title"))
                rssItem.setTitle(parser.nextText());
            else if(name.equalsIgnoreCase("description"))
                rssItem.setDescription(parser.nextText());
            else if(name.equalsIgnoreCase("pubDate"))
                rssItem.setPubDate(parser.nextText());
        }
        return rssItem;
    }
}
