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
import be.tarsos.dsp.AudioDispatcher
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
import java.lang.Thread.activeCount
import java.util.*
import java.util.Collections.frequency
import kotlin.collections.ArrayList
import kotlin.math.round
import kotlin.math.roundToInt
import kotlin.math.roundToLong


class MainActivity : AppCompatActivity() {
    private val REQUEST_RECORD_CODE = 0
    private var freqText : TextView? = null
    private var centsText : TextView? = null
    private var noteText : TextView? = null
    private var octaveText : TextView? = null
    private var halfGauge : com.ekn.gruzer.gaugelibrary.HalfGauge? = null
    private var tunningNameText : TextView? = null
    private var tunningTonesText : TextView? = null

    private var dispatcher: AudioDispatcher? = null
    private var flag: Boolean = true

    private var tunningString : String = ""
    private var curent_tone : ChromaticScale? = null

    var tunningList1 : List<String>? = listOf(
            "C0", "Db0", "D0", "Eb0", "E0", "F0", "Gb0", "G0", "Ab0", "A0", "Bb0", "B0",
            "C1", "Db1", "D1", "Eb1", "E1", "F1", "Gb1", "G1", "Ab1", "A1", "Bb1", "B1",
            "C2", "Db2", "D2", "Eb2", "E2", "F2", "Gb2", "G2", "Ab2", "A2", "Bb2", "B2",
            "C3", "Db3", "D3", "Eb3", "E3", "F3", "Gb3", "G3", "Ab3", "A3", "Bb3", "B3",
            "C4", "Db4", "D4", "Eb4", "E4", "F4", "Gb4", "G4", "Ab4", "A4", "Bb4", "B4",
            "C5", "Db5", "D5", "Eb5", "E5", "F5", "Gb5", "G5", "Ab5", "A5", "Bb5", "B5",
            "C6", "Db6", "D6", "Eb6", "E6", "F6", "Gb6", "G6", "Ab6", "A6", "Bb6", "B6",
            "C7", "Db7", "D7", "Eb7", "E7", "F7", "Gb7", "G7", "Ab7", "A7", "Bb7", "B7",
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setPermission()
        freqText = findViewById(R.id.frequency)
        centsText = findViewById(R.id.cents)
        halfGauge = findViewById(R.id.halfGauge)
        noteText = findViewById(R.id.noteTextView)
        octaveText = findViewById(R.id.octaveTextView)
        tunningNameText = findViewById(R.id.tunningNameTV)
        tunningTonesText = findViewById(R.id.tunningTonesTV)

        setUpHalfGuage()

        tunningNameText?.text = intent.getStringExtra("tunning_name")
        tunningTonesText?.text = intent.getStringExtra("tunning_tones")

        if (tunningNameText?.text == "") tunningNameText?.visibility = View.GONE
        if (tunningTonesText?.text == "") tunningTonesText?.visibility = View.GONE


        chooseTunningButton.setOnClickListener {
            startActivity(Intent(this@MainActivity, NavActivity::class.java))
        }

    }


    private fun sortTones(toneList: ArrayList<String>): List<String>
    {
        val size = toneList.size
        var min_index = 0

        for (i in 0..size - 1) {
            min_index = i
            for (j in (i + 1)..size - 1) {
                if(ChromaticScale.valueOf(toneList[j]).frequency < ChromaticScale.valueOf(toneList[min_index]).frequency) {
                    min_index = j
                }
            }
            val tmp = toneList[i]
            toneList[i] = toneList[min_index]
            toneList[min_index] = tmp
        }
        return toneList.toList()
    }


    override fun onStart() {
        flag = true
        tunningString = intent.getStringExtra("tunning_tones").toString()

        if (tunningString.length > 1 && !tunningString.equals("null") && tunningString!= null)
        {
            val unsortedList = tunningString.split(" ") // "E2 A2 D3 G3 B3 E4"
            if (unsortedList.distinct().size > 1)
                tunningList1 =  sortTones(unsortedList.distinct().toList() as ArrayList<String>)
            else
                tunningList1 = unsortedList.distinct().toList()
        }
        super.onStart()
    }

    override fun onResume() {
        flag = true
        super.onResume()
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
        dispatcher = fromDefaultMicrophone(22050, 1024, 0)

        var tunningList = tunningList1

        val pdh = PitchDetectionHandler { result, e ->
            if (flag == true) {
                val probability = result.probability
                val pitchInHz = result.pitch
                runOnUiThread(
                        Runnable()
                        {

                            if (probability > 0.92) {
                                if (tunningList != null) {
                                    val listSize = tunningList!!.size
                                    if (listSize >= 3) {
                                        for (i in 1..listSize - 1) {
                                            if (ChromaticScale.valueOf(tunningList[i - 1]).frequency <= pitchInHz && pitchInHz <= ChromaticScale.valueOf(tunningList[i]).frequency)
                                            {
//                                                if (compareTones(ChromaticScale.valueOf(tunningList[i - 1]).frequency, ChromaticScale.valueOf(tunningList[i]).frequency, pitchInHz))
//                                                    curent_tone = ChromaticScale.valueOf(tunningList[i])
//                                                else
//                                                    curent_tone = ChromaticScale.valueOf(tunningList[i - 1])
//                                                break
                                                if (pitchInHz - ChromaticScale.valueOf(tunningList[i - 1]).frequency < ChromaticScale.valueOf(tunningList[i]).frequency - pitchInHz)
                                                    curent_tone = ChromaticScale.valueOf(tunningList[i - 1])
                                                else
                                                    curent_tone = ChromaticScale.valueOf(tunningList[i])
                                                break
                                            }
                                        }
                                        if (pitchInHz <= ChromaticScale.valueOf(tunningList[0]).frequency)
                                            curent_tone = ChromaticScale.valueOf(tunningList[0])
                                        else if (pitchInHz >= ChromaticScale.valueOf(tunningList[listSize - 1]).frequency)
                                            curent_tone = ChromaticScale.valueOf(tunningList[listSize - 1])

                                    } else if (listSize == 2) {
                                        if (ChromaticScale.valueOf(tunningList[0]).frequency <= pitchInHz &&
                                                pitchInHz - ChromaticScale.valueOf(tunningList[0]).frequency < ChromaticScale.valueOf(tunningList[1]).frequency - pitchInHz) {
                                            //                                    if (pitchInHz - ChromaticScale.valueOf(tunningList[0]).frequency < ChromaticScale.valueOf(tunningList[1]).frequency - pitchInHz)
                                            curent_tone = ChromaticScale.valueOf(tunningList[0])
                                        } else
                                            curent_tone = ChromaticScale.valueOf(tunningList[1])
                                        if (pitchInHz <= ChromaticScale.valueOf(tunningList[0]).frequency) curent_tone = ChromaticScale.valueOf(tunningList[0])
                                        else if (pitchInHz >= ChromaticScale.valueOf(tunningList[1]).frequency) curent_tone = ChromaticScale.valueOf(tunningList[listSize - 1])
                                    } else if (listSize == 1) curent_tone = ChromaticScale.valueOf(tunningList[0])
                                }

                                val ratiocent = PitchConverter.ratioToCent((pitchInHz / curent_tone?.frequency!!).toDouble())
                                noteText?.text = curent_tone?.tone
                                octaveText?.text = curent_tone?.octave.toString()
                                halfGauge?.value = ratiocent
                                centsText?.text = ratiocent.roundToInt().toString() + " cents"
                                freqText?.text = pitchInHz.roundToInt().toString() + " Hz"

                                val name: String? = intent.getStringExtra("tunning_name")
                                val tones: String? = intent.getStringExtra("tunning_tones")
                                tunningNameText?.text = name
                                tunningTonesText?.text = tones
                                changeCurrentTone(ratiocent)
                            } else if (pitchInHz < 0) { // -1 ako ne prepozna
//                                freqText?.text = "0 Hz"
                            }
                        }
                )
            }
            //run on ui thread finish
        }
        val p: AudioProcessor = PitchProcessor(PitchEstimationAlgorithm.FFT_YIN, 22050F, 1024, pdh)
        dispatcher?.addAudioProcessor(p)
        Thread(dispatcher, "Audio Dispatcher").start()
    }

    override fun onPause() {

        flag = false
        super.onPause()
    }



    private fun startRecording()
    {
        val audioRecorder = PitchAudioRecorder(AudioRecord(MediaRecorder.AudioSource.DEFAULT,
                44100,
                AudioFormat.CHANNEL_IN_DEFAULT,
                AudioFormat.ENCODING_PCM_16BIT,
                AudioRecord.getMinBufferSize(44100, AudioFormat.CHANNEL_IN_DEFAULT, AudioFormat.ENCODING_PCM_16BIT)))

        val guitarTunerListener = object: GuitarTunerListener {

            override fun onNoteReceived(tunerResult: TunerResult) {
                showHz(tunerResult)
            }

            override fun onError(e: Throwable) {
                Log.e("TAG> Recording Error", e.toString())
            }
        }

        val guitarTuner = GuitarTuner(audioRecorder, guitarTunerListener)
        guitarTuner.start()
    }

    private fun showHz(frequency: TunerResult)
    {
        Log.e("freq>>>", frequency.diffFrequency.toString())
        freqText?.text = (frequency.expectedFrequency + frequency.diffFrequency).toString() + " Hz"
    }

    private fun compareTones(t1: Float, t2: Float, hz: Float) : Boolean
    {
        val mid = (t1 + t2) * 0.5
        if (hz >= mid)
            return true
        return false
    }

    private fun setUpHalfGuage() {
        val range_red_L = Range()
        range_red_L.color = Color.parseColor("#ED4856")
        range_red_L.from = -50.0
        range_red_L.to = -25.0

        val range_red_R = Range()
        range_red_R.color = Color.parseColor("#ED4856")
        range_red_R.from = 25.0
        range_red_R.to = 50.0

        val range_yellow_L = Range()
        range_yellow_L.color = Color.parseColor("#FFEA7F")
        range_yellow_L.from = -25.0
        range_yellow_L.to = -5.0

        val range_yellow_R = Range()
        range_yellow_R.color = Color.parseColor("#FFEA7F")
        range_yellow_R.from = 5.0
        range_yellow_R.to = 25.0

        val range_green_L = Range()
        range_green_L.color = Color.parseColor("#68e7ba")
        range_green_L.from = -5.0
        range_green_L.to = 0.0

        val range_green_R = Range()
        range_green_R.color = Color.parseColor("#68e7ba")
        range_green_R.from = 0.0
        range_green_R.to = 5.0

        halfGauge?.addRange(range_red_L)
        halfGauge?.addRange(range_red_R)
        halfGauge?.addRange(range_yellow_L)
        halfGauge?.addRange(range_yellow_R)
        halfGauge?.addRange(range_green_L)
        halfGauge?.addRange(range_green_R)

        halfGauge?.minValue = -50.0
        halfGauge?.maxValue = 50.0
        halfGauge?.value = 0.0

//        halfGauge?.setNeedleColor(Color.parseColor("#68e7ba"))
        halfGauge?.setNeedleColor(Color.parseColor("#35363b"))
//        halfGauge?.setNeedleColor(Color.parseColor("#808080"))
//        halfGauge?.minValueTextColor = Color.parseColor("#68e7ba")
//        halfGauge?.maxValueTextColor = Color.parseColor("#68e7ba")
    }

    private fun changeCurrentTone (cents : Double)
    {
        if ( -6 <= cents && cents <= 6)
        {
            noteText?.setTextColor(Color.parseColor("#68e7ba"))
            octaveText?.setTextColor(Color.parseColor("#68e7ba"))
        }
        else if ( -25 <= cents && cents <= 25 )
        {
            noteText?.setTextColor(Color.parseColor("#FFEA7F"))
            octaveText?.setTextColor(Color.parseColor("#FFEA7F"))
        }
        else
        {
            noteText?.setTextColor(Color.parseColor("#ED4856"))
            octaveText?.setTextColor(Color.parseColor("#ED4856"))
        }

    }


}