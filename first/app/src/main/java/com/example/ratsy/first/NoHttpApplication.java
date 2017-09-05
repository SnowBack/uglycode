package com.example.ratsy.first;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.yolanda.nohttp.Logger;
import com.yolanda.nohttp.NoHttp;
import com.yolanda.nohttp.cookie.DBCookieStore;
import com.yolanda.nohttp.rest.RequestQueue;
import com.yolanda.nohttp.tools.HeaderUtil;

import java.net.HttpCookie;
import java.net.URI;

/**
 * Created by ratsy on 2017/2/20.
 */

public class NoHttpApplication extends Application {
    static RequestQueue queue;
    @Override
    public void onCreate() {
        super.onCreate();
        NoHttp.initialize(this,new NoHttp.Config()
                .setCookieStore(new DBCookieStore(this).setCookieStoreListener(mListener)));
        Logger.setDebug(true);
        Logger.setTag("NoHttpSample");

        queue = NoHttp.newRequestQueue(1);
    };
    /**
     * Cookie管理监听。
     */
    private DBCookieStore.CookieStoreListener mListener = new DBCookieStore.CookieStoreListener() {
        @Override
        public void onSaveCookie(URI uri, HttpCookie cookie) { // Cookie被保存时被调用。
            // 1. 判断这个被保存的Cookie是我们服务器下发的Session。
            // 2. 这里的JSessionId是Session的name，
            //    比如java的是JSessionId，PHP的是PSessionId，
            //    当然这里只是举例，实际java中和php不一定是这个，具体要咨询你们服务器开发人员。
            if("sessionid".equals(cookie.getName())) {
                // 设置有效期为最大。
                cookie.setMaxAge(HeaderUtil.getMaxExpiryMillis());
            }
            Log.d("cookie name", cookie.getName());
        }

        @Override
        public void onRemoveCookie(URI uri, HttpCookie cookie) {// Cookie被移除时被调用。
            SharedPreferences preferences = getSharedPreferences("login_status_setting", Context.MODE_PRIVATE);
            SharedPreferences.Editor edit = preferences.edit();
            edit.clear();
            edit.apply();
        }
    };
}
