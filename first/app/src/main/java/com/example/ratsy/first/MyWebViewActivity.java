package com.example.ratsy.first;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.menu.ExpandedMenuView;
import android.util.Log;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.yolanda.nohttp.NoHttp;
import com.yolanda.nohttp.RequestMethod;
import com.yolanda.nohttp.rest.OnResponseListener;
import com.yolanda.nohttp.rest.Request;
import com.yolanda.nohttp.rest.Response;

import org.json.JSONException;
import org.json.JSONObject;

import static com.example.ratsy.first.NoHttpApplication.queue;

/**
 * Created by Xiangyu on 8/31/17.
 */
class LocationListener implements AMapLocationListener {
    String server_ip = null;
    String port = null;
    Context context = null;
    SharedPreferences preferences = null;
    MyWebViewActivity test = null;

    LocationListener(String si, String po, Context ct, SharedPreferences preferences, MyWebViewActivity test) {
        this.server_ip = si;
        this.port = po;
        this.context = ct;
        this.preferences = preferences;
        this.test = test;
//        queue = NoHttp.newRequestQueue(1);
    }

    @Override
    public void onLocationChanged(AMapLocation amapLocation) {

        if (amapLocation != null) {
            if (amapLocation.getErrorCode() == 0) {
                //解析amapLocation获取相应内容。
                Log.d("AmapSuccess", amapLocation.getLocationDetail());
                int a = amapLocation.getLocationType();//获取当前定位结果来源，如网络定位结果，详见定位类型表
                Log.d("Amap Type", String.valueOf(a));
                double b = amapLocation.getLatitude();//获取纬度
                double c = amapLocation.getLongitude();//获取经度
                Log.d("Amap weidu", String.valueOf(b));
                Log.d("Amap jiangdu", String.valueOf(c));
                Request<JSONObject> location =
                        NoHttp.createJsonObjectRequest("http://" + server_ip + ":" + port + "/location", RequestMethod.POST);
                JSONObject location_post = new JSONObject();
                try {
                    location_post.put("user_id", "1");
                    location_post.put("longitude", c);
                    location_post.put("latitude", b);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                location.setDefineRequestBodyForJson(location_post);
                queue.add(1, location, new OnResponseListener<JSONObject>() {
                    @Override
                    public void onStart(int what) {
                        Log.d("http", "start");
                    }

                    @Override
                    public void onSucceed(int what, Response<JSONObject> response) {
                        try {

                            String status = response.get().get("loc_status").toString();
                            Log.d("http", status);
                            if ("true".equals(status.toLowerCase())) {
                                Log.d("http Succeed", "111");
                            } else Log.d("http Succeed", "222");
                        } catch (JSONException e) {
                            e.getCause();
                        }
                    }

                    @Override
                    public void onFailed(int what, Response<JSONObject> response) {
                        Log.d("http onFail", response.toString());
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.clear();
                        editor.apply();
                        Toast.makeText(test.getApplicationContext(), "Post Geoinfo fail!", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFinish(int what) {
                        Log.d("http on Finish", "finish");
                    }
                });
                Log.d("http??", "http");

            } else {
                //定位失败时，可通过ErrCode（错误码）信息来确定失败的原因，errInfo是错误信息，详见错误码表。
                Log.e("AmapError", "location Error, ErrCode:"
                        + amapLocation.getErrorCode() + ", errInfo:"
                        + amapLocation.getErrorInfo());
            }
        }
    }
}

public class MyWebViewActivity extends CheckPermissionsActivity {
    private RelativeLayout webviewlayout;
    private WebView my_webview;
    private WebSettings mWebSettings;
    private ProgressBar loading;
    //    private Button post_data;
    private SmartPlug plug;
    private String server_ip;
    private String port;
    private boolean post_tag;
    public AMapLocationClient mLocationClient = null;
    //声明定位回调监听器

    private AMapLocationClientOption mLocationOption = null;

    private LocationListener mLocationListener = null;

    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);
        webviewlayout = (RelativeLayout) findViewById(R.id.webviewlayout);
        my_webview = (WebView) findViewById(R.id.my_webview);
        loading = (ProgressBar) findViewById(R.id.bar_Loading);
//        post_data = (Button) findViewById(R.id.post_data);
        plug = new SmartPlug();
        post_tag = false;
        server_ip = getResources().getString(R.string.server_ip);
        port = getResources().getString(R.string.port);
        mWebSettings = my_webview.getSettings();
        mWebSettings.setAppCacheEnabled(true);
        mWebSettings.setDomStorageEnabled(true);
        mWebSettings.setJavaScriptEnabled(true);
        my_webview.loadData("", "text/html", "UTF-8");
        my_webview.loadUrl("http://www.baidu.com");
//        my_webview.loadUrl("http://" + server_ip + ":" + port);
        my_webview.setWebViewClient(new WebViewClient() {
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });
        my_webview.setWebChromeClient(new WebChromeClient() {
            //获取加载进度
            public void onProgressChanged(WebView view, int newProgress) {
                if (newProgress < 100) {
                    loading.setProgress(newProgress);
                } else if (newProgress == 100) {
                    webviewlayout.removeView(loading);
                }
            }
        });
        //设置WebViewClient类
        my_webview.setWebViewClient(new WebViewClient() {
            //设置加载前的函数
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
//                beginLoading.setText("开始加载了");

            }

            //设置结束加载函数
            @Override
            public void onPageFinished(WebView view, String url) {
//                endLoading.setText("结束加载了");
            }
        });

        SharedPreferences preferences = getSharedPreferences("login_status_setting", Context.MODE_PRIVATE);

        mLocationListener = new LocationListener(server_ip, port, this, preferences, MyWebViewActivity.this);

        //初始化定位
        mLocationClient = new AMapLocationClient(getApplicationContext());
        //设置定位回调监听
        mLocationClient.setLocationListener(mLocationListener);
        // 初始化AMapLocationClientOption对象
        mLocationOption = new AMapLocationClientOption();

        // 设置定位模式为AMapLocationMode.Hight_Accuracy，高精度模式。
        mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);


        // 获取最近3s内精度最高的一次定位结果：
        // 设置setOnceLocationLatest(boolean b)接口为true，
        // 启动定位时SDK会返回最近3s内精度最高的一次定位结果。
        // 如果设置其为true，setOnceLocation(boolean b)接口也会被设置为true，反之不会，默认为false。
        mLocationOption.setOnceLocationLatest(true);

        // 设置是否返回地址信息（默认返回地址信息）
        mLocationOption.setNeedAddress(true);

        // 设置是否强制刷新WIFI，默认为true，强制刷新。
        // mLocationOption.setWifiActiveScan(false);

        //设置是否允许模拟位置,默认为false，不允许模拟位置
        mLocationOption.setMockEnable(false);

        // 缓存机制默认开启
        // mLocationOption.setLocationCacheEnable(false);


        //给定位客户端对象设置定位参数
        mLocationClient.setLocationOption(mLocationOption);
        //启动定位
        mLocationClient.startLocation();

    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!post_tag) {
            post_tag = true;
            new Thread(runnable).start();
        }
    }

    Handler handler_loop = new Handler();
    Handler handler_plug = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bundle data = msg.getData();
            Toast.makeText(getApplicationContext(), "PLUG DATA get Success!", Toast.LENGTH_SHORT).show();

            String power = data.getString("power");
            String sysinfo = data.getString("sysinfo");
            String time = data.getString("time");
            String timezone = data.getString("timezone");
            String mon_detail = data.getString("mon_detail");
            String year_detail = data.getString("year_detail");
            try {
                JSONObject power_json_obj = new JSONObject(power);
                JSONObject power_obj = power_json_obj.getJSONObject("emeter");
                JSONObject power_value = power_obj.getJSONObject("get_realtime");

                JSONObject sysinfo_json_obj = new JSONObject(sysinfo);
                JSONObject sysinfo_obj = sysinfo_json_obj.getJSONObject("system");
                JSONObject sysinfo_value = sysinfo_obj.getJSONObject("get_sysinfo");

                JSONObject time_json_obj = new JSONObject(time);
                JSONObject time_obj = time_json_obj.getJSONObject("time");
                JSONObject time_value = time_obj.getJSONObject("get_time");

                JSONObject timezone_json_obj = new JSONObject(timezone);
                JSONObject timezone_obj = timezone_json_obj.getJSONObject("time");
                JSONObject timezone_value = timezone_obj.
                        getJSONObject("get_timezone");

                JSONObject mon_detail_json_obj = new JSONObject(mon_detail);
                JSONObject mon_detail_obj = mon_detail_json_obj.getJSONObject("emeter");
                JSONObject mon_detail_value = mon_detail_obj.getJSONObject("get_daystat");

                JSONObject year_detail_json_obj = new JSONObject(year_detail);
                JSONObject year_detail_obj = year_detail_json_obj.getJSONObject("emeter");
                JSONObject year_detail_value = year_detail_obj.getJSONObject("get_monthstat");

                JSONObject upload_json = new JSONObject();
                upload_json.put("device_id", "1");//disgrace
                upload_json.put("power_current", power_value);
                upload_json.put("sysinfo", sysinfo_value);
                upload_json.put("time", time_value);
                upload_json.put("timezone", timezone_value);
                upload_json.put("mon_detail", mon_detail_value);
                upload_json.put("year_detail", year_detail_value);

                Request<JSONObject> request =
                        NoHttp.createJsonObjectRequest("http://" + server_ip + ":" + port + "/upload", RequestMethod.POST);
                request.setDefineRequestBodyForJson(upload_json);
                queue.add(0, request, new OnResponseListener<JSONObject>() {
                    @Override
                    public void onStart(int what) {

                    }

                    @Override
                    public void onSucceed(int what, Response<JSONObject> response) {
                        Toast.makeText(getApplicationContext(), "Post JSON Success", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailed(int what, Response<JSONObject> response) {
                        Toast.makeText(getApplicationContext(), "Post JSON Fail", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFinish(int what) {

                    }
                });
                Log.i("JSON upload", upload_json.toString());

            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), "json fail in handler2 of MyWebView!", Toast.LENGTH_SHORT).show();

                Log.e("json fail", "json fail in handler2 of MyWebView");
            }

        }
    };
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            while (true) {
                try {
                    Thread.sleep(10000);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                Message msg = new Message();
                Bundle data = new Bundle();
                String time = plug.getTime();
                String mon_detail = null;
                String year_detail = null;
                try {
                    JSONObject time_json_obj = new JSONObject(time);
                    JSONObject time_obj = time_json_obj.getJSONObject("time").getJSONObject("get_time");
                    int year = time_obj.getInt("year");
                    Log.e("year", String.valueOf(year));
                    int mon = time_obj.getInt("month");
                    Log.e("month", String.valueOf(mon));
                    mon_detail = plug.getConsuptionforMonth(
                            String.valueOf(year), String.valueOf(mon));
                    year_detail = plug.getConsuptionforYear(
                            String.valueOf(year));
                } catch (Exception e) {
                    // TODO: handle exception
                    e.printStackTrace();
                    mon_detail = plug.getConsuptionforMonth("2017", "8");
                    year_detail = plug.getConsuptionforYear("2017");
                }

                String power = plug.getPower();
                String sysinfo = plug.getSysInfo();

                String timezone = plug.getTimeZone();
                data.putString("mon_detail", mon_detail);
                data.putString("year_detail", year_detail);
                data.putString("power", power);
                data.putString("sysinfo", sysinfo);
                data.putString("time", time);
                data.putString("timezone", timezone);

                msg.setData(data);
                handler_plug.sendMessage(msg);
            }
        }
    };

}
