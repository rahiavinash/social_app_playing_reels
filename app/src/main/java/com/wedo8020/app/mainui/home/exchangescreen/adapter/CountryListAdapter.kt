package com.wedo8020.app.mainui.home.exchangescreen.adapter

import com.wedo8020.app.databinding.DropDownItemsBinding
import com.wedo8020.app.mainui.base.BaseRecyclerAdapter
import com.wedo8020.app.response.Country

class CountryListAdapter(override val layoutId: Int,private val itemClick: CountryClick?= null):
    BaseRecyclerAdapter<DropDownItemsBinding, Country>() {


    override fun bind(holder: ViewHolder, item: Country, position: Int) {
        holder.binding.countryName.text = item.name

        holder.itemView.setOnClickListener {
            item.name?.let { it1 -> item._id?.let { it2 -> itemClick?.onClick(it1, it2) } }
        }
    }

    interface CountryClick {
        fun onClick(country: String, countryId: String)
    }

}