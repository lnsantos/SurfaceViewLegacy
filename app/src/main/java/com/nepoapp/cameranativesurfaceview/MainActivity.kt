package com.nepoapp.cameranativesurfaceview

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Camera
import android.media.CamcorderProfile
import android.media.MediaRecorder
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintSet
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

class MainActivity : Activity() , CoroutineScope{

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

        // path where file must be created
        val uri = intent.getParcelableExtra<Uri>(MediaStore.EXTRA_OUTPUT)

        if (uri != null){
            outputUri = uri
        }

        setupListenerView()

        job = Job()
    }

    override fun onResume() {
        super.onResume()
        if (PermissionUtils.validateExistPermissions(this)){
           openCamera()
        }else{
            PermissionUtils.requestPermissions(this,REQUEST_PERMISSIONS)
        }
    }

    override fun onPause() {
        super.onPause()
        releaseMediaRecorder()
        releaseCamera()

        if (isRecording){
            outputUri?.let { contentResolver.delete(it, null,null) }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        job?.cancel()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        setResult(Activity.RESULT_CANCELED)
        outputUri?.let { contentResolver.delete(it, null,null) }
    }

    private fun releaseCamera() {
        if (camera != null){
            camera?.release()
            camera = null

            surfacePreview?.holder?.removeCallback(surfacePreview)
        }
    }

    private fun releaseMediaRecorder() {
        mediaRecord?.run {
            reset()
            release()
        }

        // release camera for others app of device use
        camera?.lock()
        mediaRecord = null
    }

    private fun setupListenerView() {
        buttonCapture.setOnClickListener(buttonCaptureLister)
    }

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    private val buttonCaptureLister = View.OnClickListener {

            recordVideo()

    }

    private fun recordVideo() {
        if (isRecording){
            finishRecording()
        }else{
            launch {
                val success = withContext(Dispatchers.Default){
                    prepareRecording()
                    true
                }

                if (success){
                    mediaRecord?.start()
                    buttonCapture.text = "PARAR DE GRAVAR"
                    isRecording = true
                }else{
                    releaseMediaRecorder()
                }
            }
        }
    }

    private fun prepareRecording() : Boolean{

        // block others app use camera of device
        camera?.unlock()

        outputUri?.let { uri ->
            try {
                val fileDescription = contentResolver
                    .openFileDescriptor(uri,"rw")?.fileDescriptor

                mediaRecord = MediaRecorder().apply {
                    setCamera(camera)
                    setAudioSource(MediaRecorder.AudioSource.CAMCORDER)
                    setVideoSource(MediaRecorder.VideoSource.CAMERA)
                    setVideoEncoder(MediaRecorder.VideoEncoder.H264)
                    setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH))
                    setOutputFile(fileDescription)
                    setMaxDuration(5000) // 5 second
                    setOnInfoListener { mr, what, extra ->
                        if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED){
                            finishRecording()
                        }
                    }
                    setPreviewDisplay(surfacePreview?.holder?.surface)
                }
                mediaRecord?.prepare()
            }catch (e: Exception){
                releaseMediaRecorder()
                return false
            }
        }
        return true
    }

    private fun finishRecording() {
        mediaRecord?.stop()
        releaseMediaRecorder()

        camera?.lock()
        isRecording = false

        val intent = Intent()
        intent.data = outputUri

        Toast.makeText(this,outputUri.toString(),Toast.LENGTH_LONG).show()
        buttonCapture.text = "Iniciar gravação"

        setResult(RESULT_OK, intent)
        finish()
    }

    private fun openCamera() {
        try {
            camera = Camera.open()

            camera?.let { cam ->
                surfacePreview = CameraView(this,cam).apply {
                    previewSizeReadyCallback = { width, height ->
                        val constraintSet = ConstraintSet()
                        constraintSet.clone(rootLayout)
                        constraintSet.setDimensionRatio(R.id.previewCameraView,"h,$width:$height")
                        constraintSet.applyTo(rootLayout)
                    }
                }

                // drawn visualization camera in frameLayout
                previewCameraView.addView(surfacePreview)
                cam.parameters.apply {
                    focusMode = Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE
                }
            }
        }catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.none { it == PackageManager.PERMISSION_DENIED }){
            openCamera()
        }
    }

    companion object{
        const val REQUEST_PERMISSIONS = 1
    }

}
