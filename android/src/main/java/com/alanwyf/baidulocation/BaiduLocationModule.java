package com.alanwyf.baidulocation;

import android.util.Log;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.Poi;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeMap;

import service.LocationService;

/**
 * Created by alan on 2017/8/23.
 */

public class BaiduLocationModule extends ReactContextBaseJavaModule {
    private LocationService locationService;
    private Promise mPromise;
    private String mLocation;
    String TAG = "location";

    public BaiduLocationModule(ReactApplicationContext reactApplicationContext){
        super(reactApplicationContext);
    }

    @Override
    public String getName() {
        return "BaiduLocation";
    }

    public void init(){
        locationService = new LocationService(getReactApplicationContext());
        locationService.registerListener(mListener);
        locationService.setLocationOption(locationService.getDefaultLocationClientOption());
    }

    @ReactMethod
    public void getLocation(Promise promise){
        try{
            Log.v(TAG, "getLocation");
            mPromise = promise;
            if(locationService == null){
                this.init();
                 Log.v(TAG, "init");
            }
                             Log.v(TAG, "start");
            locationService.start();
        }catch (Exception e){
            mPromise.reject("-1", "location failed");
        }
    }

    /*****
     *
     * 定位结果回调，重写onReceiveLocation方法，可以直接拷贝如下代码到自己工程中修改
     *
     */
    private BDAbstractLocationListener mListener = new BDAbstractLocationListener() {

        @Override
        public void onReceiveLocation(BDLocation location) {
            Log.v(TAG, "onReceiveLocation");
            if (null == location || location.getLocType() == BDLocation.TypeServerError) {
                mPromise.reject("-1", "location failed");
                return;
            }
            WritableMap result = new WritableNativeMap();
            result.putString("code", "1");
            result.putString("msg", "location success");
            result.putString("time", location.getTime());
            result.putString("locType", location.getLocType() + "");
            result.putString("locType description", location.getLocTypeDescription());
            result.putString("latitude", location.getLatitude() + "");
            result.putString("longitude", location.getLongitude() + "");
            result.putString("radius", location.getRadius() + "");
            result.putString("CountryCode", location.getCountryCode());
            result.putString("Country", location.getCountry());
            result.putString("citycode", location.getCityCode());
            result.putString("city", location.getCity());
            result.putString("District", location.getDistrict());
            result.putString("Street", location.getStreet());
            result.putString("addr", location.getAddrStr());
            result.putString("UserIndoorState", location.getUserIndoorState() + "");
            result.putString("Direction(not all devices have value)", location.getDirection() + "");
            result.putString("locationdescribe", location.getLocationDescribe());
            if (location.getPoiList() != null && !location.getPoiList().isEmpty()) {
                for (int i = 0; i < location.getPoiList().size(); i++) {
                    Poi poi = (Poi) location.getPoiList().get(i);
                    result.putString("Poi", poi.getName());
                }
            }
            if (location.getLocType() == BDLocation.TypeGpsLocation) {// GPS定位结果
                result.putString("speed", location.getSpeed() + "");// 速度 单位：km/h
                result.putString("satellite", location.getSatelliteNumber() + "");// 卫星数目
                result.putString("height", location.getAltitude() + "");// 海拔高度 单位：米
                result.putString("gps status", location.getGpsAccuracyStatus() + "");// *****gps质量判断*****
                result.putString("describe", "gps定位成功");
            } else if (location.getLocType() == BDLocation.TypeNetWorkLocation) {// 网络定位结果
                // 运营商信息
                if (location.hasAltitude()) {// *****如果有海拔高度*****
                    result.putString("height", location.getAltitude() + "");
                }
                result.putString("operationers", location.getOperators() + "");// 运营商信息
                result.putString("describe", "网络定位成功");
            } else if (location.getLocType() == BDLocation.TypeOffLineLocation) {// 离线定位结果
                result.putString("describe", "离线定位成功，离线定位结果也是有效的");
            } else if (location.getLocType() == BDLocation.TypeServerError) {
                result.putString("describe", "服务端网络定位失败");
            } else if (location.getLocType() == BDLocation.TypeNetWorkException) {
                result.putString("describe", "网络不同导致定位失败，请检查网络是否通畅");
            } else if (location.getLocType() == BDLocation.TypeCriteriaException) {
                result.putString("describe", "无法获取有效定位依据导致定位失败，一般是由于手机的原因，处于飞行模式下一般会造成这种结果，可以试着重启手机");
            }
            mPromise.resolve(result);
            locationService.stop();
        }
    };
}
