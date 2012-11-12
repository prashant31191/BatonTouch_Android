package com.batontouch.managebaton;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.batontouch.R;
import com.batontouch.model.User;
import com.batontouch.utils.Global;

public class BatonManageClientAdapter extends ArrayAdapter<User> {
	private Context mContext;
	private int mResource;
	private ArrayList<User> mUsers;
	private LayoutInflater mInflater;
	private User user;
	private String task_id;

	private SharedPreferences mPreferences;
	private String auth_token;
	
	private int StatusCode;

	public BatonManageClientAdapter(Context context, int mResource,
			ArrayList<User> mUsers, String task_id) {
		super(context, mResource, mUsers);
		this.mContext = context;
		this.mResource = mResource;
		this.mUsers = mUsers;
		this.task_id = task_id;

		mPreferences = context.getSharedPreferences("CurrentUser",
				context.MODE_PRIVATE);
		auth_token = mPreferences.getString("AuthToken", "");
	}

	// 각 항목의 뷰 생성
	public View getView(final int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		user = mUsers.get(position);
		if (convertView == null) {
			this.mInflater = (LayoutInflater) mContext
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = mInflater.inflate(mResource, null);
			holder = new ViewHolder();

			holder.userImage = (ImageView) convertView
					.findViewById(R.id.userImage);
			holder.userName = (TextView) convertView
					.findViewById(R.id.userName);
			holder.selectBtn = (Button) convertView
					.findViewById(R.id.selectBtn);

			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		if (user != null) {
			// holder.userImage
			holder.userName.setText(user.getName());

			// 버튼 클릭했을때 select Action
			holder.selectBtn.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					user = mUsers.get(position);
					RunnerChoiceDialog(user.getTradestat_id());
				}
			});
		}

		convertView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				user = mUsers.get(position);

			}
		});

		return convertView;
	}

	class ViewHolder {
		ImageView userImage;
		TextView userName;
		Button selectBtn;
	}

	// 러너 선택 다이얼로그 method
	private void RunnerChoiceDialog(String tradestat_id) {
		final String params[] = new String [1];
		params[0] = tradestat_id;
		AlertDialog.Builder altBld = new AlertDialog.Builder(mContext);
		altBld.setMessage("이 러너를 선택하시겠습니까?").setCancelable(false)
				.setPositiveButton("넹", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						new SelectRunner().execute(params[0]);
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

	class SelectRunner extends AsyncTask<String, Void, Void> {
		@Override
		protected Void doInBackground(String... params) {
			try {
				String tradestat_id = params[0];
				HttpClient httpClient = new DefaultHttpClient();
				HttpPut putRequest = new HttpPut(Global.ServerUrl + "tasks/"
						+ task_id + "/selectclient?auth_token=" + auth_token);
				MultipartEntity reqEntity = new MultipartEntity(
						HttpMultipartMode.BROWSER_COMPATIBLE);
				
				reqEntity.addPart("tradestat_id", new StringBody(tradestat_id, Charset.forName("UTF-8")));
				
				putRequest.setEntity(reqEntity);
				putRequest.setHeader("Accept", Global.Acceptversion);
				putRequest.setHeader("Authorization", Global.AuthorizationToken);
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
				Log.e("batonmanage", e.getClass().getName() + e.getMessage()
						+ " Asynctask IOException SelectRunner");
			} catch (Exception e) {
				Log.e("batonmanage", e.getClass().getName() + e.getMessage()
						+ " Asynctask Exception SelectRunner");
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
		}
	}
}