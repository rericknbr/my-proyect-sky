package com.example.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.TextView
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.example.R
import com.example.ui.theme.MyApplicationTheme

class FloatingWidgetService : Service() {

    private lateinit var windowManager: WindowManager
    private var floatingView: View? = null

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        showFloatingWidget()
    }

    private fun showFloatingWidget() {
        val layoutParams = WindowManager.LayoutParams().apply {
            type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_PHONE
            }
            format = PixelFormat.TRANSLUCENT
            flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
            width = WindowManager.LayoutParams.WRAP_CONTENT
            height = WindowManager.LayoutParams.WRAP_CONTENT
            gravity = Gravity.TOP or Gravity.START
            x = 100
            y = 100
        }

        // We can dynamically create a clean interactive frame that supports drag-and-drop
        val container = FrameLayout(this)
        
        // Simple custom native view for ultra-compatibility in service overlay contexts
        val overlayView = LayoutInflater.from(this).inflate(android.R.layout.simple_list_item_1, null)
        val textView = overlayView.findViewById<TextView>(android.R.id.text1)
        textView.text = "🟢 Viaje Rentable: Activo"
        textView.setTextColor(0xFFD4FF00.toInt())
        textView.setBackgroundColor(0xFF1B1E24.toInt())
        textView.setPadding(30, 20, 30, 20)
        
        container.addView(overlayView)
        floatingView = container

        // Drag listener
        floatingView?.setOnTouchListener(object : View.OnTouchListener {
            private var initialX = 0
            private var initialY = 0
            private var initialTouchX = 0f
            private var initialTouchY = 0f

            override fun onTouch(v: View, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        initialX = layoutParams.x
                        initialY = layoutParams.y
                        initialTouchX = event.rawX
                        initialTouchY = event.rawY
                        return true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        layoutParams.x = initialX + (event.rawX - initialTouchX).toInt()
                        layoutParams.y = initialY + (event.rawY - initialTouchY).toInt()
                        windowManager.updateViewLayout(floatingView, layoutParams)
                        return true
                    }
                }
                return false
            }
        })

        windowManager.addView(floatingView, layoutParams)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (floatingView != null) {
            windowManager.removeView(floatingView)
        }
    }
}
