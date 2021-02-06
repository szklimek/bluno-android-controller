package com.szklimek.bluno.core

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.annotation.Nullable
import androidx.fragment.app.Fragment

abstract class BaseFragment : Fragment() {

    override fun onAttach(context: Context) {
        super.onAttach(context)
        Log.v("onAttach: ${javaClass.simpleName}")
    }

    override fun onCreate(@Nullable savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.v("onCreate: ${javaClass.simpleName}")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.v("onViewCreated: ${javaClass.simpleName}")
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        Log.v("onActivityCreated at ${javaClass.simpleName} activity: ${activity?.javaClass?.simpleName}")
    }

    override fun onStart() {
        super.onStart()
        Log.v("onStart: ${javaClass.simpleName}")
    }

    override fun onResume() {
        super.onResume()
        Log.v("onResume: ${javaClass.simpleName}")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.v("onActivityResult: ${javaClass.simpleName} requestCode: $requestCode, resultCode: $resultCode, data: $data")
    }

    override fun onPause() {
        super.onPause()
        Log.v("onPause: ${javaClass.simpleName}")
    }

    override fun onStop() {
        super.onStop()
        Log.v("onStop: ${javaClass.simpleName}")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.v("onDestroyView: ${javaClass.simpleName}")
    }

    override fun onDetach() {
        super.onDetach()
        Log.v("onDetach: ${javaClass.simpleName}")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.v("onDestroy: ${javaClass.simpleName}")
    }
}
