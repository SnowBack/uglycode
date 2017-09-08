package yalantis.com.sidemenu.sample.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.yolanda.nohttp.NoHttp;
import com.yolanda.nohttp.RequestMethod;
import com.yolanda.nohttp.rest.OnResponseListener;
import com.yolanda.nohttp.rest.Request;
import com.yolanda.nohttp.rest.Response;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import yalantis.com.sidemenu.interfaces.ScreenShotable;
import yalantis.com.sidemenu.sample.LocationListener;
import yalantis.com.sidemenu.sample.MainActivity;

import yalantis.com.sidemenu.sample.PlugDetail;
import yalantis.com.sidemenu.sample.R;
import yalantis.com.sidemenu.sample.SmartPlug;

import static yalantis.com.sidemenu.sample.MainActivity.PAGE_TAG_PLUG;
import static yalantis.com.sidemenu.sample.MainActivity.PAGE_TAG_WEBVIEW;
import static yalantis.com.sidemenu.sample.MainActivity.handler_draw;
import static yalantis.com.sidemenu.sample.NoHttpApplication.queue;

/**
 * Created by Konstantin on 22.12.2014.
 */
public class ContentFragment extends Fragment implements ScreenShotable {
    public static final String CLOSE = "Close";
    public static final String WEB_PAGE = "Web Page";
    public static final String PLUG_LIST = "Plug List";
    public static final String ADD_PLUG = "Add Plug";

    private View containerView;
    protected ListView pluglist;
    protected int res;

    private Button add_device_btn;
    private Bitmap bitmap;
    private WebView mywebview;
    private WebSettings my_web_settings;
    private ArrayList<HashMap<String, String>> mylist;
    private String server_ip;
    private String port;
    private SmartPlug plug;
    private boolean post_tag;

    private RelativeLayout webviewlayout;
    private ProgressBar loading;
    public AMapLocationClient mLocationClient = null;
    //声明定位回调监听器

    private AMapLocationClientOption mLocationOption = null;

    private LocationListener mLocationListener = null;

    public static ContentFragment newInstance(int resId) {
        ContentFragment contentFragment = new ContentFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(Integer.class.getName(), resId);
        contentFragment.setArguments(bundle);
        return contentFragment;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (res == PAGE_TAG_PLUG)
            this.containerView = view.findViewById(R.id.container_pluglist);
        else if (res == PAGE_TAG_WEBVIEW)
            this.containerView = view.findViewById(R.id.container_webview);

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        res = getArguments().getInt(Integer.class.getName());
        post_tag = false;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = null;
        if (res == PAGE_TAG_PLUG) {
            rootView = inflater.inflate(R.layout.fragment_pluglist, container, false);

            pluglist = (ListView) rootView.findViewById(R.id.pluglist);
            add_device_btn = (Button) rootView.findViewById(R.id.add_device);
            add_device_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });

            mylist = new ArrayList<HashMap<String, String>>();
            HashMap<String, String> map = new HashMap<String, String>();
            map.put("TaskId", "1");
            map.put("ItemTitle", "plug 1");
            map.put("ItemText", "Using for Fan");
            mylist.add(map);
            HashMap<String, String> map2 = new HashMap<String, String>();
            map2.put("TaskId", "2");
            map2.put("ItemTitle", "plug 2");
            map2.put("ItemText", "Using for Air condition");
            mylist.add(map2);
            SimpleAdapter mSchedule = new SimpleAdapter(rootView.getContext(), //没什么解释
                    mylist,//数据来源
                    R.layout.list_item_template,//ListItem的XML实现
                    //动态数组与ListItem对应的子项
                    new String[] {"TaskId", "ItemTitle", "ItemText"},
                    //ListItem的XML文件里面的两个TextView ID
                    new int[] {R.id.device_id, R.id.ItemTitle, R.id.ItemText});
            pluglist.setAdapter(mSchedule);
            pluglist.addFooterView(new ViewStub(rootView.getContext()));
            pluglist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    String device_id = ((TextView)view.findViewById(R.id.device_id)).getText().toString();
                    String title = ((TextView)view.findViewById(R.id.ItemTitle)).getText().toString();
                    String description = ((TextView)view.findViewById(R.id.ItemText)).getText().toString();
                    Bundle bundle = new Bundle();
                    bundle.putString("device_id", device_id);
                    bundle.putString("title", title);
                    bundle.putString("description", description);
                    Intent intent =new Intent();
                    intent.putExtras(bundle);
                    intent.setClass(getContext(), PlugDetail.class);
                    startActivity(intent);
                }
            });
            pluglist.setVisibility(View.VISIBLE);

        } else if (res == PAGE_TAG_WEBVIEW) {
            rootView = inflater.inflate(R.layout.fragment_main, container, false);
            mywebview = (WebView) rootView.findViewById(R.id.mywebview);
            webviewlayout = (RelativeLayout) rootView.findViewById(R.id.container_webview);
            loading = (ProgressBar) rootView.findViewById(R.id.bar_Loading);
            server_ip = getResources().getString(R.string.server_ip);
            port = getResources().getString(R.string.port);
            post_tag = false;
            my_web_settings = mywebview.getSettings();
            my_web_settings.setAppCacheEnabled(true);
            my_web_settings.setDomStorageEnabled(true);
            my_web_settings.setJavaScriptEnabled(true);
            mywebview.loadData("", "text/html", "UTF-8");
//        my_webview.loadUrl("http://www.baidu.com");
            mywebview.loadUrl("http://" + server_ip + ":" + port);
            mywebview.setWebViewClient(new WebViewClient() {
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    view.loadUrl(url);
                    return true;
                }
            });

            mywebview.setWebChromeClient(new WebChromeClient() {
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
            mywebview.setWebViewClient(new WebViewClient() {
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

            mLocationListener = new LocationListener(server_ip, port, MainActivity.myGetContext());

            //初始化定位
            mLocationClient = new AMapLocationClient(MainActivity.myGetContext());
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

            //设置是否允许模拟位置,默认为false，不允许模拟位置
            mLocationOption.setMockEnable(false);

            //给定位客户端对象设置定位参数
            mLocationClient.setLocationOption(mLocationOption);
            //启动定位
            mLocationClient.startLocation();

            mywebview.setVisibility(View.VISIBLE);
        }
        return rootView;
    }

    public void onStart(){
        super.onStart();
//        if (!post_tag) {
//            post_tag = true;
//            plug = new SmartPlug();
//            new Thread(runnable).start();
//        }
    }

    @Override
    public void takeScreenShot() {
        Thread thread = new Thread() {
            @Override
            public void run() {
                handler_draw.post(runnable_draw);
            }
        };
        thread.start();
    }

    @Override
    public Bitmap getBitmap() {
        return bitmap;
    }

    Runnable runnable_draw = new Runnable(){
        public void run(){
            Bitmap bitmap = Bitmap.createBitmap(containerView.getWidth(),
                    containerView.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            containerView.draw(canvas);
            ContentFragment.this.bitmap = bitmap;
        }
    };
    Handler handler_loop = new Handler();
//    Handler handler_plug = new Handler() {
//        @Override
//        public void handleMessage(Message msg) {
//            super.handleMessage(msg);
//            Bundle data = msg.getData();
//
//            String power = data.getString("power");
//            String sysinfo = data.getString("sysinfo");
//            String time = data.getString("time");
//            String timezone = data.getString("timezone");
//            String mon_detail = data.getString("mon_detail");
//            String year_detail = data.getString("year_detail");
//            try {
//                JSONObject power_json_obj = new JSONObject(power);
//                JSONObject power_obj = power_json_obj.getJSONObject("emeter");
//                JSONObject power_value = power_obj.getJSONObject("get_realtime");
//
//                JSONObject sysinfo_json_obj = new JSONObject(sysinfo);
//                JSONObject sysinfo_obj = sysinfo_json_obj.getJSONObject("system");
//                JSONObject sysinfo_value = sysinfo_obj.getJSONObject("get_sysinfo");
//
//                JSONObject time_json_obj = new JSONObject(time);
//                JSONObject time_obj = time_json_obj.getJSONObject("time");
//                JSONObject time_value = time_obj.getJSONObject("get_time");
//
//                JSONObject timezone_json_obj = new JSONObject(timezone);
//                JSONObject timezone_obj = timezone_json_obj.getJSONObject("time");
//                JSONObject timezone_value = timezone_obj.
//                        getJSONObject("get_timezone");
//
//                JSONObject mon_detail_json_obj = new JSONObject(mon_detail);
//                JSONObject mon_detail_obj = mon_detail_json_obj.getJSONObject("emeter");
//                JSONObject mon_detail_value = mon_detail_obj.getJSONObject("get_daystat");
//
//                JSONObject year_detail_json_obj = new JSONObject(year_detail);
//                JSONObject year_detail_obj = year_detail_json_obj.getJSONObject("emeter");
//                JSONObject year_detail_value = year_detail_obj.getJSONObject("get_monthstat");
//
//                JSONObject upload_json = new JSONObject();
//                upload_json.put("device_id", "1");//disgrace
//                upload_json.put("power_current", power_value);
//                upload_json.put("sysinfo", sysinfo_value);
//                upload_json.put("time", time_value);
//                upload_json.put("timezone", timezone_value);
//                upload_json.put("mon_detail", mon_detail_value);
//                upload_json.put("year_detail", year_detail_value);
//                Toast.makeText(MainActivity.myGetContext(), "PLUG DATA get Success!", Toast.LENGTH_SHORT).show();
//
//                Request<JSONObject> request =
//                        NoHttp.createJsonObjectRequest("http://" + server_ip + ":" + port + "/upload", RequestMethod.POST);
//                request.setDefineRequestBodyForJson(upload_json);
//                queue.add(0, request, new OnResponseListener<JSONObject>() {
//                    @Override
//                    public void onStart(int what) {
//
//                    }
//
//                    @Override
//                    public void onSucceed(int what, Response<JSONObject> response) {
//                        Toast.makeText(MainActivity.myGetContext(), "Post JSON Success", Toast.LENGTH_SHORT).show();
//                    }
//
//                    @Override
//                    public void onFailed(int what, Response<JSONObject> response) {
//                        Toast.makeText(MainActivity.myGetContext(), "Post JSON Fail", Toast.LENGTH_SHORT).show();
//                    }
//
//                    @Override
//                    public void onFinish(int what) {
//
//                    }
//                });
//                Log.i("JSON upload", upload_json.toString());
//
//            } catch (Exception e) {
//                e.printStackTrace();
//                Toast.makeText(MainActivity.myGetContext(), "json fail in handler2 of MyWebView!", Toast.LENGTH_SHORT).show();
//
//                Log.e("json fail", "json fail in handler2 of MyWebView");
//            }
//
//        }
//    };
//
//    Runnable runnable = new Runnable() {
//        @Override
//        public void run() {
//            while (true) {
//                try {
//                    Thread.sleep(10000);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//
//                Message msg = new Message();
//                Bundle data = new Bundle();
//                String time = plug.getTime();
//                String mon_detail = null;
//                String year_detail = null;
//                try {
//                    JSONObject time_json_obj = new JSONObject(time);
//                    JSONObject time_obj = time_json_obj.getJSONObject("time").getJSONObject("get_time");
//                    int year = time_obj.getInt("year");
//                    Log.e("year", String.valueOf(year));
//                    int mon = time_obj.getInt("month");
//                    Log.e("month", String.valueOf(mon));
//                    mon_detail = plug.getConsuptionforMonth(
//                            String.valueOf(year), String.valueOf(mon));
//                    year_detail = plug.getConsuptionforYear(
//                            String.valueOf(year));
//                } catch (Exception e) {
//                    // TODO: handle exception
//                    e.printStackTrace();
//                    mon_detail = plug.getConsuptionforMonth("2017", "8");
//                    year_detail = plug.getConsuptionforYear("2017");
//                }
//
//                String power = plug.getPower();
//                String sysinfo = plug.getSysInfo();
//
//                String timezone = plug.getTimeZone();
//                data.putString("mon_detail", mon_detail);
//                data.putString("year_detail", year_detail);
//                data.putString("power", power);
//                data.putString("sysinfo", sysinfo);
//                data.putString("time", time);
//                data.putString("timezone", timezone);
//
//                msg.setData(data);
//                handler_plug.sendMessage(msg);
//            }
//        }
//    };

}

