package edu.ggc.jory.endangeredspeciesmeatsmagic8ball;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.Locale;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private Integer[] animals = {R.drawable.eagle,R.drawable.elephant,R.drawable.gorilla,
            R.drawable.panda,R.drawable.panther,R.drawable.polar};

    private String[] names = {"Eagle", "Elephant", "Gorilla", "Panda", "Panther", "Polar Bear"};

    private ImageView pic;
    private int num;

    private float[] gravity = new float[3];
    private float[] accel = new float[3];
    private static final float ALPHA = 0.80f; // weighing factor used by the low pass filter
    private static final String TAG = "OMNI";
    private static final float VERTICAL_TOL = 0.3f;
    private SensorManager manager;
    private long lastUpdate;
    private MediaPlayer backgroundPlayer;
    private TextToSpeech tts;
    private Random rand = new Random();
    private GridView grid;
    private boolean isDown = false;
    private boolean isUp = true;
    private boolean firstRun = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        grid = (GridView) findViewById(R.id.gridView);
        final ImageView pic = (ImageView) findViewById(R.id.imgLarge);
        grid.setAdapter(new ImageAdapter(this));
        grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(getBaseContext(),"Selected Secies " + (position + 1),Toast.LENGTH_SHORT).show();
                pic.setImageResource(animals[position]);
            }
        });



        manager = (SensorManager) getSystemService(SENSOR_SERVICE);
        lastUpdate = System.currentTimeMillis();
        //popPlayer = MediaPlayer.create(this, R.raw.pop);
        backgroundPlayer = MediaPlayer.create(this, R.raw.empanada_music_ambient_cool_);


        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                tts.setLanguage(Locale.US);
            }
        }
        );

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

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {

            Toast.makeText(getBaseContext(),"Flip the device over to randomly select an animal or tap a picutre to select" +
                    "\n Created by Robert Jory Alexander for Itec 4550 on 3/23.2017" +
                    "\n ",Toast.LENGTH_SHORT).show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    protected void onResume() {
        super.onResume();
        manager.registerListener(this, manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_UI);
        backgroundPlayer.start();

    }

    @Override
    protected void onPause() {
        super.onPause();
        manager.unregisterListener(this);
        backgroundPlayer.pause();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        ImageView pic2 = (ImageView) findViewById(R.id.imgLarge) ;
        gravity[0] = lowPass(event.values[0], gravity[0]);
        gravity[1] = lowPass(event.values[1], gravity[1]);
        gravity[2] = lowPass(event.values[2], gravity[2]);
        accel[0] = highPass(event.values[0], accel[0]);
        accel[1] = highPass(event.values[1], accel[1]);
        accel[2] = highPass(event.values[2], accel[2]);

        Log.i(TAG, "Range for down" + inRange(gravity[2], -9.81f, VERTICAL_TOL));
        Log.i(TAG, "Range for up" + inRange(gravity[2], 9.81f, VERTICAL_TOL));
        Log.i(TAG, "is down" + isDown);
        Log.i(TAG, "is up" + isUp);

        if (event.timestamp - lastUpdate > 100) {
            if (inRange(gravity[2], -9.81f, VERTICAL_TOL)) {
                Log.i(TAG, "Down");
                if (!isDown) {
                    backgroundPlayer.setVolume(0.1f, 0.1f);



                    Log.i(TAG, "number generated is " + num);
                    grid.setSelection(num);
                    tts.speak("Selecting animal", TextToSpeech.QUEUE_FLUSH, null);
                    backgroundPlayer.setVolume(1.0f, 1.0f);
                    pic2.setImageResource(animals[num]);
                    isDown = true;
                    isUp = false;
                }

            } else if (inRange(gravity[2], 9.81f, VERTICAL_TOL)) {
                if (!isUp) {
                    backgroundPlayer.setVolume(0.1f, 0.1f);
                    Log.i(TAG, "Up");
                    Log.i(TAG, "Up");

                        tts.speak(names[num] + "What a surprise", TextToSpeech.QUEUE_FLUSH, null);

                    backgroundPlayer.setVolume(1.0f, 1.0f);
                    isUp = true;
                    isDown = false;
                }

            } else {
                Log.i(TAG, "In between");
            }
            lastUpdate = event.timestamp;
        }


    }

    private boolean inRange(float value, float target,  float tol) {
        return value >= target-tol && value <= target+tol;
    }

    // Deemphasize transient forces
    private float lowPass(float current, float gravity) {
        return current * (1-ALPHA) + gravity * ALPHA;
    }

    // Deemphasize constant forces
    private float highPass(float current, float gravity) {
        return current - gravity;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public class ImageAdapter extends BaseAdapter {
        private Context context;

        public ImageAdapter(MainActivity mainActivity) {
            context = mainActivity;
        }
        @Override
        public int getCount() {
            return animals.length;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            pic = new ImageView(context);
            pic.setImageResource(animals[position]);
            pic.setScaleType(ImageView.ScaleType.FIT_XY);
            pic.setLayoutParams(new GridView.LayoutParams(330,300));
            return pic;
        }
    }
}
