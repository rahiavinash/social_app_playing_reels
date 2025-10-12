package com.wedo8020.app.mainui.home.exchangescreen.adapter


import com.wedo8020.app.databinding.RegionsLayoutBinding
import com.wedo8020.app.mainui.base.BaseRecyclerAdapter
import com.wedo8020.app.response.TreesCountData


class MostPlantedTreesByRegionAdapter(override val layoutId: Int):
    BaseRecyclerAdapter<RegionsLayoutBinding, TreesCountData>() {
    override fun bind(holder: ViewHolder, item: TreesCountData, position: Int) {
        holder.binding.country.text = item.name
        holder.binding.textCount.text = item.totalTrees.toString()
    }


}