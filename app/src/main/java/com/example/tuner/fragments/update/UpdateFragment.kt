package com.example.tuner.fragments.update

import android.os.Bundle
import android.text.TextUtils
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.tuner.R
import com.example.tuner.model.Tunning
import com.example.tuner.viewmodel.TunningViewModel
import kotlinx.android.synthetic.main.fragment_update.*
import kotlinx.android.synthetic.main.fragment_update.view.*
import kotlinx.coroutines.InternalCoroutinesApi


class UpdateFragment : Fragment() {

    private val args by navArgs<UpdateFragmentArgs>()

    @InternalCoroutinesApi
    private lateinit var mTunningViewModel: TunningViewModel

    @InternalCoroutinesApi
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_update, container, false)

        mTunningViewModel = ViewModelProvider(this).get(TunningViewModel::class.java)

        view.updateTunningNameEditText.setText(args.currentTunning.tunningName)
        view.updateTunningTonesEditText.setText(args.currentTunning.tunningTones)

        view.updateTunningButton.setOnClickListener {
            updateItem()
        }
        return view
    }

    @InternalCoroutinesApi
    private fun updateItem()
    {
        val tunningName = updateTunningNameEditText.text.toString()
        val tunningTones = updateTunningTonesEditText.text.toString()

        if (inputCheck(tunningName, tunningTones))
        {
            // create tunning object
            val updatedTunning = Tunning(args.currentTunning.id, tunningName, tunningTones)
            // update current tunning
            mTunningViewModel.updateTunning(updatedTunning)
            Toast.makeText(requireContext(), "Tunning updated", Toast.LENGTH_SHORT).show()
            // navigate back
            findNavController().navigate(R.id.action_updateFragment_to_listFragment)
        }
        else
        {
            Toast.makeText(requireContext(), "Input fields are empty", Toast.LENGTH_SHORT).show()
        }

    }

    private fun inputCheck(tunningName : String, tunningTones : String) : Boolean {
        return !(TextUtils.isEmpty(tunningName) && TextUtils.isEmpty(tunningTones))
    }
}