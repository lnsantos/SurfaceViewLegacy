package com.nepoapp.cameranativesurfaceview

import android.content.Context
import android.hardware.Camera
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import java.io.IOException
import java.lang.Exception
import kotlin.math.abs

class CameraView(context: Context, private val camera: Camera) : SurfaceView(context),
    SurfaceHolder.Callback {

    // Callback param1 = width , param2 = height
    var previewSizeReadyCallback: ((width: Int, height: Int) -> Unit)? = null
    private var previewSize: Camera.Size? = null

    companion object {
        @JvmStatic
        private val TAG = this::class.java.simpleName.toString()
    }

    init {
        // this holder is o SurfaceHolder
        holder.addCallback(this)

    }

    override fun surfaceChanged(
        surfaceHolder: SurfaceHolder,
        format: Int,
        width: Int,
        height: Int
    ) {

        // determine the dimensions and screen position of the Surface
        if (surfaceHolder.surface == null) return

        currentCameraStopPreview()

        try {
            camera.apply {
                parameters.apply {
                    setPreviewSize(getPreviewWidth(),getPreviewHeight())
                }
                setPreviewDisplay(surfaceHolder)
            }
            camera.startPreview()
        }catch (e: Exception){
            Log.e(TAG, "Occurred an error to start preview camera device...")
        }
    }

    override fun surfaceDestroyed(surfaceHolder: SurfaceHolder) {
        Log.i(TAG, "CameraView finished with success.")
    }

    override fun surfaceCreated(surfaceHolder: SurfaceHolder) {
        try {
            camera.setPreviewDisplay(surfaceHolder)
            camera.startPreview()
        } catch (e: IOException) {
            Log.e(TAG, "Occurred an problem with visualization camera of device...${e.printStackTrace()}"
            )
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width  = getWidthView(widthMeasureSpec)
        val height = getHeightView(heightMeasureSpec)

        setMeasuredDimension(width,height)

        try {
            val previewSizes = camera.parameters.supportedPreviewSizes
            if (previewSizes != null){
                previewSize = getOptimalPreviewSize(previewSizes, width, height)
                previewSizeReadyCallback?.invoke(getPreviewWidth(),getPreviewHeight())
            }
        }catch (e: Exception){
            Log.e(TAG,"Fail in resolve size preview...")
        }
    }

    private fun getOptimalPreviewSize(currentCameraPreviewSizes: List<Camera.Size>, width: Int, height: Int): Camera.Size? {

        val ASPECT_TOLERANCE = 0.1
        val targetRatio = height.toDouble() / width

        var optimalSize: Camera.Size? = null
        var minDiff = Double.MAX_VALUE

        if (currentCameraPreviewSizes == null) return null

        for (previewSize in currentCameraPreviewSizes){
            val previewWidht = previewSize.width
            val previewHeight = previewSize.height

            val ratio = previewWidht.toDouble() / previewHeight

            if (abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue
            if (abs(previewHeight - height) < minDiff){
                optimalSize = previewSize
                minDiff = abs(previewHeight - height).toDouble()
            }
        }

        if (optimalSize == null){

            minDiff = Double.MAX_VALUE

            for (previewSize in currentCameraPreviewSizes){
                val previewHeight = previewSize.height

                if (abs(previewHeight - height) < minDiff){
                    optimalSize = previewSize
                    minDiff = abs(previewHeight - height).toDouble()
                }
            }
        }

        return optimalSize
    }

    private fun getPreviewWidth()  = previewSize?.width  ?: 0
    private fun getPreviewHeight() = previewSize?.height ?: 0

    private fun getWidthView(specWidth:Int) = View.resolveSize(suggestedMinimumWidth, specWidth)
    private fun getHeightView(specHeight:Int) = View.resolveSize(suggestedMinimumHeight, specHeight)

    private fun currentCameraStopPreview() {
        try {
            camera.stopPreview()
        }catch (e: Exception){
            Log.i(TAG, "Occurred an problem with, stop preview of camera...")
        }
    }
}