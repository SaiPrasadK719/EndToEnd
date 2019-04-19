package com.example.endtoend;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.app.LoaderManager.LoaderCallbacks;

import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.Manifest.permission.READ_CONTACTS;
import static android.provider.AlarmClock.EXTRA_MESSAGE;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity  {

    Context context;
    ProgressDialog progress;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        context = this;

        SharedPreferences sharedpreferences = getSharedPreferences("EndToEnd", Context.MODE_PRIVATE);
        if (sharedpreferences.contains("user")) {
            String username = sharedpreferences.getString("user", "User");
            Toast.makeText(context, "Welcome " + username, Toast.LENGTH_LONG).show();
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
        } else {
            setContentView(R.layout.activity_login);

            Button login = (Button) findViewById(R.id.email_sign_in_button);
            login.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {

                    EditText editText = (EditText) findViewById(R.id.username);
                    String username = editText.getText().toString();
                    if (username.isEmpty()) {
                        Toast.makeText(context, "Please Enter Username", Toast.LENGTH_LONG).show();
                    } else {
                        LoginActivity.AsyncCall asyncCall = new LoginActivity.AsyncCall();
                        asyncCall.execute("Login", "username", username);


                        progress = new ProgressDialog(context);
                        progress.setMessage("Processing..");
                        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                        progress.setIndeterminate(false);
                        progress.show();
                    }
                }
            });

            Button register = (Button) findViewById(R.id.email_register_button);
            register.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(LoginActivity.this, Registration.class);
                    startActivity(intent);
                }
            });
        }
    }




    public class AsyncCall extends AsyncTask<String,Void,String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override

        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (result.split("\n")[0].equals("-1")){
                    Toast.makeText(context, "Error: "+result.split("\n")[1], Toast.LENGTH_LONG).show();
            }
            else{
                String user_id = result.split("\n")[0];

                Toast.makeText(context,"Login Success!", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
            }



            if (progress!=null)
                if (progress.isShowing())
                    progress.dismiss();


        }
        private String getPostDataString(HashMap<String, String> params) throws UnsupportedEncodingException {
            StringBuilder result = new StringBuilder();
            boolean first = true;
            for(Map.Entry<String, String> entry : params.entrySet()){
                if (first)
                    first = false;
                else
                    result.append("&");

                result.append(URLEncoder.encode(entry.getKey(), "utf-8"));
                result.append("=");
                result.append(URLEncoder.encode(entry.getValue(), "utf-8"));
            }

            return result.toString();
        }

        @Override
        protected String doInBackground(String... params){
            String result = new String("");
            try {


                URL url = new URL(Config.URL+params[0]);
                HttpURLConnection client = null;
                client = (HttpURLConnection) url.openConnection();
                client.setRequestMethod("GET");
                client.setConnectTimeout(4000);
                HashMap<String,String> postData = new HashMap<>();

                int count = params.length;
                for(int i=1;i<count;i+=2){
                    postData.put(params[i],params[i+1]);
                }
                client.setDoOutput(true);

                OutputStreamWriter wr = new OutputStreamWriter(client.getOutputStream());
                wr.write(getPostDataString(postData));
                wr.flush();
                wr.close();
                BufferedReader reader = new BufferedReader(new InputStreamReader(client.getInputStream()));
                String line;
                while((line=reader.readLine())!=null){
                    result = result + line + "\n";

                }

                reader.close();
                if (client!=null){
                    client.disconnect();
                }


            }
            catch (MalformedURLException e){

                result = "-1\n"+e.getMessage();
                e.printStackTrace();
            }
            catch (SocketTimeoutException e){
                result = "-1\n"+e.getMessage();
                e.printStackTrace();
            }
            catch (IOException e){

                result = "-1\n"+e.getMessage();
                e.printStackTrace();
            }
            catch (Exception e){
                result = "-1\n"+e.getMessage();
                e.printStackTrace();
            }
            return result;
        }


    }
}

