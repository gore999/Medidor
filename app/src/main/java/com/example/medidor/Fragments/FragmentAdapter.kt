package com.example.medidor.Fragments

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class FragmentAdapter (fm: FragmentActivity): FragmentStateAdapter(fm) {
    override fun getItemCount(): Int =2 //equivale a return 2
    override fun createFragment(position: Int): Fragment {
        when(position){
          0-> {return FragmentDistancia()
          }
          1-> {return FragmentSuperficie()
          }
        }
        return Fragment()
    }
}