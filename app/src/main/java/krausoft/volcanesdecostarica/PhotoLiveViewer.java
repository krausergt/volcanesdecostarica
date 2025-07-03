package krausoft.volcanesdecostarica;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.core.app.NavUtils;
import androidx.core.content.FileProvider;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

// Imports de Universal Image Loader eliminados

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestOptions;
import androidx.annotation.Nullable;
import android.graphics.drawable.Drawable;


import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import krausoft.volcanesdecostarica.Tools.HashCodeFileNameWithDummyExtGenerator;


public class PhotoLiveViewer extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {
    private static final String AUTHORITY = "krausoft.volcanesdecostarica.fileprovider";
    //we are going to use a handler to be able to run in our TimerTask
    final Handler handler = new Handler();
    Timer timer;
    TimerTask timerTask;
    int option;
    String url;
    String url_success;
    String message;
    private ImageView imageView;
    private SwipeRefreshLayout mSwipeRefreshLayout;

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

        if (getSupportActionBar() != null) {
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        // Get the message from the intent
        Intent intent = getIntent();
        option = intent.getIntExtra(NavigationDrawerFragment.OPTION_SELECTED, 0);
        // Universal Image Loader ya no se inicializa aquí. Glide se inicializa automáticamente.

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
                url = url + getString(R.string.irazu_feed);
                textView.setText(getString(R.string.irazu_info));
                break;
            case 2:
                url = url + getString(R.string.poas_feed);
                textView.setText(getString(R.string.poas_info));
                break;
            case 3:
                url = url + getString(R.string.poas_feed2);
                textView.setText(getString(R.string.craterpoas_info));
                break;
            case 4:
                url = url + getString(R.string.rvieja_feed);
                textView.setText(getString(R.string.rvieja_info));
                break;
            case 5:
                url = url + getString(R.string.rvieja2_feed);
                textView.setText(getString(R.string.rvieja2_info));
                break;
            default:
                url = url + getString(R.string.poas_feed2);
                textView.setText(getString(R.string.craterpoas_info));
                break;

        }

        TextView textViewSource = (TextView) findViewById(R.id.volcan_source);
        textViewSource.setText(Html.fromHtml(getString(R.string.source)));
        textViewSource.setClickable(true);
        textViewSource.setMovementMethod(LinkMovementMethod.getInstance());
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        FloatingActionButton fabShare = (FloatingActionButton) findViewById(R.id.fabShare);
        fabShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (url_success != null) {
                    sharePicture();
                } else {
                    Toast.makeText(mDecorView.getContext(), message, Toast.LENGTH_SHORT).show();
                }
            }
        });

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
        long period;
        switch (option) {
            case 0:
                period = Long.parseLong(getString(R.string.Timer10seg));
                break;
            case 1:
                period = Long.parseLong(getString(R.string.Timer60seg));
                break;
            case 2:
                period = Long.parseLong(getString(R.string.Timer60seg));
                break;
            case 3:
                period = Long.parseLong(getString(R.string.Timer60seg));
                break;
            case 4:
                period = Long.parseLong(getString(R.string.Timer60seg));
                break;
            case 5:
                period = Long.parseLong(getString(R.string.Timer5min));
                break;
            default:
                period = Long.parseLong(getString(R.string.Timer10seg));
                break;
        }
        //initialize the TimerTask's job
        initializeTimerTask();
        //schedule the timer, after the first 5000ms the TimerTask will run every 10000ms
        timer.schedule(timerTask, 0, period); //
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
                        //ImageLoader imageLoader = ImageLoader.getInstance();
                        //imageLoader.cancelDisplayTask(imageView);
                        getImageFromInternet();
                    }
                });
            }
        };
    }

    public void getImageFromInternet() {
        //get the current timeStamp
        long unixTime = System.currentTimeMillis() / 1000L;
        String url_complete = url + getString(R.string.url_end) + unixTime;

        RequestOptions requestOptions = new RequestOptions()
                .diskCacheStrategy(DiskCacheStrategy.NONE) // No cache para obtener siempre la más reciente
                .skipMemoryCache(true)
                .placeholder(R.drawable.ic_empty)
                .error(R.drawable.ic_error);

        Glide.with(this)
                .load(url_complete)
                .apply(requestOptions)
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        mSwipeRefreshLayout.setRefreshing(false);
                        message = getString(R.string.download_error); // Mensaje genérico para Glide
                        if (e != null) {
                            e.printStackTrace();
                            message = e.getMessage();
                        }
                        Toast.makeText(PhotoLiveViewer.this, message, Toast.LENGTH_SHORT).show();
                        // Intentar cargar la última imagen exitosa si existe
                        if (url_success != null && !url_success.isEmpty()){
                            Glide.with(PhotoLiveViewer.this).load(url_success).into(imageView);
                        }
                        return false; // Retornar false para que Glide maneje el error placeholder
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        mSwipeRefreshLayout.setRefreshing(false);
                        url_success = url_complete; // Guardar la URL de la imagen cargada exitosamente
                        return false; // Retornar false para que Glide muestre la imagen
                    }
                })
                .into(imageView);
    }

    private void sharePicture() {
        if (url_success == null || url_success.isEmpty()) {
            Toast.makeText(this, getString(R.string.no_image_to_share), Toast.LENGTH_SHORT).show();
            return;
        }

        // Glide guarda en caché las imágenes. Para compartir, necesitamos obtener el archivo de la caché.
        // Esto es más complejo con Glide que con UIL, ya que Glide maneja su caché internamente.
        // Una forma es descargar la imagen de nuevo a un archivo temporal o usar la caché de Glide.

        // Opción simplificada: Asumir que la imagen está en el ImageView y compartir el Bitmap del ImageView.
        // Esto no es ideal para imágenes muy grandes y no usa el archivo original.
        // Para una implementación robusta, se necesitaría descargar la imagen a un archivo específico
        // o acceder al archivo en la caché de Glide de forma asíncrona.

        // Por ahora, vamos a intentar obtener el archivo de la caché de Glide.
        // Nota: Esto requiere que la imagen ya esté en caché y puede ser asíncrono.
        // Para simplificar este ejemplo, lo haremos de forma síncrona (puede bloquear UI).
        // En una app real, esto debería ser asíncrono.

        Glide.with(this)
                .asFile()
                .load(url_success)
                .listener(new RequestListener<File>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<File> target, boolean isFirstResource) {
                        Toast.makeText(PhotoLiveViewer.this, getString(R.string.error_sharing_image), Toast.LENGTH_SHORT).show();
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(File resource, Object model, Target<File> target, DataSource dataSource, boolean isFirstResource) {
                        shareFile(resource);
                        return true;
                    }
                }).submit(); // submit() para operaciones en segundo plano, pero aquí necesitamos el archivo ahora.
                            // Usar .downloadOnly() o .into(FileTarget) sería mejor en un escenario real.
                            // Para este reemplazo, usaremos un listener y submit(), y llamaremos a shareFile desde onResourceReady.
    }

    private void shareFile(File file) {
        try {
            Intent intent = getIntent(); // Esto podría ser null si la actividad se recrea, mejor obtenerlo de nuevo o pasarlo.
            DateFormat df = new SimpleDateFormat(getString(R.string.dateFormat), Locale.getDefault());
            String date = df.format(Calendar.getInstance().getTime());
            String mensaje = date;
            switch (option) {
                case 0:
                    mensaje = getString(R.string.Mensaje1) +
                            " [" + date + "]";
                    break;
                case 1:
                    mensaje = getString(R.string.Mensaje2) +
                            " [" + date + "]";
                    break;
                case 2:
                    mensaje = getString(R.string.Mensaje3) +
                            " [" + date + "]";
                    break;
                case 3:
                    mensaje = getString(R.string.Mensaje4) +
                            " [" + date + "]";
                    break;
                case 4:
                    mensaje = getString(R.string.Mensaje5) +
                            " [" + date + "]";
                    break;
                case 5:
                    mensaje = getString(R.string.Mensaje6) +
                            " [" + date + "]";
                    break;
            }
            option = intent.getIntExtra(NavigationDrawerFragment.OPTION_SELECTED, 0);
            Uri uriToImage = FileProvider.getUriForFile(getBaseContext(), AUTHORITY, file);
            Intent share = new Intent(Intent.ACTION_SEND);
            share.setType("image/*");
            share.putExtra(Intent.EXTRA_STREAM, uriToImage);
            share.putExtra(Intent.EXTRA_TEXT, mensaje);
            startActivity(Intent.createChooser(share, getString(R.string.compartir)));
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
        /*final BasicImageDownloader downloader = new BasicImageDownloader(new OnImageLoaderListener() {
            @Override
            public void onError(ImageError error) {
                error.printStackTrace();
            }

            @Override
            public void onProgressChange(int percent) {
            }

            @Override
            public void onComplete(Bitmap result) {
                        *//* save the image - I'm gonna use JPEG *//*
                final Bitmap.CompressFormat mFormat = Bitmap.CompressFormat.JPEG;
                        *//* don't forget to include the extension into the file name *//*
                long unixTime = System.currentTimeMillis() / 1000L;
                String fileName = unixTime + getString(R.string.fileNameExt);
                final File myImageFile = new File(getString(R.string.cache_path) + fileName);
                if (myImageFile.exists()) {
                    myImageFile.delete();
                }
                BasicImageDownloader.writeToDisk(myImageFile, result, new BasicImageDownloader.OnBitmapSaveListener() {
                    @Override
                    public void onBitmapSaved() {
                        Log.e("onBitmapSaved", "Image saved as: " + myImageFile.getAbsolutePath());
                    }

                    @Override
                    public void onBitmapSaveError(ImageError error) {
                        Log.e("onBitmapSaveError", "Error code " + error.getErrorCode() + ": " +
                                error.getMessage());
                        error.printStackTrace();
                    }
                }, mFormat, false);

                try {

                    Intent intent = getIntent();
                    DateFormat df = new SimpleDateFormat(getString(R.string.dateFormat), Locale.getDefault());
                    String date = df.format(Calendar.getInstance().getTime());
                    String mensaje = date;
                    switch (option) {
                        case 0:
                            mensaje = getString(R.string.Mensaje1) +
                                    " [" + date + "]";
                            break;
                        case 1:
                            mensaje = getString(R.string.Mensaje2) +
                                    " [" + date + "]";
                            break;
                        case 2:
                            mensaje = getString(R.string.Mensaje3) +
                                    " [" + date + "]";
                            break;
                        case 3:
                            mensaje = getString(R.string.Mensaje4) +
                                    " [" + date + "]";
                            break;
                    }
                    option = intent.getIntExtra(NavigationDrawerFragment.OPTION_SELECTED, 0);
                    Uri uriToImage = FileProvider.getUriForFile(getBaseContext(), AUTHORITY, myImageFile);
                    Intent share = new Intent(Intent.ACTION_SEND);
                    share.setType("image/*");
                    share.putExtra(Intent.EXTRA_STREAM, uriToImage);
                    share.putExtra(Intent.EXTRA_TEXT, mensaje);
                    startActivity(Intent.createChooser(share, getString(R.string.compartir)));
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                }
            }
        });
        downloader.download(url_success, false);*/
    }

    @Override
    public void onRefresh() {
        stopTimerTask(findViewById(R.id.imageView));
        startTimer();
    }

}


