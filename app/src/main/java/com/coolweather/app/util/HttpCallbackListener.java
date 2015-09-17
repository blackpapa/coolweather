package com.coolweather.app.util;

/**
 * Created by andy on 2015/9/17.
 */
public interface HttpCallbackListener {

    void onFinish(String response);

    void onError(Exception e);

}

