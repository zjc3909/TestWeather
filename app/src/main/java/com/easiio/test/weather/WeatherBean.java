package com.easiio.test.weather;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by gavin on 10/30/15.
 */
public class WeatherBean implements Parcelable {

    public String city;
    public String weather;
    public String temp;
    public String l_tmp;
    public String h_tmp;
    public String wd;
    public String ws;

    public int errNum;
    public String errMsg;

    public WeatherBean(){

    }


    @Override
    public String toString(){
        StringBuilder builder = new StringBuilder();
        builder.append("Result:").append(errMsg).append("\n")
                .append("City = ").append(city).append("\n")
                .append("Weather = ").append(weather).append("\n")
                .append("Temp = ").append(temp).append("\n")
                .append("Low Temp = ").append(l_tmp).append("\n")
                .append("High Temp = ").append(h_tmp).append("\n")
                .append("WD = ").append(wd).append("\n")
                .append("WS = ").append(ws).append("\n");

        return builder.toString();
    }

    private Object mLock = new Object();

    private WeatherBean(Parcel in){
        readFromParcel( in );
    }

    public void readFromParcel( Parcel in ){
        synchronized (mLock) {
            errNum = in.readInt();
            errMsg = in.readString();
            city = in.readString();
            weather = in.readString();
            temp = in.readString();
            l_tmp = in.readString();
            h_tmp = in.readString();
            wd = in.readString();
            ws = in.readString();
        }

    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        synchronized (mLock) {
            dest.writeInt(errNum);
            dest.writeString(errMsg);
            dest.writeString(city);
            dest.writeString(weather);
            dest.writeString(temp);
            dest.writeString(l_tmp);
            dest.writeString(h_tmp);
            dest.writeString(wd);
            dest.writeString(ws);
        }

    }


    public static final Creator<WeatherBean> CREATOR = new Creator<WeatherBean>() {

        public WeatherBean createFromParcel( Parcel in ){
            return new WeatherBean(in);
        }

        public WeatherBean[] newArray( int size){
            return new WeatherBean[size];
        }
    };

}
