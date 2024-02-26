package com.example.sol_application

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.util.Pair
import com.example.sol_application.databinding.ActivityApplicationRunningBinding
import com.google.android.material.datepicker.MaterialDatePicker

class ApplicationRunning : AppCompatActivity() {
    private lateinit var binding: ActivityApplicationRunningBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityApplicationRunningBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val today = MaterialDatePicker.todayInUtcMilliseconds()
        val materialDatePicker = MaterialDatePicker.Builder.dateRangePicker()
            .setSelection(Pair.create(today, today)) // 오늘 날짜로 설정
            .build()

        binding.btSetDay.setOnClickListener {
            // 버튼이 클릭 되었을 때 할 일
            materialDatePicker.show(supportFragmentManager, "Tag_picker")
            materialDatePicker.addOnPositiveButtonClickListener {
                binding.selectedDay.text = materialDatePicker.headerText
            }
        }

        binding.bgSetTime.setOnClickListener {
            supportFragmentManager.beginTransaction()
                .replace(R.id.viewPager, NumberPicker())
                .commit()
        }
    }
}