package com.example.benigps;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.benigps.Model.Model;
import com.example.benigps.service.Gps_api;
import com.example.benigps.utils.URLConstants;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class User_Login extends AppCompatActivity {

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    JSONObject jsonObject;
    String deviceID;
    String token;
    String roleString;
    EditText user,pwd;
    private ProgressDialog pDialog;
    SharedPreferences getToken;
    SharedPreferences.Editor mmmedit;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_login);

        deviceID = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        Log.d("",""+deviceID);
        user = (EditText) findViewById(R.id.Username);
        pwd = (EditText) findViewById(R.id.Password);

        sharedPreferences = getApplicationContext().getSharedPreferences("LoginDetails", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        // Set up the login form.



        if(sharedPreferences.getString("user_name","").matches(""))
        {

        }
        else
        {
            //Intent intent=new Intent(getApplicationContext(),User_Class.class);
            //startActivity(intent);
            //finish();
            user.setText(sharedPreferences.getString("user_name","t"));
            pwd.setText(sharedPreferences.getString("user_pwd","t"));
        }



        Button btnRegister=(Button)findViewById(R.id.UserRegister);
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getToken = getSharedPreferences("TokenDetails", Context.MODE_PRIVATE);


                token = getToken.getString("token", "t");
                Log.d("",""+token);
                String username = user.getText().toString();
                if(!username.isEmpty())
                {
                    createJsonObject();

                }
                else
                {
                    Toast.makeText(getApplicationContext(),"Enter username",Toast.LENGTH_SHORT).show();
                }



            }
        });

    }
    public void storeShared()
    {
        String username = user.getText().toString();
        String userpassword=pwd.getText().toString();
        editor.putString("user_name", username);
        editor.putString("user_pwd",userpassword);
        editor.commit();
        Intent intent=new Intent(getApplicationContext(),User_Class.class);
        startActivity(intent);
        finish();
    }

    public void createJsonObject() {
        jsonObject = new JSONObject();
        //  JSONObject locationObject = new JSONObject();
        // JSONArray jsonArray=new JSONArray();
        try {
            jsonObject.put("username",user.getText().toString());
            jsonObject.put("password",pwd.getText().toString());
            jsonObject.put("deviceId",deviceID.toString());
            jsonObject.put("role","user");
            jsonObject.put("refreshToken",token.toString());
            new SaveUser().execute();

        } catch (JSONException e) {
            Log.e("Error:", e.toString());
        }
    }


    class SaveUser extends AsyncTask<Void, Void, Model> {

        String URL = URLConstants.Register;
        //String URL = "http://192.168.1.2:3000/api/user/registerUser";
        //http:// 192.168.1.2:3000/api/track

        @Override
        protected  Model doInBackground(Void... voids) {



//            try {
//                result = postJsonObject(URL, jsonObject);
//            }catch (Exception e) {
//                e.printStackTrace();
//            }
//
//
//            return result




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
                    .baseUrl("    http://2b951580.ngrok.io/")
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
            try {
                Model model1 = gps_api.reguser(model.getUsername(),model.getPassword(),model.getDeviceId(),model.getRole(),model.getRefreshToken()).execute().body();
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("username", model1.getUsername());
                jsonObject.put("password", model1.getPassword());
                jsonObject.put("deviceId", model1.getDeviceId());
                jsonObject.put("role",model1.getRole());
                jsonObject.put("refreshToken",model1.getRefreshToken());


                return model1;

            } catch (Exception e) {
                e.printStackTrace();
                Log.e("sgffsgs",e.toString());

                return null;
            }


        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog

            pDialog = new ProgressDialog(User_Login.this);
            pDialog.setMax(100);
            pDialog.setMessage("Please wait...");
            pDialog.setCancelable(false);
            pDialog.show();
            Runnable progressRunnable = new Runnable() {

                @Override
                public void run() {
                    pDialog.cancel();
                }
            };
        }

        @Override
        protected void onPostExecute(Model json) {
            super.onPostExecute(json);
            // Dismiss the progress dialog
            if (pDialog.isShowing())

                pDialog.dismiss();
            if (json != null) {
                storeShared();


            } else {
                Toast.makeText(getApplicationContext(), "Server is down..Contact Administrator", Toast.LENGTH_LONG).show();
            }
            //     LoadListView();

        }

    }
}
