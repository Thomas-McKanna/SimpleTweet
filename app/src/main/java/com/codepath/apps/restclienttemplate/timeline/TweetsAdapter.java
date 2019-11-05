package com.codepath.apps.restclienttemplate.timeline;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.BaseTarget;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.codepath.apps.restclienttemplate.R;
import com.codepath.apps.restclienttemplate.TwitterApp;
import com.codepath.apps.restclienttemplate.TwitterClient;
import com.codepath.apps.restclienttemplate.models.Tweet;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import jp.wasabeef.glide.transformations.RoundedCornersTransformation;
import okhttp3.Headers;

public class TweetsAdapter extends RecyclerView.Adapter<TweetsAdapter.ViewHolder>{

    private final String TAG = TweetsAdapter.class.getSimpleName();

    Context context;
    List<Tweet> tweets;

    TwitterClient client;

    // Pass in the context and list of tweets
    public TweetsAdapter(Context context, List<Tweet> tweets) {
        this.context = context;
        this.tweets = tweets;

        client = TwitterApp.getRestClient(context);
    }

    // For each row, inflate the layout
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_tweet, parent, false);
        return new ViewHolder(view);
    }

    // Bind values based on the position of the element
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Get the data at position
        Tweet tweet = tweets.get(position);

        // Bind the tweet with the view holder
        holder.bind(tweet);
    }

    @Override
    public int getItemCount() {
        return tweets.size();
    }

    public void addTweetsToFront(List<Tweet> list) {
        int startIndex = tweets.size();
        Collections.reverse(tweets);
        Collections.reverse(list);
        tweets.addAll(list);
        Collections.reverse(tweets);

        if (list.size() != 0) {
            notifyItemRangeChanged(startIndex, list.size());
        }
    }

    public void addTweetsToBack(List<Tweet> list) {
        int startIndex = tweets.size();
        tweets.addAll(list);

        if (list.size() != 0) {
            notifyItemRangeChanged(startIndex, list.size());
        }
    }

    // Define a viewholder
    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView ivProfileImage;
        ImageView ivThumbnail;
        ImageView ivHeart;
        ImageView ivRetweet;
        TextView tvBody;
        TextView tvUsername;
        TextView tvHandle;
        TextView tvAge;
        TextView tvRetweetCount;
        TextView tvFavoriteCount;
        Tweet tweet;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            Typeface regular = ResourcesCompat.getFont(context, R.font.helvetica_neue_regular);
            Typeface bold = ResourcesCompat.getFont(context, R.font.helvetica_neue_bold);

            ivProfileImage = itemView.findViewById(R.id.ivUserPicture);

            ivThumbnail =itemView.findViewById(R.id.ivThumbnail);

            ivHeart = itemView.findViewById(R.id.ivHeart);

            ivRetweet = itemView.findViewById(R.id.ivRepeat);

            tvBody = itemView.findViewById(R.id.tvBody);
            tvBody.setTypeface(regular);

            tvUsername = itemView.findViewById(R.id.tvUsername);
            tvUsername.setTypeface(bold);

            tvHandle = itemView.findViewById(R.id.tvHandle);
            tvHandle.setTypeface(regular);

            tvAge = itemView.findViewById(R.id.tvAge);
            tvAge.setTypeface(regular);

            tvRetweetCount = itemView.findViewById(R.id.tvRetweetCount);
            tvRetweetCount.setTypeface(regular);

            tvFavoriteCount = itemView.findViewById(R.id.tvFavoriteCount);
            tvFavoriteCount.setTypeface(regular);
        }

        public void bind(final Tweet tweet) {
            this.tweet = tweet;

            if (tweet.shortenedUrl != null && tweet.imageUrl == null) {
                // Try to fetch an image for this tweet
                new FetchOGImage().execute(tweet);
            }

            tvBody.setText(tweet.body);
            tvUsername.setText(tweet.user.name);
            tvHandle.setText("@" + tweet.user.screenName);
            tvAge.setText(Tweet.getFormattedTimestamp(tweet));
            tvRetweetCount.setText(Integer.toString(tweet.retweetCount));
            tvFavoriteCount.setText(Integer.toString(tweet.favoriteCount));

            // Set listener for when user "likes" a tweet
            ivHeart.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Tweet tweet = ViewHolder.this.tweet;
                    if (tweet != null) {
                        if (tweet.favorited) {
                            client.unfavoriteTweet(tweet.id, new JsonHttpResponseHandler() {
                                @Override
                                public void onSuccess(int statusCode, Headers headers, JSON json) {
                                    Log.i(TAG, "Successfully unfavorited the tweet");
                                    ViewHolder.this.tweet.favorited = false;
                                    ivHeart.setImageResource(R.drawable.ic_heart_outline);
                                    // Decrement favorite counter
                                    String currentFavoriteCount = tvFavoriteCount.getText().toString();
                                    Integer newCount = Integer.parseInt(currentFavoriteCount) - 1;
                                    tvFavoriteCount.setText(newCount.toString());
                                }

                                @Override
                                public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                                    Log.e(TAG, "Failed to unfavorite the tweet", throwable);
                                }
                           });
                        }
                        else {
                            client.favoriteTweet(tweet.id, new JsonHttpResponseHandler() {
                                @Override
                                public void onSuccess(int statusCode, Headers headers, JSON json) {
                                    Log.i(TAG, "Successfully favorited the tweet");
                                    ViewHolder.this.tweet.favorited = true;
                                    ivHeart.setImageResource(R.drawable.ic_heart_outline_red);
                                    // Increment favorite counter
                                    String currentFavoriteCount = tvFavoriteCount.getText().toString();
                                    Integer newCount = Integer.parseInt(currentFavoriteCount) + 1;
                                    tvFavoriteCount.setText(newCount.toString());
                                }

                                @Override
                              public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                                   Log.e(TAG, "Failed to favorite the tweet", throwable);
                               }
                            });
                       }
                    }
                }
            });

            // Set listener for when user "retweets" a tweet
            ivRetweet.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Tweet tweet = ViewHolder.this.tweet;
                    if (tweet != null) {
                        if (tweet.retweeted) {
                            client.unretweet(tweet.id, new JsonHttpResponseHandler() {
                                @Override
                                public void onSuccess(int statusCode, Headers headers, JSON json) {
                                    Log.i(TAG, "Successfully unretweeted the tweet");
                                    ViewHolder.this.tweet.retweeted = false;
                                    ivRetweet.setImageResource(R.drawable.ic_repeat);
                                    // Decrement retweeted counter
                                    String currentRetweetCount = tvRetweetCount.getText().toString();
                                    Integer newCount = Integer.parseInt(currentRetweetCount) - 1;
                                    tvRetweetCount.setText(newCount.toString());
                                }

                                @Override
                                public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                                    Log.e(TAG, "Failed to unfavorite the tweet", throwable);
                                }
                            });
                        }
                        else {
                            client.retweet(tweet.id, "", new JsonHttpResponseHandler() {
                                @Override
                                public void onSuccess(int statusCode, Headers headers, JSON json) {
                                    Log.i(TAG, "Successfully retweeted the tweet");
                                    ViewHolder.this.tweet.retweeted = true;
                                    ivRetweet.setImageResource(R.drawable.ic_repeat_green);
                                    // Increment retweeted counter
                                    String currentRetweetCount = tvRetweetCount.getText().toString();
                                    Integer newCount = Integer.parseInt(currentRetweetCount) + 1;
                                    tvRetweetCount.setText(newCount.toString());
                                }

                                @Override
                                public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                                    Log.e(TAG, "Failed to favorite the tweet", throwable);
                                }
                            });
                        }
                    }
                }
            });

            // Change color of icon if user has favorited this tweet
            if (tweet.favorited) {
                ivHeart.setImageResource(R.drawable.ic_heart_outline_red);
            }

            // Change color of icon if user have retweeted this tweet
            if (tweet.retweeted) {
                ivRetweet.setImageResource(R.drawable.ic_repeat_green);
            }

            Glide.with(context)
                    .load(tweet.user.profileImageUrl)
                    .apply(RequestOptions.circleCropTransform())
                    .into(ivProfileImage);

            if (tweet.imageUrl != null) {
                Glide.with(context)
                        .load(tweet.imageUrl)
                        .transform(new RoundedCornersTransformation(60, 10))
                        .into(ivThumbnail);
                ivThumbnail.setVisibility(View.VISIBLE);
            } else {
                ivThumbnail.setVisibility(View.GONE);
            }
        }

        private class FetchOGImage extends AsyncTask<Tweet, Void, String> {

            private Tweet tweet;

            @Override
            protected String doInBackground(Tweet... tweets) {
                tweet = tweets[0];
                String url = "";

                try {
                    // Connect to website
                    Document doc = Jsoup.connect(tweet.expandedUrl).get();

                    // Get url
                    url = doc.select("meta[property=og:image]").first().attr("content");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return url;
            }

            @Override
            protected void onPostExecute(String url) {
                tweet.imageUrl = url;
                notifyItemChanged(getAdapterPosition());
            }
        }
    }

}
