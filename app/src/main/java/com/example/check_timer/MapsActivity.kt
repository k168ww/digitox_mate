package com.example.check_timer

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.os.SystemClock
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.check_timer.databinding.ActivityMapsBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import android.widget.Chronometer
import com.google.android.gms.maps.model.*

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityMapsBinding
    private lateinit var mMap: GoogleMap
    private lateinit var chronometer: Chronometer
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var selectedMarker: Marker? = null
    private var isStopwatchRunning = false
    private var stopwatchBaseTime: Long = 0
    private val LOCATION_PERMISSION_REQUEST_CODE = 1

    private var startLatitude: Double = 0.0
    private var startLongitude: Double = 0.0
    private var finishLatitude: Double = 0.0
    private var finishLongitude: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // TextView 초기화
        chronometer = binding.mapChronometer

        // Chronometer 초기화
        chronometer.base = SystemClock.elapsedRealtime()

        // 인텐트에서 값 받아오기
        val elapsedTimeMillis = intent.getLongExtra("elapsedTimeMillis", 0)
        startLatitude = intent.getDoubleExtra("startLatitude", 0.0)
        startLongitude = intent.getDoubleExtra("startLongitude", 0.0)
        finishLatitude = intent.getDoubleExtra("finishLatitude", 0.0)
        finishLongitude = intent.getDoubleExtra("finishLongitude", 0.0)

        // 스톱워치 초기화 및 설정
        isStopwatchRunning = true
        stopwatchBaseTime = SystemClock.elapsedRealtime() - elapsedTimeMillis
        chronometer.base = SystemClock.elapsedRealtime() - elapsedTimeMillis
        chronometer.stop()

        // 지도 초기화
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as? SupportMapFragment
        mapFragment?.getMapAsync(this)

        // FusedLocationProviderClient 초기화
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // 권한 요청
        requestLocationPermission()
    }

    private fun requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            enableMyLocation()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    private fun enableMyLocation() {
        if (::mMap.isInitialized) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                mMap.isMyLocationEnabled = true

                // 현재 위치로 이동
                fusedLocationClient.lastLocation
                    .addOnSuccessListener { location ->
                        location?.let {
                            val currentLatLng = LatLng(it.latitude, it.longitude)
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 18f))

                            // 현재 위치에 마커 추가
                            mMap.addMarker(MarkerOptions().position(currentLatLng).title("현재 위치"))
                        } ?: run {
                            // 위치가 null인 경우 처리
                            // 메시지 표시 또는 기본 위치 사용 가능
                            val defaultLocation = LatLng(37.56, 126.97)
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 18f))
                        }
                    }
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // 현재 위치 버튼 활성화
        mMap.uiSettings.isMyLocationButtonEnabled = true

        // 권한이 허용되었을 때 현재 위치로 이동
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            enableMyLocation()
        }

        // 기본 위치 설정 (서울)
        val defaultLocation = LatLng(37.56, 126.97)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 18f))

        // 지도 클릭 이벤트 처리
        mMap.setOnMapClickListener { latLng ->
            // 클릭된 위치로 이동
            mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng))

            // 이전에 선택된 마커가 있다면 제거
            selectedMarker?.remove()

            // 클릭된 위치에 마커 추가
            selectedMarker = mMap.addMarker(MarkerOptions().position(latLng).title("클릭된 위치"))

            // 마커를 클릭한 경우 해당 마커의 위치로 이동
            mMap.setOnMarkerClickListener { marker ->
                mMap.animateCamera(CameraUpdateFactory.newLatLng(marker.position))
                true
            }
        }

        // 시작 위치 마커 추가
        if (startLatitude != 0.0 && startLongitude != 0.0) {
            val startLocationLatLng = LatLng(startLatitude, startLongitude)
            val startMarker = mMap.addMarker(MarkerOptions().position(startLocationLatLng).title("시작 위치").snippet("스타트"))
            startMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(startLocationLatLng, 18f))
        }

// 종료 위치 마커 추가
        if (finishLatitude != 0.0 && finishLongitude != 0.0) {
            val finishLocationLatLng = LatLng(finishLatitude, finishLongitude)
            val finishMarker = mMap.addMarker(MarkerOptions().position(finishLocationLatLng).title("종료 위치").snippet("피니쉬"))
            finishMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(finishLocationLatLng, 18f))
        }

// 마커들을 연결하는 선 추가
        if (startLatitude != 0.0 && startLongitude != 0.0 && finishLatitude != 0.0 && finishLongitude != 0.0) {
            val polylineOptions = PolylineOptions()
                .add(LatLng(startLatitude, startLongitude))
                .add(LatLng(finishLatitude, finishLongitude))
                .color(Color.YELLOW)
                .width(15f)

            val polyline: Polyline = mMap.addPolyline(polylineOptions)
        }
    }
}