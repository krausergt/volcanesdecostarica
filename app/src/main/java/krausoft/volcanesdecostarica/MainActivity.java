package krausoft.volcanesdecostarica;

import android.os.Bundle;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast; // Para mostrar errores o información

// Imports de Twitter Kit eliminados
// import com.twitter.sdk.android.core.Callback;
// import com.twitter.sdk.android.core.Result;
// import com.twitter.sdk.android.core.Twitter;
// import com.twitter.sdk.android.core.TwitterException;
// import com.twitter.sdk.android.core.models.Tweet;
// import com.twitter.sdk.android.tweetui.TimelineResult;
// import com.twitter.sdk.android.tweetui.TweetTimelineRecyclerViewAdapter;
// import com.twitter.sdk.android.tweetui.UserTimeline;

import java.util.List; // Para la lista de Statuses (Tweets)
import java.util.ArrayList; // Para inicializar la lista

import twitter4j.Status; // Reemplazo de com.twitter.sdk.android.core.models.Tweet
import twitter4j.Twitter; // Clase principal de Twitter4J
import twitter4j.TwitterFactory;
import twitter4j.Paging; // Para paginación del timeline
import twitter4j.TwitterException; // Excepción de Twitter4J
import twitter4j.conf.ConfigurationBuilder;

// Necesitaremos un nuevo Adapter para Twitter4J
import krausoft.volcanesdecostarica.TweetAdapter;


public class MainActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeLayout;
    private TweetAdapter adapter; // Adapter para Twitter4J
    private List<Status> tweetList = new ArrayList<>();
    private Twitter twitter; // Instancia de Twitter4J

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Twitter.initialize(this); // Ya no es necesario con Twitter4J, la configuración es diferente
        setContentView(R.layout.activity_main);
        View mDecorView = getWindow().getDecorView();
        mDecorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        toolbar = (Toolbar) findViewById(R.id.tool_bar); // Attaching the layout to the toolbar object
        if (!toolbar.isInEditMode())
            setSupportActionBar(toolbar);// Setting toolbar as the ActionBar with setSupportActionBar() call

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        NavigationDrawerFragment drawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.fragment_navigation_drawer);
        drawerFragment.setUp(R.id.fragment_navigation_drawer, (DrawerLayout) findViewById(R.id.DrawerLayout), toolbar);

        swipeLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_layout);
        recyclerView = (RecyclerView) findViewById(R.id.tweetfeed);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Configurar Twitter4J (idealmente las claves irían en un lugar seguro, no hardcodeadas)
        ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setDebugEnabled(true)
                .setOAuthConsumerKey(getString(R.string.twitter_consumer_key)) // Necesitarás añadir estas strings
                .setOAuthConsumerSecret(getString(R.string.twitter_consumer_secret))
                .setOAuthAccessToken(getString(R.string.twitter_access_token))
                .setOAuthAccessTokenSecret(getString(R.string.twitter_access_token_secret));
        TwitterFactory tf = new TwitterFactory(cb.build());
        twitter = tf.getInstance();

        adapter = new TweetAdapter(this, tweetList); // Inicializar el nuevo adapter
        recyclerView.setAdapter(adapter); // Establecer el nuevo adapter

        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                fetchTimeline();
            }
        });

        // Carga inicial del timeline
        fetchTimeline();
    }

    private void fetchTimeline() {
        swipeLayout.setRefreshing(true);
        // La obtención de datos con Twitter4J debe hacerse en un hilo secundario
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Paging paging = new Paging(1, 20); // Página 1, 20 tweets
                    final List<Status> statuses = twitter.v1().timelines().getUserTimeline(getString(R.string.userTimelineName), paging);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            adapter.clearTweets();
                            adapter.addTweets(statuses);
                            swipeLayout.setRefreshing(false);
                        }
                    });
                } catch (TwitterException e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            swipeLayout.setRefreshing(false);
                            Toast.makeText(MainActivity.this, "Error al cargar tweets: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        }).start();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();


        return super.onOptionsItemSelected(item);
    }
}
