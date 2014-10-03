package me.elemir.yetanotherfeedreader.provider;

import java.util.ArrayList;
import java.util.Date;

public class RSSFeed {
    private String mTitle, mDescription, mLink;
    private ArrayList<RSSItem> mItems;

    public RSSFeed() {
        mItems = new ArrayList<RSSItem>();
    }

    public void setLink(String link) {
        mLink = link;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public void setDescription(String description) {
        mDescription = description;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getDescription() {
        return mDescription;
    }

    public String getLink() {
        return mLink;
    }

    public ArrayList<RSSItem> getItems() {
        return mItems;
    }

    public static class RSSItem {
        private String mTitle, mDescription, mGuid, mPubDate;

        public String getTitle() {
            return mTitle;
        }

        public void setTitle(String title) {
            mTitle = title;
        }

        public String getDescription() {
            return mDescription;
        }

        public void setDescription(String description) {
            mDescription = description;
        }

        public String getGuid() {
            return mGuid;
        }

        public void setGuid(String guid) {
            mGuid = guid;
        }

        public String getPubDate() {
            return mPubDate;
        }

        public void setPubDate(String pubDate) {
            mPubDate = pubDate;
        }
    }
}
