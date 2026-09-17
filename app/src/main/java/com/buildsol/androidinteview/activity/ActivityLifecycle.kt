package com.buildsol.androidinteview.activity

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp


class ActivityLifecycle(): ComponentActivity(){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("ActivityLifecycle", "We Are in onCreate Stage")
        setContent {
            Scaffold(modifier = Modifier.fillMaxSize()) {padding->
                Box(modifier = Modifier.padding(padding).fillMaxSize()){
                    Text(
                        text = "Activity Lifecycle",
                        fontSize = 30.sp
                    )
                }
            }
        }
    }
    override fun onStart() {
        super.onStart()
        Log.d("ActivityLifecycle", "We Are in onStart Stage")
    }
    override fun onResume() {
        super.onResume()
        Log.d("ActivityLifecycle", "We Are in onResume Stage")
    }
    override fun onPause() {
        super.onPause()
        Log.d("ActivityLifecycle", "We Are in onPause Stage")
    }
    override fun onStop() {
        super.onStop()
        Log.d("ActivityLifecycle", "We Are in onStop Stage")
    }
    override fun onDestroy() {
        super.onDestroy()
        Log.d("ActivityLifecycle", "We Are in onDestroy Stage")
    }
    override fun onRestart() {
        super.onRestart()
        Log.d("ActivityLifecycle", "We Are in onRestart Stage")
    }
}