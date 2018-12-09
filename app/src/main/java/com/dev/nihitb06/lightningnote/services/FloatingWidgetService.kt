package com.dev.nihitb06.lightningnote.services

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.PixelFormat
import android.graphics.Point
import android.os.Build
import android.os.CountDownTimer
import android.os.Handler
import android.os.IBinder
import android.provider.Settings
import android.view.*
import android.widget.EditText
import com.dev.nihitb06.lightningnote.R
import com.dev.nihitb06.lightningnote.databaseutils.LightningNoteDatabase
import com.dev.nihitb06.lightningnote.databaseutils.entities.Note
import kotlinx.android.synthetic.main.layout_floating_widget.view.*
import kotlin.math.roundToInt

class FloatingWidgetService : Service() {

    private lateinit var windowManager: WindowManager
    private lateinit var floatingWidgetView: View
    private lateinit var removeFloatingWidgetView: View

    private lateinit var collapsedView: View
    private lateinit var expandedView: View
    private lateinit var removeImageView: View

    private val windowSize = Point()
    private var isLeft = true

    override fun onCreate() {
        super.onCreate()

        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        windowManager.defaultDisplay.getSize(windowSize)

        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        removeImageView = addRemoveView(inflater)
        val viewPair = addFloatingWidgetView(inflater)
        collapsedView = viewPair.first
        expandedView = viewPair.second

        implementClickListeners()
        initializeOnTouchListener()
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)

        windowManager.defaultDisplay.getSize(windowSize)

        val layoutParams = floatingWidgetView.layoutParams as WindowManager.LayoutParams
        if(newConfig?.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            val barHeight = getStatusBarHeight()
            if(layoutParams.y + floatingWidgetView.height + barHeight > windowSize.y) {
                layoutParams.y = windowSize.y - (floatingWidgetView.height + barHeight)
                windowManager.updateViewLayout(floatingWidgetView,  layoutParams)
            }
        } else if(newConfig?.orientation == Configuration.ORIENTATION_PORTRAIT) {
            if(layoutParams.x > windowSize.x)
                resetPosition(windowSize.x.toFloat())
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onDestroy() {
        windowManager.removeView(floatingWidgetView)
        windowManager.removeView(removeFloatingWidgetView)

        super.onDestroy()
    }

    private fun addFloatingWidgetView(inflater: LayoutInflater): Pair<View, View> {
        floatingWidgetView = inflater.inflate(R.layout.layout_floating_widget, null)

        val widgetParams = WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                else
                    WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
        )
        widgetParams.gravity = Gravity.TOP or Gravity.START

        widgetParams.x = 0
        widgetParams.y = 100

        windowManager.addView(floatingWidgetView, widgetParams)

        return Pair(floatingWidgetView.findViewById(R.id.collapsedView), floatingWidgetView.findViewById(R.id.expandedView))
    }
    private fun addRemoveView(inflater: LayoutInflater): View {
        removeFloatingWidgetView = inflater.inflate(R.layout.layout_floating_widget_remove, null)

        val removeParams =  WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                else
                    WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                PixelFormat.TRANSLUCENT
        )
        removeParams.gravity = Gravity.TOP or Gravity.START

        removeFloatingWidgetView.visibility = View.GONE
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            if(Settings.canDrawOverlays(this))
                windowManager.addView(removeFloatingWidgetView, removeParams)
        else
            windowManager.addView(removeFloatingWidgetView, removeParams)

        return removeFloatingWidgetView.findViewById(R.id.removeFloatingWidget)
    }

    private fun implementClickListeners() {
        expandedView.icon.setOnClickListener { onFloatingWidgetClick() }
        expandedView.btnSave.setOnClickListener {
            Thread {
                val text = expandedView.findViewById<EditText>(R.id.etNoteText).text

                if(text.contains('.')) {
                    LightningNoteDatabase.getDatabaseInstance(this).noteDao().insertNote(Note(text.split('.')[0], text.toString()))
                } else {
                    LightningNoteDatabase.getDatabaseInstance(this).noteDao().insertNote(Note(text.split(' ')[0], text.toString()))
                }

                expandedView.etNoteText.setText("")
            }.start()
            collapsedView.performClick()
        }

        collapsedView.setOnClickListener { onFloatingWidgetClick() }
        floatingWidgetView.setOnLongClickListener {
            onFloatingWidgetLongClick()
            return@setOnLongClickListener true
        }
    }

    private fun initializeOnTouchListener() {
        collapsedView.setOnTouchListener(object: View.OnTouchListener {
            var timeStart = 0L; var timeEnd = 0L
            var isLongCLick = false; var inBound = false
            var removeImageWidth = 0; var removeImageHeight = 0

            val longClickHandler = Handler()
            val longClickRunnable = Runnable {
                isLongCLick = true
                removeFloatingWidgetView.visibility = View.VISIBLE

                floatingWidgetView.performLongClick()
            }

            val layoutParams = floatingWidgetView.layoutParams as WindowManager.LayoutParams

            var xInit = 0f; var yInit = 0f; var xInitTouch = 0; var yInitTouch = 0
            var xFinal = 0f; var yFinal = 0f

            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                val xCurrent = event?.rawX ?: 0f; val yCurrent = event?.rawY ?: 0f
                when(event?.action) {
                    MotionEvent.ACTION_DOWN -> {
                        timeStart = System.currentTimeMillis()

                        longClickHandler.postDelayed(longClickRunnable, 600)

                        removeImageWidth = removeImageView.layoutParams.width; removeImageHeight = removeImageView.layoutParams.height

                        xInit = xCurrent; yInit = yCurrent
                        xInitTouch = layoutParams.x; yInitTouch = layoutParams.y

                        return true
                    }

                    MotionEvent.ACTION_UP -> {
                        isLongCLick = false

                        removeFloatingWidgetView.visibility = View.GONE
                        removeImageView.layoutParams.width = removeImageWidth
                        removeImageView.layoutParams.height = removeImageHeight

                        longClickHandler.removeCallbacks(longClickRunnable)

                        if(inBound) {
                            stopSelf()
                            inBound = false
                            return true
                        }

                        val xDiff = xCurrent - xInit
                        val yDiff = yCurrent - yInit

                        if(Math.abs(xDiff) < 5 || Math.abs(yDiff) < 5) {
                            timeEnd = System.currentTimeMillis()

                            if(timeEnd - timeStart < 300 && !isLongCLick) {
                                collapsedView.performClick()
                            }
                        }

                        yFinal = yInitTouch + yDiff
                        val barHeight = getStatusBarHeight()
                        if(yFinal < 0) {
                            yFinal = 0f
                        } else if(yFinal + floatingWidgetView.height + barHeight > windowSize.y) {
                            yFinal = (windowSize.y - (floatingWidgetView.height + barHeight)) * 1.0f
                        }

                        layoutParams.y = yFinal.toInt()
                        (floatingWidgetView.layoutParams as WindowManager.LayoutParams).y = yFinal.toInt()
                        inBound = false

                        resetPosition(xCurrent)

                        return true
                    }

                    MotionEvent.ACTION_MOVE -> {
                        val xDiff = xCurrent - xInit
                        val yDiff = yCurrent - yInit

                        xFinal = xInitTouch + xDiff
                        yFinal = yInitTouch + yDiff

                        if(isLongCLick) {
                            val xBoundStart = windowSize.x/2 - (removeImageWidth*1.4).toInt()
                            val xBoundEnd = windowSize.x/2 + (removeImageWidth*1.4).toInt()
                            val yBoundTop = windowSize.y - (removeImageHeight*1.4).toInt()

                            if((xCurrent >= xBoundStart && xCurrent <= xBoundEnd) && yCurrent >= yBoundTop) {
                                inBound = true

                                if(removeImageView.layoutParams.height == removeImageHeight) {
                                    removeImageView.layoutParams.width = (removeImageWidth * 1.4).toInt()
                                    removeImageView.layoutParams.height = (removeImageHeight * 1.4).toInt()

                                    val removeParams = removeFloatingWidgetView.layoutParams as WindowManager.LayoutParams
                                    windowManager.updateViewLayout(removeFloatingWidgetView, removeParams)
                                }
                            } else {
                                inBound = false

                                removeImageView.layoutParams.width = removeImageWidth
                                removeImageView.layoutParams.height = removeImageHeight
                            }
                        }

                        layoutParams.x = xFinal.toInt()
                        layoutParams.y = yFinal.toInt()

                        windowManager.updateViewLayout(floatingWidgetView, layoutParams)

                        return true
                    }

                    else -> return false
                }
            }
        })
    }

    private fun onFloatingWidgetLongClick() {
        val removeParams = removeFloatingWidgetView.layoutParams as WindowManager.LayoutParams

        val x = (windowSize.x - removeFloatingWidgetView.width) / 2
        val y = windowSize.y - (removeFloatingWidgetView.height + getStatusBarHeight())

        removeParams.x = x
        removeParams.y = y

        windowManager.updateViewLayout(removeFloatingWidgetView, removeParams)
    }
    private fun onFloatingWidgetClick() {
        if(isViewCollapsed()) {
            collapsedView.visibility = View.GONE
            expandedView.visibility = View.VISIBLE

            toggleKeypadEnabled(true)
        } else {
            collapsedView.visibility = View.VISIBLE
            expandedView.visibility = View.GONE

            toggleKeypadEnabled(false)
        }
    }

    private fun toggleKeypadEnabled(enable: Boolean) {
        windowManager.removeViewImmediate(floatingWidgetView)

        val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                else
                    WindowManager.LayoutParams.TYPE_PHONE,
                if(enable) WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL else WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
        )

        windowManager.addView(floatingWidgetView, params)
    }

    private fun isViewCollapsed() = collapsedView.visibility == View.VISIBLE
    private fun getStatusBarHeight() = Math.ceil(25.0 * resources.displayMetrics.density).toInt() + 16

    private fun resetPosition(x_current: Float) {
        if(x_current <= windowSize.x / 2) {
            isLeft = true
            moveToLeft(x_current)
        } else {
            isLeft = false
            moveToRight(x_current)
        }
    }
    private fun moveToLeft(x_current: Float) {
        val layoutParams = floatingWidgetView.layoutParams as WindowManager.LayoutParams
        object: CountDownTimer(500, 5) {
            override fun onTick(millisUntilFinished: Long) {
                layoutParams.x = 0 - (x_current*x_current*(500 - millisUntilFinished) / 5).roundToInt()
                windowManager.updateViewLayout(floatingWidgetView, layoutParams)
            }

            override fun onFinish() {
                layoutParams.x = 0
                windowManager.updateViewLayout(floatingWidgetView, layoutParams)
            }
        }.start()
    }
    private fun moveToRight(x_current: Float) {
        val layoutParams = floatingWidgetView.layoutParams as WindowManager.LayoutParams
        object: CountDownTimer(500, 5) {
            override fun onTick(millisUntilFinished: Long) {
                layoutParams.x = windowSize.x
                + (x_current*x_current*(500 - millisUntilFinished) / 5).roundToInt()
                - floatingWidgetView.width

                windowManager.updateViewLayout(floatingWidgetView, layoutParams)
            }

            override fun onFinish() {
                layoutParams.x = windowSize.x - floatingWidgetView.width
                windowManager.updateViewLayout(floatingWidgetView, layoutParams)
            }
        }.start()
    }
}
