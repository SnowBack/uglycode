package yalantis.com.sidemenu.sample.fragment;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import yalantis.com.sidemenu.interfaces.ScreenShotable;
import yalantis.com.sidemenu.sample.R;

import static yalantis.com.sidemenu.sample.MainActivity.PAGE_TAG_PLUG;
import static yalantis.com.sidemenu.sample.MainActivity.PAGE_TAG_WEBVIEW;

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
    protected ImageView mimage;
    protected int res;
    private Bitmap bitmap;
    private WebView mywebview;
    private WebSettings mWebSettings;
    private ArrayList<HashMap<String, String>> mylist;

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

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = null;
        if (res == PAGE_TAG_PLUG) {
            rootView = inflater.inflate(R.layout.fragment_pluglist, container, false);

            pluglist = (ListView) rootView.findViewById(R.id.pluglist);
            mylist = new ArrayList<HashMap<String, String>>();
            HashMap<String, String> map = new HashMap<String, String>();
            map.put("TaskId", "ID1");
            map.put("ItemTitle", "Title1");
            map.put("ItemText", "Text1");
            mylist.add(map);
            HashMap<String, String> map2 = new HashMap<String, String>();
            map2.put("TaskId", "ID2");
            map2.put("ItemTitle", "Title2");
            map2.put("ItemText", "Text2");
            mylist.add(map2);
            SimpleAdapter mSchedule = new SimpleAdapter(rootView.getContext(), //没什么解释
                    mylist,//数据来源
                    R.layout.list_item_template,//ListItem的XML实现
                    //动态数组与ListItem对应的子项
                    new String[] {"TaskId", "ItemTitle", "ItemText"},
                    //ListItem的XML文件里面的两个TextView ID
                    new int[] {R.id.device_id, R.id.ItemTitle, R.id.ItemText});
            pluglist.setAdapter(mSchedule);
            Log.e("PLUG CLICK", "click");
            pluglist.setVisibility(View.VISIBLE);
//            mimage = (ImageView)rootView.findViewById(R.id.image_content);
//            mimage.setClickable(true);
//            mimage.setVisibility(View.VISIBLE);

        } else if (res == PAGE_TAG_WEBVIEW) {
            rootView = inflater.inflate(R.layout.fragment_main, container, false);
            mywebview = (WebView) rootView.findViewById(R.id.mywebview);

            mWebSettings = mywebview.getSettings();
            mWebSettings.setAppCacheEnabled(true);
            mWebSettings.setDomStorageEnabled(true);
            mWebSettings.setJavaScriptEnabled(true);
            mWebSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
            mywebview.loadUrl("https://www.baidu.com");
            mywebview.setWebViewClient(new WebViewClient() {
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    view.loadUrl(url);
                    return true;
                }
            });
            mywebview.setVisibility(View.VISIBLE);
        }
        return rootView;
    }

    @Override
    public void takeScreenShot() {
        Thread thread = new Thread() {
            @Override
            public void run() {
                Bitmap bitmap = Bitmap.createBitmap(containerView.getWidth(),
                        containerView.getHeight(), Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(bitmap);
                containerView.draw(canvas);
                ContentFragment.this.bitmap = bitmap;
            }
        };

        thread.start();

    }

    @Override
    public Bitmap getBitmap() {
        return bitmap;
    }
}

