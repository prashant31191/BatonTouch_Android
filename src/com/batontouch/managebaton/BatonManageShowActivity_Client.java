package com.batontouch.managebaton;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.batontouch.main.R;
import com.batontouch.model.Giftcon;
import com.batontouch.model.Task;
import com.batontouch.model.User;
import com.batontouch.utils.Global;
import com.batontouch.utils.ImageDownloader;
import com.batontouch.utils.NetHelper;
import com.google.gson.Gson;

public class BatonManageShowActivity_Client extends Activity {
	private TextView tvUsername, textClientStatus;
	private TextView tvName, tvStatus, tvDescription, tvResttime, tvToloc,
			tvFromloc, tvDate, giftcon_name;
	private ImageButton confirmBtn;
	private ImageView imUser, imCompany, giftcon_image;
	private SharedPreferences mPreferences;
	private String auth_token;
	private String task_id;
	private String mResult;
	private int StatusCode;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.batonmanage_managetasks_clientpage_progress);

		mPreferences = getSharedPreferences("CurrentUser", MODE_PRIVATE);
		auth_token = mPreferences.getString("AuthToken", "");

		Intent in = getIntent();
		task_id = in.getStringExtra("task_id");

		confirmBtn = (ImageButton) findViewById(R.id.confirmBtn);

		imUser = (ImageView) findViewById(R.id.user_image);

		tvUsername = (TextView) findViewById(R.id.user_name);

		tvName = (TextView) findViewById(R.id.deal_name);
		tvDate = (TextView)findViewById(R.id.deal_date);
		tvStatus = (TextView) findViewById(R.id.deal_status);
		tvDescription = (TextView) findViewById(R.id.deal_description);
		tvToloc = (TextView) findViewById(R.id.deal_toloc);
		tvFromloc = (TextView) findViewById(R.id.deal_fromloc);
		tvResttime = (TextView) findViewById(R.id.rest_time);
		
		giftcon_image = (ImageView)findViewById(R.id.giftcon_image);
		giftcon_name = (TextView)findViewById(R.id.giftcon_name);

		textClientStatus = (TextView) findViewById(R.id.textClientStatus);

		new GetBatonShow().execute();
	}

	public void ClientConfirmBtnClick(View v) {
		ClientConfirmDialog();
	}

	// 클라이언트 확인 다이얼로그
	private void ClientConfirmDialog() {
		AlertDialog.Builder altBld = new AlertDialog.Builder(this);
		altBld.setMessage("일을 완료하셨습니까?").setCancelable(false)
				.setPositiveButton("넹", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						new ClientConfirm().execute();
					}
				})
				.setNegativeButton("아뇨", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
					}
				});
		AlertDialog alert = altBld.create();
		alert.show();
	}
	
	private class GetBatonShow extends AsyncTask<Void, Void, Void> {
		@Override
		protected Void doInBackground(Void... params) {
			mResult = NetHelper.DownloadHtml(Global.ServerUrl + "tasks/"
					+ task_id + "?auth_token=" + auth_token);
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			Gson gson = new Gson();
			Task task = gson.fromJson(mResult, Task.class);
			User user = task.getUser();
			Giftcon giftcon = task.getGiftcon();
			try {
				int status = task.getStatus();
				String uimage = user.getProfile_image();
				String gimage = giftcon.getImage();
				// User
				tvUsername.setText(user.getName());
				if (uimage != null) {
					ImageDownloader.download(Global.ServerOriginalUrl + uimage, imUser);
				}else{
					imUser.setImageResource(R.drawable.ic_launcher);
				}
				
				giftcon_name.setText(giftcon.getName());
				if (gimage != null) {
					ImageDownloader.download(Global.ServerOriginalUrl + gimage, giftcon_image);
				}else{
					giftcon_image.setImageResource(R.drawable.ic_launcher);
				}
				// imUSer // Image Setting
				// Task
				tvName.setText(task.getName());
				tvDate.setText(task.getDay());
				tvStatus.setText(Global.userJudge(status));
				tvDescription.setText(task.getDescription());
				tvToloc.setText(task.getToloc());
				tvFromloc.setText(task.getFromloc());
				
//				tvResttime.setText(task.getEnddate());
				
				if (status == 2) {
					confirmBtn.setVisibility(View.GONE);
					textClientStatus.setText("유저의 승인을 기다리고 있습니다.");
				}
			} catch (Exception e) {
				Log.e("batonmanage",
						e.getClass().getName() + " " + e.getMessage()
								+ " BatonManageShow Gson Exception");
			}
			super.onPostExecute(result);
		}
	}

	private class ClientConfirm extends AsyncTask<Void, Void, Void> {
		@Override
		protected Void doInBackground(Void... params) {
			try {
				HttpClient httpClient = new DefaultHttpClient();
				HttpPut putRequest = new HttpPut(Global.ServerUrl + "tasks/" + task_id + "/clientcomplete?auth_token=" +auth_token);
				putRequest.setHeader("Accept", Global.Acceptversion);
				putRequest
						.setHeader("Authorization", Global.AuthorizationToken);
				HttpResponse response = httpClient.execute(putRequest);

				StatusCode = response.getStatusLine().getStatusCode();

				BufferedReader reader = new BufferedReader(
						new InputStreamReader(
								response.getEntity().getContent(), "UTF-8"));

				String sResponse;
				StringBuilder s = new StringBuilder();
				while ((sResponse = reader.readLine()) != null) {
					s = s.append(sResponse);
				}
				Log.d("batonmanage", "StatusCode : " + StatusCode + ", "
						+ "Response : " + s);
			} catch (IOException e) {
				Log.e("batonmange", e.getClass().getName() + e.getMessage()
						+ " Asynctask IOException ClientConfirm");
			} catch (Exception e) {
				Log.e("batonmange", e.getClass().getName() + e.getMessage()
						+ " Asynctask Exception ClientConfirm");
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			if (StatusCode == 200) {
				new GetBatonShow().execute();
			}
			super.onPostExecute(result);
		}
	}
}
