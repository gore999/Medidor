package com.example.medidor

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.example.medidor.Fragments.FragmentAdapter
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class PropiosActivity : AppCompatActivity() {
    private val adapter by lazy { FragmentAdapter(this) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_propios)
        val pager=findViewById<ViewPager2>(R.id.viewpager)
        pager.adapter=adapter
        val tabLy=findViewById<TabLayout>(R.id.tabLayout)
        val tabLayoutMediator=TabLayoutMediator(tabLy,pager,TabLayoutMediator.TabConfigurationStrategy{
            tab,position->
            when(position+1){
                1->tab.text="Distancia"
                2->tab.text="Área"
            }

        })
        tabLayoutMediator.attach()
/*

    ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

 */
    }
}