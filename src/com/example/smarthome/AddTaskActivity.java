package com.example.smarthome;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

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

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.os.Vibrator;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class AddTaskActivity extends Activity {

	EditText titleEdit;
	EditText runtimeEdit;
	EditText annotationEdit;
	EditText parametersEdit;

	Button btn_ok;
	Button btn_cancel;

	String URL_connect = LoginActivity.IP_Server + "/smarthome/task/add_task";

	String mid;
	String strtasks;
	boolean result_back;
	private ProgressDialog pDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_task);

		Intent intent = this.getIntent();
		mid = intent.getStringExtra("mid");
		strtasks=intent.getStringExtra(("strtasks"));

		titleEdit = (EditText) findViewById(R.id.titleEdit);
		runtimeEdit = (EditText) findViewById(R.id.runtimeEdit);
		annotationEdit = (EditText) findViewById(R.id.annotationEdit);
		parametersEdit = (EditText) findViewById(R.id.parametersEdit);

		btn_ok = (Button) findViewById(R.id.btn_ok);
		btn_cancel = (Button) findViewById(R.id.btn_cancel);

		btn_ok.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				// Extreamos datos de los EditText
				String str_titleEdit = titleEdit.getText().toString();
				String str_runtimeEdit = runtimeEdit.getText().toString();
				String str_annotationEdit = annotationEdit.getText().toString();
				String str_parametersEdit = parametersEdit.getText().toString();

				new asynclogin().execute(str_titleEdit, str_runtimeEdit,
						str_annotationEdit, str_parametersEdit, mid);
			}
		});
		btn_cancel.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				Intent intenta;
				intenta = new Intent().setClass(AddTaskActivity.this,
						TaskActivity.class);
				intenta.putExtra("strtasks", strtasks);
				intenta.putExtra("mid", mid);
				startActivity(intenta);
			}
		});
	}

	class asynclogin extends AsyncTask<String, String, String> {
		String title, runtime, annotation, parameter, mid;
		protected void onPreExecute() {
			// para el progress dialog
			pDialog = new ProgressDialog(AddTaskActivity.this);
			pDialog.setMessage("Adding Task....");
			pDialog.setIndeterminate(false);
			pDialog.setCancelable(false);
			pDialog.show();
		}

		protected String doInBackground(String... params) {
			// obtnemos usr y pass
			title = params[0];
			runtime = params[1];
			annotation = params[2];
			parameter = params[3];
			mid = params[4];
			executeRequest();
			
			return "ok";
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
			parameters.add(new BasicNameValuePair("title", title));
			parameters.add(new BasicNameValuePair("time", runtime));
			parameters.add(new BasicNameValuePair("annotation", annotation));
			parameters.add(new BasicNameValuePair("params", parameter));
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
						intent = new Intent().setClass(AddTaskActivity.this,
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
