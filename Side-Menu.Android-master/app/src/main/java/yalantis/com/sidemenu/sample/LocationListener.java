package yalantis.com.sidemenu.sample;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationListener;
import com.yolanda.nohttp.NoHttp;
import com.yolanda.nohttp.RequestMethod;
import com.yolanda.nohttp.rest.OnResponseListener;
import com.yolanda.nohttp.rest.Request;
import com.yolanda.nohttp.rest.Response;

import org.json.JSONException;
import org.json.JSONObject;

import static yalantis.com.sidemenu.sample.NoHttpApplication.queue;

/**
 * Created by Xiangyu on 9/5/17.
 */

public class LocationListener implements AMapLocationListener {
    String server_ip = null;
    String port = null;
//    SharedPreferences preferences = null;
    Context test = null;

    public LocationListener(String si, String po, Context test) {
        this.server_ip = si;
        this.port = po;
//        this.preferences = preferences;
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
//                        SharedPreferences.Editor editor = preferences.edit();
//                        editor.clear();
//                        editor.apply();
                        Toast.makeText(test, "Post Geoinfo fail!", Toast.LENGTH_SHORT).show();
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
