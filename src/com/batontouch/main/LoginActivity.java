package com.batontouch.main;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.batontouch.R;

public class LoginActivity extends Activity{
	private SharedPreferences mPreferences;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.loginpage);
		
		// SharedPreferences 에서 CurrentUser 설정 가져오기
		mPreferences = getSharedPreferences("CurrentUser", MODE_PRIVATE);
		
		if (!checkLoginInfo()) {
			
		}else{
			Intent intent = new Intent(getApplicationContext(), MainActivity.class);
			startActivity(intent);
			finish();
		}
	}
	
	public void FacebookClick(View v){
	}
	
	public void UserLoginClick(View v){
	}
	
	public void RegisterClick(View v){
		Intent intent = new Intent(getApplicationContext(), RegisterActivity.class);
		startActivity(intent);
	}
	
	public void mOnClick(View v){
		Toast.makeText(getApplicationContext(), "로그인 완료!", 3000).show();
		Intent intent = new Intent(getApplicationContext(), MainActivity.class);
		startActivity(intent);
	}
	
	private final boolean checkLoginInfo(){
		boolean authtoken_set = mPreferences.contains("AuthToken");
		if (authtoken_set) {
			return true;
		}
		return false;
	}

}
