package com.example.tuner.fragments.list

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.tuner.MainActivity
import com.example.tuner.R
import com.example.tuner.model.Tunning
import kotlinx.android.synthetic.main.tunning_row.view.*
import java.security.AccessController.getContext

class ListAdapter (private var mContext: Context): RecyclerView.Adapter<ListAdapter.MyViewHolder>() {

    private var tunningList = emptyList<Tunning>()

    class MyViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.tunning_row,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {

        val currentItem = tunningList[position]
        holder.itemView.tunningNameItem.text = currentItem.tunningName
        holder.itemView.tonesItem.text = currentItem.tunningTones
        holder.itemView.editTunningButton.setOnClickListener {
            val action = ListFragmentDirections.actionListFragmentToUpdateFragment(currentItem)
            holder.itemView.findNavController().navigate(action)
        }

        holder.itemView.tunningItem.setOnClickListener {
            val intent = Intent(holder.itemView.context, MainActivity::class.java)
            intent.putExtra("tunning_name", currentItem.tunningName)
            intent.putExtra("tunning_tones", currentItem.tunningTones)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            holder.itemView.context.startActivity(intent)
        }

    }

    override fun getItemCount(): Int {
        return tunningList.size
    }

    fun setData(tunning: List<Tunning>) {
        this.tunningList = tunning
        notifyDataSetChanged()
    }
}