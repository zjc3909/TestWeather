package com.easiio.test.weather;

import android.app.Service;
import android.content.Intent;
import android.os.DeadObjectException;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by gavin on 10/30/15.
 */
public class WeatherService extends Service{

    private static final String TAG = "[EASIIO]WeatherService";

    private HashMap<Integer, IWeatherServiceCallback> m_callback = new HashMap<Integer, IWeatherServiceCallback>();

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "onCreate");

    }

    @Override
    public IBinder onBind(Intent intent) {
        return new WeatherBinder();
    }

    private class WeatherBinder extends IWeatherInterface.Stub{

        @Override
        public void registerCallback(int hash, IWeatherServiceCallback callback) throws RemoteException {
            if (m_callback != null && !m_callback.containsKey(hash)){
                m_callback.put(hash, callback);
                Log.w(TAG, "Add callback hash = " + hash);
            }
        }

        @Override
        public void unregisterCallback(int hash) throws RemoteException {
            if (m_callback != null) {
                Iterator<Integer> it = m_callback.keySet().iterator();
                while (it.hasNext()) {
                    Integer i_hash = it.next();
                    if (i_hash.equals(hash)) {
                        it.remove();
                        break;
                    }
                }

                Log.i(TAG, "Removed callback: " + hash + " callbacks: " + m_callback.size());
            }
        }

        @Override
        public void startGetWeather(final String citypinyin) throws RemoteException {
            Log.w(TAG, "startGetWeather");
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {

                    WeatherBean weather = null;
                    String urlStr = "http://apistore.baidu.com/microservice/weather?citypinyin=" + citypinyin;
                    try{
                        URL url = new URL(urlStr);
                        URLConnection connection = url.openConnection();
                        connection.connect();
                        InputStream inputStream = connection.getInputStream();
                        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
                        StringBuffer sb = new StringBuffer();
                        String str = "";
                        while ((str = reader.readLine()) != null)
                        {
                            sb.append(str).append("\n");
                        }

                        str = sb.toString();
                        Log.w(TAG, "Get weather result = " + str);
                        weather = parseJsonForWeather(str);

                    } catch (Exception ex){
                        Log.e(TAG, "Open url failed, ex : " + ex.getLocalizedMessage());
                    }

                    if (weather == null){
                        weather = new WeatherBean();
                        weather.errMsg = "Weather is Null.";
                    }

                    callbackShowWeather(weather);

                }
            });

            thread.start();
        }
    }

    private void callbackShowWeather(WeatherBean weather){
        Set<Integer> clientsHash = null;
        if (m_callback != null) {
            clientsHash = m_callback.keySet();
        }

        if (clientsHash == null) {
            return;
        }

        for (Integer hash : clientsHash) {
            IWeatherServiceCallback callback = m_callback.get(hash);
            if (callback != null){
                try {
                    callback.showWeather(weather);
                } catch (DeadObjectException e_do) {
                    Log.w(TAG, "Callback removed. DeadObjectException: hash " + hash);
                    m_callback.remove(hash);
                } catch (RemoteException re) {
                    Log.w(TAG, "RemoteException:", re);
                }
            }

        }
    }

    private WeatherBean parseJsonForWeather(String str){
        if (TextUtils.isEmpty(str)){
            return null;
        }
        try{
            WeatherBean weather = new WeatherBean();
            JSONObject json = new JSONObject(str);
            weather.errNum = json.getInt("errNum");
            weather.errMsg = json.getString("errMsg");
            if (weather.errNum == 0){
                JSONObject dataJson = json.getJSONObject("retData");
                weather.city = dataJson.getString("city");
                weather.weather = dataJson.getString("weather");
                weather.temp = dataJson.getString("temp");
                weather.l_tmp = dataJson.getString("l_tmp");
                weather.h_tmp = dataJson.getString("h_tmp");
                weather.wd = dataJson.getString("WD");
                weather.ws = dataJson.getString("WS");
            }

            return weather;
        } catch (JSONException ex){
            Log.e(TAG, "parseJson failed : " + ex.getLocalizedMessage());
            return null;
        }

    }

}
