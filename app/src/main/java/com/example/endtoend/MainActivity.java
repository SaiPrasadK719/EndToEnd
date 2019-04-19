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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.endtoend.ART.ARTKey;
import com.example.endtoend.ART.ARTKeyPair;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.provider.AlarmClock.EXTRA_MESSAGE;
import static com.example.endtoend.ART.Stringify.fromString;

public class MainActivity extends AppCompatActivity {


    Context context;
    ProgressDialog progress;
    ArrayAdapter adapter;
    String username;
    ArrayList<String> groupArray = new ArrayList<String>();
    ArrayList<String> desc = new ArrayList<String>();
    ArrayList<String> setup_message = new ArrayList<String>();
    ArrayList<String> state = new ArrayList<String>();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;
        SharedPreferences sharedpreferences = getSharedPreferences("EndToEnd", Context.MODE_PRIVATE);
        if (sharedpreferences.contains("user")) {
            username = sharedpreferences.getString("user", "User");
        }



        adapter = new ArrayAdapter<String>(this,R.layout.activity_listview, groupArray);


        ListView listView = (ListView) findViewById(R.id.mobile_list);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                String selectedItem = (String) parent.getItemAtPosition(position);
                Intent intent = new Intent(MainActivity.this, Conversations.class);
                intent.putExtra("group_name", selectedItem);
                intent.putExtra("setup_message", setup_message.get(position));
                intent.putExtra("state", state.get(position));
                startActivity(intent);

            }
        });

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(MainActivity.this, CreateGroup.class);
                startActivity(intent);



                // call create group activity

            }
        });



        progress=new ProgressDialog(context);
        progress.setMessage("Fetching groups..");
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progress.setIndeterminate(false);
        progress.show();

        MainActivity.AsyncCall asyncRegistration=new MainActivity.AsyncCall();
        asyncRegistration.execute("Groups","username",username);

    }

    @Override
    public void onBackPressed() {
        return;
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
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(1);
            return true;
        }
        if (id == R.id.refresh){
            MainActivity.AsyncCall asyncRegistration=new MainActivity.AsyncCall();
            asyncRegistration.execute("Groups","username",username);

            progress=new ProgressDialog(context);
            progress.setMessage("Refreshing..");
            progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progress.setIndeterminate(false);
            progress.show();
        }

        return super.onOptionsItemSelected(item);
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
            else{
                try {
                    groupArray.clear();
                    setup_message.clear();
                    state.clear();
                    JSONObject json = new JSONObject(result);
                    JSONArray jsonArray = json.getJSONArray("groups");
                    int jsonLength = jsonArray.length();
                    for(int i=0;i<jsonLength;i++){
                        JSONObject group = (JSONObject) jsonArray.get(i);
                        if (!group.getString("group_name").equalsIgnoreCase(username)) {
                            groupArray.add(group.getString("group_name"));
                            setup_message.add(group.getString("setup_message"));
                            state.add(group.getString("state"));
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
            System.out.println(result.toString());
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
