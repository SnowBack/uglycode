package yalantis.com.sidemenu.sample;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.yolanda.nohttp.NoHttp;
import com.yolanda.nohttp.RequestMethod;
import com.yolanda.nohttp.rest.OnResponseListener;
import com.yolanda.nohttp.rest.Request;
import com.yolanda.nohttp.rest.Response;

import org.json.JSONObject;

import static yalantis.com.sidemenu.sample.NoHttpApplication.queue;

/**
 * Created by Xiangyu on 9/9/17.
 */

public class PlugDetail extends AppCompatActivity {
    String device_id = null;
    String title = null;
    String description = null;
    boolean post_tag = false;
    SmartPlug plug = null;
    private String server_ip = null;
    private String port = null;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plug_detail);
        Bundle bundle = this.getIntent().getExtras();
        device_id = bundle.getString("device_id");
        title = bundle.getString("title");
        description = bundle.getString("description");
        server_ip = getResources().getString(R.string.server_ip);
        port = getResources().getString(R.string.port);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!post_tag) {
            post_tag = true;
            plug = new SmartPlug();
            new Thread(runnable).start();
        }
    }
    Handler handler_plug = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bundle data = msg.getData();

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
                Toast.makeText(getApplicationContext(), "PLUG DATA get Success!", Toast.LENGTH_SHORT).show();

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
