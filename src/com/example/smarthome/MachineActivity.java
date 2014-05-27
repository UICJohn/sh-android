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

public class MachineActivity extends ListActivity {
	String[] machines;
	JSONArray jmachines;
	List<Map<String, String>> list = new ArrayList<Map<String, String>>();
	int p;
	private ProgressDialog pDialog;
	String mid;
	
	String URL_connect = LoginActivity.IP_Server + "/smarthome/task";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Intent intent = this.getIntent();
		String strmachines = intent.getStringExtra("strmachines");

		try {
			jmachines = new JSONArray(strmachines);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		machines = new String[jmachines.length()];

		for (int i = 0; i < jmachines.length(); i++) {
			try {
				JSONObject item = jmachines.getJSONObject(i);
				String id = item.getString("id");
				String name = item.getString("name");
				machines[i] = name;
				Map<String, String> map = new HashMap<String, String>();
				map.put("id", id);
				map.put("name", name);
				list.add(map);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		setListAdapter(new ArrayAdapter<String>(this,
				R.layout.activity_machine, machines));

		ListView lv = getListView();
		lv.setTextFilterEnabled(true);

		lv.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// When clicked, show a toast with the TextView text
				AlertDialog.Builder alertDialog = new AlertDialog.Builder(
						MachineActivity.this);

				// Setting Dialog Title
				alertDialog.setTitle("Confirm...");

				// Setting Dialog Message
				alertDialog
						.setMessage("Are you sure to see the tasks of this machine?");

				// Setting Icon to Dialog
				alertDialog.setIcon(R.drawable.warning);
				p = position;
				// Setting Positive Yes Button
				alertDialog.setPositiveButton("YES",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								// User pressed Cancel button. Write Logic Here
								Toast.makeText(getApplicationContext(),
										"You clicked on YES",
										Toast.LENGTH_SHORT).show();
								String mid=list.get(p).get("id");
								new asyncconfirm().execute(mid);
							}
						});
				// Setting Positive Yes Button
				alertDialog.setNeutralButton("NO",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								// User pressed No button. Write Logic Here
								Toast.makeText(getApplicationContext(),
										"You clicked on NO", Toast.LENGTH_SHORT)
										.show();
							}
						});
				alertDialog.show();
			}
		});
	}

	class asyncconfirm extends AsyncTask<String, String, String> {
		String mid;
		protected void onPreExecute() {
			// para el progress dialog
			pDialog = new ProgressDialog(MachineActivity.this);
			pDialog.setMessage("Loading Task List....");
			pDialog.setIndeterminate(false);
			pDialog.setCancelable(false);
			pDialog.show();
		}

		protected String doInBackground(String... params) {
			mid=params[0];
			executeRequest();						
			return "ok"; // login valido
		}
		
		protected void executeRequest() {
			DefaultHttpClient httpClient;
			String uriAPI = URL_connect;
			JSONArray jdata;
			/* 建立HTTP Post连线 */
			HttpPost httpRequest = new HttpPost(uriAPI);
			// Post运作传送变数必须用NameValuePair[]阵列储存
			// 传参数 服务端获取的方法为request.getParameter("name")
			List<NameValuePair> parameters = new ArrayList<NameValuePair>();
			parameters.add(new BasicNameValuePair("mid", mid));
			try {
				// HttpConnectionParams.setConnectionTimeout(httpRequest, 3000);
				// 发出HTTP request
				httpRequest.setEntity(new UrlEncodedFormEntity(parameters,
						HTTP.UTF_8));
				if (LoginActivity.ci_session != null) {
					httpRequest.setHeader("Cookie", "ci_session=" + LoginActivity.ci_session);
				}
				// 取得HTTP response
				httpClient = new DefaultHttpClient();			
				HttpResponse httpResponse = httpClient.execute(httpRequest);

				// 若状态码为200 ok
				if (httpResponse.getStatusLine().getStatusCode() == 200) {
					// 取出回应字串
					String strResult = EntityUtils.toString(httpResponse
							.getEntity());
					try {
						jdata = new JSONArray(strResult); 
						Log.i("HAHA", jdata.toString());
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					CookieStore mCookieStore = httpClient.getCookieStore();
					List<Cookie> cookies = mCookieStore.getCookies();
					for (int i = 0; i < cookies.size(); i++) {
						// 这里是读取Cookie['PHPSESSID']的值存在静态变量中，保证每次都是同一个值
						if ("ci_session".equals(cookies.get(i).getName())) {
							LoginActivity.ci_session = cookies.get(i).getValue();
							break;
						}

					}

					if (!strResult.equals("Failed")) {
						Intent intent;
						intent = new Intent().setClass(MachineActivity.this,
								TaskActivity.class);
						intent.putExtra("strtasks", strResult);
						intent.putExtra("mid",mid);
						startActivity(intent);
					}
					
				} else {
					System.out.println("Response Error");
				}
			} catch (ClientProtocolException e) {
				System.out.println(e.getMessage().toString());
				e.printStackTrace();
			} catch (UnsupportedEncodingException e) {
				System.out.println(e.getMessage().toString());
				e.printStackTrace();
			} catch (IOException e) {
				Log.e(ACTIVITY_SERVICE, e.getMessage().toString());
				e.printStackTrace();
			}
		}

		/*
		 * Una vez terminado doInBackground segun lo que halla ocurrido pasamos
		 * a la sig. activity o mostramos error
		 */
		protected void onPostExecute(String result) {

			pDialog.dismiss();// ocultamos progess dialog.
			Log.e("onPostExecute=", "" + result);

			if (result.equals("ok")) {

			}
		}
	}

}
