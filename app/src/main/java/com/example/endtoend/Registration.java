package com.example.endtoend;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.endtoend.ART.ARTKey;
import com.example.endtoend.ART.ARTKeyPair;
import com.example.endtoend.ART.Stringify.*;


import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLEncoder;
import java.sql.SQLException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import static com.example.endtoend.ART.Stringify.fromString;
import static  com.example.endtoend.ART.Stringify.StoString;

public class Registration extends AppCompatActivity {

    Context context;
    String username;
    ProgressDialog progress;
     ARTKeyPair IKey = ARTKeyPair.getRandom();
     ARTKeyPair EKey = ARTKeyPair.getRandom();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        context = Registration.this;


        Button login = (Button) findViewById(R.id.email_sign_in_button);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Registration.this, LoginActivity.class);
                startActivity(intent);
            }
        });

        Button register = (Button) findViewById(R.id.email_register_button);
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText usernameField = (EditText) findViewById(R.id.username);
                username= usernameField.getText().toString();
                if (username.isEmpty()){
                    Toast.makeText(context,"Please Enter Username", Toast.LENGTH_LONG).show();
                }
                else{
                    AsyncCall asyncRegistration=new AsyncCall();
                    String public_ikey,public_ekey;
                    try{
                        public_ikey = StoString(IKey.getPublicKey());
                        public_ekey = StoString(EKey.getPublicKey());

                        asyncRegistration.execute("Register","username",username,"public_ikey", public_ikey,"public_ekey",public_ekey);




                    }
                    catch (Exception e){
                        Toast.makeText(context, "Error: "+e.getMessage(), Toast.LENGTH_LONG).show();
                    }



                    progress=new ProgressDialog(context);
                    progress.setMessage("Registering..");
                    progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    progress.setIndeterminate(false);
                    progress.show();


                }




            }
        });


    }





    public class AsyncCall extends AsyncTask<String,Void,String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override

        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (progress!=null)
                if (progress.isShowing())
                    progress.dismiss();

            if (result.split("\n")[0].equals("1")){


                try {
                    SharedPreferences sharedpreferences = getSharedPreferences("EndToEnd", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedpreferences.edit();
                    editor.putString("user", username);
                    editor.putString("ikey-"+username, StoString(IKey));
                    editor.putString("ekey-"+username, StoString(EKey));
                    editor.apply();
                    editor.commit();
                    Toast.makeText(context,"Success", Toast.LENGTH_LONG).show();
                    IKey = ARTKeyPair.getRandom();
                    EKey = ARTKeyPair.getRandom();
                }
                catch (Exception e){
                    Toast.makeText(context,"Error: "+ e.getMessage(), Toast.LENGTH_LONG).show();
                }

            }
            else if (result.split("\n").length==1){
                Toast.makeText(context,result, Toast.LENGTH_LONG).show();
            }
            else{
                Toast.makeText(context,"Error: "+ result.split("\n")[1], Toast.LENGTH_LONG).show();
            }




        }
        private String getPostDataString(HashMap<String, String> params) throws UnsupportedEncodingException {
            StringBuilder result = new StringBuilder();
            boolean first = true;
            for(Map.Entry<String, String> entry : params.entrySet()){
                if (first)
                    first = false;
                else
                    result.append("&");

                result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
                result.append("=");
                result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
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
                client.setRequestMethod("POST");
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
