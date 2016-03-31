package michaelbishoff.assignment2;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.Calendar;
import java.util.Date;


public class MainActivity extends AppCompatActivity implements LocationListener, View.OnClickListener {

    private LocationManager locationManager;
//    private static final String

    private EditText lat, lng, time;
    private Button getLocation;
    private SQLiteDatabase dbWrite, dbRead;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        lat = (EditText) findViewById(R.id.lat);
        lng = (EditText) findViewById(R.id.lng);
        time = (EditText) findViewById(R.id.timestamp);
        getLocation = (Button) findViewById(R.id.button);


        MySqlHelper mySqlHelper = new MySqlHelper(this);
        dbWrite = mySqlHelper.getWritableDatabase();
        dbRead = mySqlHelper.getReadableDatabase();

    }

    @Override
    protected void onResume() {
        super.onResume();

        getLocation.setOnClickListener(this);

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        // Need to check if we have permission to get location information (auto completed from
        // locationManager_.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10, 0, this); vv
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        // GPS or Network provider (indoors) for triangulation, minTime in miliseconds between
        // updates 0 is fast as possible, minDistance how far the user has to move before we read
        // another value 0 because we don't move a lot
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 10, 0, this);
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.removeUpdates(this);
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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onLocationChanged(Location location) {

        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put("timestamp", Calendar.getInstance().getTimeInMillis());
        values.put("latitude", location.getLatitude());
        values.put("longitude", location.getLongitude());

        // Insert the new row, returning the primary key value of the new row
        long id = dbWrite.insert("location", "NULL", values);
        Log.d("ME", "Inserted at ID: " + id);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button:
                // Query DB

//                dbRead.execSQL("SELECT latitude, longitude FROM location ORDER BY timestamp DES LIMIT 1");
                Cursor cursor = dbRead.query("location", new String[]{"timestamp","latitude", "longitude"}, null, null, null, null, "timestamp DESC", "1");
                cursor.moveToFirst();

                if (cursor.getPosition() == -1) {
                    Toast.makeText(MainActivity.this, "Nothing in database yet, try again", Toast.LENGTH_LONG);
                    return;
                }

                do {
                    time.setText(cursor.getString(cursor.getColumnIndex("timestamp")));
                    lat.setText(Float.toString(cursor.getFloat(cursor.getColumnIndexOrThrow("latitude"))));
                    lng.setText(Float.toString(cursor.getFloat(cursor.getColumnIndexOrThrow("longitude"))));

                } while (cursor.moveToNext());

                break;
        }

    }

    public class MySqlHelper extends SQLiteOpenHelper {

        public static final int DATABASE_VERSION = 1;
        public static final String DATABASE_NAME = "Location.db";
        public static final String TABLE_NAME = "location";

        public MySqlHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        public void onCreate(SQLiteDatabase db) {
//            db.execSQL(SQL_CREATE_ENTRIES);
            db.execSQL("create table " + TABLE_NAME + " (timestamp TEXT primary key, latitude REAL, longitude REAL)");
        }
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // This database is only a cache for online data, so its upgrade policy is
            // to simply to discard the data and start over
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
            onCreate(db);
        }
        public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            onUpgrade(db, oldVersion, newVersion);
        }
    }
}
