package com.tae.mycafe.ui.serch;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import noman.googleplaces.NRPlaces;
import noman.googleplaces.Place;
import noman.googleplaces.PlaceType;
import noman.googleplaces.PlacesException;
import noman.googleplaces.PlacesListener;


import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
//import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.material.snackbar.Snackbar;
import com.tae.mycafe.R;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class NearbyFragment extends Fragment implements OnMapReadyCallback, PlacesListener {

    private GoogleMap mMap;
    private MapView mapView = null;
    private Marker currentMarker = null;
    private FragmentActivity mContext;
    private static final String TAG = "MyTagName";

    //Fused Location Provider의 진입 점
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private LocationRequest locationRequest;
    private Location mCurrentLocation;

    // The entry point to the Places API.
    private PlacesClient mPlacesClient;

    //초기 맵 화면 위치표시를 위한 설정
    private final LatLng mDefaultLocation = new LatLng(36.146210, 128.393437); //초기 위도,경도
    private static final int DEFAULT_ZOOM = 15; //카메라 줌 15배
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1001;
    private boolean mLocationPermissionGranted; //위치 권한 허용 비교를 위한 값

    private static final int GPS_ENABLE_REQUEST_CODE = 2001;
    private static final int UPDATE_INTERVAL_MS = 1000 * 60 * 1;  // 1분 단위 시간 갱신
    private static final int FASTEST_UPDATE_INTERVAL_MS = 1000 * 30 ; // 30초 단위로 화면 갱신

    //지도 활동에서 활동 상태를 저장하기 위한 키 값
    private static final String KEY_CAMERA_POSITION = "camera_position";
    private static final String KEY_LOCATION = "location";
    private CameraPosition mCameraPosition;

    //스낵바
    private Snackbar snackbar;

    // <--- Noman googleplace PlacesListener 등록에 필요한 메서드들
    @Override
    public void onPlacesFailure(PlacesException e) {

    }

    @Override
    public void onPlacesStart() {

    }

    @Override
    public void onPlacesSuccess(final List<Place> places) {
        mContext.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                for (Place place : places) {

                    LatLng latLng = new LatLng(place.getLatitude(), place.getLongitude());
                    mMap.addMarker(new MarkerOptions().position(latLng)
                            .title(place.getName())
                            .snippet(getCurrentAddress(latLng))
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.coffee)));
                    //Log.d(TAG, "이름:" +place.getName()+ "주소:"+ getCurrentAddress(latLng));
                }
            }
        });
    }

    @Override
    public void onPlacesFinished() {

    }
    //---->

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext = (FragmentActivity) context;
        Log.d(TAG, "onAttach");
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 초기화 해야 하는 리소스들을 여기서 초기화 해준다.

        Log.d(TAG, "onCreate");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (savedInstanceState != null) { //기기의 위치와 지도의 카메라 위치(이전에 저장한 경우)를 가져옴
            mCurrentLocation = savedInstanceState.getParcelable(KEY_LOCATION);
            mCameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION);
        }
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_nearby, container, false);

        //프래그먼트가 옵션메뉴를 갖도록 설정
        setHasOptionsMenu(true);


        // mapview 설정
        mapView = (MapView)view.findViewById(R.id.map);
        if(mapView != null)
        {
            mapView.onCreate(savedInstanceState);
        }
        mapView.getMapAsync(this);

        Log.d(TAG, "onCreateView");
        return view;
    }


    @Override //액티비티에서 프래그먼트를 모두 생성하고 난 다음에 호출->Activity(or View)와 Fragment의 뷰가 모두 생성된 상태로, View를 변경하는 작업이 가능한 단계
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState != null) { //기기의 위치와 지도의 카메라 위치(이전에 저장한 경우)를 가져옴
            mCurrentLocation = savedInstanceState.getParcelable(KEY_LOCATION);
            mCameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION);
        }
        //place api 초기화?
        Places.initialize(mContext, getString(R.string.google_maps_key));
        mPlacesClient = Places.createClient(mContext);

        //액티비티가 처음 생성될 때 실행되는 함수
        MapsInitializer.initialize(mContext);

        // 위치 요청 설정 LocationRequest객체
        locationRequest = new LocationRequest()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY) // 가장 정확도가 높은 옵션 최우선적으로 고려(GPS를 이용해 찾을 가능성이 큼)
                .setInterval(UPDATE_INTERVAL_MS) // 위치가 Update 되는 주기
                .setFastestInterval(FASTEST_UPDATE_INTERVAL_MS); // 위치 획득후 업데이트되는 주기
        // 사용자 기기의 현재 위치 설정받음
        LocationSettingsRequest.Builder builder =
                new LocationSettingsRequest.Builder();
        builder.addLocationRequest(locationRequest);



        // FusedLocationProviderClient 객체 생성
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(mContext);

        Log.d(TAG, "onActivityCreated");
    }

    @Override //활동이 일시중지될 때 상태를 저장하는 콜백메서드
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
        if (mMap != null) {
            outState.putParcelable(KEY_CAMERA_POSITION, mMap.getCameraPosition());
            outState.putParcelable(KEY_LOCATION, mCurrentLocation);
        }
    }



    // 구글맵이 준비되면 올리는 메서드
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        Log.d(TAG, "onMapReady");

        getLocationPermission();

        updateLocationUI();

        getDeviceLocation(); //디바이스 기기의 위치 찾음

        setDefaultLocation(); //장소를 못 찾을 경우, 지도의 초기위치 설정


    }

    // 지도에 위치 컨트롤을 설정 -- 위치 정보 액세스 권한을 부여한 경우
    // 지도의 내 위치 레이어 및 관련 컨트롤을 사용 설정하고
    // 그렇지 않은 경우 레이어와 컨트롤을 사용 중지하고 현재 위치를 null로 설정
    private void updateLocationUI() {
        if (mMap == null) {
            return;
        }
        try {
            if (mLocationPermissionGranted) {
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                mMap.setMyLocationEnabled(false);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                mCurrentLocation = null;
                getLocationPermission();
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
        Log.d(TAG, "updateLocationUI");
    }

    private void setDefaultLocation(){
        if (currentMarker != null) currentMarker.remove();

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(mDefaultLocation);
        markerOptions.title("위치정보 가져올 수 없음");
        markerOptions.snippet("위치 퍼미션과 GPS 활성 여부 확인하세요");
        markerOptions.draggable(true);
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        currentMarker = mMap.addMarker(markerOptions);

        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM);
        mMap.moveCamera(cameraUpdate);
        Log.d(TAG, "setDefaultLocation");
    }

    String getCurrentAddress(LatLng latlng) {
        // 위치 정보와 지역으로부터 주소 문자열을 구한다.
        List<Address> addressList = null ;
        Geocoder geocoder = new Geocoder( mContext, Locale.getDefault());

        // 지오코더를 이용하여 주소 리스트를 구한다.
        try {
            addressList = geocoder.getFromLocation(latlng.latitude,latlng.longitude,1);
        } catch (IOException e) {
            Toast. makeText( mContext, "위치로부터 주소를 인식할 수 없습니다. 네트워크가 연결되어 있는지 확인해 주세요.", Toast.LENGTH_SHORT ).show();
            e.printStackTrace();
            return "주소 인식 불가" ;
        }

        if (addressList.size() < 1) { // 주소 리스트가 비어있는지 비어 있으면
            return "해당 위치에 주소 없음" ;
        }

        // 주소를 담는 문자열을 생성하고 리턴
        Address address = addressList.get(0);
        StringBuilder addressStringBuilder = new StringBuilder();
        for (int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
            addressStringBuilder.append(address.getAddressLine(i));
            if (i < address.getMaxAddressLineIndex())
                addressStringBuilder.append("\n");
        }

        Log.d(TAG, "getCurrentAddress");
        return addressStringBuilder.toString();
    }

    LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            super.onLocationResult(locationResult);

            Log.d(TAG, "LocationCallback");
            List<Location> locationList = locationResult.getLocations();

            if (locationList.size() > 0) {
                Location location = locationList.get(locationList.size() - 1);

                LatLng currentPosition
                        = new LatLng(location.getLatitude(), location.getLongitude());

                String markerTitle = getCurrentAddress(currentPosition); //getCurrentAddress()사용
                String markerSnippet = "위도:" + location.getLatitude()
                        + " 경도:" + location.getLongitude(); //String.valueOf(location.getLongitude())

                Log.d(TAG, "Time :" + CurrentTime() + " onLocationResult : " + markerSnippet);

                //현재 위치에 마커 생성하고 이동
                //setCurrentLocation(location, markerTitle, markerSnippet); //setCurrentLocation()사용
                mCurrentLocation = location;
            }
        }

    };

    private String CurrentTime(){
        Date today = new Date();
        SimpleDateFormat date = new SimpleDateFormat("yyyy/MM/dd");
        SimpleDateFormat time = new SimpleDateFormat("hh:mm:ss a");
        return time.format(today);
    }

    public void setCurrentLocation(Location location, String markerTitle, String markerSnippet) {
        if (currentMarker != null) currentMarker.remove();

        LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(currentLatLng);
        markerOptions.title(markerTitle);
        markerOptions.snippet(markerSnippet);
        markerOptions.draggable(true);

        currentMarker = mMap.addMarker(markerOptions);

        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLng(currentLatLng);
        mMap.moveCamera(cameraUpdate);

        Log.d(TAG, "setCurrentLocation");
    }

    private void getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */


        Log.d(TAG, "getDeviceLocation");

        try {
            Task<Location> locationResult = mFusedLocationProviderClient.getLastLocation();
            locationResult.addOnCompleteListener(mContext, new OnCompleteListener<Location>() {
                @Override
                public void onComplete(@NonNull Task<Location> task) {
                    if (task.isSuccessful()) {
                        // Set the map's camera position to the current location of the device.
                        mCurrentLocation = task.getResult();
                        if (mCurrentLocation != null) {

                            LatLng myLocation = new LatLng(mCurrentLocation.getLatitude(),
                                            mCurrentLocation.getLongitude());
                            mMap.addMarker(new MarkerOptions()
                                    .position(myLocation)
                                    .title("현재 위치!")
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker)));
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation,DEFAULT_ZOOM));
                        }
                    } else {
                        Log.d(TAG, "Current location is null. Using defaults.");
                        Log.e(TAG, "Exception: %s", task.getException());
                        mMap.moveCamera(CameraUpdateFactory
                                .newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));
                        mMap.getUiSettings().setMyLocationButtonEnabled(false);
                    }
                }
            });

        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }


    //사용자가 상세한 위치 정보 액세스 권한을 부여했는지 확인
    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        Log.d(TAG, "getLocationPermission");
        if (ContextCompat.checkSelfPermission(mContext,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
            Toast.makeText(mContext,"위치 권한이 승인되었습니다.", Toast.LENGTH_SHORT).show();
        } else {
            ActivityCompat.requestPermissions(mContext,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);

            snackbar = Snackbar.make(getActivity().findViewById(android.R.id.content),
                    "위치를 가져오기 위한 권한이 필요합니다.", Snackbar.LENGTH_LONG);
            Log.d(TAG,"스낵바");
            snackbar.setAction("확인", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Settings.ACTION_SETTINGS);
                    getActivity().startActivityForResult(intent, 0);
                }
            });
            snackbar.show();

        }
    }

    // 스낵바를 통해 설정창 갔다 온 후 실행할 메서드

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case 0:
                onMapReady(mMap);
                break;
        }

        Log.d(TAG, "startActivityForResult");
    }

    // 권한 요청의 결과를 처리하는 콜백함수
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                }
            }
        }
        updateLocationUI();
        Log.d(TAG, "onRequestPermissionsResult");
    }
/*
    public void findCurrentLocation() {
        // 권한 체크

        mFusedLocationProviderClient.getLastLocation().addOnSuccessListener(mContext, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    // 현재 위치
                    LatLng myLocation = new LatLng(location.getLatitude(), location.getLongitude());
                    mMap.addMarker(new MarkerOptions()
                            .position(myLocation)
                            .title("현재 위치!!!!!"));

                    mMap.moveCamera(CameraUpdateFactory.newLatLng(myLocation));

                    // 카메라 줌
                    mMap.animateCamera(CameraUpdateFactory.zoomTo(17.0f));
                }
            }
        });
        Log.d(TAG, "findCurrentLocation");
    }*/
/*
    public boolean checkLocationServicesStatus() {
        LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }*/

    // 주변 카페 찾기 메뉴바 버튼
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.current_place_menu, menu);
    }
    // 주변 카페 찾기 메뉴바 버튼 클릭시 실행
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d(TAG, "onOptionsItemSelected");

        if (item.getItemId() == R.id.option_get_place) {
            showCurrentPlace();
        }

        return true;
    }

    // 주변 카페 찾는 메서드
    private void showCurrentPlace() {
        Log.d(TAG, "showCurrentPlace");
        if (mMap == null) {
            return;
        }

        if (mLocationPermissionGranted) {
            // Use fields to define the data types to return. --> 어떤 형식의 데이터들을 반환 받을지 설정
            /*List<Place.Field> placeFields = Arrays.asList(Place.Field.NAME, Place.Field.ADDRESS,
                    Place.Field.LAT_LNG, Place.Field.TYPES);*/

            // Use the builder to create a FindCurrentPlaceRequest.--> 데이터 요청 객체
            /*FindCurrentPlaceRequest request =
                    FindCurrentPlaceRequest.newInstance(placeFields);*/

                new NRPlaces.Builder()
                        .listener(this)
                        .key("AIzaSyAMI47c8mpMq8Gz2HrAhIF8GMy_tq2EA58")
                        .latlng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude())//현재 위치
                        .radius(2000) //2000 미터 내에서 검색
                        .type(PlaceType.CAFE)//카페
                        .build()
                        .execute();


            // Get the likely places - that is, the businesses and other points of interest that
            // are the best match for the device's current location.
            /*@SuppressWarnings("MissingPermission") final
            Task<FindCurrentPlaceResponse> placeResult =
                    mPlacesClient.findCurrentPlace(request);// 현재위치의 데이터정보 요청이벤트리스너 등록
            placeResult.addOnCompleteListener (new OnCompleteListener<FindCurrentPlaceResponse>() {
                @Override
                public void onComplete(@NonNull Task<FindCurrentPlaceResponse> task) {
                    if (task.isSuccessful() && task.getResult() != null) {
                        //FindCurrentPlaceResponse --> 데이터 응답
                        FindCurrentPlaceResponse likelyPlaces = task.getResult();
*/
                        // Set the count, handling cases where less than 5 entries are returned.
                        /*int count;

                        if (likelyPlaces.getPlaceLikelihoods().size() < M_MAX_ENTRIES) {
                            count = likelyPlaces.getPlaceLikelihoods().size();
                        } else {
                            count = M_MAX_ENTRIES;
                        }


                        mLikelyPlaceNames = new String[count];
                        mLikelyPlaceAddresses = new String[count];
                        mLikelyPlaceAttributions = new List[count];
                        mLikelyPlaceLatLngs = new LatLng[count];
                        mLikelyPlaceTypes = new List[count];
*/
                        /*int i = 0;
                        int j = 0;
                        for (PlaceLikelihood placeLikelihood : likelyPlaces.getPlaceLikelihoods()) {
                            // Build a list of likely places to show the user.
                            mLikelyPlaceNames[i] = placeLikelihood.getPlace().getName();
                            mLikelyPlaceAddresses[i] = placeLikelihood.getPlace().getAddress();
                            mLikelyPlaceAttributions[i] = placeLikelihood.getPlace()
                                    .getAttributions();
                            mLikelyPlaceLatLngs[i] = placeLikelihood.getPlace().getLatLng();
                            mLikelyPlaceTypes[i] = placeLikelihood.getPlace().getTypes();
                            Object[] typeOfPlace = placeLikelihood.getPlace().getTypes().toArray();

                            for(Object PlaceType : typeOfPlace){
                                if(PlaceType.toString().equals("CAFE")){
                                    mMap.addMarker(new MarkerOptions()
                                            .position(placeLikelihood.getPlace().getLatLng())
                                            .title(placeLikelihood.getPlace().getName())
                                            .draggable(true));

                                    j++;
                                    Log.d(TAG, "갯수: "+ j);
                                }

                            }*/
                            //카페만 찾게 하기 위한
                            /*if(mLikelyPlaceTypes[i].toString().contains("CAFE")){
                                mMap.addMarker(new MarkerOptions()
                                        .position(mLikelyPlaceLatLngs[i])
                                        .title(mLikelyPlaceNames[i])
                                        .draggable(true));
                                j++;
                            }*/



                            //Log.d(TAG, "데이터 :" + mLikelyPlaceNames[i]);
                            /*if(mLikelyPlaceTypes[i].toString().contains("CAFE")) {
                                Log.d(TAG, "타입 :" + mLikelyPlaceTypes[i]);
                            }*/
                            //Log.d(TAG, "타입2 :" + mLikelyPlaceTypes[i].toString().contains("CAFE"));


                            /*if (j > (count - 1)) {
                                Log.d(TAG, "갯수: "+ j);
                                break;
                            }*/
                       /* }

                    }
                    else {
                        Log.e(TAG, "Exception: %s", task.getException());
                    }
                }
            });*/

        } else {
            // The user has not granted permission.
            Log.d(TAG, "The user did not grant location permission.");

            new NRPlaces.Builder()
                    .listener(this)
                    .key("AIzaSyAMI47c8mpMq8Gz2HrAhIF8GMy_tq2EA58")
                    .latlng(mDefaultLocation.latitude, mDefaultLocation.longitude)// 권한 허용 안될 시 기본 위치값(금오공대)
                    .radius(2000) //2000 미터 내에서 검색
                    .type(PlaceType.CAFE)//카페
                    .build()
                    .execute();
            // Prompt the user for permission.
            getLocationPermission();
        }
    }

    @Override
    public void onStart() { //사용자에게 프레그먼트가 보이게 함
        super.onStart();
        mapView.onStart();
        Log.d(TAG, "onStart ");
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
        if (mFusedLocationProviderClient != null) {
            Log.d(TAG, "onStop : removeLocationUpdates");
            mFusedLocationProviderClient.removeLocationUpdates(locationCallback);
        }
    }

    @Override
    public void onResume() {// 유저에게 Fragment가 보여지고, 유저와 상호작용이 가능하게 되는 부분
        super.onResume();
        mapView.onResume();
        Log.d(TAG, "onResume ");
        if (mLocationPermissionGranted) {
            Log.d(TAG, "onResume : requestLocationUpdates");
            mFusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
            if (mMap!=null)
                mMap.setMyLocationEnabled(true);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
        Log.d(TAG, "onPause ");
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
        Log.d(TAG, "onLowMemory ");
    }

    @Override
    public void onDestroyView() { // 프래그먼트와 관련된 View 가 제거되는 단계
        super.onDestroyView();
        if (mFusedLocationProviderClient != null) {
            Log.d(TAG, "onDestroyView : removeLocationUpdates");
            mFusedLocationProviderClient.removeLocationUpdates(locationCallback);
        }
        Log.d(TAG, "onDestroyView ");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onLowMemory();
        Log.d(TAG, "onDestroy ");
    }


}
