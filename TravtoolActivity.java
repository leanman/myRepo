package com.linix.travtool;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
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
import com.baidu.mapapi.search.MKPoiResult;
import com.baidu.mapapi.search.MKSearch;
import com.baidu.mapapi.search.MKSearchListener;
import com.baidu.mapapi.search.MKSuggestionResult;
import com.baidu.mapapi.search.MKTransitRouteResult;
import com.baidu.mapapi.search.MKWalkingRouteResult;
import com.baidu.platform.comapi.basestruct.GeoPoint;


public class TravtoolActivity extends Activity {
	
	static MapView mMapView = null;
	public BMapManager mBMapManager = null;
	private MapController mMapController = null;
	public MKMapViewListener mMapListener = null;
	private MKSearch mkSearch = null;
	
	LocationClient mLocClient;
	public MyLocationListenner myListener = new MyLocationListenner();
	
	MyLocationOverlay myLocationOverlay = null;
	int index =0;
	LocationData locData = null;
	
	private Spinner spinnerPoi;
	private ArrayAdapter adapterPoi;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mBMapManager = new BMapManager(this);
		mBMapManager.init(this.getString(R.string.baidu_key), null);
		mBMapManager.start();
		setContentView(R.layout.activity_travtool);
		mMapView = (MapView) findViewById(R.id.bmapView);
		mMapController = mMapView.getController();
		mMapView.setLongClickable(true);
		
		mLocClient = new LocationClient( this );
        mLocClient.registerLocationListener( myListener );
        
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true);//打开gps
        option.setAddrType("detail"); 
        option.setCoorType("bd09ll");     //设置坐标类型
        option.setScanSpan(5000);
        option.disableCache(true);
        mLocClient.setLocOption(option);
        mLocClient.start();
        mMapController.setZoom(12);
        mMapController.enableClick(true);
        mMapView.setBuiltInZoomControls(true);
        
        mMapListener = new MKMapViewListener(){

			@Override
			public void onClickMapPoi(MapPoi mapPoiInfo) {
				// TODO Auto-generated method stub
				String title = "";
				if (mapPoiInfo != null){
					title = mapPoiInfo.strText;
					Toast.makeText(TravtoolActivity.this,title,Toast.LENGTH_SHORT).show();
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
        mkSearch.init(mBMapManager, new MySearchListener());
//        if (mkSearch.geocode("五四广场", "青岛") == 0)  
//        {  
//            System.out.println("搜索成功");  
//        }  
//        else  
//        {  
//            System.out.println("搜索失败");  
//        }  
        
        myLocationOverlay = new MyLocationOverlay(mMapView);
		locData = new LocationData();
		
	    myLocationOverlay.setData(locData);
		mMapView.getOverlays().add(myLocationOverlay);
		myLocationOverlay.enableCompass();
		mMapView.refresh();
		//mMapController.animateTo(new GeoPoint((int)(locData.latitude* 1e6), (int)(locData.longitude *  1e6)));

        spinnerPoi = (Spinner)findViewById(R.id.spinnerPoi);
		adapterPoi = ArrayAdapter.createFromResource(this, R.array.poi, android.R.layout.simple_spinner_item);
		adapterPoi.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); 
		spinnerPoi.setAdapter(adapterPoi);
		spinnerPoi.setOnItemSelectedListener(new OnItemSelectedListener(){

			@Override
			public void onItemSelected(AdapterView<?> adapter, View arg1,
					int position, long arg3) {
				// TODO Auto-generated method stub
				String poi = adapter.getItemAtPosition(position).toString();
				GeoPoint point =new GeoPoint((int)myLocationOverlay.getMyLocation().latitude, (int)myLocationOverlay.getMyLocation().longitude);
				mkSearch.poiSearchNearBy(poi, point, 500);
				Toast.makeText(TravtoolActivity.this,poi,Toast.LENGTH_SHORT).show();
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
				
			}
			
		});
		
		spinnerPoi.setVisibility(View.VISIBLE);
	}
	
	@Override
    protected void onPause() {
        mMapView.onPause();
        super.onPause();
    }
    
    @Override
    protected void onResume() {
        mMapView.onResume();
        super.onResume();
    }
    
    
    @Override
    protected void onDestroy() {
        if (mLocClient != null)
            mLocClient.stop();
        mMapView.destroy();
        //DemoApplication app = (DemoApplication)this.getApplication();
        if (mBMapManager != null) {
            mBMapManager.destroy();
            mBMapManager = null;
        }
        super.onDestroy();
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.travtool, menu);
		return true;
	}

	
	private class MyLocationListenner implements BDLocationListener{

		@Override
		public void onReceiveLocation(BDLocation location) {
			// TODO Auto-generated method stub
//			if (location == null)
//                return ;
//            
//            locData.latitude = location.getLatitude();
//            locData.longitude = location.getLongitude();
//            locData.accuracy = location.getRadius();
//            locData.direction = location.getDerect();
//            locData.direction = 2.0f;
//            myLocationOverlay.setData(locData);
//            mMapView.refresh();
//            mMapController.animateTo(new GeoPoint((int)(locData.latitude* 1e6), (int)(locData.longitude *  1e6)));
		}

		@Override
		public void onReceivePoi(BDLocation arg0) {
			// TODO Auto-generated method stub
			
		}

	}
	
	private class MySearchListener implements MKSearchListener{

		@Override
		public void onGetAddrResult(MKAddrInfo arg0, int arg1) {
			// TODO Auto-generated method stub
			mMapController.animateTo(arg0.geoPt);  
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
		public void onGetPoiResult(MKPoiResult arg0, int arg1, int arg2) {
			// TODO Auto-generated method stub
//			PoiOverlay poioverlay = new PoiOverlay(TravtoolActivity.this, mMapView);  
//            poioverlay.setData(arg0.getAllPoi());  
//            mMapView.getOverlays().add(poioverlay);
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
}
