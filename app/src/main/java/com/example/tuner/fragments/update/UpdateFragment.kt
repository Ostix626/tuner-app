package com.example.tuner.fragments.update

import android.app.AlertDialog
import android.os.Bundle
import android.text.TextUtils
import android.view.*
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.tuner.R
import com.example.tuner.model.Tunning
import com.example.tuner.viewmodel.TunningViewModel
import kotlinx.android.synthetic.main.fragment_add.*
import kotlinx.android.synthetic.main.fragment_add.view.*
import kotlinx.android.synthetic.main.fragment_update.*
import kotlinx.android.synthetic.main.fragment_update.view.*
import kotlinx.coroutines.InternalCoroutinesApi


class UpdateFragment : Fragment() {

    private val args by navArgs<UpdateFragmentArgs>()
    private var tunningTonesArray = mutableListOf<String>()

    @InternalCoroutinesApi
    private lateinit var mTunningViewModel: TunningViewModel

    override fun onResume() {
        super.onResume()
        val tones = resources.getStringArray(R.array.notes)
        val arrayAdapter = ArrayAdapter(requireContext(), R.layout.dropdown_item, tones)
        noteAutoCompleteTextViewUpdate.setAdapter(arrayAdapter)
        val octaves = resources.getStringArray(R.array.octaves)
        val octavesArrayAdapter = ArrayAdapter(requireContext(), R.layout.dropdown_item, octaves)
        octaveAutoCompleteTextViewUpdate.setAdapter(octavesArrayAdapter)
    }


    @InternalCoroutinesApi
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_update, container, false)
        view.updateTunningTonesEditText.setFocusable(false)

        mTunningViewModel = ViewModelProvider(this).get(TunningViewModel::class.java)


        val tmp = args.currentTunning.tunningTones.split(" ") as MutableList<String> // "E2 A2 D3 G3 B3 E4"
        if (tmp.size == 1)
            tunningTonesArray.add(tmp[0])  // za bug kada je samo jedan ton
        else
            tunningTonesArray = tmp


        view.updateTunningNameEditText.setText(args.currentTunning.tunningName)
        view.updateTunningTonesEditText.setText(args.currentTunning.tunningTones)
        Toast.makeText(requireContext(), tunningTonesArray.toString(), Toast.LENGTH_LONG).show()


        view.updateTunningButton.setOnClickListener {
            updateItem()
        }
        // add menu
        setHasOptionsMenu(true)

        view.addUpdate.setOnClickListener{
//            var tmpTone = view.noteAutoCompleteTextView.text.toString().plus(view.octaveAutoCompleteTextView.text)
            var tmpTone : String = view.noteAutoCompleteTextViewUpdate.text.toString()
            val tmpOctave : String = view.octaveAutoCompleteTextViewUpdate.text.toString()
            when (tmpTone) {
                "C#/Db" -> tmpTone = "Db"
                "D#/Eb" -> tmpTone = "Eb"
                "F#/Gb" -> tmpTone = "Gb"
                "G#/Ab" -> tmpTone = "Ab"
                "A#/Bb" -> tmpTone = "Bb"
            }
            tunningTonesArray.add(tmpTone.plus(tmpOctave))
            updateTunningTonesEditText.setText(tunningTonesArray.joinToString(" "))
        }

        view.deleteUpdate.setOnClickListener{
            if (!tunningTonesArray.isEmpty()) tunningTonesArray.removeLast()
            updateTunningTonesEditText.setText(tunningTonesArray.joinToString(" "))
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
        return !(TextUtils.isEmpty(tunningName) || TextUtils.isEmpty(tunningTones))
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.delete_menu, menu)
    }

    @InternalCoroutinesApi
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == R.id.menu_delete) {
            deleteTunning()
        }
        return super.onOptionsItemSelected(item)
    }

    @InternalCoroutinesApi
    private fun deleteTunning() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setPositiveButton("Yes") {_, _ ->
            mTunningViewModel.deleteTunning(args.currentTunning)
            Toast.makeText(requireContext(), "Successfully removed: ${args.currentTunning.tunningName}", Toast.LENGTH_SHORT)
            findNavController().navigate(R.id.action_updateFragment_to_listFragment)
        }
        builder.setNegativeButton("No") {_, _ -> }
        builder.setTitle("Delete ${args.currentTunning.tunningName}?")
        builder.setMessage("Are you sure you want to delete ${args.currentTunning.tunningTones} tunning?")
        builder.create().show()
    }
}