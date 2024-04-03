package com.example.medidor

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.medidor.databinding.ActivityConvertBinding
import com.example.medidor.databinding.ActivityMainBinding

class ConvertActivity : AppCompatActivity() {
    private lateinit var binding: ActivityConvertBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_convert)
        binding.buttonBinding.setOnClickListener{

        }
    }
}