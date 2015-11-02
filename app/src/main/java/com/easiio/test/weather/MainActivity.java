package com.easiio.test.weather;

import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.support.design.widget.Snackbar;
import android.support.v4.util.LogWriter;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "[EASIIO]MainActivity";

    private static final int MSG_WHAT_GET_SUCCESS = 0;

    private TextView mShowWeatherView;
    private EditText mCityPinyinET;

    private ProgressDialog mProgressDialog;

    private IWeatherInterface mIWeatherInterface;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate...");

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mShowWeatherView = (TextView) this.findViewById(R.id.weather_text_view);
        mCityPinyinET = (EditText) this.findViewById(R.id.city_pinyin_edittext);

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage("Loading...");
        this.findViewById(R.id.button_get_weather).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String pinyin = mCityPinyinET.getEditableText().toString();
                if (TextUtils.isEmpty(pinyin)){
                    Toast.makeText(MainActivity.this, "Not empty", Toast.LENGTH_SHORT).show();
                    return;
                }

                mProgressDialog.show();
                try {
                    if (mIWeatherInterface != null){
                        mIWeatherInterface.startGetWeather(pinyin);
                    }
                } catch (RemoteException ex){
                    Log.e(TAG, "Start get weather failed, ex : " + ex.getLocalizedMessage());
                }


            }
        });

        if(!bindService(new Intent(this, WeatherService.class), mServiceConnection, Context.BIND_AUTO_CREATE)){
            Toast.makeText(this, "Bind service failed.", Toast.LENGTH_SHORT);
            finish();
            return;
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mServiceConnection != null){
            this.unbindService(mServiceConnection);
        }
    }

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            try {
                mIWeatherInterface = IWeatherInterface.Stub.asInterface(iBinder);
                mIWeatherInterface.registerCallback(mIWeatherServiceCallback.hashCode(), mIWeatherServiceCallback);
            } catch (Exception ex){
                Log.e(TAG, "onServiceConnected failed : " + ex.getLocalizedMessage());
            }

        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            try {
                mIWeatherInterface.unregisterCallback(mIWeatherServiceCallback.hashCode());
            } catch (Exception ex){
                Log.e(TAG, "onServiceConnected failed : " + ex.getLocalizedMessage());
            }
        }
    };

    private IWeatherServiceCallback mIWeatherServiceCallback = new IWeatherServiceCallback.Stub () {
        @Override
        public void showWeather(WeatherBean weather) throws RemoteException {
            Log.i(TAG, "shoWeather : " + weather.toString());
            Message msg = mHandler.obtainMessage();
            msg.what = MSG_WHAT_GET_SUCCESS;
            msg.obj = weather;
            mHandler.sendMessage(msg);
        }

    };

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == MSG_WHAT_GET_SUCCESS){
                mProgressDialog.dismiss();
                if (msg.obj == null){
                    mShowWeatherView.setText("Weather is null.");
                    return;
                }
                WeatherBean weather = (WeatherBean) msg.obj;
                if (weather == null){
                    mShowWeatherView.setText("Weather is null.");
                    return;
                }
                mShowWeatherView.setText(weather.toString());
            }
        }
    };

}
