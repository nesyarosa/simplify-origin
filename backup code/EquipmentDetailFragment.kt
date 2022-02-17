package noid.simplify.ui.components.equipment

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import noid.simplify.data.dto.Equipment
import noid.simplify.data.network.DataUtil
import noid.simplify.data.network.Resource
import noid.simplify.data.network.RetrofitInstance
import noid.simplify.databinding.FragmentEquipmentBinding
import noid.simplify.interfaces.OnLostConnection
import noid.simplify.ui.base.BaseFragment
import noid.simplify.ui.base.ToolbarBuilder
import noid.simplify.ui.components.jobdetail.JobDetailActivity.Companion.getIntent
import noid.simplify.utils.Tools
import noid.simplify.utils.extensions.observeData
import noid.simplify.utils.extensions.observeError
import noid.simplify.utils.extensions.toLinearVertical
import noid.simplify.utils.extensions.visible

@AndroidEntryPoint
class EquipmentDetailFragment : BaseFragment<FragmentEquipmentBinding>({
    FragmentEquipmentBinding.inflate(it)
}), OnLostConnection {

    private var idEquip = 0
    private var createAt: String? = null
    private var updateAt: String? = null
    private var equipmentList: List<Equipment> = emptyList()

    private val adapter by lazy { EquipmentDetailAdapter() }
    private val viewModel: EquipmentDetailViewModel by viewModels()

    override fun buildToolbar(): ToolbarBuilder {
        return ToolbarBuilder.Builder().build()
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun FragmentEquipmentBinding.onViewCreated(savedInstanceState: Bundle?) {
        arguments?.let {
            idEquip = it.getInt(EXTRA_ID_EQUIP)
            createAt = it.getString(EXTRA_CREATE_DATE)
            updateAt = it.getString(EXTRA_UPDATE_DATE)

        }
        binding.swipeRefresh.setOnRefreshListener {
            adapter.items.clear()
            adapter.notifyDataSetChanged()
            viewModel.fetchEquipment(idEquip)
        }

        adapter.apply {
            binding.listEquipment.toLinearVertical(viewAdapter = this)
        }
        viewModel.fetchEquipment(idEquip = idEquip)
        observeViewModel()
    }

    companion object {
        private const val EXTRA_ID_EQUIP = "key.EXTRA_ID_EQUIP"
        private const val EXTRA_CREATE_DATE = "key.EXTRA_CREATE_DATE"
        private const val EXTRA_UPDATE_DATE = "key.EXTRA_UPDATE_DATE"

        fun newInstance(idEquip: Int, createAt: String, updateAt: String): EquipmentDetailFragment {
            return EquipmentDetailFragment().apply {
                arguments = bundleOf(
                    EXTRA_ID_EQUIP to idEquip,
                    EXTRA_UPDATE_DATE to createAt,
                    EXTRA_UPDATE_DATE to updateAt
                )
            }
        }
    }

    private fun observeViewModel(){
        observeData(viewModel.equipmentLiveData, ::handleResponseJobs)
        observeError(viewModel.errorLiveData, childFragmentManager,this)
    }

    private fun handleResponseJobs(response:Resource<String>) {
        when (response) {
            is Resource.Loading -> {
                binding.swipeRefresh.isRefreshing = true
            }
            is Resource.Success -> {
                binding.swipeRefresh.isRefreshing = false
                response.data?.let {
                    val result = Tools.getResponseStringByKey(data = it, key ="equipment")
                    val list = DataUtil.toList<Equipment>(result)
                    this.equipmentList = list
                }
            }
            is Resource.DataError -> {
            binding.swipeRefresh.isRefreshing = false
                viewModel.setErrorResponse(response.error)
                this.setListViewAndState()
            }
        }
    }

            private fun setListViewAndState(){
               // binding.emptyState.visible(isVisible = adapter.items.isEmpty())
                binding.listEquipment.visible(isVisible = adapter.items.isNotEmpty())
            }


    override fun onRetry(url: String?) {
        viewModel.fetchEquipment(idEquip)
    }
}