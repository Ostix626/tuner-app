package com.example.tuner.fragments.list

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.tuner.R
import com.example.tuner.model.Tunning
import kotlinx.android.synthetic.main.tunning_row.view.*

class ListAdapter: RecyclerView.Adapter<ListAdapter.MyViewHolder>() {

    private var tunningList = emptyList<Tunning>()

    class MyViewHolder (itemView: View): RecyclerView.ViewHolder(itemView) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.tunning_row, parent, false))
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {

        val currentItem = tunningList[position]
        holder.itemView.tunningNameItem.text = currentItem.tunningName
        holder.itemView.tonesItem.text = currentItem.tunningTones
        holder.itemView.tunningItem.setOnClickListener {
            val action = ListFragmentDirections.actionListFragmentToUpdateFragment(currentItem)
            holder.itemView.findNavController().navigate(action)
        }

//        holder.itemView.setOnClickListener(View.OnClickListener {
//             val pref = mContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit()
//            pref.putString("profileId", user.getUid())
//            pref.apply()
//
//            val prefs = user.getUid()
//
//            val intent = Intent(holder.itemView.context, ProfileActivity::class.java)
//            intent.putExtra("uid", prefs)
//            mContext.startActivity(intent)
//        })

    }

    override fun getItemCount(): Int {
        return tunningList.size
    }

    fun setData(tunning: List<Tunning>) {
        this.tunningList = tunning
        notifyDataSetChanged()
    }
}