// IWeatherServiceCallback.aidl
package com.easiio.test.weather;

import com.easiio.test.weather.WeatherBean;

interface IWeatherServiceCallback {

    void showWeather(in WeatherBean weather);

}
