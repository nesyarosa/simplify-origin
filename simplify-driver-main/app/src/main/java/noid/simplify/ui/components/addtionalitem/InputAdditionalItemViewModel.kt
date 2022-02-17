package noid.simplify.ui.components.addtionalitem

import androidx.databinding.Bindable
import androidx.lifecycle.MediatorLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import noid.simplify.BR
import noid.simplify.data.dto.ServiceItem
import noid.simplify.ui.base.BaseViewModel
import noid.simplify.utils.extensions.toPriceWithoutCurrency
import javax.inject.Inject

@HiltViewModel
class InputAdditionalItemViewModel @Inject constructor() : BaseViewModel() {

    private var _item = ServiceItem()

    @get:Bindable
    var itemName: String
        get() = _item.name
        set(value) {
            _item.name = value
            notifyPropertyChanged(BR.itemName)
            validateForm()
        }

    @get:Bindable
    var description: String
        get() = _item.description ?: ""
        set(value) {
            _item.description = value
            notifyPropertyChanged(BR.description)
            validateForm()
        }

    @get:Bindable
    val totalAmount: String
        get() = _item.totalPrice.toPriceWithoutCurrency()

    val inputMediator = MediatorLiveData<Boolean>()

    private fun validateForm() {
        inputMediator.value = _item.name.isNotBlank() && _item.quantity > 0
    }
}