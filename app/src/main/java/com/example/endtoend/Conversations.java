package com.example.endtoend;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.example.endtoend.ART.ARTKey;
import com.example.endtoend.ART.ARTKeyPair;
import com.example.endtoend.ART.Crypto;
import com.example.endtoend.ART.SymmetricCrypto;
import com.example.endtoend.ART.Stringify.*;
import com.example.endtoend.ART.SetupMessage;
import com.example.endtoend.ART.State;
import com.example.endtoend.ART.Crypto.*;
import com.example.endtoend.ART.UpdateKeyMessage;

import org.json.JSONArray;
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
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static com.example.endtoend.ART.Stringify.StoString;
import static com.example.endtoend.ART.Stringify.fromString;

public class Conversations extends AppCompatActivity {


    private EditText editText;
    String group_name;
    State state=null;
    SetupMessage setupMessage;

    Context context;
    MessageAdapter messageAdapter;
    ListView messagesView;
    String message;
    int last_id=0;
    String username;
    ARTKeyPair IKey;
    ARTKeyPair EKey;

    ProgressDialog progress;
    HashMap<String,MemberData> memberDataHashMap = new HashMap<>();



    public void sendMessage() {
        message = editText.getText().toString();
        refresh();
        editText.setText("");
        if (message.length() > 0) {


            //  user messaging, group id (all public keys), state, message as a string
            if (state==null){
                Toast.makeText(context, "Network Error", Toast.LENGTH_LONG).show();
            }
            else {
                try {


                UpdateKeyMessage updateKey = Crypto.updateKey(state);
                ARTKey stageKey = state.getStageKey();
                String ciphertext = SymmetricCrypto.encrypt(stageKey, message);
                String updateKeyMessage = StoString(updateKey);


                Conversations.AsyncCall asyncRegistration = new Conversations.AsyncCall();


                    asyncRegistration.execute("SendMessage", "group_name", group_name, "sender", username, "update_key_message", updateKeyMessage, "cipher_text", ciphertext, "state",StoString(state));
                } catch (Exception e) {
                    Toast.makeText(context, "Errors: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }


            // take updateKeyMessage and call Crypto.processKeyUpdate(), it will modify state of group
            // state.getStageKey() to decrypt
            // having message now, I can show it on activity





        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversations);
        context = this;
        messageAdapter = new MessageAdapter(context);
        messagesView = (ListView) findViewById(R.id.messages_view);
        messagesView.setAdapter(messageAdapter);
        group_name= getIntent().getStringExtra("group_name");
        final ImageButton sendButton = (ImageButton) findViewById(R.id.send_message);

        SharedPreferences sharedpreferences = getSharedPreferences("EndToEnd", Context.MODE_PRIVATE);
        if (sharedpreferences.contains("user")) {
            username = sharedpreferences.getString("user", "User");
            String IKey_String = sharedpreferences.getString("ikey-"+username, "");
            String EKey_String = sharedpreferences.getString("ekey-"+username, "");
            try {
                if (IKey_String.isEmpty() || EKey_String.isEmpty()){
                    sharedpreferences.edit().remove("user");
                    Intent intent = new Intent(Conversations.this, LoginActivity.class);
                    startActivity(intent);
                }
                else {
                    IKey = (ARTKeyPair) fromString(IKey_String);
                    EKey = (ARTKeyPair) fromString(EKey_String);
                }
            }
            catch (Exception e){
                Toast.makeText(context, "Error: "+ e.getMessage(), Toast.LENGTH_SHORT).show();
                sendButton.setVisibility(View.INVISIBLE);
                e.printStackTrace();
            }
        }

        try {
            setupMessage = (SetupMessage) fromString(getIntent().getStringExtra("setup_message"));
        }
        catch (Exception e){
            Toast.makeText(context,"Error: "+ e.getMessage(), Toast.LENGTH_LONG).show();
        }

        refresh();


        editText = (EditText) findViewById(R.id.editText);
        editText.setText("");
        editText.setHint("Write a message @"+group_name);

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });


        //MemberData data = new MemberData(getRandomName(), getRandomColor());


    }
    public void refresh(){
        Conversations.AsyncCall asyncRegistration=new Conversations.AsyncCall();
        asyncRegistration.execute("Messages","group_name",group_name,"last_id",""+last_id);

        progress=new ProgressDialog(context);
        progress.setMessage("Refreshing..");
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progress.setIndeterminate(false);
        progress.show();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_conversations, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement

        if (id == R.id.refresh){
            refresh();
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
            if (progress!=null)
                if (progress.isShowing())
                    progress.dismiss();

            if (result.split("\n")[0].equals("1")){
                Toast.makeText(context,"Message Sent", Toast.LENGTH_LONG).show();


                Conversations.AsyncCall asyncRegistration=new Conversations.AsyncCall();
                asyncRegistration.execute("Messages","group_name",group_name,"last_id",""+last_id);

            }
            else if (result.split("\n")[0].equals("-1") || result.split("\n").length==1){
                Toast.makeText(context,"Error: "+ result, Toast.LENGTH_LONG).show();
            }
            else{




                messageAdapter.clearMessage();
                memberDataHashMap.clear();

                try {

                    JSONObject json = new JSONObject(result);
                    setupMessage = (SetupMessage)fromString(json.getString("setup_message"));

                    String creator = json.getString("creator");
                    if (creator.equalsIgnoreCase(username)) {
                        String stateString = json.getString("state");
                        state = (State) fromString(stateString);
                    }
                    else {
                        state = new State(IKey, EKey);
                        Crypto.processSetupMessage(state, setupMessage);
                    }

                    JSONArray users = json.getJSONArray("users");
                    JSONArray jsonArray = json.getJSONArray("messages");
                    for(int i=0;i<users.length();i++){
                        String user=(String) users.get(i);
                        memberDataHashMap.put(user,new MemberData(user));
                    }

                    int jsonLength = jsonArray.length();
                    for(int i=0;i<jsonLength;i++){

                        JSONObject message = (JSONObject) jsonArray.get(i);
                        String updateKeyMessage="",cipher_text,sender,send_time;
                        sender = message.getString("sender");
                        cipher_text = message.getString("cipher_text");
                        send_time = message.getString("send_time");

                        State sender_state = (State) fromString(message.getString("state"));


                        if (sender.equalsIgnoreCase(username)){
                            state = (State) fromString(message.getString("state"));
                        }
                        else{
                            updateKeyMessage = message.getString("update_key_message");
                            UpdateKeyMessage updateKey = (UpdateKeyMessage) fromString(updateKeyMessage);
                            Crypto.processKeyUpdate(state,updateKey);
                        }


                        ARTKey stageKey = state.getStageKey();

                        String current_message =  SymmetricCrypto.decrypt(stageKey,cipher_text);

                        Message msg = new Message(current_message,memberDataHashMap.get(sender),sender.equalsIgnoreCase(username));


                        messageAdapter.add(msg);
                        messagesView.setSelection(messagesView.getCount() - 1);

                    }

                    if (messageAdapter!=null)
                        messageAdapter.notifyDataSetChanged();

                }
                catch (Exception e){
                    Toast.makeText(context,"Error: "+ e.getMessage(), Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }

                if (messageAdapter.getCount()==0){
                    Toast.makeText(context,"No messages", Toast.LENGTH_LONG).show();

                }







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
                    System.out.println(params[i]+" -- " +params[i+1]);
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