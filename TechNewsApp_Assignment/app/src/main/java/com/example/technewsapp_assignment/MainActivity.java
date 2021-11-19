package com.example.technewsapp_assignment;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;



public class MainActivity extends AppCompatActivity {

    public class getContent extends AsyncTask<String, Void, String> {


        @Override
        protected String doInBackground(String... strings) {
            URL url;
            HttpURLConnection con;
            String result="";

            try {
                url = new URL(strings[0]);
                con = (HttpURLConnection) url.openConnection();
                con.connect();

                InputStream in = con.getInputStream();
                InputStreamReader reader = new InputStreamReader(in);
                BufferedReader bufferReader = new BufferedReader(reader);


                String data = bufferReader.readLine();

                while (data !=null) {
                    result +=data;
                    data = bufferReader.readLine();
                }



            } catch (Exception e) {
                e.printStackTrace();

            }
            return result;

        }
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        try {
            // --- COMMENTED CODE START ---


            // getting the keys and storing them in an array
            getContent fetchKeys = new getContent();
            String keysContent = fetchKeys.execute("https://hacker-news.firebaseio.com/v0/topstories.json").get();
            JSONArray jsonKeysArray = new JSONArray(keysContent);

            String [] keys = new String[20];
            for(int i=0;i<20;i++){
                keys[i] = jsonKeysArray.getString(i);
            }

            // getting the title and url of each article

            String [] titleArray = new String[20];
            String [] URLArray = new String[20];
            for(int i=0;i<20;i++) {
                getContent fetchDet = new getContent();
                String detContent = fetchDet.execute("https://hacker-news.firebaseio.com/v0/item/" + keys[i] + ".json").get();
                JSONObject jsonDetObject = new JSONObject(detContent);

                if(!jsonDetObject.has("url") || !jsonDetObject.has("title")){}
                else{
                    titleArray[i]= jsonDetObject.getString("title");
                    URLArray[i]= jsonDetObject.getString("url");
                }
            }

            // --- COMMENTED CODE END ---

            // store in SQLite database

            SQLiteDatabase db = this.openOrCreateDatabase("technewsdb",MODE_PRIVATE,null);
            db.execSQL("CREATE TABLE IF NOT EXISTS articles(article_id VARCHAR,article_title VARCHAR,article_url VARCHAR);");

            // --- COMMENTED CODE START ---

            for(int i=0;i<10;i++) {
                db.execSQL("INSERT INTO articles(article_id,article_title,article_url) " +
//                        "VALUES('testing','testing','testing');");
                        "VALUES('" + keys[i] +"','" + titleArray[i] +"','" + URLArray[i] +"');");

            }

            // --- COMMENTED CODE END ---



            // put the articles in a listview

            ListView listView = (ListView) findViewById(R.id.list);
            ArrayList<String> arrayList = new ArrayList<>();
            ArrayList<String> urlArrayList = new ArrayList<>();

            Cursor cursor = db.rawQuery("SELECT * FROM articles",null);

            int idIndex = cursor.getColumnIndex("article_id");
            int titleIndex = cursor.getColumnIndex("article_title");
            int URLIndex = cursor.getColumnIndex("article_url");


            cursor.moveToFirst();
            do{
                arrayList.add(cursor.getString(titleIndex));
                urlArrayList.add(cursor.getString(URLIndex));
            }
            while(cursor.moveToNext());

            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,arrayList);
            listView.setAdapter(adapter);

//            db.close();
//            getApplicationContext().deleteDatabase("technewsdb");



            // link the listview click to a webview

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    Intent intent = new Intent(getApplicationContext(), WebViewActivity.class);
                    String url = urlArrayList.get(i);
                    intent.putExtra("urlClicked",url);
                    startActivity(intent);
                }
            });



        } catch (Exception e) {
            e.printStackTrace();

        }
    }

}