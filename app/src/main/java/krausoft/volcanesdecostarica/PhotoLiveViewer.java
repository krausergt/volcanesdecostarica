package krausoft.volcanesdecostarica;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.NavUtils;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import java.util.Timer;
import java.util.TimerTask;


public class PhotoLiveViewer extends ActionBarActivity implements SwipeRefreshLayout.OnRefreshListener {
    private ImageView imageView;
    Timer timer;
    TimerTask timerTask;
    int option;
    String url;
    //we are going to use a handler to be able to run in our TimerTask
    final Handler handler = new Handler();
    private SwipeRefreshLayout mSwipeRefreshLayout;
    String message;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View mDecorView = getWindow().getDecorView();
        mDecorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        setContentView(R.layout.activity_photo_live_viewer);
        Toolbar toolbar = (Toolbar) findViewById(R.id.tool_bar); // Attaching the layout to the toolbar object

        if (!toolbar.isInEditMode())
            setSupportActionBar(toolbar);

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // Get the message from the intent
        Intent intent = getIntent();

        option = intent.getIntExtra(NavigationDrawerFragment.OPTION_SELECTED, 0);
        // UNIVERSAL IMAGE LOADER SETUP
        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                .showImageForEmptyUri(R.drawable.ic_empty) // resource or drawable
                .showImageOnFail(R.drawable.ic_error) // resource or drawable
                .cacheOnDisk(true).cacheInMemory(true)
                .imageScaleType(ImageScaleType.EXACTLY)
                .displayer(new FadeInBitmapDisplayer(300)).build();

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
                getApplicationContext())
                .defaultDisplayImageOptions(defaultOptions)
                .diskCacheSize(100 * 1024 * 1024).build();

        ImageLoader.getInstance().init(config);
        // END - UNIVERSAL IMAGE LOADER SETUP
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeImage);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        imageView = (ImageView) findViewById(R.id.imageView);
        TextView textView = (TextView) findViewById(R.id.volcan_info);
        url = getString(R.string.url_main);
        switch (option) {
            case 0:
                url = url + getString(R.string.turrialba_feed);
                textView.setText(getString(R.string.turrialba_info));
                break;
            case 1:
                url = url + getString(R.string.poas_feed);
                textView.setText(getString(R.string.poas_info));
                break;
            case 2:
                url = url + getString(R.string.irazu_feed);
                textView.setText(getString(R.string.irazu_info));
                break;
            default:
                url = url + getString(R.string.turrialba_feed);
                textView.setText(getString(R.string.turrialba_info));
                break;
        }

        TextView textViewSource = (TextView) findViewById(R.id.volcan_source);
        textViewSource.setText(Html.fromHtml(getString(R.string.source)));
        textViewSource.setClickable(true);
        textViewSource.setMovementMethod(LinkMovementMethod.getInstance());
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_photo_live_viewer, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.home) {
            NavUtils.navigateUpFromSameTask(this);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();

        //onResume we start our timer so it can start when the app comes from the background
        startTimer();
    }

    public void startTimer() {
        //set a new Timer
        timer = new Timer();

        //initialize the TimerTask's job
        initializeTimerTask();

        //schedule the timer, after the first 5000ms the TimerTask will run every 10000ms
        timer.schedule(timerTask, 0, 10000); //
    }

    @Override
    protected void onStop() {
        super.onStop();  // Always call the superclass method first
        stopTimerTask(findViewById(R.id.imageView));
    }

    public void stopTimerTask(View v) {
        //stop the timer, if it's not already null
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    public void initializeTimerTask() {

        timerTask = new TimerTask() {
            public void run() {
                //use a handler to run a toast that shows the current timestamp
                handler.post(new Runnable() {
                    public void run() {
                        if (!mSwipeRefreshLayout.isRefreshing()) {
                            getImageFromInternet();
                        }
                    }
                });
            }
        };
    }

    public void getImageFromInternet() {
        //get the current timeStamp
        //long unixTime = System.currentTimeMillis() / 1000L;
        ImageLoader imageLoader = ImageLoader.getInstance();
        imageLoader.handleSlowNetwork(true);
        String url_complete = url + getString(R.string.url_end);// + unixTime;
        imageLoader.displayImage(url_complete, imageView, new ImageLoadingListener() {
            @Override
            public void onLoadingStarted(String imageUri, View view) {
                mSwipeRefreshLayout.setRefreshing(true);
            }

            @Override
            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                message = null;
                switch (failReason.getType()) {
                    case IO_ERROR:
                        message = getString(R.string.input_output_error);
                        break;
                    case DECODING_ERROR:
                        message = getString(R.string.decode_error);
                        break;
                    case NETWORK_DENIED:
                        message = getString(R.string.download_error);
                        break;
                    case OUT_OF_MEMORY:
                        message = getString(R.string.out_of_memory_error);
                        break;
                    case UNKNOWN:
                        message = getString(R.string.unknown_error);
                        break;
                }
                Toast.makeText(view.getContext(), message, Toast.LENGTH_SHORT).show();
                if (mSwipeRefreshLayout.isRefreshing()){
                    mSwipeRefreshLayout.setRefreshing(false);
                }
            }

            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                if (mSwipeRefreshLayout.isRefreshing()){
                    mSwipeRefreshLayout.setRefreshing(false);
                }
            }
            @Override
            public void onLoadingCancelled(String imageUri, View view) {
            }
        });
    }

    @Override
    public void onRefresh() {
        stopTimerTask(findViewById(R.id.imageView));
        startTimer();
    }
}
