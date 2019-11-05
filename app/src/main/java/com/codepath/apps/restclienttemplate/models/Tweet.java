package com.codepath.apps.restclienttemplate.models;

import android.util.Log;

import com.codepath.apps.restclienttemplate.utility.TimeFormatter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcel;

import java.util.ArrayList;
import java.util.List;

@Parcel
public class Tweet {

    public static final String TAG = Tweet.class.getSimpleName();

    public long id;
    public String body;
    public String createdAt;
    public User user;
    public String shortenedUrl;
    public String displayUrl;
    public String expandedUrl;
    public String imageUrl;
    public int retweetCount;
    public int favoriteCount;
    public Boolean favorited;
    public Boolean retweeted;

    // Empty constructor needed by Parceler library
    public Tweet(){}

    public static Tweet fromExtendedJson(JSONObject jsonObject) throws JSONException {
        Tweet tweet = new Tweet();

        tweet.displayUrl = null;
        tweet.expandedUrl = null;
        tweet.imageUrl = null;

        JSONObject entities = null;
        try {
            entities = jsonObject.getJSONObject("entities");
            JSONArray urls = entities.getJSONArray("urls");
            JSONObject first_url = urls.getJSONObject(0);
            tweet.shortenedUrl = first_url.getString("url");
            tweet.displayUrl = first_url.getString("display_url");
            tweet.expandedUrl = first_url.getString("expanded_url");
        } catch (JSONException e) {
            Log.i(Tweet.TAG, "Could not retrieve an URLs associated with the tweet.");
        }

        tweet.id = jsonObject.getLong("id");
        tweet.createdAt = jsonObject.getString("created_at");
        tweet.user = User.fromJson(jsonObject.getJSONObject("user"));
        tweet.retweetCount = jsonObject.getInt("retweet_count");
        tweet.favoriteCount = jsonObject.getInt("favorite_count");
        tweet.favorited = jsonObject.getBoolean("favorited");
        tweet.retweeted = jsonObject.getBoolean("retweeted");

        // Remove twitter link from the end of the tweet
        String pattern = "https://t.co/[\\w]{10}$";
        tweet.body = jsonObject.getString("full_text").replaceAll(pattern, "");

        // Replace shortened twitter links with actual link
        if (tweet.shortenedUrl != null && tweet.displayUrl != null) {
            tweet.body = tweet.body.replaceAll(tweet.shortenedUrl, tweet.displayUrl);
        }

        return tweet;
    }

    public static Tweet fromJson(JSONObject jsonObject) throws JSONException {
        Tweet tweet = new Tweet();

        tweet.displayUrl = null;
        tweet.expandedUrl = null;
        tweet.imageUrl = null;

        tweet.id = jsonObject.getLong("id");
        tweet.createdAt = jsonObject.getString("created_at");
        tweet.user = User.fromJson(jsonObject.getJSONObject("user"));
        tweet.retweetCount = jsonObject.getInt("retweet_count");
        tweet.favoriteCount = jsonObject.getInt("favorite_count");

        // Remove twitter link from the end of the tweet
        tweet.body = jsonObject.getString("text");

        return tweet;
    }

    public static List<Tweet> fromJsonArray(JSONArray jsonArray) throws JSONException{
        List<Tweet> tweets = new ArrayList<>();

        for (int i = 0; i < jsonArray.length(); i++) {
            tweets.add(fromExtendedJson(jsonArray.getJSONObject(i)));
        }

        return tweets;
    }

    public static String getFormattedTimestamp(Tweet tweet) {
        return TimeFormatter.getTimeDifference(tweet.createdAt);
    }
}
