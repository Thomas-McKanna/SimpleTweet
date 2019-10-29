package com.codepath.apps.restclienttemplate.timeline;

import android.content.Context;
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
import com.codepath.apps.restclienttemplate.models.Tweet;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import jp.wasabeef.glide.transformations.RoundedCornersTransformation;

public class TweetsAdapter extends RecyclerView.Adapter<TweetsAdapter.ViewHolder>{

    private final String TAG = TweetsAdapter.class.getSimpleName();

    Context context;
    List<Tweet> tweets;

    // Pass in the context and list of tweets
    public TweetsAdapter(Context context, List<Tweet> tweets) {
        this.context = context;
        this.tweets = tweets;
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
        TextView tvBody;
        TextView tvUsername;
        TextView tvHandle;
        TextView tvAge;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            Typeface regular = ResourcesCompat.getFont(context, R.font.helvetica_neue_regular);
            Typeface bold = ResourcesCompat.getFont(context, R.font.helvetica_neue_bold);

            ivProfileImage = itemView.findViewById(R.id.ivUserPicture);

            ivThumbnail =itemView.findViewById(R.id.ivThumbnail);

            tvBody = itemView.findViewById(R.id.tvBody);
            tvBody.setTypeface(regular);

            tvUsername = itemView.findViewById(R.id.tvUsername);
            tvUsername.setTypeface(bold);

            tvHandle = itemView.findViewById(R.id.tvHandle);
            tvHandle.setTypeface(regular);

            tvAge = itemView.findViewById(R.id.tvAge);
            tvAge.setTypeface(regular);
        }

        public void bind(Tweet tweet) {

            if (tweet.shortenedUrl != null && tweet.imageUrl == null) {
                // Try to fetch an image for this tweet
                new FetchOGImage().execute(tweet);
            }

            tvBody.setText(tweet.body);
            tvUsername.setText(tweet.user.name);
            tvHandle.setText("@" + tweet.user.screenName);
            tvAge.setText(Tweet.getFormattedTimestamp(tweet));

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
