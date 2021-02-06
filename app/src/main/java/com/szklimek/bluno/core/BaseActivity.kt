package com.szklimek.bluno.core

import android.content.Intent
import android.os.Bundle

import androidx.annotation.Nullable
import androidx.appcompat.app.AppCompatActivity

abstract class BaseActivity : AppCompatActivity() {

    override fun onCreate(@Nullable savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.v("onCreate: ${javaClass.simpleName}")
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

    override fun onDestroy() {
        super.onDestroy()
        Log.v("onDestroy: ${javaClass.simpleName}")
    }
}
