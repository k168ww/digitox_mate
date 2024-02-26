package com.example.check_timer

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.SystemClock
import android.view.KeyEvent
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.check_timer.databinding.ActivityMainBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class MainActivity : AppCompatActivity() {
    // 뒤로가기 버튼을 누른 시각을 저장하는 속성
    private var initTime = 0L

    // 멈춘 시각을 저장하는 속성
    private var pauseTime = 0L

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMainBinding

    private var startLatitude: Double = 0.0
    private var startLongitude: Double = 0.0

    private var finishLatitude: Double = 0.0
    private var finishLongitude: Double = 0.0

    private val LOCATION_PERMISSION_REQUEST_CODE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // FusedLocationProviderClient 초기화
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // 권한 요청
        requestLocationPermission()

        binding.startButton.setOnClickListener {
            // 위치 정보 가져오기
            /*
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return@setOnClickListener
            }
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    location?.let {
                        // 시작 지점의 위도와 경도 저장
                        startLatitude = it.latitude
                        startLongitude = it.longitude
                    } ?: run {
                        // 위치가 null인 경우 처리
                        Toast.makeText(this, "위치 정보를 가져올 수 없습니다.", Toast.LENGTH_SHORT).show()
                    }
                }
             */
            // 광화문으로 위치 지정
            val fixedLocation = LatLng(37.5760222, 126.9769000)
            startLatitude = fixedLocation.latitude
            startLongitude = fixedLocation.longitude


            // 스톱워치 시작 및 버튼 상태 조정
            binding.chronometer.base = SystemClock.elapsedRealtime() + pauseTime
            binding.chronometer.start()
            binding.startButton.isEnabled = false
            binding.finishButton.isEnabled = true
            binding.stopButton.isEnabled = true
        }

        binding.stopButton.setOnClickListener {
            pauseTime = binding.chronometer.base - SystemClock.elapsedRealtime()
            binding.chronometer.stop()
            binding.startButton.isEnabled = true
            binding.finishButton.isEnabled = true
            binding.stopButton.isEnabled = false
        }

        binding.finishButton.setOnClickListener {
            // 위치 정보 가져오기
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return@setOnClickListener
            }
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    location?.let {
                        // 종료 지점의 위도와 경도 저장
                        finishLatitude = it.latitude
                        finishLongitude = it.longitude
                    } ?: run {
                        // 위치가 null인 경우 처리
                        Toast.makeText(this, "위치 정보를 가져올 수 없습니다.", Toast.LENGTH_SHORT).show()
                    }

                    // 수정된 부분: 스톱워치 시간과 위치 정보를 인텐트로 전달
                    val intent = Intent(this, MapsActivity::class.java)
                    intent.putExtra("elapsedTimeMillis", SystemClock.elapsedRealtime() - binding.chronometer.base)
                    intent.putExtra("startLatitude", startLatitude)
                    intent.putExtra("startLongitude", startLongitude)
                    intent.putExtra("finishLatitude", finishLatitude)
                    intent.putExtra("finishLongitude", finishLongitude)
                    startActivity(intent)

                    // 나머지 코드는 그대로 유지
                    pauseTime = 0L
                    binding.chronometer.base = SystemClock.elapsedRealtime()
                    binding.chronometer.stop()
                    binding.startButton.isEnabled = true
                    binding.finishButton.isEnabled = false
                    binding.stopButton.isEnabled = false
                }
        }
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
        // 지도 초기화
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as? SupportMapFragment
        mapFragment?.getMapAsync { googleMap ->
            mMap = googleMap

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

    // 뒤로가기 버튼 이벤트 핸들러
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        // 뒤로가기 버튼을 눌렀을 때 처리
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            // 뒤로가기 버튼을 처음 눌렀거나 누른 지 3초가 지났을 때 처리
            if (System.currentTimeMillis() - initTime > 3000) {
                Toast.makeText(this, "종료하려면 한 번 더 누르세요!!", Toast.LENGTH_SHORT).show()
                initTime = System.currentTimeMillis()
                return true
            }
        }

        return super.onKeyDown(keyCode, event)
    }
}
