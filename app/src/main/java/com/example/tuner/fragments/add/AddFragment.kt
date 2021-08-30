package com.example.tuner.fragments.add

import android.os.Bundle
import android.renderscript.ScriptGroup
import android.text.TextUtils
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.tuner.R
import com.example.tuner.model.Tunning
import com.example.tuner.viewmodel.TunningViewModel
import kotlinx.android.synthetic.main.fragment_add.*
import kotlinx.android.synthetic.main.fragment_add.view.*
import kotlinx.coroutines.InternalCoroutinesApi

class AddFragment : Fragment() {

    @InternalCoroutinesApi
    private lateinit var mTunningViewModel : TunningViewModel

    private val octaves = arrayOf("0 ","1 ", "2 ", "3 ", "4 ", "5 ", "6 ", "7 ")
    private var tunningTonesArray = mutableListOf<String>()

//    private var _binding: FragmentFirstBinding? = null
//    private var _binding: ScriptGroup.Binding? = null
//    private val binding get() = _binding!!

    override fun onResume() {
        super.onResume()
        val tones = resources.getStringArray(R.array.notes)
        val arrayAdapter = ArrayAdapter(requireContext(), R.layout.dropdown_item, tones)
        noteAutoCompleteTextView.setAdapter(arrayAdapter)
        val octaves = resources.getStringArray(R.array.octaves)
        val octavesArrayAdapter = ArrayAdapter(requireContext(), R.layout.dropdown_item, octaves)
        octaveAutoCompleteTextView.setAdapter(octavesArrayAdapter)
    }

    @InternalCoroutinesApi
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_add, container, false)

        view.tunningTonesEditText.setFocusable(false)

//        _binding = FragmentFirstBinding.inflate(inflater, container, false)
//        _binding = ScriptGroup.Binding().inflate(inflater, container, false)

//        val tones = resources.getStringArray(R.array.notes)
//        val arrayAdapter = ArrayAdapter(requireContext(), R.layout.dropdown_item, tones)
//        view.noteAutoCompleteTextView.setAdapter(arrayAdapter)

        view.add.setOnClickListener{
//            var tmpTone = view.noteAutoCompleteTextView.text.toString().plus(view.octaveAutoCompleteTextView.text)
            var tmpTone : String = view.noteAutoCompleteTextView.text.toString()
            val tmpOctave : String = view.octaveAutoCompleteTextView.text.toString()
            when (tmpTone) {
                "C#/Db" -> tmpTone = "Db"
                "D#/Eb" -> tmpTone = "Eb"
                "F#/Gb" -> tmpTone = "Gb"
                "G#/Ab" -> tmpTone = "Ab"
                "A#/Bb" -> tmpTone = "Bb"
            }
            tunningTonesArray.add(tmpTone.plus(tmpOctave))
            tunningTonesEditText.setText(tunningTonesArray.joinToString(" "))
        }

        view.delete.setOnClickListener{
            if (!tunningTonesArray.isEmpty()) tunningTonesArray.removeLast()
            tunningTonesEditText.setText(tunningTonesArray.joinToString(" "))
        }

//        val arrayAdapter = ArrayAdapter(requireContext(), R.layout.support_simple_spinner_dropdown_item, octaves)
//        spinner.adapter = arrayAdapter
//        spinner.onItemSelectedListener = object  : AdapterView.OnItemSelectedListener{
//            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
//                TODO("Not yet implemented")
//            }
//
//            override fun onNothingSelected(p0: AdapterView<*>?) {
//                TODO("Not yet implemented")
//            }
//        }

        mTunningViewModel = ViewModelProvider(this).get(TunningViewModel::class.java)

        view.addNewTunningButton.setOnClickListener {
            insertDataToDatabase()
        }

        return view
    }

    @InternalCoroutinesApi
    private fun insertDataToDatabase() {
        val tunningName = tunningNameEditText.text.toString()
        val tunningTones = tunningTonesEditText.text.toString()

        if (inputCheck(tunningName, tunningTones)) {
            // Create Tunning Object
            val tunning = Tunning(0, tunningName, tunningTones)
            // Add Data to Database
            mTunningViewModel.addTunning(tunning)
            Toast.makeText(requireContext(), "Tunning added", Toast.LENGTH_SHORT).show()
            // Navigate back
            findNavController().navigate(R.id.action_addFragment_to_listFragment)
        } else {
            Toast.makeText(requireContext(), "Input fields are empty", Toast.LENGTH_SHORT).show()
        }
    }

    private fun inputCheck(tunningName : String, tunningTones : String) : Boolean {
        return !(TextUtils.isEmpty(tunningName) || TextUtils.isEmpty(tunningTones))
    }
}