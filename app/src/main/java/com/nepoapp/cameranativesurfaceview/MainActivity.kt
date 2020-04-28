package com.nepoapp.cameranativesurfaceview

import android.hardware.Camera
import android.media.MediaRecorder
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlin.coroutines.CoroutineContext

class MainActivity : AppCompatActivity() , CoroutineScope{

    private var camera        : Camera?        = null
    private var surfacePreview: CameraView?    = null
    private var mediaRecord   : MediaRecorder? = null

    private var isRecording   : Boolean        = false
    private var inPreviewMode : Boolean        = false
    private var outputUri     : Uri?           = null

    private lateinit var job : Job

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val uri = intent.getParcelableExtra<Uri>(MediaStore.EXTRA_OUTPUT)

        if (uri != null){
            outputUri = uri
        }

        setupListenerView()

        job = Job()
    }

    private fun setupListenerView() {
        buttonCapture.setOnClickListener(buttonCaptureLister)
    }

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    private val buttonCaptureLister = View.OnClickListener {
        val action = intent.action

        if (action == MediaStore.ACTION_VIDEO_CAPTURE){
            recordVideo()
        }
    }

    private fun recordVideo() {
        TODO("Not yet implemented")
    }

}
