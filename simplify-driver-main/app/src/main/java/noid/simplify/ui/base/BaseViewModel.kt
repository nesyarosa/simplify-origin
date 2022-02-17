package noid.simplify.ui.base

import androidx.databinding.Observable
import androidx.databinding.PropertyChangeRegistry
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import noid.simplify.data.error.ErrorResponse
import noid.simplify.utils.SingleEvent

abstract class BaseViewModel : ViewModel(), Observable {

    private val callbacks: PropertyChangeRegistry = PropertyChangeRegistry()
    val errorLiveData = MutableLiveData<SingleEvent<Any>>()

    override fun addOnPropertyChangedCallback(callback: Observable.OnPropertyChangedCallback) {
        callbacks.add(callback)
    }

    override fun removeOnPropertyChangedCallback(callback: Observable.OnPropertyChangedCallback) {
        callbacks.remove(callback)
    }

    fun notifyPropertyChanged(fieldId: Int) {
        callbacks.notifyCallbacks(this, fieldId, null)
    }

    fun setErrorResponse(errorResponse: ErrorResponse?) {
        errorLiveData.value = SingleEvent(errorResponse)
    }
}
