package com.example.tuner


import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
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
import com.ekn.gruzer.gaugelibrary.Range
import com.lbbento.pitchuptuner.GuitarTuner
import com.lbbento.pitchuptuner.GuitarTunerListener
import com.lbbento.pitchuptuner.audio.PitchAudioRecorder
import com.lbbento.pitchuptuner.service.TunerResult
import io.reactivex.rxkotlin.toSingle
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import java.util.Collections.frequency


class MainActivity : AppCompatActivity() {
    private val REQUEST_RECORD_CODE = 0
    private var freqText : TextView? = null
    private var probText : TextView? = null
    private var noteText : TextView? = null
    private var octaveText : TextView? = null
    private var halfGauge : com.ekn.gruzer.gaugelibrary.HalfGauge? = null


    private val tunningString : String = ""
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
        halfGauge = findViewById(R.id.halfGauge)
        noteText = findViewById(R.id.noteTextView)
        octaveText = findViewById(R.id.octaveTextView)

        chooseTunningButton.setOnClickListener {
            startActivity(Intent(this@MainActivity, NavActivity::class.java))
        }

        val range_red_L = Range()
        range_red_L.color = Color.parseColor("#ce0000")
        range_red_L.from = -50.0
        range_red_L.to = -25.0

        val range_red_R = Range()
        range_red_R.color = Color.parseColor("#ce0000")
        range_red_R.from = 25.0
        range_red_R.to = 50.0

        val range_blue_L = Range()
        range_blue_L.color = Color.parseColor("#E3E500")
        range_blue_L.from = -25.0
        range_blue_L.to = -5.0

        val range_blue_R = Range()
        range_blue_R.color = Color.parseColor("#E3E500")
        range_blue_R.from = 5.0
        range_blue_R.to = 25.0

        val range_green_L = Range()
        range_green_L.color = Color.parseColor("#00b20b")
        range_green_L.from = -5.0
        range_green_L.to = 0.0

        val range_green_R = Range()
        range_green_R.color = Color.parseColor("#00b20b")
        range_green_R.from = 0.0
        range_green_R.to = 5.0

        //add color ranges to gauge
        halfGauge?.addRange(range_red_L)
        halfGauge?.addRange(range_red_R)
        halfGauge?.addRange(range_blue_L)
        halfGauge?.addRange(range_blue_R)
        halfGauge?.addRange(range_green_L)
        halfGauge?.addRange(range_green_R)

        //set min max and current value
        halfGauge?.minValue = -50.0
        halfGauge?.maxValue = 50.0
        halfGauge?.value = 0.0

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

    private fun addElement(arr: Array<ChromaticScale>, element: ChromaticScale): Array<ChromaticScale> {
        val mutableArray = arr.toMutableList()
        mutableArray.add(element)
        return mutableArray.toTypedArray()
    }

    private fun record()
    {
        val dispatcher = fromDefaultMicrophone(22050, 1024, 0)


        var tunningList : List<String>? = listOf(
            "C0", "Db0", "D0", "Eb0", "E0", "F0", "Gb0", "G0", "Ab0", "A0", "Bb0", "B0",
            "C1", "Db1", "D1", "Eb1", "E1", "F1", "Gb1", "G1", "Ab1", "A1", "Bb1", "B1",
            "C2", "Db2", "D2", "Eb2", "E2", "F2", "Gb2", "G2", "Ab2", "A2", "Bb2", "B2",
            "C3", "Db3", "D3", "Eb3", "E3", "F3", "Gb3", "G3", "Ab3", "A3", "Bb3", "B3",
            "C4", "Db4", "D4", "Eb4", "E4", "F4", "Gb4", "G4", "Ab4", "A4", "Bb4", "B4",
            "C5", "Db5", "D5", "Eb5", "E5", "F5", "Gb5", "G5", "Ab5", "A5", "Bb5", "B5",
            "C6", "Db6", "D6", "Eb6", "E6", "F6", "Gb6", "G6", "Ab6", "A6", "Bb6", "B6",
            "C7", "Db7", "D7", "Eb7", "E7", "F7", "Gb7", "G7", "Ab7", "A7", "Bb7", "B7",
        )
//        var tunningScale : List<String>? = null
//        var test : ArrayList<ChromaticScale>? = null

        if (tunningString.length > 0)
        {
            tunningList = tunningString.split(" ") // "E2 A2 D3 G3 B3 E4"
            // TODO: NAPISAT ABECEDNI SORT LISTE
            Log.d("LIST", tunningList.toString())
        }


//        Log.d("SCALE", test.toString())
        val pdh = PitchDetectionHandler { result, e ->
            val probability = result.probability
            val pitchInHz = result.pitch

            runOnUiThread {
                if(probability > 0.92)
                {
                    if (tunningList != null)
                    {
                        val listSize = tunningList.size
                        for (i in 1..listSize-1)
                        {
                            ChromaticScale.valueOf(tunningList[i-1]).frequency
                            if (ChromaticScale.valueOf(tunningList[i-1]).frequency <= pitchInHz && pitchInHz <= ChromaticScale.valueOf(tunningList[i]).frequency )
                            {
                                if (pitchInHz - ChromaticScale.valueOf(tunningList[i-1]).frequency < ChromaticScale.valueOf(tunningList[i]).frequency - pitchInHz)
                                    curent_tone = ChromaticScale.valueOf(tunningList[i-1])
                                else
                                    curent_tone = ChromaticScale.valueOf(tunningList[i])
                                break
                            }
                        }

                    }


//                    for (i in 1..SCALE_SIZE-1)
//                    {
//                        if (SCALE[i-1].frequency <= pitchInHz && pitchInHz <= SCALE[i].frequency )
//                        {
//                            if (pitchInHz - SCALE[i-1].frequency < SCALE[i].frequency - pitchInHz)
//                                curent_tone = SCALE[i-1]
//                            else
//                                curent_tone = SCALE[i]
//
//                            break
//                        }
//                    }



//                    for (f : ChromaticScale in ChromaticScale.values())
//                    {
//                        if (pitchInHz >)
//                        Log.d("frekv", f.frequency.toString())
//                    }
//                    Log.d("SCALE>>>", ChromaticScale.C0.toString())
                    val centsabs = PitchConverter.hertzToAbsoluteCent(pitchInHz.toDouble())
                    val centsrel = PitchConverter.hertzToRelativeCent(pitchInHz.toDouble())
//                    val centsrel = curent_tone?.name
                    val ratiocent = PitchConverter.ratioToCent((pitchInHz / curent_tone?.frequency!!).toDouble())
                    noteText?.text = curent_tone?.tone
                    octaveText?.text = curent_tone?.octave.toString()
                    halfGauge?.value = ratiocent
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