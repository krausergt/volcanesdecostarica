package krausoft.volcanesdecostarica;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import twitter4j.Status;
import twitter4j.User;

public class TweetAdapter extends RecyclerView.Adapter<TweetAdapter.TweetViewHolder> {

    private Context context;
    private List<Status> tweetList;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yy HH:mm", Locale.getDefault());

    public TweetAdapter(Context context, List<Status> tweetList) {
        this.context = context;
        this.tweetList = tweetList;
    }

    @NonNull
    @Override
    public TweetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_tweet, parent, false);
        return new TweetViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TweetViewHolder holder, int position) {
        Status status = tweetList.get(position);
        User user = status.getUser();

        holder.userName.setText(user.getName());
        holder.screenName.setText("@" + user.getScreenName());
        holder.tweetText.setText(status.getText());
        holder.createdAt.setText(dateFormat.format(status.getCreatedAt()));

        Glide.with(context)
                .load(user.getProfileImageURLHttps())
                .apply(RequestOptions.circleCropTransform())
                .placeholder(R.drawable.ic_launcher_foreground) // Un placeholder genérico
                .error(R.drawable.ic_launcher_background) // Un error genérico
                .into(holder.profileImage);
    }

    @Override
    public int getItemCount() {
        return tweetList.size();
    }

    public void clearTweets() {
        tweetList.clear();
        notifyDataSetChanged();
    }

    public void addTweets(List<Status> newTweets) {
        tweetList.addAll(newTweets);
        notifyDataSetChanged();
    }

    static class TweetViewHolder extends RecyclerView.ViewHolder {
        ImageView profileImage;
        TextView userName;
        TextView screenName;
        TextView tweetText;
        TextView createdAt;

        public TweetViewHolder(@NonNull View itemView) {
            super(itemView);
            profileImage = itemView.findViewById(R.id.tweet_profile_image);
            userName = itemView.findViewById(R.id.tweet_user_name);
            screenName = itemView.findViewById(R.id.tweet_screen_name);
            tweetText = itemView.findViewById(R.id.tweet_text);
            createdAt = itemView.findViewById(R.id.tweet_created_at);
        }
    }
}
