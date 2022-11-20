package com.example.gabriel.soundrecorder.recorder

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.arch.lifecycle.MutableLiveData
import android.icu.text.SimpleDateFormat
import android.media.MediaRecorder
import android.os.Build
import android.os.Environment
import java.io.IOException
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*




class RecorderRepository{
    private var viewModel: RecorderViewModel? = null
    companion object {
        @Volatile
        private var instance: RecorderRepository? = null

        fun getInstance() =
            instance ?: synchronized(this) {
                instance ?: RecorderRepository().also { instance = it }
            }
    }

    private var output: String? = null
    private var mediaRecorder: MediaRecorder? = null
    private val dir: File = File(Environment.getExternalStorageDirectory().absolutePath + "/soundrecorder/")


    var time = Calendar.getInstance().time
    val formatter = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss")
    val current = formatter.format(time)

    // TODO set length from UI
    private var default_recordingTime: Long = 10
    private var recordingTime: Long = default_recordingTime
    // Might be useful for blacklog
    private var timer = Timer()
    private val recordingTimeString = MutableLiveData<String>()

    init {
        try{
            // create a File object for the parent directory
            val recorderDirectory = File(Environment.getExternalStorageDirectory().absolutePath+"/soundrecorder/")
            // have the object build the directory structure, if needed.
            recorderDirectory.mkdirs()
        }catch (e: IOException){
            e.printStackTrace()
        }

        if(dir.exists()){
//            val count = dir.listFiles().size
            val count = current
            output = Environment.getExternalStorageDirectory().absolutePath + "/soundrecorder/recording_number_"+count+".mp3"
        }

        mediaRecorder = MediaRecorder()

        mediaRecorder?.setAudioSource(MediaRecorder.AudioSource.MIC)
        mediaRecorder?.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
        mediaRecorder?.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
        mediaRecorder?.setOutputFile(output)
    }

    @SuppressLint("RestrictedApi")
    fun startRecording() {
        try {
            println("Starting recording!")
            mediaRecorder?.prepare()
            mediaRecorder?.start()
            startTimer()
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    @SuppressLint("RestrictedApi")
    fun stopRecording(){
        // TODO stop recording when time is up
        println("Stopping recording!")
        mediaRecorder?.stop()
        mediaRecorder?.release()
        stopTimer()
        resetTimer()

        initRecorder()
    }


    @TargetApi(Build.VERSION_CODES.N)
    @SuppressLint("RestrictedApi")
    fun pauseRecording(){
        stopTimer()
        mediaRecorder?.pause()
    }

    @TargetApi(Build.VERSION_CODES.N)
    @SuppressLint("RestrictedApi")
    fun resumeRecording(){
        timer = Timer()
        startTimer()
        mediaRecorder?.resume()
    }
    private fun initRecorder() {
        mediaRecorder = MediaRecorder()

        if(dir.exists()){
//            val count = dir.listFiles().size
            val count = current

            output = Environment.getExternalStorageDirectory().absolutePath + "/soundrecorder/recording"+count+".mp3"
        }

        mediaRecorder?.setAudioSource(MediaRecorder.AudioSource.MIC)
        mediaRecorder?.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
        mediaRecorder?.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
        mediaRecorder?.setOutputFile(output)
    }

    private fun startTimer(){
        timer.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                if(recordingTime > 0) {
                    recordingTime -= 1
                    updateDisplay()
                } else {
                    stopRecording()
//                    startRecording()
                }
            }
        }, 1000, 1000)
    }

    private fun stopTimer(){
        timer.cancel()
    }

    private fun restartRecording() {
        // TODO stop current recording and start new one
        // Option: save all recordings in AWS S3 and set removing
        // records from S3 after 24 or 48 hours
    }

    private fun resetTimer() {
        timer.cancel()
        recordingTime = default_recordingTime
        // TODO add manual limit setting
        recordingTimeString.postValue("15:00")
    }

    private fun updateDisplay(){
        val minutes = recordingTime / (60)
        val seconds = recordingTime % 60
        val str = String.format("0%d:%02d", minutes, seconds)
        recordingTimeString.postValue(str)
    }

    fun getRecordingTime() = recordingTimeString
}