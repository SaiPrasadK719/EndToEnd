package com.example.endtoend;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.util.SparseBooleanArray;
import android.widget.Toast;

import com.example.endtoend.ART.ARTKey;
import com.example.endtoend.ART.ARTKeyPair;
import com.example.endtoend.ART.Crypto;
import com.example.endtoend.ART.SetupMessage;
import com.example.endtoend.ART.State;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
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
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.endtoend.ART.Stringify.StoString;
import static com.example.endtoend.ART.Stringify.fromString;
import static java.lang.Math.min;


public class CreateGroup extends AppCompatActivity {

    Context context;
    ProgressDialog progress;
    ArrayAdapter adapter;
    String username;
    ArrayList<String> contactsArray = new ArrayList<String>();
    ArrayList<String> seletectedContacts = new ArrayList<>();
    final List<ARTKey> IKeys = new ArrayList<>();
    final List<ARTKey> EKeys = new ArrayList<>();
    ARTKeyPair IKey;
    ARTKeyPair EKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //String Message = savedInstanceState.getBinder(EXTRA_MESSAGE);
        setContentView(R.layout.activity_create_group);

        Button submitButton = (Button)findViewById(R.id.create_group_button);

        context = this;

        FileOutputStream fos = null;
        ObjectOutputStream oos = null;


        adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_multiple_choice, contactsArray);
        final ListView listView=(ListView)findViewById(R.id.list);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        listView.setAdapter(adapter);

        SharedPreferences sharedpreferences = getSharedPreferences("EndToEnd", Context.MODE_PRIVATE);
        if (sharedpreferences.contains("user")) {
            username = sharedpreferences.getString("user", "User");
            String IKey_String = sharedpreferences.getString("ikey-"+username, "");
            String EKey_String = sharedpreferences.getString("ekey-"+username, "");
            try {
                if (IKey_String.isEmpty() || EKey_String.isEmpty()){
                    sharedpreferences.edit().remove("user");
                    Intent intent = new Intent(CreateGroup.this, LoginActivity.class);
                    startActivity(intent);
                }
                else {
                    IKey = (ARTKeyPair) fromString(IKey_String);
                    EKey = (ARTKeyPair) fromString(EKey_String);
                }
            }
            catch (Exception e){
                Toast.makeText(context, "Error: "+ e.getMessage(), Toast.LENGTH_SHORT).show();
                submitButton.setVisibility(View.INVISIBLE);
                e.printStackTrace();
            }


        }

        progress=new ProgressDialog(context);
        progress.setMessage("Processing..");
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progress.setIndeterminate(false);
        progress.show();

        AsyncCall asyncRegistration=new AsyncCall();
        asyncRegistration.execute("Contacts");


        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    seletectedContacts.clear();
                    EditText groupName = findViewById(R.id.groupName);
                    String groupNameStr =  groupName.getText().toString();
                    ArrayList<ARTKey> publicIKeys = new ArrayList<>();
                    ArrayList<ARTKey> publicEKeys = new ArrayList<>();
                    SparseBooleanArray checked = listView.getCheckedItemPositions();
                    boolean flag=false;
                    for (int i = 0; i < checked.size(); i++) {
                        flag = true;
                        if (checked.valueAt(i) == true) {
                            int index = checked.keyAt(i);
                            seletectedContacts.add(contactsArray.get(index));
                            publicIKeys.add(IKeys.get(index));
                            publicEKeys.add(EKeys.get(index));
                        }
                    }

                    if (!groupNameStr.isEmpty() && flag) {

                        State state = new State(IKey, EKey);
                        ARTKey[] publicEKeysArray = new ARTKey[publicEKeys.size()];
                        publicEKeysArray = publicEKeys.toArray(publicEKeysArray);

                        ARTKey[] publicIKeysArray = new ARTKey[publicIKeys.size()];
                        publicIKeysArray = publicIKeys.toArray(publicIKeysArray);

                        SetupMessage setupMessage = Crypto.setupGroup(state, publicIKeysArray, publicEKeysArray);

                        // this setupMessage has to send to server along with group name, creator, time, user details
                        // state also needs to store


                        AsyncCall asyncRegistration = new AsyncCall();



                        asyncRegistration.execute("CreateGroup", "group_name", groupNameStr, "creator", username,"users",seletectedContacts.toString(), "setup_message",StoString(setupMessage),"state",StoString(state));


                    }
                    else{
                        Toast.makeText(context, "Mandatory Fields", Toast.LENGTH_SHORT).show();
                    }


                }
                catch (Exception e){
                    e.printStackTrace();
                    Toast.makeText(context, "Error: "+ e.getMessage(), Toast.LENGTH_SHORT).show();
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
            if (result.split("\n")[0].equals("-1")){
                Toast.makeText(context,"Error: "+ result.split("\n")[1], Toast.LENGTH_LONG).show();
            }
            else if (result.split("\n")[0].equals("1")){
                Toast.makeText(context,"Group Created", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(CreateGroup.this, MainActivity.class);

                startActivity(intent);

            }
            else{
                try {
                    JSONObject json = new JSONObject(result);
                    JSONArray jsonArray = json.getJSONArray("users");
                    int jsonLength = jsonArray.length();
                    for(int i=0;i<jsonLength;i++){
                        JSONObject user = (JSONObject) jsonArray.get(i);
                        if (!user.getString("username").equalsIgnoreCase(username)) {
                            contactsArray.add(user.getString("username"));
                            try {
                                IKeys.add((ARTKey) fromString(user.getString("public_ikey")));
                                EKeys.add((ARTKey) fromString(user.getString("public_ekey")));
                            } catch (Exception e) {
                                e.printStackTrace();
                                Toast.makeText(context, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                    if (adapter!=null)
                        adapter.notifyDataSetChanged();



                }
                catch (JSONException e){
                    Toast.makeText(context,"Error: "+ e.getMessage(), Toast.LENGTH_LONG).show();

                }
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

                result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
                result.append("=");
                result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
            }
            System.out.println(result);
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
