package com.example.testar.mapcam;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.CityInfo;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiDetailSearchResult;
import com.baidu.mapapi.search.poi.PoiIndoorResult;
import com.baidu.mapapi.search.poi.PoiNearbySearchOption;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;
import com.baidu.mapapi.search.poi.PoiSortType;
import com.baidu.mapapi.utils.CoordinateConverter;
import com.example.testar.MainActivityTwo;
import com.example.testar.R;
import com.example.testar.utils.LocSdkClient;
import com.example.testar.utils.PermissionHelper;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import map.baidu.ar.init.ArBuildingResponse;
import map.baidu.ar.init.ArSceneryResponse;
import map.baidu.ar.init.ArSdkManager;
import map.baidu.ar.init.OnGetDataResultListener;
import map.baidu.ar.model.ArInfoScenery;
import map.baidu.ar.model.ArLatLng;
import map.baidu.ar.model.ArPoiInfo;
import map.baidu.ar.model.PoiInfoImpl;

/**
 * ArSdk主页面 Activity
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener, OnGetDataResultListener,
        OnGetPoiSearchResultListener {

    @BindView(R.id.radius)
    EditText radius;
    @BindView(R.id.pageNum)
    EditText pageNum;
    @BindView(R.id.app_qjt)
    Button appQjt;
    @BindView(R.id.lat)
    EditText lat;
    @BindView(R.id.lon)
    EditText lon;
    private Button mArOperation;
    private Button mArExplore;
    private Button mArFind;
    private Button mArCustom;
    private Button mArMoreCustom;
    private EditText mEtCategory;
    private EditText mEtCustom;
    public static ArInfoScenery arInfoScenery; // 景区
    public static ArBuildingResponse arBuildingResponse; // 识楼
    public static List<PoiInfoImpl> poiInfos; // 探索
    private PoiSearch mPoiSearch = null;
    private ArSdkManager mArSdkManager = null;
    private LatLng center;
    // 自定义多点数据
    private ArLatLng[] latLngs = {new ArLatLng(40.082545, 116.376188), new ArLatLng(40.04326, 116.376781),
            new ArLatLng(40.043204, 116.300784), new ArLatLng(39.892352, 116.433015),
            new ArLatLng(39.970696, 116.267439), new ArLatLng(40.040553, 116.315732),
            new ArLatLng(40.032156, 116.316307), new ArLatLng(40.012707, 116.265714),
            new ArLatLng(40.010497, 116.335279), new ArLatLng(40.124643, 116.701359),
            new ArLatLng(40.042321, 116.15648), new ArLatLng(41.092678, 116.343903),
            new ArLatLng(40.083846, 116.234669), new ArLatLng(40.094444, 116.29216)};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_arsdk);
        ButterKnife.bind(this);
        mArOperation = (Button) findViewById(R.id.app_operate);
        mArExplore = (Button) findViewById(R.id.app_explore);
        mArFind = (Button) findViewById(R.id.app_find);
        mArCustom = (Button) findViewById(R.id.app_custom);
        mArMoreCustom = (Button) findViewById(R.id.app_more_custompoint);
        mEtCategory = (EditText) findViewById(R.id.category);
        mEtCustom = (EditText) findViewById(R.id.custom_category);
        mArOperation.setOnClickListener(this);
        mArExplore.setOnClickListener(this);
        mArFind.setOnClickListener(this);
        mArCustom.setOnClickListener(this);
        mArMoreCustom.setOnClickListener(this);
        appQjt.setOnClickListener(this);
        // 如果需要检索，初始化搜索模块，注册搜索事件监听
        mPoiSearch = PoiSearch.newInstance();
        mPoiSearch.setOnGetPoiSearchResultListener(this);
        // 如果需要Ar景区功能、Ar识楼功能要注册监听
        mArSdkManager = ArSdkManager.getInstance();
        mArSdkManager.setOnGetDataResultListener(this);
        // 判断权限
        getPermission();

    }


    // 定位权限申请
    private void getPermission() {
        // 第一次运行 让用户必须登录一次比较好。
        PermissionHelper helper = new PermissionHelper(this,appQjt);
        helper.requestPermissions("请在应用信息-权限中开启存储权限，以正常使用",-1, new PermissionHelper.PermissionListener() {
            @Override
            public void doAfterGrand(String... permission) {
                // 权限全部授予
                //getMyLocation();
            }

            @Override
            public void doAfterDenied(String... permission) {
                // 权限未授予 或者未全部授予
                // PermissionHelper.showMessageWindow(li_homePage, SuperIndexActivity.this);
            }
        }, Manifest.permission.ACCESS_COARSE_LOCATION);// 网络定位
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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            // 单点数据展示
            case R.id.app_custom:
                if ("".equals(lon.getText().toString()) || "".equals(lat.getText().toString())) {
                    Toast.makeText(this, "请输入经纬度", Toast.LENGTH_SHORT).show();
                    return;
                }
                poiInfos = new ArrayList<PoiInfoImpl>();
                ArPoiInfo poiInfo = new ArPoiInfo();
                ArLatLng arLatLng = new ArLatLng(Double.parseDouble(lat.getText().toString()), Double.parseDouble(lon.getText().toString()));
                PoiInfoImpl poiImpl = new PoiInfoImpl();
                poiInfo.name = mEtCustom.getText().toString();
                poiInfo.location = arLatLng;
                poiImpl.setPoiInfo(poiInfo);
                poiInfos.add(poiImpl);
                Intent intent = new Intent(MainActivity.this, ArActivity.class);
                MainActivity.this.startActivity(intent);
                break;
            // 多点数据展示
            case R.id.app_more_custompoint:
                poiInfos = new ArrayList<PoiInfoImpl>();
                int i = 0;
                for (ArLatLng all : latLngs) {
                    ArPoiInfo pTest = new ArPoiInfo();
                    pTest.name = "testPoint" + i++;
                    pTest.location = all;
                    PoiInfoImpl poiImplT = new PoiInfoImpl();
                    poiImplT.setPoiInfo(pTest);
                    poiInfos.add(poiImplT);
                }
                Intent inten = new Intent(MainActivity.this, ArActivity.class);
                MainActivity.this.startActivity(inten);
                break;
            // 景区功能 传入uid信息
            case R.id.app_operate:
                mArSdkManager.searchSceneryInfo("2a7a25ecf9cf13636d3e1bad");
                break;
            // 识楼功能
            case R.id.app_explore:
                mArSdkManager.searchBuildingInfo();
                break;
            // 探索功能
            case R.id.app_find:
                searchNearbyProcess();
                break;
            // 全景图
            case R.id.app_qjt:
                MainActivityTwo.startMainActivityTwo(this);
                break;
            default:
                break;
        }
    }

    /**
     * 响应周边搜索按钮点击事件
     *
     * @param
     */
    public void searchNearbyProcess() {
        if ("".equals(radius.getText().toString()) || "".equals(pageNum.getText().toString()) || "".equals(mEtCategory.getText().toString())) {
            Toast.makeText(this, "请输入所有条件", Toast.LENGTH_SHORT).show();
            return;
        }

        CoordinateConverter converter = new CoordinateConverter();

        BDLocation location =
                LocSdkClient.getInstance(ArSdkManager.getInstance().getAppContext()).getLocationStart()
                        .getLastKnownLocation();
        //center = new LatLng(location.getLatitude(),location.getLongitude());
        // LatLng desCoord = CoordinateConverter.convertMC2LLp()
        center = new LatLng(location.getLatitude(), location.getLongitude());
        //百度墨卡托坐标转百度经纬度坐标接口
        LatLng desCoord = converter.from(CoordinateConverter.CoordType.BD09MC).coord(center).convert();
        //LatLng desCoordTwo = converter.from(CoordinateConverter.CoordType.BD09LL).coord(desCoord).convert();
        //Log.i("myLog", "searchNearbyProcess: "+location.getLatitude()+"_____"+location.getLongitude());
        PoiNearbySearchOption nearbySearchOption =
                new PoiNearbySearchOption().keyword(mEtCategory.getText().toString()).sortType(PoiSortType
                        .distance_from_near_to_far).location(desCoord).radius(Integer.parseInt(radius.getText().toString())).pageNum(Integer.parseInt(pageNum.getText().toString()));
        mPoiSearch.searchNearby(nearbySearchOption);
    }

    @Override
    public void onGetPoiResult(PoiResult result) {
        if (result == null || result.error == SearchResult.ERRORNO.RESULT_NOT_FOUND) {
            Toast.makeText(this, "未找到结果", Toast.LENGTH_LONG)
                    .show();
            return;
        }
        if (result.error == SearchResult.ERRORNO.NO_ERROR) {
            poiInfos = new ArrayList<PoiInfoImpl>();
            for (PoiInfo poi : result.getAllPoi()) {
                ArPoiInfo poiInfo = new ArPoiInfo();
                ArLatLng arLatLng = new ArLatLng(poi.location.latitude, poi.location.longitude);
                poiInfo.name = poi.name;
                poiInfo.location = arLatLng;
                //poiInfo.uid = poi.uid;
                poiInfo.address = poi.city + poi.area + poi.name;
                PoiInfoImpl poiImpl = new PoiInfoImpl();
                poiImpl.setPoiInfo(poiInfo);
                poiInfos.add(poiImpl);
            }
            Toast.makeText(this, "查询到: " + poiInfos.size() + " ,个POI点", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(MainActivity.this, ArActivity.class);
            MainActivity.this.startActivity(intent);
            return;
        }
        if (result.error == SearchResult.ERRORNO.AMBIGUOUS_KEYWORD) {

            // 当输入关键字在本市没有找到，但在其他城市找到时，返回包含该关键字信息的城市列表
            String strInfo = "在";
            for (CityInfo cityInfo : result.getSuggestCityList()) {
                strInfo += cityInfo.city;
                strInfo += ",";
            }
            strInfo += "找到结果";
            Toast.makeText(this, strInfo, Toast.LENGTH_LONG)
                    .show();
        }
    }

    @Override
    public void onGetPoiDetailResult(PoiDetailResult result) {
        if (result.error != SearchResult.ERRORNO.NO_ERROR) {
            Toast.makeText(this, "抱歉，未找到结果", Toast.LENGTH_SHORT)
                    .show();
        } else {
            Toast.makeText(this, result.getName() + ": " + result.getAddress(), Toast.LENGTH_SHORT)
                    .show();
        }
    }

    @Override
    public void onGetPoiDetailResult(PoiDetailSearchResult poiDetailSearchResult) {

    }

    @Override
    public void onGetPoiIndoorResult(PoiIndoorResult poiIndoorResult) {

    }

    /**
     * 返回景区数据，跳转到景区Activity
     *
     * @param arSceneryResponse
     */
    @Override
    public void onGetSceneryResult(ArSceneryResponse arSceneryResponse) {
        if (arSceneryResponse != null) {
            if (arSceneryResponse != null && arSceneryResponse.getData() != null
                    && arSceneryResponse.getData().getSon() != null
                    && arSceneryResponse.getData().getSon().size() > 0
                    && arSceneryResponse.getData().getAois() != null
                    && arSceneryResponse.getData().getAois().size() > 0
                    && arSceneryResponse.getData().getAois()
                    .get(0) != null && arSceneryResponse.getData().getAois().get(0).length > 0) {
                arInfoScenery = arSceneryResponse.getData();
                arInfoScenery.init();
                Intent intent = new Intent(MainActivity.this, SceneryArActivity.class);
                MainActivity.this.startActivity(intent);
            } else {
                Toast.makeText(getBaseContext(), "数据出错，请稍后再试", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(getBaseContext(), "数据出错，请稍后再试", Toast.LENGTH_LONG).show();
        }

    }

    /**
     * 返回楼块数据，跳转到识楼Activity
     *
     * @param arResponse
     */
    @Override
    public void onGetBuildingResult(ArBuildingResponse arResponse) {
        if (arResponse != null) {
            arBuildingResponse = arResponse;
            Intent intent = new Intent(MainActivity.this, BuildingArActivity.class);
            MainActivity.this.startActivity(intent);
        } else {
            Toast.makeText(getBaseContext(), "数据出错，请稍后再试", Toast.LENGTH_LONG).show();
        }
    }

}
