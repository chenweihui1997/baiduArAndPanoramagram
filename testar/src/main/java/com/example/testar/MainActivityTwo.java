package com.example.testar;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.lbsapi.model.BaiduPanoData;
import com.baidu.lbsapi.panoramaview.PanoramaRequest;
import com.baidu.lbsapi.panoramaview.PanoramaView;
import com.baidu.lbsapi.panoramaview.PanoramaViewListener;
import com.baidu.location.BDLocation;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.sug.OnGetSuggestionResultListener;
import com.baidu.mapapi.search.sug.SuggestionResult;
import com.baidu.mapapi.search.sug.SuggestionSearch;
import com.baidu.mapapi.search.sug.SuggestionSearchOption;
import com.baidu.mapapi.utils.CoordinateConverter;
import com.baidu.pano.platform.plugin.indooralbum.IndoorAlbumCallback;
import com.baidu.pano.platform.plugin.indooralbum.IndoorAlbumPlugin;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import map.baidu.ar.init.ArSdkManager;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivityTwo extends AppCompatActivity implements PanoramaViewListener {

    @BindView(R.id.jwd)
    TextView jwd;
    @BindView(R.id.panorama)
    PanoramaView panorama;
    @BindView(R.id.addr_et)
    AutoCompleteTextView addrEt;
    @BindView(R.id.addr_tv)
    TextView addrTv;

    private Double lon;
    private Double lat;

    private SuggestionSearch suggestionSearch;
    private ArrayList<String> arrayList = new ArrayList<String>();
    private List<SuggestionResult.SuggestionInfo> suggestionResultsnew = new ArrayList<SuggestionResult.SuggestionInfo>();
    private ArrayAdapter arrayAdapter;
    private String addr = "北京市天安门广场";

    public static void startMainActivityTwo(Activity context) {
        Intent intent = new Intent(context, MainActivityTwo.class);
        context.startActivity(intent);
    }

    public static void startMainActivityTwo(Activity context, double lat, double lon, String addr) {
        Intent intent = new Intent(context, MainActivityTwo.class);
        intent.putExtra("lon", lon);
        intent.putExtra("lat", lat);
        intent.putExtra("addr", addr);
        context.startActivity(intent);
    }

    public static void startMainActivityTwo(Activity context, String uid) {
        Intent intent = new Intent(context, MainActivityTwo.class);
        intent.putExtra("uid", uid);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_two);
        ButterKnife.bind(this);
        panorama.setPanoramaViewListener(this);
        //设置全景图的俯仰角
        //panorama.setPanoramaPitch();
        //获取当前全景图的俯仰角
        //更新俯仰角的取值范围：室外景[-15, 90], 室内景[-25, 90],
        //90为垂直朝上方向，0为水平方向
        panorama.getPanoramaPitch();
        //设置全景图的偏航角
        //panorama.setPanoramaHeading(heading);
        //获取当前全景图的偏航角
        panorama.getPanoramaHeading();
        //设置全景图的缩放级别
        //level分为1-5级
        //panorama.setPanoramaLevel(5);
        //获取当前全景图的缩放级别
        //panorama.getPanoramaLevel();
        //是否显示邻接街景箭头（有邻接全景的时候）
        panorama.setShowTopoLink(true);

        //设置全景图片的显示级别
        //根据枚举类ImageDefinition来设置清晰级别
        //较低清晰度 ImageDefinationLow
        //中等清晰度 ImageDefinationMiddle
        //较高清晰度 ImageDefinationHigh
        panorama.setPanoramaImageLevel(PanoramaView.ImageDefinition.ImageDefinitionHigh);

        //根据bitmap设置箭头的纹理(2.0.0新增)
        //panorama.setArrowTextureByBitmap(bitmap);
        //根据url设置箭头的纹理(2.0.0新增)
        //panorama.setArrowTextureByUrl(url);

        /*PanoDemoApplication app = (PanoDemoApplication) this.getApplication();
        if (app.mBMapManager == null) {
            app.mBMapManager = new BMapManager(app);

            app.mBMapManager.init(new PanoDemoApplication.MyGeneralListener());
        }*/
        //panorama.setPanorama("0100220000130817164838355J5");
        // 默认相册
        IndoorAlbumPlugin.getInstance().init();
        //IndoorAlbumCallback.EntryInfo info = new IndoorAlbumCallback.EntryInfo();
        //info.setEnterPid("0900220000141205144547300IN");
        //IndoorAlbumPlugin.getInstance().loadAlbumView(panorama,info);
        //panorama.setPanoramaByUid("7c5e480b109e67adacb22aae", PanoramaView.PANOTYPE_INTERIOR);


        // 默认相册
        //IndoorAlbumPlugin.getInstance().init();

        /*panorama.setPanoramaZoomLevel(5);
        panorama.setArrowTextureByUrl("http://d.lanrentuku.com/down/png/0907/system-cd-disk/arrow-up.png");
        panorama.setPanoramaImageLevel(PanoramaView.ImageDefinition.ImageDefinitionMiddle);
        String uid = "7aea43b75f0ee3e17c29bd71";
        panorama.setPanoramaByUid(uid, PanoramaView.PANOTYPE_STREET);*/
        addrEt.addTextChangedListener(textWatcher);
        addrEt.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                addr = suggestionResultsnew.get(i).getCity() + suggestionResultsnew.get(i).getDistrict() + suggestionResultsnew.get(i).getKey();
                //PanoramaRequest request = PanoramaRequest.getInstance(MainActivityTwo.this);
                /*BaiduPoiPanoData poiPanoData = request.getPanoramaInfoByUid(suggestionResultsnew.get(i).getUid());
                //开发者可以判断是否有外景(街景)及内景
                poiPanoData.hasStreetPano();
                poiPanoData.hasInnerPano();*/

                if (suggestionResultsnew.get(i).getPt() != null) {
                    search(suggestionResultsnew.get(i).getPt().longitude, suggestionResultsnew.get(i).getPt().latitude, addr);
                } else {
                    Toast.makeText(MainActivityTwo.this, addr + "此地暂无全景图", Toast.LENGTH_SHORT);
                }
            }
        });


        suggestionSearch = SuggestionSearch.newInstance();
        suggestionSearch.setOnGetSuggestionResultListener(new OnGetSuggestionResultListener() {
            @Override
            public void onGetSuggestionResult(SuggestionResult suggestionResult) {
                if (suggestionResult == null || suggestionResult.getAllSuggestions() == null) {
                    return;
                    //未找到相关结果
                }
                arrayList.clear();
                suggestionResultsnew = suggestionResult.getAllSuggestions();
                //获取在线建议检索结果
                for (int i = 0; i < suggestionResult.getAllSuggestions().size(); i++) {
                    if (i <= 5) {
                        arrayList.add(suggestionResult.getAllSuggestions().get(i).getKey());
                        //Log.i("myLog", "onGetSuggestionResult: "+suggestionResult.getAllSuggestions().get(i));
                    }
                }
                arrayAdapter = new ArrayAdapter(MainActivityTwo.this, R.layout.support_simple_spinner_dropdown_item, arrayList);
                addrEt.setAdapter(arrayAdapter);
                arrayAdapter.notifyDataSetChanged();
            }
        });

        BDLocation location =
                com.example.testar.utils.LocSdkClient.getInstance(ArSdkManager.getInstance().getAppContext()).getLocationStart()
                        .getLastKnownLocation();
        CoordinateConverter converter = new CoordinateConverter();
        LatLng desCoord = converter.from(CoordinateConverter.CoordType.BD09MC).coord(new LatLng(location.getLatitude(), location.getLongitude())).convert();

        if (getIntent().getDoubleExtra("lon", 0.0) != 0.0) {
            search(getIntent().getDoubleExtra("lon", 0.0), getIntent().getDoubleExtra("lat", 0.0),
                    getIntent().getStringExtra("addr"));
        } else {
            double lat = desCoord.latitude;
            double lon = desCoord.longitude;
            search(lon, lat, location.getCity() + location.getDistrict() + location.getAddrStr());
        }
    }

    public void search(double lon, double lat, String addr) {
        PanoramaRequest request = PanoramaRequest.getInstance(MainActivityTwo.this);
        locationPanoData = request.getPanoramaInfoByLatLon(lon, lat);
        if (locationPanoData.getPid() == null || "".equals(locationPanoData.getPid())) {
            Toast.makeText(MainActivityTwo.this, addr + "此地暂无全景图", Toast.LENGTH_SHORT);
        } else {
            addrTv.setText("目前区域: " + addr);
            IndoorAlbumCallback.EntryInfo info = new IndoorAlbumCallback.EntryInfo();
            info.setEnterPid(locationPanoData.getPid());
            //info.setExitUid(suggestionResultsnew.get(i).getUid());
            IndoorAlbumPlugin.getInstance().loadAlbumView(panorama, info);
            panorama.setPanorama(locationPanoData.getPid());
            //panorama.setPanoramaByUid(suggestionResultsnew.get(i).getUid(), PanoramaView.PANOTYPE_STREET);
            //panorama.setPanorama(suggestionResultsnew.get(i).getUid());
        }
    }

    //输入框内容改变进行百度检索
    private TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void afterTextChanged(Editable editable) {
            if (editable.toString().length() > 0) {
                //根据用户输入的检索
                suggestionSearch.requestSuggestion((new SuggestionSearchOption())
                        .keyword(editable.toString())
                        .city("上海"));
            }
        }
    };


    private BaiduPanoData locationPanoData;

    /**
     * 根据用户输入中文地址查询经纬度（使用web API中的逆地理编码接口）
     * 缺点:用户输入的地址范围过大或有相同地址,那么返回的结果可能有误
     */
    public void getGeocoder() {
        //http://api.map.baidu.com/geocoder/v2/?address='.$address.'&output=json&ak=hKSq4jsczk9n4qGZHdRbjaWmacgIZefl
        Request request = new Request.Builder()//创建Request 对象。
                .url("http://api.map.baidu.com/geocoder/v2/?address=" + addrEt.getText().toString() +
                        "&output=json&ak=3ZVlRSbv508lnqZI7mIMhiGSma5sVcVk&mcode=D2:C4:6B:F0:64:EA:EB:3C:29:E5:E8:24:5A:6D:61:23:80:2D:79:EB;com.example.baidumap")
                .get()
                .build();

        OkHttpClient client = new OkHttpClient();//创建OkHttpClient对象。
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    JSONObject jsonObject = new JSONObject(response.body().string());
                    if ("0".equals(jsonObject.get("status").toString())) {
                        lon = Double.parseDouble(jsonObject.getJSONObject("result").getJSONObject("location").get("lng").toString());
                        lat = Double.parseDouble(jsonObject.getJSONObject("result").getJSONObject("location").get("lat").toString());

                        PanoramaRequest request = PanoramaRequest.getInstance(MainActivityTwo.this);
                        locationPanoData = request.getPanoramaInfoByLatLon(lon, lat);
                        //开发者可以判断是否有外景(街景)
                        //Log.i("myLog", "onResponse: "+request.getPanoramaRecommendInfo(locationPanoData.getPid()));
                        handler.sendEmptyMessage(1);

                    } else {

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            IndoorAlbumCallback.EntryInfo info = new IndoorAlbumCallback.EntryInfo();
            info.setEnterPid(locationPanoData.getPid());
            IndoorAlbumPlugin.getInstance().loadAlbumView(panorama, info);
            /*IndoorAlbumCallback.EntryInfo info = new IndoorAlbumCallback.EntryInfo();
            info.setEnterPid(locationPanoData.getPid());
            IndoorAlbumPlugin.getInstance().loadAlbumView(panorama,info);*/
            //panorama.setPanoramaByUid("7c86f335bbcc18fc5fbe8669", PanoramaView.PANOTYPE_STREET);
            //panorama.setPanorama(lon, lat);
            panorama.setPanorama(locationPanoData.getPid());
        }
    };

    @Override
    protected void onPause() {
        super.onPause();
        panorama.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        panorama.onResume();
    }

    @Override
    protected void onDestroy() {
        suggestionSearch.destroy();
        panorama.destroy();
        super.onDestroy();
    }

    @Override
    public void onDescriptionLoadEnd(String s) {

    }

    @Override
    public void onLoadPanoramaBegin() {

    }

    @Override
    public void onLoadPanoramaEnd(String s) {

    }

    @Override
    public void onLoadPanoramaError(String s) {

    }

    @Override
    public void onMessage(String s, int i) {

    }

    @Override
    public void onCustomMarkerClick(String s) {

    }

    @Override
    public void onMoveStart() {

    }

    @Override
    public void onMoveEnd() {

    }

    @OnClick(R.id.ss_bt)
    public void onViewClicked() {
        //mArSdkManager.searchBuildingInfo();
        //init();
    }

    /**
     * 打开设置权限界面
     *
     * @param
     */
    public void openSetting() {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivity(intent);
    }

}
