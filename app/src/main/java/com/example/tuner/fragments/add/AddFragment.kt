package com.example.tuner.fragments.add

import android.os.Bundle
import android.text.TextUtils
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

    @InternalCoroutinesApi
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_add, container, false)

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
        return !(TextUtils.isEmpty(tunningName) && TextUtils.isEmpty(tunningTones))
    }
}