package com.prem.grovertest;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import android.support.v7.app.ActionBarActivity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends ActionBarActivity {
	final String url = "http://agiotesting.appspot.com/save";
	HttpClient httpclient = null;
	private ProgressDialog pDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		final Button button = (Button) findViewById(R.id.submit);
		button.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if(isNetworkAvailable())
				(new SubmitData()).execute("url");
				else
				Toast.makeText(getBaseContext(), "Network Is Not Available.....", Toast.LENGTH_LONG).show();
			}
		});
		httpclient = new DefaultHttpClient();
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case 0:
			pDialog = new ProgressDialog(this);
			pDialog.setMessage("Submitting data. Please wait...");
			pDialog.setIndeterminate(false);
			pDialog.setMax(100);
			pDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			pDialog.setCancelable(true);
			pDialog.show();
			return pDialog;
		default:
			return null;
		}
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		if(pDialog != null)
		pDialog.dismiss();
	}

	class SubmitData extends AsyncTask<String, String, String> {
		@SuppressWarnings("deprecation")
		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			showDialog(0);
			pDialog.setProgress(0);
		}

		@SuppressWarnings("deprecation")
		@Override
		protected void onPostExecute(String result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
	        dismissDialog(0);
	        if(result.equals("neterror"))
				Toast.makeText(getBaseContext(), "Network Connection Lost.....", Toast.LENGTH_LONG).show();
	        if(result.equals("unsuccess"))
				Toast.makeText(getBaseContext(), "There is some problem in submitting records.....", Toast.LENGTH_LONG).show();
	        else
				Toast.makeText(getBaseContext(), "All records submitted successfully.....", Toast.LENGTH_LONG).show();
		}

		@Override
		protected void onProgressUpdate(String... progress) {
			// TODO Auto-generated method stub

			pDialog.setProgress(Integer.parseInt(progress[0]));
		}

		@Override
		protected String doInBackground(String... params) {
			// TODO Auto-generated method stub
			String result = "success";
			InputStream is = getResources().openRawResource(R.raw.input);
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			String line = null;
			int total_records = 50;
			int rec_count = 0;
			try {
				while ((line = br.readLine()) != null) {
					if(!isNetworkAvailable())
					{
						 result = "neterror";
						 break;
					}
					rec_count++;
					String[] record = line.split("[,\\s]+");
					String data = "{'name':'"+record[0]+"','latitude':'"+record[1]+"','longitude':'"+record[2]+"'}";
					HttpPost httppost = new HttpPost(url);
					httppost.setEntity(new StringEntity(data));
					HttpResponse postResponse = httpclient.execute(httppost);
					int statusCode = postResponse.getStatusLine()
							.getStatusCode();
					publishProgress(""+(rec_count*100)/total_records);
					if(statusCode != 200)
						result = "unsuccess";
					System.out.println("response code: " + statusCode);
				}
				// Toast.makeText(getBaseContext(), record[0], 1000).show();
			} catch (Exception e) {
				// TODO Auto-generated catch block
					 result = "unsuccess";
				}
			return result;
		}

	}
	private boolean isNetworkAvailable() {
	    ConnectivityManager connectivityManager 
	          = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
	    return activeNetworkInfo != null && activeNetworkInfo.isConnected();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
