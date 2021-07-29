package com.example.tuner


import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import be.tarsos.dsp.AudioProcessor
import be.tarsos.dsp.io.android.AudioDispatcherFactory
import be.tarsos.dsp.io.android.AudioDispatcherFactory.fromDefaultMicrophone
import be.tarsos.dsp.pitch.PitchDetectionHandler
import be.tarsos.dsp.pitch.PitchProcessor
import be.tarsos.dsp.pitch.PitchProcessor.PitchEstimationAlgorithm
import be.tarsos.dsp.util.PitchConverter
import com.lbbento.pitchuptuner.GuitarTuner
import com.lbbento.pitchuptuner.GuitarTunerListener
import com.lbbento.pitchuptuner.audio.PitchAudioRecorder
import com.lbbento.pitchuptuner.service.TunerResult
import java.util.Collections.frequency


class MainActivity : AppCompatActivity() {
    private val REQUEST_RECORD_CODE = 0
    private var freqText : TextView? = null
    private var probText : TextView? = null

    private val allNotes = arrayOf("A", "A#", "B", "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#")
    private val concertPitch = 440
    private val SCALE = ChromaticScale.values()
    private val SCALE_SIZE = SCALE.size
    private var tone_name = ""
    private var curent_tone : ChromaticScale? = null

    private var isInitailized = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setPermission()
        freqText = findViewById(R.id.frequency)
        probText = findViewById(R.id.probability)
    }

    private fun setPermission() {
        val permission = ContextCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO)
        if (permission != PackageManager.PERMISSION_GRANTED)
        {
            Log.d("TAG> Permission", "RECORD_AUDIO permission denied")
        }
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.RECORD_AUDIO))
        {
            val builder = AlertDialog.Builder(this)
            builder.setMessage("Permission to access the michrophone is required for this app to record guitar audio")
            builder.setTitle("Permission request")
            builder.setPositiveButton("OK")
            {
                dialog, which ->
                Log.d("TAG> Permission", "RECORD_AUDIO clicked")
                makeRequest()
            }
            val dialog=builder.create()
            dialog.show()
        }
        else
        {
            makeRequest()
        }
    }

    private fun makeRequest()
    {
        ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.RECORD_AUDIO), REQUEST_RECORD_CODE)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode)
        {
            REQUEST_RECORD_CODE -> {
                if(grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED)
                {
                    Log.d("TAG> Permission", "RECORD_AUDIO has been denied")
                }
                else
                {
                    Log.d("TAG> Permission", "RECORD_AUDIO has been granted --> start recording...")
                    record()
//                    startTuner()
//                    TarsosDSP()
                    //startRecording()
                }
            }
        }
    }

    private fun record()
    {
        val dispatcher = fromDefaultMicrophone(22050, 1024, 0)

        val pdh = PitchDetectionHandler { result, e ->
            val probability = result.probability
            val pitchInHz = result.pitch

            runOnUiThread {
                if(probability > 0.92)
                {
                    for (i in 1..SCALE_SIZE-1)
                    {
//                        Log.d("frekv", SCALE[i-1].frequency.toString())
//                        Log.d("frekv", SCALE[i].frequency.toString())
                        if (SCALE[i-1].frequency <= pitchInHz && pitchInHz <= SCALE[i].frequency )
                        {
                            if (pitchInHz - SCALE[i-1].frequency < SCALE[i].frequency - pitchInHz)
                                curent_tone = SCALE[i-1]
                            else
                                curent_tone = SCALE[i]

//                            tone_name = SCALE[i-1].name
//                            curent_tone = SCALE[i-1]
                            break
                        }
                    }
//                    for (f : ChromaticScale in ChromaticScale.values())
//                    {
//                        if (pitchInHz >)
//                        Log.d("frekv", f.frequency.toString())
//                    }
//                    Log.d("SCALE>>>", ChromaticScale.C0.toString())
                    val centsabs = PitchConverter.hertzToAbsoluteCent(pitchInHz.toDouble())
//                    val centsrel = PitchConverter.hertzToRelativeCent(pitchInHz.toDouble())
                    val centsrel = curent_tone?.name
                    val ratiocent = PitchConverter.ratioToCent((pitchInHz / curent_tone?.frequency!!).toDouble())
                    freqText?.text = pitchInHz.toString() + "Hz"
                    probText?.text = "Probability: " + probability.toString()
                    val abs : TextView = findViewById(R.id.centsabs)
                    abs?.text = "abs> " + centsabs.toString()
                    val rel : TextView = findViewById(R.id.centsabs2)
                    rel?.text = "rel> " + centsrel.toString()
                    val ratio : TextView = findViewById(R.id.centsabs3)
                    ratio?.text = "ratio> " + ratiocent.toString()
                }
                else if ( pitchInHz < 0)
                {
                    freqText?.text = "0 Hz"
                    probText?.text = "Probability: 0"
                }
            }
        }
        val p: AudioProcessor = PitchProcessor(PitchEstimationAlgorithm.FFT_YIN, 22050F, 1024, pdh)
        dispatcher.addAudioProcessor(p)
        Thread(dispatcher, "Audio Dispatcher").start()
    }



//    private fun startTuner()
//    {
//        val dispatcher = AudioDispatcherFactory.fromDefaultMicrophone(22050, 1024, 0)
//        val pdh = PitchDetectionHandler { result, _ ->
//            val pitchInHz = result.pitch
//            runOnUiThread {
//
//                //tuner = findViewById(R.id.circleTunerView)
//                isInitailized = true
//                val frequencyText = findViewById<TextView>(R.id.frequency)
//                //frequencyText.text = getString(R.string.frequency, pitchInHz.toString())
//                frequencyText.tag = pitchInHz
//                frequencyText.text = pitchInHz.toString()
//                val frequency = pitchInHz.toDouble()
////                if (frequency != -1.0) {
////                    if (frequencyRange.size < 10) frequencyRange.add(frequency)
////                    else {
////                        val tempList = frequencyRange.clone() as ArrayList<Double>
////                        tempList.add(frequency)
////                        if (calculateSD(frequencyRange) > calculateSD(tempList)) {
////                            frequencyRange = replaceOutlier(frequencyRange, frequency)
////                            currentPage = viewPager.currentItem
////                            val variance = tuner.updateIndicator2Angle(frequencyText, frequencyList[currentPage].toDouble())
////                            if (pitchInHz == -1f) warningText.text = null
////                            else {
////                                if (pitchInHz > (pitchInHz - variance) * 2 ) {
////                                    warningText.text = getString(R.string.octave_too_high)
////                                    warningText.setTextColor(ContextCompat.getColor(baseContext, R.color.circle_tuner_view_default_out_of_tune_color))
////                                    tuner.outOfTuneChangeColor()
////                                }
////                                else if (pitchInHz < (pitchInHz - variance) / 2) {
////                                    warningText.text = getString(R.string.octave_too_low)
////                                    warningText.setTextColor(ContextCompat.getColor(baseContext, R.color.circle_tuner_view_default_out_of_tune_color))
////                                    tuner.outOfTuneChangeColor()
////                                }
////                                else {
////                                    if (abs(variance) / (pitchInHz - variance) < 0.01) {
////                                        warningText.text = getString(R.string.octave_tune_perfect)
////                                        warningText.setTextColor(ContextCompat.getColor(baseContext, R.color.circle_tuner_view_default_in_tune_color))
////                                        tuner.inTuneChangeColor()
////                                    }
////                                    else {
////                                        if (variance > 0) warningText.text = getString(R.string.octave_tune_high)
////                                        else warningText.text = getString(R.string.octave_tune_low)
////                                        warningText.setTextColor(ContextCompat.getColor(baseContext, R.color.circle_tuner_view_default_inner_circle_color))
////                                        tuner.inRangeChangeColor()
////                                    }
////                                }
////                            }
////                        }
////                    }
////                }
//            }
//        }
//    }

//    private fun TarsosDSP()
//    {
//        val dispatcher: AudioDispatcher = AudioDispatcherFactory.fromDefaultMicrophone(44100, 4096, 3072)
//        val pdh = PitchDetectionHandler { res, _ ->
//            val pitchInHz: Float = res.pitch
//            runOnUiThread { processing.closestNote(pitchInHz)}
//        }
//        val pitchProcessor: AudioProcessor =
//                PitchProcessor(PitchProcessor.PitchEstimationAlgorithm.FFT_YIN,
//                        44100F, 4096, pdh)
//        dispatcher.addAudioProcessor(pitchProcessor)
//
//        val audioThread = Thread(dispatcher, "Audio Thread")
//        audioThread.start()
//    }
//
//    fun closestNote(pitchInHz: Float) {
//        if (pitchInHz != -1F) {
//            val roundHz = closestPitch(pitchInHz)
//            val i = (round(log2(roundHz / concertPitch) * 12)).toInt()
//            val closestNote = allNotes[(i % 12 + 12) % 12]
//            freqText?.text = closestNote // updates note text
//        }
//    }
//    private fun closestPitch(pitchInHz: Float): Float {
//        val i = (round(log2(pitchInHz / concertPitch) * 12)).toInt()
//        val closestPitch = concertPitch * 2.toDouble().pow(i.toDouble() / 12)
//        return closestPitch.toFloat()
//    }



    private fun startRecording()
    {
        val audioRecorder = PitchAudioRecorder(AudioRecord(MediaRecorder.AudioSource.DEFAULT,
                44100,
                AudioFormat.CHANNEL_IN_DEFAULT,
                AudioFormat.ENCODING_PCM_16BIT,
                AudioRecord.getMinBufferSize(44100, AudioFormat.CHANNEL_IN_DEFAULT, AudioFormat.ENCODING_PCM_16BIT)))


        //Create listener
        val guitarTunerListener = object: GuitarTunerListener {

            override fun onNoteReceived(tunerResult: TunerResult) {
                showHz(tunerResult)
            }

            override fun onError(e: Throwable) {
                Log.e("TAG> Recording Error", e.toString())
//                showError(e)
            }
        }

        //Start listening to Guitar tuner
        val guitarTuner = GuitarTuner(audioRecorder, guitarTunerListener)
        guitarTuner.start()
    }

    private fun showHz(frequency: TunerResult)
    {
        Log.e("freq>>>", frequency.diffFrequency.toString())
        freqText?.text = (frequency.expectedFrequency + frequency.diffFrequency).toString() + " Hz"
    }

//    private fun hasPermission() : Boolean {
//        return ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
//    }
//
//    private fun requestPermission() {
//        var permission = mutableListOf<String>
//
//        if(!hasPermission()){
//
//        }
//    }
}