package com.example.tuner.fragments.list

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.tuner.R
import com.example.tuner.data.TunningViewModel
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

        return view
    }


}