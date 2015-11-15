package com.example.alex.weatherwebservice;

import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.squareup.picasso.Picasso;

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
import java.util.HashMap;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

  public static final String WEATHER_URL_REQEST = "http://api.openweathermap.org/data/2.5/weather?";
  public static final String APPID = "12a649a36571d5278d59d903ede2520b";
  private GoogleMap mMap;
  private TextView location;
  private TextView temperature;
  private TextView weatherInfo;
  private ImageView stateIcon;
  private boolean isLocationNew = true;
  private double latitude;
  private double longitude;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.map_activity);

    weatherInfo = (TextView) findViewById(R.id.weatherInfo);
    location = (TextView) findViewById(R.id.location);
    temperature = (TextView) findViewById(R.id.temperature);
    stateIcon = (ImageView) findViewById(R.id.stateIcon);

    SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
            .findFragmentById(R.id.map);

    mapFragment.getMapAsync(this);
  }

  @Override
  public void onMapReady(GoogleMap googleMap) {
    mMap = googleMap;

    mMap.setMyLocationEnabled(true);
    mMap.getUiSettings().setZoomControlsEnabled(true);
    mMap.getUiSettings().setMyLocationButtonEnabled(true);

    if (mMap != null) {
      mMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {

        @Override
        public void onMyLocationChange(Location arg0) {
          // TODO Auto-generated method stub
          mMap.addMarker(new MarkerOptions()
                  .position(new LatLng(arg0.getLatitude(), arg0.getLongitude()))
                  .title("You are here!"));

          if (isLocationNew) {
            isLocationNew = false;
            latitude = arg0.getLatitude();
            longitude = arg0.getLongitude();
            new BackGroundLoader().execute(WEATHER_URL_REQEST + "lat=" + latitude +
                    "&" + "lon=" + longitude + "&APPID=" + APPID);
          }
        }
      });
    }

  }

  public class BackGroundLoader extends AsyncTask<String, Void, HashMap<String, String>> {

    @Override
    protected HashMap<String, String> doInBackground(String... params) {
      HttpURLConnection urlConnection = null;
      BufferedReader reader = null;
      String strJson = "";
      HashMap<String, String> map = new HashMap<>();

      try {
        URL url = new URL(params[0]);
        urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setReadTimeout(10000);
        urlConnection.setConnectTimeout(15000);
        urlConnection.setRequestMethod("GET");
        urlConnection.connect();

        InputStream inputStream = urlConnection.getInputStream();
        StringBuffer buffer = new StringBuffer();

        reader = new BufferedReader(new InputStreamReader(inputStream));

        String line;
        while ((line = reader.readLine()) != null) {
          buffer.append(line);
        }

        strJson = buffer.toString();
        try {

          JSONObject obj = new JSONObject(strJson);

          JSONObject coordObj = obj.getJSONObject("coord");
          JSONObject sysObj = obj.getJSONObject("sys");
          JSONObject mainObj = obj.getJSONObject("main");
          JSONObject windObj = obj.getJSONObject("wind");
          JSONArray weatherArray = obj.getJSONArray("weather");
          JSONObject firstWeatherObj = weatherArray.getJSONObject(0);

          double temperature = (int) (mainObj.getDouble("temp") - 273.15);
          double min_t = (int) (mainObj.getDouble("temp_min") - 273.15);
          double max_t = (int) (mainObj.getDouble("temp_max") - 273.15);

          String allWeatherInfo = firstWeatherObj.getString("main")
                  + " (" + firstWeatherObj.getString("description") + ")"
                  + " Wind: " + windObj.getDouble("speed") + "mPs, Wind deg: "
                  + windObj.getDouble("deg") + "°\nPressure: " + mainObj.getDouble("pressure")
                  + "hPa, Humidity: " + mainObj.getDouble("humidity") + "%";

          map.put("temperature", "Temperature: " + temperature + "°C. Min temp: " + min_t
                  + "°C, Max temp: " + max_t + "°C");
          map.put("location", "Country: " + obj.getString("name") + ", "
                  + sysObj.getString("country") + " (" + coordObj.getDouble("lon") + ", "
                  + coordObj.getDouble("lat") + ")");
          map.put("icon", firstWeatherObj.getString("icon"));
          map.put("weather", allWeatherInfo);

        } catch (JSONException e) {
          e.printStackTrace();
        }
      } catch (MalformedURLException e) {
        e.printStackTrace();
      } catch (IOException e) {
        e.printStackTrace();
      }

      return map;
    }

    @Override
    protected void onPostExecute(HashMap<String, String> result) {
      if (result.isEmpty()) {
        location.setText("Oops.. Something wrong!\nCheck Internet connection...");
      } else {
        location.setText(result.get("location"));
        temperature.setText(result.get("temperature"));
        weatherInfo.setText(result.get("weather"));

        Picasso.with(MapsActivity.this)
                .load("http://api.openweathermap.org/img/w/" + result.get("icon"))
                .into(stateIcon);
      }
    }
  }
}

