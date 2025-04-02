package com.example.androidproject.activities

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

class LayoutFragment : Fragment() {

    companion object {
        private const val LAYOUT_RES_ID = "layout_res_id"

        fun newInstance(layoutResId: Int): LayoutFragment {
            val fragment = LayoutFragment()
            val args = Bundle()
            args.putInt(LAYOUT_RES_ID, layoutResId)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val layoutResId = arguments?.getInt(LAYOUT_RES_ID) ?: 0
        return inflater.inflate(layoutResId, container, false)
    }
}