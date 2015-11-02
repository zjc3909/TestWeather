// IWeatherInterface.aidl
package com.easiio.test.weather;

import com.easiio.test.weather.IWeatherServiceCallback;


// Declare any non-default types here with import statements

interface IWeatherInterface {

    void registerCallback(int hash, IWeatherServiceCallback callback);
    void unregisterCallback(int hash);
    void startGetWeather(String citypinyin);

}
