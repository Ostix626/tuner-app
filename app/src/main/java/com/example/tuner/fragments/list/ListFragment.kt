package com.example.tuner.fragments.list

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.tuner.R
import com.example.tuner.viewmodel.TunningViewModel
import kotlinx.android.synthetic.main.fragment_list.view.*
import kotlinx.coroutines.InternalCoroutinesApi


class ListFragment : Fragment() {

    @InternalCoroutinesApi
    private lateinit var mTunningViewModel: TunningViewModel

    @InternalCoroutinesApi
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_list, container, false)

        Log.d("threads", Thread.activeCount().toString())

        // RecyclerView
        val adapter = ListAdapter()
        val recyclerView = view.recyclerview
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // TunningViewModel
        mTunningViewModel = ViewModelProvider(this).get(TunningViewModel::class.java)
        mTunningViewModel.readAllData.observe(viewLifecycleOwner, Observer { tunning ->
            adapter.setData(tunning)
        })

        view.floatingActionButton.setOnClickListener {
            findNavController().navigate(R.id.action_listFragment_to_addFragment)
        }

        setHasOptionsMenu(true)

        return view
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.delete_menu, menu)
    }

    @InternalCoroutinesApi
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == R.id.menu_delete) {
            deleteAllTunnings()
        }
        return super.onOptionsItemSelected(item)
    }

    @InternalCoroutinesApi
    private fun deleteAllTunnings() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setPositiveButton("Yes") {_, _ ->
            mTunningViewModel.deleteAllTunnings()
            Toast.makeText(requireContext(), "Successfully removed all tunnings", Toast.LENGTH_SHORT)
        }
        builder.setNegativeButton("No") {_, _ -> }
        builder.setTitle("Delete all tunnings?")
        builder.setMessage("Are you sure you want to delete everything?")
        builder.create().show()
    }
}