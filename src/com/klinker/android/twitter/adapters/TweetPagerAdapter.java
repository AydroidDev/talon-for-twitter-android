package com.klinker.android.twitter.adapters;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v13.app.FragmentPagerAdapter;

import com.klinker.android.twitter.R;
import com.klinker.android.twitter.settings.AppSettings;
import com.klinker.android.twitter.ui.tweet_viewer.fragments.ConversationFragment;
import com.klinker.android.twitter.ui.tweet_viewer.fragments.DiscussionFragment;
import com.klinker.android.twitter.ui.tweet_viewer.fragments.DiscussionFragment;
import com.klinker.android.twitter.ui.tweet_viewer.fragments.TweetFragment;
import com.klinker.android.twitter.ui.tweet_viewer.fragments.TweetYouTubeFragment;
import com.klinker.android.twitter.ui.tweet_viewer.fragments.WebFragment;

import java.util.ArrayList;

public class TweetPagerAdapter extends FragmentPagerAdapter {

    private Context context;
    private AppSettings settings;

    private int pageCount;
    private boolean youtube = false;
    private boolean hasWebpage = false;
    private String video = "";
    private ArrayList<String> webpages;

    private String name;
    private String screenName;
    private String tweet;
    private long time;
    private String retweeter;
    private String webpage;
    private String proPic;
    private boolean picture;
    private long tweetId;
    private String[] users;
    private String[] hashtags;
    private String[] otherLinks;
    private boolean isMyTweet;
    private boolean isMyRetweet;

    public TweetPagerAdapter(FragmentManager fm, Context context,
         String name, String screenName, String tweet, long time, String retweeter, String webpage,
         String proPic, long tweetId, boolean picture, String[] users, String[] hashtags, String[] links,
         boolean isMyTweet, boolean isMyRetweet) {

        super(fm);
        this.context = context;

        settings = new AppSettings(context);

        this.name = name;
        this.screenName = screenName;
        this.tweet = tweet;
        this.time = time;
        this.retweeter = retweeter;
        this.webpage = webpage;
        this.proPic = proPic;
        this.picture = picture;
        this.tweetId = tweetId;
        this.users = users;
        this.hashtags = hashtags;
        this.isMyRetweet = isMyRetweet;
        this.isMyTweet = isMyTweet;
        this.otherLinks = links;

        webpages = new ArrayList<String>();

        if (links == null) {
            links = new String[0];
        }

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);

        if (links.length > 0 && !links[0].equals("")) {
            for (String s : links) {
                if (s.contains("youtu")) {
                    video = s;
                    youtube = true;
                    break;
                } else {
                    webpages.add(s);
                }
            }

            if (youtube) {
                if (links.length > 1 && sharedPrefs.getBoolean("inapp_browser", true)) {
                    this.hasWebpage = true;
                } else {
                    this.hasWebpage = false;
                }
            } else {
                if (sharedPrefs.getBoolean("inapp_browser", true)) {
                    this.hasWebpage = true;
                } else {
                    this.hasWebpage= false;
                }
            }
        } else {
            this.hasWebpage = false;
        }

        pageCount = 3; // tweet, conversations, and Discussion will always be there

        if (this.hasWebpage) {
            pageCount++;
        }
        if(youtube) {
            pageCount++;
        }
    }

    @Override
    public Fragment getItem(int i) {

        if (pageCount == 3) {
            switch (i) {
                case 0:
                    TweetFragment tweetFragment = new TweetFragment(settings, name, screenName, tweet, time, retweeter, webpage, proPic, tweetId, picture, users, hashtags, otherLinks, isMyTweet, isMyRetweet);
                    return tweetFragment;
                case 1:
                    DiscussionFragment discussion = new DiscussionFragment(settings, tweetId, screenName);
                    return discussion;
                case 2:
                    ConversationFragment conversation = new ConversationFragment(settings, tweetId);
                    return conversation;
            }
        } else if (pageCount == 4 && youtube) {
            switch (i) {
                case 0:
                    TweetYouTubeFragment youTube = new TweetYouTubeFragment(settings, video);
                    return youTube;
                case 1:
                    TweetFragment tweetFragment = new TweetFragment(settings, name, screenName, tweet, time, retweeter, webpage, proPic, tweetId, picture, users, hashtags, otherLinks, isMyTweet, isMyRetweet);
                    return tweetFragment;
                case 2:
                    DiscussionFragment discussion = new DiscussionFragment(settings, tweetId, screenName);
                    return discussion;
                case 3:
                    ConversationFragment conversation = new ConversationFragment(settings, tweetId);
                    return conversation;
            }
        } else if (pageCount == 4) { // no youtube, just a hasWebpage
            switch (i) {
                case 0:
                    WebFragment web = new WebFragment(settings, webpages);
                    return web;
                case 1:
                    TweetFragment tweetFragment = new TweetFragment(settings, name, screenName, tweet, time, retweeter, webpage, proPic, tweetId, picture, users, hashtags, otherLinks, isMyTweet, isMyRetweet);
                    return tweetFragment;
                case 2:
                    DiscussionFragment discussion = new DiscussionFragment(settings, tweetId, screenName);
                    return discussion;
                case 3:
                    ConversationFragment conversation = new ConversationFragment(settings, tweetId);
                    return conversation;
            }
        } else { // every page is shown
            switch (i) {
                case 0:
                    TweetYouTubeFragment youTube = new TweetYouTubeFragment(settings, video);
                    return youTube;
                case 1:
                    WebFragment web = new WebFragment(settings, webpages);
                    return web;
                case 2:
                    TweetFragment tweetFragment = new TweetFragment(settings, name, screenName, tweet, time, retweeter, webpage, proPic, tweetId, picture, users, hashtags, otherLinks, isMyTweet, isMyRetweet);
                    return tweetFragment;
                case 3:
                    DiscussionFragment Discussion = new DiscussionFragment(settings, tweetId, screenName);
                    return Discussion;
                case 4:
                    ConversationFragment conversation = new ConversationFragment(settings, tweetId);
                    return conversation;
            }
        }

        return null;
    }

    @Override
    public int getCount() {
        return pageCount;
    }

    public boolean getHasWebpage() {
        return hasWebpage;
    }

    public boolean getHasYoutube() {
        return youtube;
    }

    @Override
    public CharSequence getPageTitle(int i) {
        if (pageCount == 3) {
            switch (i) {
                case 0:
                    return context.getResources().getString(R.string.tweet);
                case 1:
                    return context.getResources().getString(R.string.discussion);
                case 2:
                    return context.getResources().getString(R.string.conversation);
            }
        } else if (pageCount == 4 && youtube) {
            switch (i) {
                case 0:
                    return context.getResources().getString(R.string.tweet_youtube);
                case 1:
                    return context.getResources().getString(R.string.tweet);
                case 2:
                    return context.getResources().getString(R.string.discussion);
                case 3:
                    return context.getResources().getString(R.string.conversation);
            }
        } else if (pageCount == 4) { // no youtube, just a hasWebpage
            switch (i) {
                case 0:
                    return context.getResources().getString(R.string.webpage);
                case 1:
                    return context.getResources().getString(R.string.tweet);
                case 2:
                    return context.getResources().getString(R.string.discussion);
                case 3:
                    return context.getResources().getString(R.string.conversation);
            }
        } else { // every page is shown
            switch (i) {
                case 0:
                    return context.getResources().getString(R.string.webpage);
                case 1:
                    return context.getResources().getString(R.string.tweet_youtube);
                case 2:
                    return context.getResources().getString(R.string.tweet);
                case 3:
                    return context.getResources().getString(R.string.discussion);
                case 4:
                    return context.getResources().getString(R.string.conversation);
            }
        }
        return null;
    }
}