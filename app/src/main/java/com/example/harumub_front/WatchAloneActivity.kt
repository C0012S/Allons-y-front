package com.example.harumub_front

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.amazonaws.auth.AWSCredentials
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener
import com.amazonaws.mobileconnectors.s3.transferutility.TransferNetworkLossHandler
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility
import com.amazonaws.regions.Region
import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.AmazonS3Client
import kotlinx.android.synthetic.main.activity_watch_alone.*
import java.io.File
import java.lang.Thread.sleep
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.HashMap

class WatchAloneActivity : AppCompatActivity() {
    private lateinit var retrofitBuilder: RetrofitBuilder
    private lateinit var retrofitInterface : RetrofitInteface

    private var cameraThread: CameraThread? = null
    lateinit var cameraHandler: CameraHandler

    val WATCH_START = 0
    val WATCH_END = 1

    private var imageCapture: ImageCapture? = null

    private lateinit var outputDirectory: File
    private lateinit var cameraExecutor: ExecutorService

    // 현재 로그인하고 있는 사용자 아이디, 선택한 영화 아이디
    private val id = intent.getStringExtra("user_id")
    private val movie_title = intent.getStringExtra("movie_title")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_watch_alone)

        retrofitBuilder = RetrofitBuilder
        retrofitInterface = retrofitBuilder.api

        // 현재 로그인하고 있는 사용자 아이디 / 영화 아이디 (수정 필요) --수민 작성
//        var userid = ""
//        var movieid = ""

        // 검색 페이지에서 전달받은 인텐트 데이터 확인
        if (intent.hasExtra("user_id")&&intent.hasExtra("movie_title")) {
            Log.d("WatchAloneActivity", "검색에서 받아온 id : $id , movie title : $movie_title")
        } else {
            Log.e("WatchAloneActivity", "가져온 데이터 없음")
        }

        // Request camera permissions
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }

        cameraHandler = CameraHandler()

        // 감상시작 버튼 클릭
        // 감상시작 버튼 누르면 -> 노드에 map 전송
        watch_start.setOnClickListener {
            var map = HashMap<String, String>()
            map.put("id", id!!)
            map.put("movieTitle", movie_title!!)
            map.put("signal", "start")

            var call = retrofitInterface.executeWatchAloneStart(map)

            call!!.enqueue(object : Callback<Void?> {
                override fun onResponse(call: Call<Void?>, response: Response<Void?>) {
                    if(response.code() == 200){
                        Toast.makeText(this@WatchAloneActivity, "감상시작 신호 보내기 성공", Toast.LENGTH_SHORT).show()
                    }
                    else if (response.code() == 400){
                        Toast.makeText(this@WatchAloneActivity, "감상시작 신호 보내기 실패", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<Void?>, t: Throwable) {
                    Toast.makeText(this@WatchAloneActivity, t.message, Toast.LENGTH_SHORT).show()
                }
            })

            Log.d("감상 시작 : ", SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.KOREA).format(System.currentTimeMillis()))
//            takePhoto() // 감상 시작하자마자 캡처
            sleep(10000)

            if (cameraThread != null) {
                cameraThread!!.endThread()
            }
            cameraThread = CameraThread()
            cameraThread!!.start()
        }

        outputDirectory = getOutputDirectory()

        cameraExecutor = Executors.newSingleThreadExecutor()

        // 감상종료 버튼 클릭
        watch_end.setOnClickListener {
            val intent = Intent(applicationContext, AddreviewActivity::class.java)
            startActivity(intent)

            var map = HashMap<String, String>()
            map.put("signal", "end")

            var call = retrofitInterface.executeWatchAloneEnd(map)

            call!!.enqueue(object : Callback<Void?> {
                override fun onResponse(call: Call<Void?>, response: Response<Void?>) {
                    if(response.code() == 200){
                        Toast.makeText(this@WatchAloneActivity, "감상종료 신호 보내기 성공", Toast.LENGTH_SHORT).show()

                        // 감상 리뷰 작성 페이지로 이동 (액티비티 -> 프래그먼트)
//                        supportFragmentManager.beginTransaction()
//                            .replace(R.id.watch_alone, AddreviewFragment())
//                            .commit()
                        val intent = Intent(applicationContext, AddreviewActivity::class.java)
                        intent.putExtra("user_id", id)
                        intent.putExtra("movie_id", movie_title)
                        startActivity(intent)

                        Log.d("text : ", "선택")
                    }
                    else if (response.code() == 400){
                        Toast.makeText(this@WatchAloneActivity, "감상종료 신호 보내기 실패", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<Void?>, t: Throwable) {
                    Toast.makeText(this@WatchAloneActivity, t.message, Toast.LENGTH_SHORT).show()
                }
            })

            cameraHandler.sendEmptyMessage(WATCH_END)
            Log.d("감상 : ", "종료되었습니다.")
        }
    }

    inner class CameraThread : Thread() {
        var i = 0
        var ended = false

        fun endThread() {
            ended = true
        }

        override fun run() {
            super.run()

            while (!ended) {
                var message: Message = Message.obtain()
                message.what = WATCH_START

                takePhoto("Emotion")
                sleep(10000)
                takePhoto("Emotion")
                sleep(10000)
                takePhoto("Emotion")

                takePhoto("Eye")
                sleep(1000)
                takePhoto("Eye")
                sleep(1000)
                takePhoto("Eye")
                cameraHandler.sendMessage(message)

                sleep(8000)
            }
        }
    }

    inner class CameraHandler : Handler() {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)

            when (msg.what) {
                WATCH_START -> {

                }
                WATCH_END -> {
                    cameraThread?.endThread()
                }
                else -> {

                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults:
        IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) { // 요청 코드가 올바른지 확인
            if (allPermissionsGranted()) { // 권한이 부여되면 startCamera() 함수 호출
                startCamera()
            } else { // 권한이 부여되지 않은 경우 사용자에게 권한이 부여되지 않았음을 알리는 Toast 메시지 표시
                Toast.makeText(this,
                    "Permissions not granted by the user.",
                    Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun takePhoto(s3Bucket_FolderName: String?) {
        // Get a stable reference of the modifiable image capture use case
        val imageCapture = imageCapture ?: return

        // Create time-stamped output file to hold the image
        val photoFile = File(
            outputDirectory,
            SimpleDateFormat(FILENAME_FORMAT, Locale.KOREA
            ).format(System.currentTimeMillis()) + ".jpg") // 이미지를 저장할 파일을 만든다.

        // Create output options object which contains file + metadata
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        // Set up image capture listener, which is triggered after photo has been taken
        imageCapture.takePicture(
            outputOptions, ContextCompat.getMainExecutor(this), object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val savedUri = Uri.fromFile(photoFile)
                    val msg = "Photo capture succeeded: $savedUri"
//                    Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
                    Log.d(TAG, msg)
                    uploadWithTransferUtilty(s3Bucket_FolderName, photoFile.name, photoFile)
                }
            }
        )
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener(Runnable {
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(viewFinder.surfaceProvider)
                }

            imageCapture = ImageCapture.Builder()
                .build()

            // Select front camera as a default
            val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA // 전면 카메라 // 후면 카메라 : DEFAULT_BACK_CAMERA

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture)

            } catch(exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(this))
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    private fun getOutputDirectory(): File {
        val mediaDir = externalMediaDirs.firstOrNull()?.let {
            File(it, resources.getString(R.string.app_name)).apply { mkdirs() } }
        return if (mediaDir != null && mediaDir.exists())
            mediaDir else filesDir
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    companion object {
        private const val TAG = "CameraXBasic"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }

    // S3 Bucket Upload
    fun uploadWithTransferUtilty(s3Bucket_FolderName: String?, fileName: String?, file: File?) {
        val awsCredentials: AWSCredentials =
            BasicAWSCredentials("access_Key", "secret_Key") // IAM User의 (accessKey, secretKey)
        val s3Client = AmazonS3Client(awsCredentials, Region.getRegion(Regions.US_EAST_1))
        val transferUtility =
            TransferUtility.builder().s3Client(s3Client).context(this.applicationContext).build()
        TransferNetworkLossHandler.getInstance(this.applicationContext)
        val uploadObserver =
            transferUtility.upload("bucket_Name/" + s3Bucket_FolderName, fileName, file) // (bucket name, file 이름, file 객체)
        uploadObserver.setTransferListener(object : TransferListener {
            override fun onStateChanged(id: Int, state: TransferState) {
                if (state === TransferState.COMPLETED) {
                    // Handle a completed upload
                    Log.d("S3 Bucket ", "Upload Completed!")

                    // S3 Bucket에 file 업로드 후 Emulator에서 삭제
                    if (file != null) {
                        file.delete()
                        Log.d("Emulator : ", "파일 삭제")
                    }
                    else {
                        Log.d("Emulator : ", "삭제할 파일이 없습니다.")
                    }
                }
            }

            override fun onProgressChanged(id: Int, current: Long, total: Long) {
                val done = (current.toDouble() / total * 100.0).toInt()
                Log.d("MYTAG", "UPLOAD - - ID: \$id, percent done = \$done")
            }

            override fun onError(id: Int, ex: java.lang.Exception) {
                Log.d("MYTAG", "UPLOAD ERROR - - ID: \$id - - EX:$ex")
            }
        })
    }
}