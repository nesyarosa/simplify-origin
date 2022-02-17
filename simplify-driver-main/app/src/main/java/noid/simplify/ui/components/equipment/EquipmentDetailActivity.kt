package noid.simplify.ui.components.equipment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.tabs.TabLayout
import dagger.hilt.android.AndroidEntryPoint
import noid.simplify.R
import noid.simplify.constants.FilterBy
import noid.simplify.constants.General
import noid.simplify.constants.Url
import noid.simplify.data.dto.Equipment
import noid.simplify.data.dto.JobDetail
import noid.simplify.data.dto.JobList
import noid.simplify.data.network.DataUtil
import noid.simplify.data.network.Resource
import noid.simplify.databinding.ActivityEquipmentDetailBinding
import noid.simplify.interfaces.OnLostConnection
import noid.simplify.ui.base.BaseActivity
import noid.simplify.ui.base.ToolbarBuilder
import noid.simplify.ui.components.main.JobFragment
import noid.simplify.ui.components.main.MainActivity
import noid.simplify.ui.components.main.MainViewModel
import noid.simplify.utils.Tools
import noid.simplify.utils.extensions.*
import java.util.prefs.AbstractPreferences
import javax.inject.Inject

@AndroidEntryPoint
class EquipmentDetailActivity : BaseActivity<ActivityEquipmentDetailBinding>({
    ActivityEquipmentDetailBinding.inflate(it) }),
        OnLostConnection {

    private var idEquip = 0
    private val adapter by lazy { EquipmentDetailAdapter() }
    private var equipList: List<Equipment> = emptyList()
    val viewModel: EquipmentDetailViewModel by viewModels()
    val datas = "{\n" +
            "\t\"count\": 4,\n" +
            "\t\"equipments\": [\n" +
            "\t\t{\n" +
            "\t\t\t\"id\": 1,\n" +
            "\t\t\t\"brand\": \"SAMSUNG\",\n" +
            "\t\t\t\"model\": \"1/2PK\",\n" +
            "\t\t\t\"serialNumber\": \"s002\",\n" +
            "\t\t\t\"location\": \"Bed room\",\n" +
            "\t\t\t\"dateWorkDone\": \"2022-01-14\",\n" +
            "\t\t\t\"remarks\": null,\n" +
            "\t\t\t\"serviceAddressId\": 2524,\n" +
            "\t\t\t\"updatedBy\": 30,\n" +
            "\t\t\t\"isActive\": true,\n" +
            "\t\t\t\"createdAt\": \"2022-01-05T03:28:24.640Z\",\n" +
            "\t\t\t\"updatedAt\": \"2022-01-14T08:19:03.105Z\",\n" +
            "\t\t\t\"address\": \"damai street, 1-1, 122342\",\n" +
            "\t\t\t\"displayName\": \"David Yap\"\n" +
            "\t\t},\n" +
            "\t\t{\n" +
            "\t\t\t\"id\": 2,\n" +
            "\t\t\t\"brand\": \"DAIKIN\",\n" +
            "\t\t\t\"model\": \"2PK\",\n" +
            "\t\t\t\"serialNumber\": \"D002\",\n" +
            "\t\t\t\"location\": \"Living Room\",\n" +
            "\t\t\t\"dateWorkDone\": \"2022-01-10\",\n" +
            "\t\t\t\"remarks\": null,\n" +
            "\t\t\t\"serviceAddressId\": 2533,\n" +
            "\t\t\t\"updatedBy\": 30,\n" +
            "\t\t\t\"isActive\": true,\n" +
            "\t\t\t\"createdAt\": \"2022-01-05T06:51:10.260Z\",\n" +
            "\t\t\t\"updatedAt\": \"2022-01-10T03:09:41.517Z\",\n" +
            "\t\t\t\"address\": \"TOA PAYOH TOWERS, 145 LORONG 2 TOA PAYOH, 2-2, 310145\",\n" +
            "\t\t\t\"displayName\": \"David Yap\"\n" +
            "\t\t},\n" +
            "\t\t{\n" +
            "\t\t\t\"id\": 3,\n" +
            "\t\t\t\"brand\": \"LG\",\n" +
            "\t\t\t\"model\": \"NEW\",\n" +
            "\t\t\t\"serialNumber\": \"LG90\",\n" +
            "\t\t\t\"location\": \"GARAGE\",\n" +
            "\t\t\t\"dateWorkDone\": \"2022-01-07\",\n" +
            "\t\t\t\"remarks\": null,\n" +
            "\t\t\t\"serviceAddressId\": 2524,\n" +
            "\t\t\t\"updatedBy\": 30,\n" +
            "\t\t\t\"isActive\": true,\n" +
            "\t\t\t\"createdAt\": \"2022-01-06T07:40:40.119Z\",\n" +
            "\t\t\t\"updatedAt\": \"2022-01-07T02:59:31.132Z\",\n" +
            "\t\t\t\"address\": \"damai street, 1-1, 122342\",\n" +
            "\t\t\t\"displayName\": \"David Yap\"\n" +
            "\t\t},\n" +
            "\t\t{\n" +
            "\t\t\t\"id\": 8,\n" +
            "\t\t\t\"brand\": \"test\",\n" +
            "\t\t\t\"model\": \"TES\",\n" +
            "\t\t\t\"serialNumber\": \"tes\",\n" +
            "\t\t\t\"location\": \"sssss\",\n" +
            "\t\t\t\"dateWorkDone\": null,\n" +
            "\t\t\t\"remarks\": null,\n" +
            "\t\t\t\"serviceAddressId\": 2524,\n" +
            "\t\t\t\"updatedBy\": 30,\n" +
            "\t\t\t\"isActive\": false,\n" +
            "\t\t\t\"createdAt\": \"2022-01-07T07:50:51.389Z\",\n" +
            "\t\t\t\"updatedAt\": \"2022-01-10T07:15:32.402Z\",\n" +
            "\t\t\t\"address\": \"damai street, 1-1, 122342\",\n" +
            "\t\t\t\"displayName\": \"David Yap\"\n" +
            "\t\t}\n" +
            "\t]\n" +
            "}"

    val result = Tools.getResponseStringByKey(data = datas, key = "equipments")
    val list = DataUtil.toList<Equipment>(result)

    override fun buildToolbar(): ToolbarBuilder {
        return ToolbarBuilder.Builder()
            .withToolbar(binding.appbar.toolbar)
            .withTitle(getString(R.string.equipment_details))
            .withActionGoBack(true)
            .build()
    }

    override fun onRetry(url: String?) {
        binding.viewModel?.fetchEquipment(idEquip)
    }

    override fun ActivityEquipmentDetailBinding.onCreate(savedInstanceState: Bundle?) {
        binding.viewModel = viewModel
        observeViewModel()
        listEquipment.toLinearVertical(viewAdapter = adapter) //menampilkan data dari adapter
}

    private fun observeViewModel() {
        observeData(binding.viewModel?.equipmentLiveData, ::handleResponseEquipment)
        observeError(binding.viewModel?.errorLiveData, supportFragmentManager, this)

        adapter.setItems(list)
    }

    //dari job fragment / job act
    fun handleResponseEquipment(response: Resource<String>?) {
        when(response) {
            is Resource.Loading -> {
                    binding.swipeRefresh.isRefreshing = true
            }
            is Resource.Success -> {
                binding.swipeRefresh.isRefreshing = false
                response.data?.let{
                    val res = Tools.getResponseStringByKey(data = datas, key = "equipments")
                    val list = DataUtil.toList<Equipment>(res)
                    this.adapter.addItems(list)
                    if(list.size == General.LIMIT_PER_LOAD){
                        viewModel.fetchEquipment(adapter.itemCount)
                    this.equipList = list
                    }
                    this.setListViewAndState()
                    }
                }
            is Resource.DataError -> {
                binding.swipeRefresh.isRefreshing = false
                viewModel.setErrorResponse(response.error)
                this.setListViewAndState()
            }
        }
    }

    private fun setListViewAndState() {
        binding.emptyState.visible(isVisible = adapter.items.isEmpty())
        binding.listEquipment.visible(isVisible = adapter.items.isNotEmpty())
    }
}

