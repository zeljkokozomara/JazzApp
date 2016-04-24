package zeljkok.autumnsky.jazzapp;

import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity implements View.OnClickListener
{
    // standard tag for logging
    public static final String TAG = "MobileJazz.Application";

    Button btnPost;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnPost = (Button)this.findViewById(R.id.btnPost);
        btnPost.setOnClickListener(this);

    }


    @Override
    public void onClick(View view)
    {
        // call AsynTask to perform network operation on separate thread
        HttpAsyncTask task = new HttpAsyncTask();
        task.execute(this.getString(R.string.jazz_url));
    }


    /* Override AsyncTask to allow safe background operations on UI thread */
    private class HttpAsyncTask extends AsyncTask<String, Void, String>
    {
        @Override
        protected String doInBackground(String... urls)
        {
            String result = "";
            URL jazz = null;
            HttpURLConnection conn = null;

            // 1. open the connection
            try
            {
                jazz = new URL(urls[0]);
                conn = (HttpURLConnection) jazz.openConnection();
            }
            catch (Exception ex)
            {
                result =  "Exception thrown while opening URL connection. Reason: " + ex.getLocalizedMessage();
                Log.e(TAG, result);

                return result;
            }

            try
            {
                // 2. configure for POST (default is GET)
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);

                // 3. set content type
                conn.setRequestProperty("Content-Type", "application/json");

                // 4. build jsonObject with our job application as per specs
                String message = buildJSON();
                Log.d(TAG, message);

                // 5. connect
                conn.connect();

                // 6. send
                OutputStream os = new BufferedOutputStream(conn.getOutputStream());
                os.write(message.getBytes());
                os.close();

                // 7. check if it worked out
                int response = conn.getResponseCode();
                if (response == HttpURLConnection.HTTP_OK)
                   result = "Jazz Application sent successfully";
                else
                   result = "Jazz Application not sent. Server response: " + Integer.toString(response);
            }
            catch (Exception ex)
            {
                result =  "Exception thrown while sending POST request. Reason: " + ex.getLocalizedMessage();
                Log.e(TAG, result);
            }

            finally
            {
                conn.disconnect();   // regardless of status, close connection always
            }

            return result;
        }


        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result)
        {
            Toast.makeText(getBaseContext(), result, Toast.LENGTH_LONG).show();
        }

        // utility to build JSON message
        private String buildJSON() throws JSONException
        {
            JSONObject json = new JSONObject();
            Context c = getApplicationContext();

            // required mappings: name, email, about -- strings
            json.put(c.getString(R.string.json_name_key), c.getString(R.string.json_name_value) );
            json.put(c.getString(R.string.json_email_key), c.getString(R.string.json_email_value) );
            json.put(c.getString(R.string.json_about_key), c.getString(R.string.json_about_value) );

            // flex mappings:  array of url string credentials
            JSONArray refs = new JSONArray();
            JSONObject vals = new JSONObject();

            // 1. Resume
            vals.put(c.getString(R.string.json_resume_key), c.getString(R.string.json_resume_value) );

            // 2. Code samples
            vals.put(c.getString(R.string.json_codesamples_key), c.getString(R.string.json_codesamples_value) );

            // 3. Linkedin
            vals.put(c.getString(R.string.json_linkedin_key), c.getString(R.string.json_linkedin_value) );

            // 4. Photo site
            vals.put(c.getString(R.string.json_photosite_key), c.getString(R.string.json_photosite_value) );

            // 5. This code git repository

            // stick in array
            refs.put(vals);
            json.put(c.getString(R.string.json_urls_key), refs);

            // and return full string, which will be sent over httpUrlConnection
            return json.toString();

        }
    }

    }
