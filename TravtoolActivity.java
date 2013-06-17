package com.linix.outdoor;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.BMapManager;
import com.baidu.mapapi.map.LocationData;
import com.baidu.mapapi.map.MKMapViewListener;
import com.baidu.mapapi.map.MapController;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationOverlay;
import com.baidu.mapapi.map.PoiOverlay;
import com.baidu.mapapi.search.MKAddrInfo;
import com.baidu.mapapi.search.MKBusLineResult;
import com.baidu.mapapi.search.MKDrivingRouteResult;
import com.baidu.mapapi.search.MKPoiInfo;
import com.baidu.mapapi.search.MKPoiResult;
import com.baidu.mapapi.search.MKSearch;
import com.baidu.mapapi.search.MKSearchListener;
import com.baidu.mapapi.search.MKSuggestionResult;
import com.baidu.mapapi.search.MKTransitRouteResult;
import com.baidu.mapapi.search.MKWalkingRouteResult;
import com.baidu.platform.comapi.basestruct.GeoPoint;

public class Outdoor extends Activity {

	private static final String TAG = "TAG";
	
	private BMapManager mBMapManager = null;
	private MapView mMapView = null;
	private Spinner spinnerPoi ;
	private ArrayAdapter arrayAdapter;
	public MKMapViewListener mMapListener = null;
	private MKSearch mkSearch = null;
	private MapController mMapController = null;
	private GeoPoint geoPoint = null;

	//get the location information
	private LocationClient mLocationClient = null;
	private BDLocationListener mBDLocationListener = new MyBDLocationListener();
	
	//get my location info
	private MyLocationOverlay myLocationOverlay = null;
	private LocationData mLocationData = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mBMapManager = new BMapManager(getApplication());
		mBMapManager.init(this.getString(R.string.baiduKey), null);
		mBMapManager.start();
		
		setContentView(R.layout.activity_outdoor);
		
		mMapView = (MapView)findViewById(R.id.bmapsView);
		mMapView.setBuiltInZoomControls(true);
		
		//get mapview controller
		mMapController = mMapView.getController();
		
		mLocationClient = new LocationClient(getApplicationContext());
		mLocationClient.registerLocationListener(mBDLocationListener);
		
		LocationClientOption locationOption = new LocationClientOption();
		locationOption.setOpenGps(true);
		locationOption.setAddrType("all");
		locationOption.setCoorType("bd09ll");
		locationOption.setScanSpan(5000);
		locationOption.disableCache(true);
		locationOption.setPoiNumber(5); // 最多返回POI个数
		locationOption.setPoiDistance(1000); // poi查询距离
		locationOption.setPriority(LocationClientOption.NetWorkFirst);
		
		mLocationClient.setLocOption(locationOption);
		mLocationClient.start();
		if (mLocationClient != null && mLocationClient.isStarted()) {
			mLocationClient.requestLocation();
		} else {
			Log.d(TAG,"locClient is null or not started");
		}
		//setup the map zoom level
		mMapController.setZoom(18);
		mMapController.enableClick(true);
		
		Button findMe = (Button)findViewById(R.id.locate);
		findMe.setOnClickListener(new View.OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				try {

					LocationManager locationManager;
					String context = Context.LOCATION_SERVICE;
					locationManager = (LocationManager) getSystemService(context);
					// String provider = LocationManager.GPS_PROVIDER;

					Criteria criteria = new Criteria();
					criteria.setAccuracy(Criteria.ACCURACY_FINE);
					criteria.setAltitudeRequired(false);
					criteria.setBearingRequired(false);
					criteria.setCostAllowed(true);
					criteria.setPowerRequirement(Criteria.POWER_LOW);
					String provider = locationManager.getBestProvider(criteria, true);
					Location location = locationManager.getLastKnownLocation(provider);
					geoPoint = new GeoPoint((int) (location.getLatitude() * 1e6),
							(int) (location.getLongitude() * 1e6));
					mMapController.animateTo(geoPoint);
					mMapController.setCenter(geoPoint);
					mMapController.setZoom(20);
				} catch (Exception e) {
					// TODO: handle exception
					Log.d("Location Error:", e.getMessage().toString());
				}
			}
			
		});
		
		mMapListener = new MKMapViewListener(){
			
			@Override
			public void onClickMapPoi(MapPoi mapPoiInfo) {
				// TODO Auto-generated method stub
				String title = "";
				if (mapPoiInfo != null){
					title = mapPoiInfo.strText;
					Toast.makeText(Outdoor.this,title,Toast.LENGTH_SHORT).show();
				}
				//单击某个兴趣点时 作相应的处理
			}

			@Override
			public void onGetCurrentMap(Bitmap arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onMapAnimationFinish() {
				// TODO Auto-generated method stub

			}

			@Override
			public void onMapMoveFinish() {
				// TODO Auto-generated method stub

			}
        	
        };
        mMapView.regMapViewListener(mBMapManager, mMapListener);
        
        mkSearch = new MKSearch();
        		
		myLocationOverlay = new MyLocationOverlay(mMapView);
		mLocationData = new LocationData();
		myLocationOverlay.setData(mLocationData);
		mMapView.getOverlays().add(myLocationOverlay);
		myLocationOverlay.enableCompass();
		
		mMapView.refresh();
		
		spinnerPoi = (Spinner)findViewById(R.id.spinnerPoi);
		arrayAdapter = ArrayAdapter.createFromResource(this, R.array.Poi, android.R.layout.simple_spinner_dropdown_item);
		spinnerPoi.setAdapter(arrayAdapter);
		
		spinnerPoi.setOnItemSelectedListener(new OnItemSelectedListener(){

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				String mSelection = parent.getItemAtPosition(position).toString();
				GeoPoint point =new GeoPoint((int)myLocationOverlay.getMyLocation().latitude, (int)myLocationOverlay.getMyLocation().longitude);
				mkSearch.poiSearchNearBy("KTV", point, 5000);
				
				//此处搜索有问题不能得到相应的兴趣点
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				// TODO Auto-generated method stub
				
			}
			
		});	
		mkSearch.init(mBMapManager, new MySearchListener());
	}
	
	@Override
	protected void onStart() {
		mLocationClient.start();
		super.onStart();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.outdoor, menu);
		return true;
	}
	
	@Override
	protected void onStop() {
		mLocationClient.stop();
		super.onStop();
	}

	@Override
	protected void onDestroy() {

		mMapView.destroy();
		mLocationClient.unRegisterLocationListener(mBDLocationListener);

		if (mBMapManager != null) {
			mBMapManager.destroy();
			mBMapManager = null;
		}
		mLocationClient.stop();
		myLocationOverlay.disableCompass();
		myLocationOverlay = null;
		mMapView = null;
		mLocationClient = null;
		mLocationData = null;
		super.onDestroy();
	}

	@Override
	protected void onPause() {

		mMapView.onPause();
		if (mBMapManager != null) {
			mBMapManager.stop();
		}
		mLocationClient.stop();

		super.onPause();
	}

	@Override
	protected void onResume() {

		mMapView.onResume();
		if (mBMapManager != null) {
			mBMapManager.start();
		}
		mLocationClient.start();
		super.onResume();
	}

	private class MyBDLocationListener implements BDLocationListener {

		@Override
		public void onReceiveLocation(BDLocation position) {
			// TODO Auto-generated method stub
			mLocationData.latitude = position.getLatitude();
			mLocationData.longitude = position.getLongitude();
			mLocationData.accuracy = position.getRadius();
			mLocationData.direction = position.getDerect();
			myLocationOverlay.setData(mLocationData);
			mMapView.refresh();
			mMapView.getController().animateTo(new GeoPoint((int) (mLocationData.latitude * 1e6),
					(int) (mLocationData.longitude * 1e6)));
			mMapView.getController().setCenter(new GeoPoint((int) (mLocationData.latitude * 1e6),
					(int) (mLocationData.longitude * 1e6)));
		}

		@Override
		public void onReceivePoi(BDLocation position) {
			// TODO Auto-generated method stub
			
		}
		
	}
	
	private class MySearchListener implements MKSearchListener{

		@Override
		public void onGetAddrResult(MKAddrInfo arg0, int arg1) {
			// TODO Auto-generated method stub
			mMapController.animateTo(arg0.geoPt);  
			Log.d("onGetAddrResult--------",arg1+"-12-");
		}

		@Override
		public void onGetBusDetailResult(MKBusLineResult arg0, int arg1) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onGetDrivingRouteResult(MKDrivingRouteResult arg0, int arg1) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onGetPoiDetailSearchResult(int arg0, int arg1) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onGetPoiResult(MKPoiResult res, int type, int error) {
			// TODO Auto-generated method stub
//			PoiOverlay poioverlay = new PoiOverlay(TravtoolActivity.this, mMapView);  
//            poioverlay.setData(arg0.getAllPoi());  
//            mMapView.getOverlays().add(poioverlay);
			if (error != 0 || res == null) {
                Toast.makeText(Outdoor.this, "抱歉，未找到结果", Toast.LENGTH_LONG).show();
                return;
            } else {
				Log.d("onGetPoiResult",
						"the onGetPoiResult res is "
								+ res.getCurrentNumPois() + "__"
								+ res.getNumPages() + "__"
								+ res.getNumPois() + "__" + type + "__"
								+ error);
			}
			
			if (res.getCurrentNumPois() > 0) {
				PoiOverlay poiOverlay = new PoiOverlay(Outdoor.this, mMapView);
	            poiOverlay.setData(res.getAllPoi());
	            mMapView.getOverlays().clear();
	            mMapView.getOverlays().add(poiOverlay);
	            mMapView.refresh();
			}
		}

		@Override
		public void onGetSuggestionResult(MKSuggestionResult arg0, int arg1) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onGetTransitRouteResult(MKTransitRouteResult arg0, int arg1) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onGetWalkingRouteResult(MKWalkingRouteResult arg0, int arg1) {
			// TODO Auto-generated method stub

		}
	}
	
//	public class MyPoiOverlay extends PoiOverlay {
//	    
//	    MKSearch mSearch;
//
//	    public MyPoiOverlay(Activity activity, MapView mapView, MKSearch search) {
//	        super(activity, mapView);
//	        mSearch = search;
//	    }
//
//	    @Override
//	    protected boolean onTap(int i) {
//	        super.onTap(i);
//	        MKPoiInfo info = getPoi(i);
//	        if (info.hasCaterDetails) {
//	            mSearch.poiDetailSearch(info.uid);
//	        }
//	        return true;
//	    }
//
//	    
//	}
}
