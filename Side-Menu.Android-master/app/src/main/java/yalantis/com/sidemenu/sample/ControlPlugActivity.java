package yalantis.com.sidemenu.sample;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.yolanda.nohttp.NoHttp;
import com.yolanda.nohttp.RequestMethod;
import com.yolanda.nohttp.rest.OnResponseListener;
import com.yolanda.nohttp.rest.Request;
import com.yolanda.nohttp.rest.Response;

import org.json.JSONObject;

import static yalantis.com.sidemenu.sample.NoHttpApplication.queue;
import static yalantis.com.sidemenu.sample.R.string.port;
import static yalantis.com.sidemenu.sample.R.string.server_ip;

/**
 * Created by Xiangyu on 8/29/17.
 */

public class ControlPlugActivity extends AppCompatActivity {

    private Button get_power;
    private Button get_sysinfo;
    private Button get_time;
    private Button get_timezone;
    private Button mon_detail;
    private Button year_detail;

    private Button ctl_led_on;
    private Button ctl_led_off;

    private Button upload_all;

    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control);
        get_power = (Button) findViewById(R.id.get_power);
        get_sysinfo = (Button) findViewById(R.id.get_sysinfo);
        get_time = (Button) findViewById(R.id.get_time);
        get_timezone = (Button) findViewById(R.id.get_timezone);
        mon_detail = (Button) findViewById(R.id.mon_detail);
        year_detail = (Button) findViewById(R.id.year_detail);

        ctl_led_on = (Button) findViewById(R.id.ctl_led_on);
        ctl_led_off = (Button) findViewById(R.id.ctl_led_off);

        upload_all = (Button) findViewById(R.id.upload_all);


        final SmartPlug plug = new SmartPlug();
        get_power.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Message msg = new Message();
                        Bundle data = new Bundle();
                        String a = plug.getPower();
                        data.putString("value", a);
                        msg.setData(data);
                        handler.sendMessage(msg);
                    }
                }).start();
            }
        });
        get_sysinfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Message msg = new Message();
                        Bundle data = new Bundle();
                        String a = plug.getSysInfo();
                        data.putString("value", a);
                        msg.setData(data);
                        handler.sendMessage(msg);
                    }
                }).start();
            }
        });
        get_time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Message msg = new Message();
                        Bundle data = new Bundle();
                        String a = plug.getTime();
                        data.putString("value", a);
                        msg.setData(data);
                        handler.sendMessage(msg);
                    }
                }).start();
            }
        });
        get_timezone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Message msg = new Message();
                        Bundle data = new Bundle();
                        String a = plug.getTimeZone();
                        data.putString("value", a);
                        msg.setData(data);
                        handler.sendMessage(msg);
                    }
                }).start();
            }
        });
        mon_detail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Message msg = new Message();
                        Bundle data = new Bundle();
                        String a = plug.getConsuptionforMonth("2017", "8");
                        data.putString("value", a);
                        msg.setData(data);
                        handler.sendMessage(msg);
                    }
                }).start();
            }
        });
        year_detail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Message msg = new Message();
                        Bundle data = new Bundle();
                        String a = plug.getConsuptionforYear("2017");
                        data.putString("value", a);
                        msg.setData(data);
                        handler.sendMessage(msg);
                    }
                }).start();
            }
        });

        ctl_led_on.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Message msg = new Message();
                        Bundle data = new Bundle();
                        String a = plug.LedOn();
                        data.putString("value", a);
                        msg.setData(data);
                        handler.sendMessage(msg);
                    }
                }).start();

            }
        });
        ctl_led_off.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Message msg = new Message();
                        Bundle data = new Bundle();
                        String a = plug.LedOff();
                        data.putString("value", a);
                        msg.setData(data);
                        handler.sendMessage(msg);
                    }
                }).start();
            }
        });

        upload_all.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        while (true) {
                            try{
                            Thread.sleep(5000);}
                            catch (Exception e){
                                e.printStackTrace();
                            }
                            Message msg = new Message();
                            Bundle data = new Bundle();
                            String time = plug.getTime();
                            String mon_detail = null;
                            String year_detail = null;
                            try {
                                JSONObject time_json_obj = new JSONObject(time);
                                JSONObject time_obj = time_json_obj.getJSONObject("get_time");
                                int year = time_obj.getInt("year");
                                Log.i("year", String.valueOf(year));
                                int mon = time_obj.getInt("month");
                                mon_detail = plug.getConsuptionforMonth(
                                        String.valueOf(year), String.valueOf(mon));
                                year_detail = plug.getConsuptionforYear(
                                        String.valueOf(year));
                            } catch (Exception e) {
                                // TODO: handle exception
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
                            handler2.sendMessage(msg);
                        }
                    }
                }).start();
            }
        });

    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bundle data = msg.getData();
            String val = data.getString("value");
            Log.i("mylog", "请求结果为-->" + val);
            Toast.makeText(getApplicationContext(), val, Toast.LENGTH_SHORT).show();
        }
    };
    Handler handler2 = new Handler() {
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
                upload_json.put("power_current", power_value);
                upload_json.put("sysinfo", sysinfo_value);
                upload_json.put("time", time_value);
                upload_json.put("timezone", timezone_value);
                upload_json.put("mon_detail", mon_detail_value);
                upload_json.put("year_detail", year_detail_value);

//                Request<JSONObject> request =
//                        NoHttp.createJsonObjectRequest("http://" + server_ip + ":" + port + "/", RequestMethod.POST);
//                request.setDefineRequestBodyForJson(upload_json);
//                queue.add(0, request, new OnResponseListener<JSONObject>() {
//                    @Override
//                    public void onStart(int what) {
//
//                    }
//
//                    @Override
//                    public void onSucceed(int what, Response<JSONObject> response) {
//                        Toast.makeText(getApplicationContext(), "Post Success", Toast.LENGTH_SHORT).show();
//                    }
//
//                    @Override
//                    public void onFailed(int what, Response<JSONObject> response) {
//                        Toast.makeText(getApplicationContext(), "Post Fail", Toast.LENGTH_SHORT).show();
//                    }
//
//                    @Override
//                    public void onFinish(int what) {
//
//                    }
//                });
//                Log.i("JSON upload", upload_json.toString());
            } catch (Exception e) {
                e.printStackTrace();
//                Log.e("json fail", "json fail in handler2");
            }


//            Log.i("mylog", "请求结果为-->" + val);
//            console.setText(val);
//            Toast.makeText(getApplicationContext(), val, Toast.LENGTH_SHORT).show();
        }
    };

}
