package noid.simplify.ui.components.equipment

import noid.simplify.R
import noid.simplify.data.dto.Equipment
import noid.simplify.ui.base.BaseAdapter
import noid.simplify.databinding.ItemEquipmentBinding

class EquipmentDetailAdapter : BaseAdapter<Equipment, ItemEquipmentBinding>() {

    override fun getLayout() = R.layout.item_equipment

    override fun onBindViewHolder(holder: Companion.BaseViewHolder<ItemEquipmentBinding>, position:Int){
        holder.binding.item = items[position]
    }
}