package cotym.example.lenovo.zomtoapplicationusinggooglemap;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    RecyclerView recyclerView;
    ArrayList<Resturant> arrayList;
    MyAdapter myAdapter;
    MyTask myTask;
    LinearLayoutManager linearLayoutManager;
    LocationManager locationManager;
    LocationListener locationListener;


    public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.row, parent, false);
            ViewHolder viewHolder = new ViewHolder(view);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            Resturant resturant = arrayList.get(position);
            holder.textView1.setText(resturant.getName());
            holder.textView2.setText(resturant.getLocality());
            holder.textView3.setText(resturant.getAddress());
            Glide.with(MainActivity.this).load(resturant.getImageurl()).
                    placeholder(R.mipmap.ic_launcher).crossFade().into(holder.imageView);
        }

        @Override
        public int getItemCount() {
            return arrayList.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public TextView textView1, textView2, textView3;
            public ImageView imageView;
            public LinearLayout linearLayout;

            public ViewHolder(View itemView) {
                super(itemView);
                textView1 = (TextView) itemView.findViewById(R.id.textView1);
                textView2 = (TextView) itemView.findViewById(R.id.textView2);
                textView3 = (TextView) itemView.findViewById(R.id.textView3);
                imageView = (ImageView) itemView.findViewById(R.id.imageView1);
                linearLayout= (LinearLayout) itemView.findViewById(R.id.linearLayout1);
                linearLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        int pos=getAdapterPosition();
                        Resturant resturant=arrayList.get(pos);
                        Intent i = new Intent(MainActivity.this, MapsActivity.class);
                        i.putExtra("name",resturant.getName());
                        i.putExtra("latitude",resturant.getLatitude());
                        i.putExtra("longitude",resturant.getLongitude());
                        i.putExtra("address",resturant.getAddress());
                        startActivity(i);
                    }
                });

            }
        }
    }

    public class MyTask extends AsyncTask<String, Void, String> {
        URL url;
        HttpURLConnection httpURLConnection;
        InputStream inputStream;
        InputStreamReader inputStreamReader;
        BufferedReader bufferedReader;
        String line;
        StringBuilder stringBuilder;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Toast.makeText(MainActivity.this, "About To Connect", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected String doInBackground(String... strings) {
            try {
                url = new URL(strings[0]);
                httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.addRequestProperty("user-key", "c7fe02c9ad8dd9e847300b03d44da9e0");
                httpURLConnection.addRequestProperty("Accept", "application/json");
                inputStream = httpURLConnection.getInputStream();
                inputStreamReader = new InputStreamReader(inputStream);
                bufferedReader = new BufferedReader(inputStreamReader);
                line = bufferedReader.readLine();
                stringBuilder = new StringBuilder();
                while (line != null) {
                    stringBuilder.append(line);
                    line = bufferedReader.readLine();
                }
                return stringBuilder.toString();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            try {
                Log.d("CHANGES MADE","");
                JSONObject jsonObject = new JSONObject(s);
                JSONArray rs = jsonObject.getJSONArray("nearby_restaurants");
                for (int i = 0; i < rs.length(); i++) {
                    JSONObject x = rs.getJSONObject(i);
                    JSONObject res = x.getJSONObject("restaurant");
                    String name = res.getString("name");
                    JSONObject location = res.getJSONObject("location");
                    String address = location.getString("address");
                    String locality = location.getString("locality");
                    String latitude = location.getString("latitude");
                    String longitude = location.getString("longitude");
                    String imageurl = res.getString("thumb");
                    //now we have to pass these restaurant details to arraylist
                    //create a restaurant bean class object with above data
                    Resturant restaurant = new Resturant(name, locality, address, imageurl,
                            latitude, longitude);
                    //pass restaurant object to arraylist
                    arrayList.add(restaurant);
                }
                myAdapter.notifyDataSetChanged();

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerview1);
        arrayList = new ArrayList<>();
        myAdapter = new MyAdapter();
        myTask = new MyTask();
        recyclerView.setAdapter(myAdapter);
        linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
               // Toast.makeText(MainActivity.this, "lat"+latitude, Toast.LENGTH_SHORT).show();
                //Toast.makeText(MainActivity.this, "lon"+longitude, Toast.LENGTH_SHORT).show();
                if (myTask.getStatus() == AsyncTask.Status.PENDING) {
                    myTask.execute("https://developers.zomato.com/api/v2.1/geocode?lat=" + latitude + "&lon=" + longitude);
                    Toast.makeText(MainActivity.this, "lat"+latitude, Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {

            }
        };
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
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);

    }
}
