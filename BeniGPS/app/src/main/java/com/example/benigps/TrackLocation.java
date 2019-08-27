package com.example.benigps;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.benigps.Model.Model;
import com.example.benigps.service.Gps_api;
import com.example.benigps.utils.URLConstants;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class TrackLocation extends AppCompatActivity implements OnMapReadyCallback, View.OnClickListener {
    GoogleMap map;
    String mobileID,User;
    String UserString;
    JSONArray getLocation;
    private static String url = URLConstants.Track;
    private String TAG = MainActivity.class.getSimpleName();
    ArrayList<LatLng> latLngs = new ArrayList<LatLng>();
    ArrayList<LatLng> timebaseLatLng = new ArrayList<LatLng>();
    private ProgressDialog pDialog;
    private ListView lv;
    ArrayList<String> listDate = new ArrayList<String>();
    PolylineOptions polylineOptions;
    String jsonDate;
    JSONObject jsonObject, jsonTimeObject,jsonUserName;
    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
    SimpleDateFormat forma1 = new SimpleDateFormat("HH:mm:ss");
    SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a");
    Date startDATE, TrackWholeDate;
    Date endDATE;
    TextView Username;
    ArrayList<String>  Date_marker=new ArrayList<String>();


    String getdate;
    JSONArray JSONArrayresult = new JSONArray();
    JSONArray JSONArrayDate = new JSONArray();

    private EditText DateToSearch;
    private DatePickerDialog fromDatePickerDialog;
    private SimpleDateFormat dateFormatter;
    RadioButton SearchDate,SearchTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track);
        Bundle extras = getIntent().getExtras();
        mobileID = extras.getString("MobileId");
        User=extras.getString("username");
        Username=(TextView)findViewById(R.id.User);

        Spinner spin = (Spinner) findViewById(R.id.StartTime);
        Spinner spin1 = (Spinner) findViewById(R.id.EndingTime);
        Button btnTrackTime = (Button) findViewById(R.id.TrackTime);

        SearchDate = (RadioButton) findViewById(R.id.DateSearch);
        SearchTime=(RadioButton)findViewById(R.id.TimeSearch);

        Username.setText("User Name:"+User.toString());


        GetTime();
        InitMap();

        dateFormatter = new SimpleDateFormat("dd-MM-yyyy", Locale.US);
        findViewsById();

        setDateTimeField();

        ArrayAdapter aa = new ArrayAdapter(this, android.R.layout.simple_spinner_item, listDate);
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spin.setAdapter(aa);

        ArrayAdapter a1 = new ArrayAdapter(this, android.R.layout.simple_spinner_item, listDate);
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spin1.setAdapter(aa);


        spin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                //   Object item = parent.getItemAtPosition(pos);
                String str = parent.getSelectedItem().toString();
                String[] split = str.split(":");
                int hour = Integer.parseInt(split[0]);
                int Mints = Integer.parseInt(split[1]);
                int Sec = Integer.parseInt(split[2]);

                try {
                    startDATE.setHours(hour);
                    startDATE.setMinutes(Mints);
                    startDATE.setSeconds(Sec);
                    //Toast.makeText(getApplicationContext(),startDATE.toString(),Toast.LENGTH_SHORT).show();
                    DateToSearch.setText(startDATE.toString());


                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        spin1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                //   Object item = parent.getItemAtPosition(pos);
                String str = parent.getSelectedItem().toString();
                String[] split = str.split(":");
                int hour = Integer.parseInt(split[0]);
                int Mints = Integer.parseInt(split[1]);
                int Sec = Integer.parseInt(split[2]);

                try {
                    // = sdf.parse(str);
                    endDATE.setHours(hour);
                    endDATE.setMinutes(Mints);
                    endDATE.setSeconds(Sec);

                    Toast.makeText(getApplicationContext(), endDATE.toString(), Toast.LENGTH_SHORT).show();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        btnTrackTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                map.clear();
                Boolean RadioButtonState1 = SearchDate.isChecked();
                Boolean RadioButtonState = SearchTime.isChecked();
                String str = DateToSearch.toString();
                boolean answer= DateToSearch.getText().toString().trim().length() == 0;
                if (!answer) {
                    if (RadioButtonState) {
                        timebaseLatLng.clear();
                        createJSONTimeTracker();

                        try {
                            Thread.sleep(3000);
                            TimeBasedTracker();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    if (RadioButtonState1) {
                        map.clear();
                        startDATE.setHours(12);
                        startDATE.setMinutes(00);
                        startDATE.setSeconds(00);
                        PostDate();

                        try {
                            Thread.sleep(1000);
                            GetAllLocationDate();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        latLngs.clear();
                    }

                }
                else
                {
                    Toast.makeText(getApplicationContext(),"Please Select Date",Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    ///////////////////////
    private void findViewsById() {
        DateToSearch = (EditText) findViewById(R.id.SearchDate);
        DateToSearch.setInputType(InputType.TYPE_NULL);
        DateToSearch.requestFocus();

    }

    private void setDateTimeField() {
        DateToSearch.setOnClickListener(this);
        //toDateEtxt.setOnClickListener(this);

        Calendar newCalendar = Calendar.getInstance();
        fromDatePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {

            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                Calendar newDate = Calendar.getInstance();
                newDate.set(year, monthOfYear, dayOfMonth);

                //fromDateEtxt.setText(dateFormatter.format(newDate.getTime()));
                String s = dateFormatter.format(newDate.getTime());
                try {
                    //Date d = dateFormatter.parse(s);
                    startDATE = dateFormatter.parse(s);
                    startDATE.setHours(12);
                    startDATE.setMinutes(00);
                    startDATE.setSeconds(00);
                    endDATE = dateFormatter.parse(s);
                    endDATE.setHours(12);
                    endDATE.setMinutes(00);
                    endDATE.setSeconds(00);

                    DateToSearch.setText(startDATE.toString());
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

        }, newCalendar.get(Calendar.YEAR), newCalendar.get(Calendar.MONTH), newCalendar.get(Calendar.DAY_OF_MONTH));

    }

    @Override
    public void onClick(View view) {
        if (view == DateToSearch) {
            fromDatePickerDialog.show();
        }
    }

    public void InitMap() {

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }

    public void GetTime()
    {
        listDate.add("01:00:00");listDate.add("02:00:00");listDate.add("03:00:00");
        listDate.add("04:00:00");listDate.add("05:00:00");listDate.add("06:00:00");
        listDate.add("07:00:00");listDate.add("08:00:00");
        listDate.add("09:00:00");listDate.add("10:00:00");
        listDate.add("11:00:00");listDate.add("12:00:00");listDate.add("13:00:00");
        listDate.add("14:00:00");
        listDate.add("15:00:00");
        listDate.add("16:00:00");
        listDate.add("17:00:00");
        listDate.add("18:00:00");
        listDate.add("19:00:00");
        listDate.add("20:00:00");
        listDate.add("21:00:00");
        listDate.add("22:00:00");
        listDate.add("23:00:00");
        listDate.add("24:00:00");
    }

    /////////////////////////
    public void TimeBasedTracker() {

        try {
            if(JSONArrayresult.length()>0) {
                for (int i = 0; i < JSONArrayresult.length(); i++) {
                    JSONObject result = JSONArrayresult.getJSONObject(i);

                    //String id = result.getString("deviceId");
                    String temp=result.getString("date");
                    // Date_marker.add(temp);
                    Double lng = result.getDouble("longitude");
                    Double lat = result.getDouble("latitude");
                    LatLng lt = new LatLng(lat, lng);
                    timebaseLatLng.add(lt);
                }
                Addmarker(timebaseLatLng, mobileID);
                Date_marker.clear();
            }
            else
            {
                Toast.makeText(getApplicationContext(),"No data between this time",Toast.LENGTH_SHORT).show();
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void createJSONTimeTracker() {
        jsonTimeObject = new JSONObject();

        try {
            jsonTimeObject.put("deviceId", mobileID.toString());
            jsonTimeObject.put("startDateTime", startDATE.toString());
            jsonTimeObject.put("endDateTime", endDATE.toString());
            new getLocationTimer().execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /* Get Particular Whole date*/

    public void PostDate() {
        jsonTimeObject = new JSONObject();

        try {
            jsonTimeObject.put("deviceId", mobileID.toString());
            jsonTimeObject.put("startDateTime", startDATE.toString());
            jsonTimeObject.put("endDateTime", startDATE.toString());
            new TrackUser().execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    public void GetAllLocationDate() {
        String shift=null,temp=null;

        try {
            if(JSONArrayDate.length()>0) {
                JSONObject res = JSONArrayDate.getJSONObject(0);
                temp=res.getString("shift");
                for (int i = 0; i < JSONArrayDate.length(); i++) {
                    JSONObject result = JSONArrayDate.getJSONObject(i);
                    shift=result.getString("shift");

                    if(shift.equals(temp)) {
                        Double lng = result.getDouble("longitude");
                        Double lat = result.getDouble("latitude");
                        //  String temp1=result.getString("date");

                        LatLng lt = new LatLng(lat, lng);
                        latLngs.add(lt);

                    } else {
                        Addmarker(latLngs, temp);
                        temp=shift;
                        latLngs.clear();
                        Double lng = result.getDouble("longitude");
                        Double lat = result.getDouble("latitude");
                        LatLng lt = new LatLng(lat, lng);
                        latLngs.add(lt);

                        //Date_marker.clear();
                    }
                }
                Addmarker(latLngs, temp);
                //Date_marker.clear();

            }else{
                Toast.makeText(getApplicationContext(),"No Data for this date",Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map=googleMap;
    }

    /*--------------------*/


    class getLocationTimer extends AsyncTask<Void, Void, JSONArray> {

        String URL = URLConstants.Track_Time_Basis;

        @Override
        protected JSONArray doInBackground(Void... voids) {

            try {
                JSONArrayresult = postJSON(URL, jsonTimeObject);
                getdate = JSONArrayresult.getString(1);

            } catch (Exception e) {
                e.printStackTrace();
            }
            return JSONArrayresult;
        }
    }

    class TrackUser extends AsyncTask<Void, Void, JSONArray> {
        String URL = URLConstants.Track_Spilt_Time;


        @Override
        protected JSONArray doInBackground(Void... voids) {
            try {
                JSONArrayDate = postJSON(URL, jsonTimeObject);
                getdate = JSONArrayDate.getString(1);

            } catch (Exception e) {
                e.printStackTrace();
            }
            return JSONArrayDate;
        }

    }


    public JSONArray postJSON(String url, JSONObject addJobj) {

        InputStream inputStream = null;
        String result = "";


        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        httpClient.addInterceptor(new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request request = chain.request();
                Request request1 = request.newBuilder().header("Accept", "application/json")
                        .header("Content-Type", "application/json").build();

                return chain.proceed(request1);
            }
        });

        OkHttpClient httpClient1 =httpClient.build();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(" http://2b951580.ngrok.io/")
                .addConverterFactory(GsonConverterFactory.create())
                .client(httpClient1)
                .build();

        Gps_api gps_api = retrofit.create(Gps_api.class);
        Model model = null;
        try {
            String username = jsonObject.getString("username");
            String password = jsonObject.getString("password");
            String deviceId = jsonObject.getString("deviceId");
            String role= jsonObject.getString("role");
            String refreshToken = jsonObject.getString("refreshToken");

            model = new Model(username,password,deviceId,role,refreshToken);


        }
        catch (JSONException e) {
            e.printStackTrace();
        }
//        try {
//            Model model1 = gps_api.reguser(model.getUsername(),model.getPassword(),model.getDeviceId(),model.getRole(),model.getRefreshToken()).execute().body();
//            JSONObject jsonObject = new JSONObject();
//            jsonObject.put("username", model1.getUsername());
//            jsonObject.put("password", model1.getPassword());
//            jsonObject.put("deviceId", model1.getDeviceId());
//            jsonObject.put("role",model1.getRole());
//            jsonObject.put("refreshToken",model1.getRefreshToken());
//
//
//            return model1;
//
//        } catch (Exception e) {
//            e.printStackTrace();
//            Log.e("sgffsgs",e.toString());
//
//            return null;
//        }


        JSONArray json = null;

        try {
            json = new JSONArray(result);
            // jsonArray=new JSONArray(json);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        //Log.d("JSON-->", json.toString());
        return json;
    }

    private String convertInputStreamToString(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        while ((line = bufferedReader.readLine()) != null)
            result += line;
        inputStream.close();
        return result;
    }

    private void Addmarker(List<LatLng> latLngs, String deviceID) {
        LatLng lt = null;
        lt = latLngs.get(0);
        int len = latLngs.size() - 1;
        Marker marker = map.addMarker(new MarkerOptions().position(latLngs.get(0)).title(deviceID.toString()).icon(BitmapDescriptorFactory.fromResource(R.drawable.smarker)));
        Marker marker1 = map.addMarker(new MarkerOptions().position(latLngs.get(len)).title(deviceID.toString()).icon(BitmapDescriptorFactory.fromResource(R.drawable.dmarker)));
        float zoom = 15.3f;

        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(lt, zoom);
        map.animateCamera(cameraUpdate);

        Random rnd = new Random();
        int color = Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
        polylineOptions = new PolylineOptions();
        polylineOptions.color(Color.RED);
        polylineOptions.width(5);
        polylineOptions.addAll(latLngs);
        map.addPolyline(polylineOptions);


    }

}


