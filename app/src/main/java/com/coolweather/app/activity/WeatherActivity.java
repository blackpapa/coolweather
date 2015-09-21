package com.coolweather.app.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.coolweather.app.R;
import com.coolweather.app.service.AutoUpdateService;
import com.coolweather.app.util.HttpCallbackListener;
import com.coolweather.app.util.HttpUtil;
import com.coolweather.app.util.Utility;

/**
 * Created by andy on 2015/9/20.
 */
public class WeatherActivity extends Activity implements View.OnClickListener {
    private LinearLayout weatherInfoLayout;
    private TextView weatherDespText;
    private TextView cityName;
    private TextView temp1Text;
    private TextView temp2Text;
    private TextView currentDataText;
    private TextView publichText;
    private Button switchCity;
    private Button refreshWeather;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.weather_layout);
        weatherInfoLayout= (LinearLayout) findViewById(R.id.weather_info_layoout);
        weatherDespText= (TextView) findViewById(R.id.weather_desp);
        cityName= (TextView) findViewById(R.id.city_name);
        temp1Text= (TextView) findViewById(R.id.temp1);
        temp2Text= (TextView) findViewById(R.id.temp2);
        currentDataText= (TextView) findViewById(R.id.current_date);
        publichText= (TextView) findViewById(R.id.publish_text);
        switchCity= (Button) findViewById(R.id.switch_city);
        refreshWeather= (Button) findViewById(R.id.refresh_weather);
        String countryCode=getIntent().getStringExtra("country_code");
        if(!TextUtils.isEmpty(countryCode)){
            //有县级代号就去查询天气
            publichText.setText("同步中...");
            weatherInfoLayout.setVisibility(View.INVISIBLE);
            cityName.setVisibility(View.INVISIBLE);
            queryWeatherCode(countryCode);
        }else{
            //没县级代号就展示天气
            showWeather();
        }
        switchCity.setOnClickListener(this);
        refreshWeather.setOnClickListener(this);
    }
    public void onClick(View v) {
       switch (v.getId()){
           case R.id.switch_city:
               Intent intent=new Intent(this,ChooseAreaActivity.class);
               intent.putExtra("from_weather_activity", true);
               startActivity(intent);
               finish();
               break;
           case R.id.refresh_weather:
               publichText.setText("同步中...");
               SharedPreferences pres=PreferenceManager.getDefaultSharedPreferences(this);
               String weatherCode=pres.getString("weather_code","");
               if(!TextUtils.isEmpty(weatherCode)){
                   queryWeatherInfo(weatherCode);
               }
               break;
           default:break;
       }
    }

    /**
     * 查询县级代号所对应的天气代号。
     */
    private void queryWeatherCode(String countryCode){
        String address="http://www.weather.com.cn/data/list3/city"+countryCode+".xml";
        queryFromServer(address, "countryCode");
    }
    /**
     * 查询天气代号所对应的天气。
     */
    private void queryWeatherInfo(String weatherCode){
        String address = "http://www.weather.com.cn/data/cityinfo/" + weatherCode + ".html";
        queryFromServer(address, "weatherCode");

    }
    /**
     * 根据传入的地址和类型去向服务器查询天气代号或者天气信息。
     */
    private void queryFromServer(final String address,final String type){
        HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
            @Override
            public void onFinish(String response) {
                if("countryCode".equals(type)){
                    if(!TextUtils.isEmpty(response)){
                        String[]array=response.split("\\|");
                        if(array!=null&&array.length==2){
                            String weatherCode=array[1];
                            queryWeatherInfo(weatherCode);
                        }
                    }
                }else if("weatherCode".equals(type)){
                    Utility.handleWeatherResponse(WeatherActivity.this,response);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showWeather();
                        }
                    });
                }

            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        publichText.setText("同步失败");
                    }
                });
            }
        });

    }
    /**
     * 从SharedPreferences文件中读取存储的天气信息，并显示到界面上。
     */
    private void showWeather(){
        SharedPreferences prefs= PreferenceManager.getDefaultSharedPreferences(this);
        cityName.setText(prefs.getString("city_name",""));
        temp1Text.setText(prefs.getString("temp1",""));
        temp2Text.setText(prefs.getString("temp2",""));
        publichText.setText("今天"+prefs.getString("publish_time","")+"发布");
        weatherDespText.setText(prefs.getString("weather_desp",""));
        currentDataText.setText(prefs.getString("current_data", ""));
        cityName.setVisibility(View.VISIBLE);
        weatherInfoLayout.setVisibility(View.VISIBLE);
        Intent intent=new Intent(this, AutoUpdateService.class);
        startService(intent);
    }


}
