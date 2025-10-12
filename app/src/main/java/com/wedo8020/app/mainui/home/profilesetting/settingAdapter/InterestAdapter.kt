package com.wedo8020.app.mainui.home.profilesetting.settingAdapter

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.wedo8020.app.R
import com.wedo8020.app.databinding.ChoseInterestRecycleviewBinding
import com.wedo8020.app.response.Category
import com.wedo8020.app.response.UserInterestData


class InterestAdapter(val list:ArrayList<Category>, onclickListener:OnclickListener): RecyclerView.Adapter<InterestAdapter.ViewHolder>() {
  private var onClickListener:OnclickListener=onclickListener
    class ViewHolder(val view: ChoseInterestRecycleviewBinding): RecyclerView.ViewHolder(view.root){
    }

    @SuppressLint("SuspiciousIndentation")
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val mBinding=ChoseInterestRecycleviewBinding.inflate(LayoutInflater.from(parent.context))
        return  ViewHolder(mBinding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Log.e("ASAsASASAS" , list.size.toString())
        Log.e("ASAsASASAS" , list[position].name.toString())
        holder.view.tvInterest.text = list[position].name
        if (list[position].selected){
            list[position].isSelect = true
            holder.view.tvInterest.setBackgroundResource(R.drawable.interest_select_background)
            holder.view.tvInterest.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.white))
        }else{
            list[position].isSelect = false
            holder.view.tvInterest.setBackgroundResource(R.drawable.interest_background)
            holder.view.tvInterest.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.GreyPasswordColor))
        }

        holder.view.tvInterest.setOnClickListener {
            val currentItem = list[position]
            if (currentItem.isSelect) {
                currentItem.isSelect = false
                holder.view.tvInterest.setBackgroundResource(R.drawable.interest_background)
                holder.view.tvInterest.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.GreyPasswordColor))
            } else {
                currentItem.isSelect = true
                holder.view.tvInterest.setBackgroundResource(R.drawable.interest_select_background)
                holder.view.tvInterest.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.white))
            }
            onClickListener.onClickInterest(position , currentItem)
        }
    }

    override fun getItemCount(): Int {
        return  list.size
    }

    interface  OnclickListener{
        fun onClickInterest(position: Int, data: Category)
    }
}