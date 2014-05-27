package com.example.smarthome;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;
import android.view.*;

public class TaskActivity extends ListActivity {
	String[] tasks;
	JSONArray jtasks;
	List<Map<String, String>> list = new ArrayList<Map<String, String>>();
	int p;
	String mid;
	String strtasks;
	
	String URL_connect = LoginActivity.IP_Server + "/smarthome/account/login";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Intent intent = this.getIntent();
		strtasks = intent.getStringExtra("strtasks");
		mid = intent.getStringExtra("mid");

		try {
			jtasks = new JSONArray(strtasks);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		tasks = new String[jtasks.length()];

		for (int i = 0; i < jtasks.length(); i++) {
			try {
				JSONObject item = jtasks.getJSONObject(i);
				String title = item.getString("title");
				String deadline = item.getString("deadline");
				tasks[i] = title+"\t\t\t\t"+deadline;
				Map<String, String> map = new HashMap<String, String>();
				map.put("title", title);
				map.put("deadline", deadline);
				list.add(map);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}		
		
		setListAdapter(new ArrayAdapter<String>(this,
				R.layout.activity_task, tasks));

		ListView lv = getListView();
		lv.setTextFilterEnabled(true);
	}

	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.task, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent;
		switch (item.getItemId()) {
		case R.id.add:
			intent = new Intent().setClass(this, AddTaskActivity.class);
			intent.putExtra("mid",mid);
			intent.putExtra("strtasks", strtasks);
			startActivity(intent);
			return true;
		}
		return false;
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		return (applyMenuChoice(item) || super.onContextItemSelected(item));
	}

	private boolean applyMenuChoice(MenuItem item) {
		return (false);
	}
}
