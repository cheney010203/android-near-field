package com.example.bluetoothhiddemo.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import com.example.bluetoothhiddemo.R
import com.example.bluetoothhiddemo.data.BTData


class BTListAdapter(private val dataSet: ArrayList<BTData>) : Adapter<BTListAdapter.ViewHolder>() {
    private lateinit var mListener: OnItemClickListener

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val contentView =
            LayoutInflater.from(parent.context).inflate(R.layout.layout_rv_item, parent, false)
        return ViewHolder(contentView)
    }

    override fun getItemCount(): Int {
        return dataSet.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (dataSet.size > position) {
            val btData = dataSet[position]
            holder.name.text = btData.name
            holder.address.text = btData.address
            holder.content.setOnClickListener {
                mListener.onItemClick(btData.name, btData.address)
            }
        }
    }

    fun addData(data: BTData) {
        dataSet.add(data)
    }

    fun clearData() {
        dataSet.clear()
        notifyDataSetChanged()
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        mListener = listener;
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val content: ConstraintLayout
        val name: TextView
        val address: TextView

        init {
            content = view.findViewById(R.id.cl_content)
            name = view.findViewById(R.id.tv_name)
            address = view.findViewById(R.id.tv_address)
        }
    }

    interface OnItemClickListener {
        fun onItemClick(name: String, address: String)
    }
}